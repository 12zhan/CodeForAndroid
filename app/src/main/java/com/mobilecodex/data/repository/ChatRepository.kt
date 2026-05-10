package com.mobilecodex.data.repository

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.mobilecodex.data.api.*
import com.mobilecodex.model.*
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import kotlin.math.ceil

@ViewModelScoped
class ChatRepository @Inject constructor(
    private val openAIApi: OpenAIApi,
    private val gson: Gson
) {
    /**
     * 发送聊天消息（非流式）
     */
    suspend fun sendMessage(
        aiSettings: AISettings,
        messages: List<ChatMessage>
    ): Result<ChatCompletionResponse> = withContext(Dispatchers.IO) {
        try {
            val authHeader = "Bearer ${aiSettings.apiKey}"
            val messageDtos = messages.map { msg ->
                MessageDto(role = msg.role.name.lowercase(), content = msg.content)
            }
            val request = ChatCompletionRequest(
                model = aiSettings.modelId,
                messages = messageDtos,
                temperature = aiSettings.temperature,
                maxTokens = aiSettings.maxTokens,
                topP = aiSettings.topP,
                stream = false
            )

            val response = openAIApi.createChatCompletion(authHeader, request)
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string() ?: "未知错误"
                Result.failure(Exception("API 请求失败 ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络请求异常: ${e.message}", e))
        }
    }

    /**
     * 流式发送聊天消息，返回 Flow<String> 逐 token 推送
     */
    fun sendMessageStream(
        aiSettings: AISettings,
        messages: List<ChatMessage>
    ): Flow<StreamResult> = flow {
        try {
            val authHeader = "Bearer ${aiSettings.apiKey}"
            val messageDtos = messages.map { msg ->
                MessageDto(role = msg.role.name.lowercase(), content = msg.content)
            }
            val request = ChatCompletionRequest(
                model = aiSettings.modelId,
                messages = messageDtos,
                temperature = aiSettings.temperature,
                maxTokens = aiSettings.maxTokens,
                topP = aiSettings.topP,
                stream = true
            )

            val response = openAIApi.createChatCompletionStream(authHeader, request)

            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "未知错误"
                emit(StreamResult.Error("API 请求失败 ${response.code()}: $errorBody"))
                return@flow
            }

            val body = response.body() ?: run {
                emit(StreamResult.Error("响应体为空"))
                return@flow
            }

            val reader = BufferedReader(InputStreamReader(body.byteStream()))
            var fullContent = StringBuilder()

            reader.forEachLine { line ->
                if (line.startsWith("data: ")) {
                    val data = line.removePrefix("data: ").trim()

                    if (data == "[DONE]") {
                        emit(StreamResult.Done(fullContent.toString()))
                        return@flow
                    }

                    try {
                        val chunk = gson.fromJson(data, StreamChunk::class.java)
                        val delta = chunk.choices?.firstOrNull()?.delta?.content ?: ""
                        if (delta.isNotEmpty()) {
                            fullContent.append(delta)
                            emit(StreamResult.Token(delta, fullContent.toString()))
                        }
                    } catch (_: Exception) {
                        // 跳过无法解析的行
                    }
                }
            }

            // 流结束但没有 [DONE]
            emit(StreamResult.Done(fullContent.toString()))
        } catch (e: Exception) {
            emit(StreamResult.Error("流式请求异常: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 创建带有系统提示的消息列表
     */
    fun buildMessages(
        systemPrompt: String,
        conversationMessages: List<ChatMessage>,
        newUserMessage: String
    ): List<ChatMessage> {
        val allMessages = mutableListOf<ChatMessage>()

        val hasSystemMessage = conversationMessages.any { it.role == MessageRole.SYSTEM }
        if (!hasSystemMessage && systemPrompt.isNotBlank()) {
            allMessages.add(
                ChatMessage(
                    id = "system-${System.currentTimeMillis()}",
                    role = MessageRole.SYSTEM,
                    content = systemPrompt
                )
            )
        }

        allMessages.addAll(conversationMessages.filter { it.role != MessageRole.SYSTEM })
        allMessages.add(
            ChatMessage(
                id = "user-${System.currentTimeMillis()}",
                role = MessageRole.USER,
                content = newUserMessage
            )
        )

        return allMessages
    }

    /**
     * 估算 token 数量
     */
    fun estimateTokens(text: String): Int {
        val chineseChars = text.count { it in '\u4e00'..'\u9fff' }
        val otherChars = text.length - chineseChars
        return chineseChars + ceil(otherChars / 4.0).toInt()
    }
}

// --- 流式响应 DTO ---

data class StreamChunk(
    val id: String? = null,
    val `object`: String? = null,
    val created: Long? = null,
    val model: String? = null,
    val choices: List<StreamChoice>? = null
)

data class StreamChoice(
    val index: Int? = null,
    val delta: StreamDelta? = null,
    @SerializedName("finish_reason") val finishReason: String? = null
)

data class StreamDelta(
    val role: String? = null,
    val content: String? = null
)

// --- 流式结果密封类 ---

sealed class StreamResult {
    data class Token(val token: String, val fullContent: String) : StreamResult()
    data class Done(val fullContent: String) : StreamResult()
    data class Error(val message: String) : StreamResult()
}
