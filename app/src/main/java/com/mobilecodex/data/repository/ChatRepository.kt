package com.mobilecodex.data.repository

import com.google.gson.Gson
import com.mobilecodex.data.api.ChatCompletionRequest
import com.mobilecodex.data.api.ChatMessageRequest
import com.mobilecodex.data.api.FunctionDefinition
import com.mobilecodex.data.api.OpenAIApi
import com.mobilecodex.data.api.Tool
import com.mobilecodex.model.AISettings
import com.mobilecodex.model.ChatConversation
import com.mobilecodex.model.ChatMessage
import com.mobilecodex.model.MessageRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 流式响应的增量数据
 */
data class StreamDelta(
    val content: String,
    val role: String? = null,
    val finishReason: String? = null,
    val functionCall: FunctionCallDelta? = null
)

/**
 * Function Calling 增量数据
 */
data class FunctionCallDelta(
    val name: String?,
    val arguments: String?
)

/**
 * Function Calling 请求（由 AI 发起）
 */
data class FunctionCallRequest(
    val callId: String,
    val name: String,
    val arguments: String
)

/**
 * 聊天仓库
 * 封装 AI 对话逻辑：流式响应、Function Calling、对话历史管理
 */
@Singleton
class ChatRepository @Inject constructor(
    private val openAIApi: OpenAIApi,
    private val settingsRepository: SettingsRepository,
    private val gson: Gson
) {
    /** 当前对话 */
    private val _currentConversation = MutableStateFlow(ChatConversation.create())
    val currentConversation = _currentConversation.asStateFlow()

    /** 对话历史列表 */
    private val _conversations = MutableStateFlow<List<ChatConversation>>(emptyList())
    val conversations = _conversations.asStateFlow()

    /** 流式响应事件 */
    private val _streamEvents = MutableSharedFlow<StreamDelta>(extraBufferCapacity = 64)
    val streamEvents: Flow<StreamDelta> = _streamEvents

    // ==================== 对话管理 ====================

    /**
     * 创建新对话
     */
    fun createNewConversation(title: String = "新对话") {
        val current = _currentConversation.value
        if (current.hasMessages) {
            _conversations.update { it + current }
        }
        _currentConversation.value = ChatConversation.create(title = title)
    }

    /**
     * 切换到指定对话
     */
    fun switchConversation(conversationId: String): Boolean {
        val target = _conversations.value.find { it.id == conversationId } ?: return false
        val current = _currentConversation.value
        if (current.hasMessages && current.id != target.id) {
            _conversations.update { list ->
                val updated = list.toMutableList()
                val idx = updated.indexOfFirst { it.id == current.id }
                if (idx >= 0) updated[idx] = current else updated.add(current)
                updated.filter { it.id != target.id }
            }
        } else {
            _conversations.update { it.filter { c -> c.id != target.id } }
        }
        _currentConversation.value = target
        return true
    }

    /**
     * 删除对话
     */
    fun deleteConversation(conversationId: String) {
        _conversations.update { it.filter { c -> c.id != conversationId } }
        if (_currentConversation.value.id == conversationId) {
            _currentConversation.value = ChatConversation.create()
        }
    }

    /**
     * 清除当前对话
     */
    fun clearCurrentConversation() {
        _currentConversation.update { it.clearMessages() }
    }

    /**
     * 清除所有对话历史
     */
    fun clearAllConversations() {
        _currentConversation.value = ChatConversation.create()
        _conversations.value = emptyList()
    }

    /**
     * 向当前对话添加一条消息
     */
    fun addMessage(message: ChatMessage) {
        _currentConversation.update { it.withMessage(message) }
    }

    /**
     * 更新当前对话的最后一条消息
     */
    fun updateLastMessage(update: (ChatMessage) -> ChatMessage) {
        _currentConversation.update { it.updateLastMessage(update) }
    }

    // ==================== AI 响应生成 ====================

    /**
     * 生成 AI 响应（非流式）
     * @return 完整的助手消息
     */
    suspend fun generateResponse(): Result<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val settings = settingsRepository.aiSettings.first()
            if (!settings.isConfigured) {
                return@withContext Result.failure(Exception("请先配置 AI API Key"))
            }

            val conversation = _currentConversation.value
            val request = buildChatRequest(conversation, settings)

            val authHeader = "Bearer ${settings.apiKey}"
            val response = openAIApi.createChatCompletion(request, authHeader)

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "未知错误"
                return@withContext Result.failure(Exception("API 请求失败 (${response.code()}): $errorBody"))
            }

            val body = response.body()
                ?: return@withContext Result.failure(Exception("API 响应体为空"))

            val choice = body.choices.firstOrNull()
                ?: return@withContext Result.failure(Exception("API 没有返回任何选择"))

            val content = choice.message.content ?: ""
            val assistantMessage = ChatMessage.assistant(content)

            _currentConversation.update { it.withMessage(assistantMessage) }

            Result.success(assistantMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 生成 AI 响应（流式）
     * 通过 [streamEvents] 发送增量数据
     */
    suspend fun generateStreamingResponse(tools: List<Tool>? = null): Result<ChatMessage> =
        withContext(Dispatchers.IO) {
            try {
                val settings = settingsRepository.aiSettings.first()
                if (!settings.isConfigured) {
                    return@withContext Result.failure(Exception("请先配置 AI API Key"))
                }

                val conversation = _currentConversation.value
                val request = buildChatRequest(conversation, settings).copy(
                    stream = true,
                    tools = tools
                )

                val authHeader = "Bearer ${settings.apiKey}"
                val response = openAIApi.createChatCompletionStream(request, authHeader)

                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string() ?: "未知错误"
                    return@withContext Result.failure(
                        Exception("API 请求失败 (${response.code()}): $errorBody")
                    )
                }

                val body = response.body()
                    ?: return@withContext Result.failure(Exception("API 响应体为空"))

                val fullContent = parseStreamResponse(body)

                val assistantMessage = ChatMessage.assistant(fullContent)
                _currentConversation.update { it.withMessage(assistantMessage) }

                Result.success(assistantMessage)
            } catch (e: Exception) {
                _streamEvents.tryEmit(
                    StreamDelta(content = "", finishReason = "error")
                )
                Result.failure(e)
            }
        }

    /**
     * 继续 Function Calling 对话（发送函数结果后继续）
     */
    suspend fun continueWithFunctionResult(
        functionResults: List<ChatMessage>
    ): Result<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            val settings = settingsRepository.aiSettings.first()
            if (!settings.isConfigured) {
                return@withContext Result.failure(Exception("请先配置 AI API Key"))
            }

            // 将函数结果添加到对话中
            for (msg in functionResults) {
                _currentConversation.update { it.withMessage(msg) }
            }

            val conversation = _currentConversation.value
            val request = buildChatRequest(conversation, settings).copy(stream = true)

            val authHeader = "Bearer ${settings.apiKey}"
            val response = openAIApi.createChatCompletionStream(request, authHeader)

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "未知错误"
                return@withContext Result.failure(
                    Exception("API 请求失败 (${response.code()}): $errorBody")
                )
            }

            val body = response.body()
                ?: return@withContext Result.failure(Exception("API 响应体为空"))

            val fullContent = parseStreamResponse(body)

            val assistantMessage = ChatMessage.assistant(fullContent)
            _currentConversation.update { it.withMessage(assistantMessage) }

            Result.success(assistantMessage)
        } catch (e: Exception) {
            _streamEvents.tryEmit(
                StreamDelta(content = "", finishReason = "error")
            )
            Result.failure(e)
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 构建 API 请求对象
     */
    private fun buildChatRequest(
        conversation: ChatConversation,
        settings: AISettings
    ): ChatCompletionRequest {
        val messages = mutableListOf<ChatMessageRequest>()

        // 添加系统提示词
        if (settings.systemPrompt.isNotBlank()) {
            messages.add(
                ChatMessageRequest(
                    role = "system",
                    content = settings.systemPrompt
                )
            )
        }

        // 转换对话消息
        messages.addAll(
            conversation.messages.map { msg ->
                ChatMessageRequest(
                    role = msg.role.toApiRole(),
                    content = buildMessageContent(msg),
                    name = msg.functionName
                )
            }
        )

        return ChatCompletionRequest(
            model = settings.model,
            messages = messages,
            temperature = settings.temperature,
            max_tokens = settings.maxTokens,
            top_p = settings.topP,
            frequency_penalty = settings.frequencyPenalty,
            presence_penalty = settings.presencePenalty,
            stream = false
        )
    }

    /**
     * 构建消息内容（可能包含附件信息）
     */
    private fun buildMessageContent(msg: ChatMessage): String {
        if (msg.attachedFiles.isEmpty()) {
            return msg.content
        }

        val sb = StringBuilder()
        sb.appendLine(msg.content)
        sb.appendLine()
        sb.appendLine("--- 附加文件 ---")
        msg.attachedFiles.forEach { path ->
            sb.appendLine("- $path")
        }
        return sb.toString()
    }

    /**
     * 解析 SSE 流式响应
     */
    private suspend fun parseStreamResponse(body: ResponseBody): String {
        val fullContent = StringBuilder()
        val reader = BufferedReader(InputStreamReader(body.byteStream(), Charsets.UTF_8))

        try {
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue
                if (currentLine.isBlank()) continue
                if (!currentLine.startsWith("data: ")) continue

                val data = currentLine.removePrefix("data: ").trim()
                if (data == "[DONE]") {
                    _streamEvents.emit(
                        StreamDelta(content = "", finishReason = "stop")
                    )
                    break
                }

                try {
                    val chunk = gson.fromJson(data, StreamChunkDTO::class.java)
                    val delta = chunk.choices?.firstOrNull()?.delta ?: continue

                    val contentDelta = delta.content ?: ""
                    val finishReason = chunk.choices.firstOrNull()?.finish_reason

                    if (contentDelta.isNotEmpty()) {
                        fullContent.append(contentDelta)
                        _streamEvents.emit(
                            StreamDelta(
                                content = contentDelta,
                                role = delta.role,
                                finishReason = finishReason
                            )
                        )
                    }

                    // 处理 Function Calling
                    if (delta.tool_calls != null) {
                        for (toolCall in delta.tool_calls) {
                            val func = toolCall.function
                            if (func != null) {
                                _streamEvents.emit(
                                    StreamDelta(
                                        content = "",
                                        finishReason = "function_call",
                                        functionCall = FunctionCallDelta(
                                            name = func.name,
                                            arguments = func.arguments
                                        )
                                    )
                                )
                            }
                        }
                    }

                    if (finishReason != null && finishReason != "stop") {
                        _streamEvents.emit(
                            StreamDelta(content = "", finishReason = finishReason)
                        )
                    }
                } catch (e: Exception) {
                    // 跳过无法解析的行
                    continue
                }
            }
        } finally {
            reader.close()
        }

        return fullContent.toString()
    }
}

// ==================== 扩展函数 ====================

/**
 * 将领域模型的 MessageRole 转换为 API 角色字符串
 */
private fun MessageRole.toApiRole(): String = when (this) {
    MessageRole.USER -> "user"
    MessageRole.ASSISTANT -> "assistant"
    MessageRole.SYSTEM -> "system"
    MessageRole.FUNCTION -> "function"
}

/**
 * 将 FunctionTool（来自 ViewModel）转换为 API 的 Tool 定义
 */
fun com.mobilecodex.viewmodel.FunctionTool.toApiTool(): Tool {
    return Tool(
        type = "function",
        function = FunctionDefinition(
            name = this.name,
            description = this.description,
            parameters = this.parameters
        )
    )
}

// ==================== SSE 解析 DTO ====================

/**
 * 流式响应块 DTO（用于 Gson 解析）
 */
private data class StreamChunkDTO(
    val id: String?,
    val `object`: String?,
    val created: Long?,
    val model: String?,
    val choices: List<StreamChoiceDTO>?
)

private data class StreamChoiceDTO(
    val index: Int?,
    val delta: DeltaDTO?,
    val finish_reason: String?
)

private data class DeltaDTO(
    val role: String?,
    val content: String?,
    val tool_calls: List<ToolCallDTO>?
)

private data class ToolCallDTO(
    val index: Int?,
    val id: String?,
    val type: String?,
    val function: FunctionCallDTO?
)

private data class FunctionCallDTO(
    val name: String?,
    val arguments: String?
)
