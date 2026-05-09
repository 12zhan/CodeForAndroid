package com.mobilecodex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilecodex.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 聊天 UI 状态
 */
data class ChatUiState(
    val conversation: ChatConversation = ChatConversation.create(),
    val conversations: List<ChatConversation> = emptyList(),
    val inputText: String = "",
    val attachedFiles: List<String> = emptyList(),
    val isGenerating: Boolean = false,
    val isStreaming: Boolean = false,
    val streamingContent: String = "",
    val error: String? = null,
    val showConversationList: Boolean = false,
    val aiSettings: AISettings = AISettings.default()
)

/**
 * Function Calling 工具定义
 */
data class FunctionTool(
    val name: String,
    val description: String,
    val parameters: Map<String, Any>
)

/**
 * Function Calling 结果
 */
data class FunctionResult(
    val name: String,
    val callId: String,
    val result: String,
    val isError: Boolean = false
)

/**
 * 聊天事件
 */
sealed class ChatEvent {
    data class ShowError(val error: String) : ChatEvent()
    data class ShowMessage(val message: String) : ChatEvent()
    data class FunctionCallRequest(val name: String, val arguments: String, val callId: String) : ChatEvent()
    object MessageSent : ChatEvent()
    object ResponseComplete : ChatEvent()
}

/**
 * AI 聊天 ViewModel
 * 管理与 AI 的对话、Function Calling 等功能
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    // 注入的仓库将在这里添加
    // private val chatRepository: ChatRepository,
    // private val aiRepository: AIRepository
) : ViewModel() {
    
    // 聊天 UI 状态
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    // 一次性事件
    private val _events = MutableSharedFlow<ChatEvent>()
    val events: SharedFlow<ChatEvent> = _events.asSharedFlow()
    
    // 当前对话
    val conversation: StateFlow<ChatConversation> = _uiState
        .map { it.conversation }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChatConversation.create())
    
    // 对话列表
    val conversations: StateFlow<List<ChatConversation>> = _uiState
        .map { it.conversations }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 消息列表
    val messages: StateFlow<List<ChatMessage>> = _uiState
        .map { it.conversation.messages }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 输入文本
    val inputText: StateFlow<String> = _uiState
        .map { it.inputText }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    
    // 附加文件
    val attachedFiles: StateFlow<List<String>> = _uiState
        .map { it.attachedFiles }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 是否正在生成
    val isGenerating: StateFlow<Boolean> = _uiState
        .map { it.isGenerating }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    // 是否正在流式传输
    val isStreaming: StateFlow<Boolean> = _uiState
        .map { it.isStreaming }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    // 流式内容
    val streamingContent: StateFlow<String> = _uiState
        .map { it.streamingContent }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    
    // 错误状态
    val error: StateFlow<String?> = _uiState
        .map { it.error }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    // 是否有消息
    val hasMessages: StateFlow<Boolean> = _uiState
        .map { it.conversation.hasMessages }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    /**
     * 更新输入文本
     */
    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }
    
    /**
     * 添加附加文件
     */
    fun attachFile(filePath: String) {
        _uiState.update { currentState ->
            if (filePath !in currentState.attachedFiles) {
                currentState.copy(attachedFiles = currentState.attachedFiles + filePath)
            } else {
                currentState
            }
        }
    }
    
    /**
     * 移除附加文件
     */
    fun detachFile(filePath: String) {
        _uiState.update { currentState ->
            currentState.copy(attachedFiles = currentState.attachedFiles - filePath)
        }
    }
    
    /**
     * 清除所有附加文件
     */
    fun clearAttachedFiles() {
        _uiState.update { it.copy(attachedFiles = emptyList()) }
    }
    
    /**
     * 发送消息
     */
    fun sendMessage() {
        val inputText = _uiState.value.inputText.trim()
        if (inputText.isBlank() && _uiState.value.attachedFiles.isEmpty()) return
        
        viewModelScope.launch {
            // 创建用户消息
            val userMessage = ChatMessage.user(
                content = inputText,
                attachedFiles = _uiState.value.attachedFiles
            )
            
            // 添加到对话
            _uiState.update { currentState ->
                currentState.copy(
                    conversation = currentState.conversation.withMessage(userMessage),
                    inputText = "",
                    attachedFiles = emptyList(),
                    isGenerating = true,
                    error = null
                )
            }
            
            _events.emit(ChatEvent.MessageSent)
            
            // 调用 AI API
            generateResponse()
        }
    }
    
    /**
     * 生成 AI 响应
     */
    private fun generateResponse() {
        viewModelScope.launch {
            try {
                val conversation = _uiState.value.conversation
                val settings = _uiState.value.aiSettings
                
                if (!settings.isConfigured) {
                    _uiState.update { it.copy(isGenerating = false) }
                    _events.emit(ChatEvent.ShowError("请先配置 AI API Key"))
                    return@launch
                }
                
                // 添加助手消息占位符
                val assistantMessage = ChatMessage.assistant("")
                _uiState.update { currentState ->
                    currentState.copy(
                        conversation = currentState.conversation.withMessage(assistantMessage),
                        isStreaming = true,
                        streamingContent = ""
                    )
                }
                
                // TODO: 调用 AI API
                // 1. 构建请求消息列表
                // 2. 定义 Function Calling 工具
                // 3. 发送请求并处理流式响应
                // 4. 如果收到 Function Calling 请求，执行相应操作
                
                // 临时模拟响应
                val mockResponse = "这是一个模拟的 AI 响应。实际实现需要集成 OpenAI 或其他 AI 服务的 API。"
                
                // 模拟流式传输
                for (i in mockResponse.indices) {
                    kotlinx.coroutines.delay(20)
                    val partialContent = mockResponse.substring(0, i + 1)
                    _uiState.update { currentState ->
                        currentState.copy(streamingContent = partialContent)
                    }
                }
                
                // 完成响应
                _uiState.update { currentState ->
                    val updatedConversation = currentState.conversation.updateLastMessage { msg ->
                        msg.withStreamingContent(mockResponse).finishStreaming()
                    }
                    currentState.copy(
                        conversation = updatedConversation,
                        isGenerating = false,
                        isStreaming = false,
                        streamingContent = ""
                    )
                }
                
                _events.emit(ChatEvent.ResponseComplete)
            } catch (e: Exception) {
                _uiState.update { it.copy(isGenerating = false, isStreaming = false, error = e.message) }
                _events.emit(ChatEvent.ShowError("生成响应失败: ${e.message}"))
            }
        }
    }
    
    /**
     * 处理 Function Calling 结果
     */
    fun handleFunctionResult(result: FunctionResult) {
        viewModelScope.launch {
            // 添加函数结果消息
            val functionMessage = ChatMessage(
                role = MessageRole.FUNCTION,
                content = result.result,
                functionName = result.name,
                functionCallId = result.callId
            )
            
            _uiState.update { currentState ->
                currentState.copy(
                    conversation = currentState.conversation.withMessage(functionMessage)
                )
            }
            
            // 继续生成响应
            generateResponse()
        }
    }
    
    /**
     * 获取可用的 Function Calling 工具
     */
    fun getAvailableTools(): List<FunctionTool> {
        return listOf(
            FunctionTool(
                name = "listFiles",
                description = "获取当前工作区中的文件列表",
                parameters = mapOf(
                    "type" to "object",
                    "properties" to emptyMap<String, Any>(),
                    "required" to emptyList<String>()
                )
            ),
            FunctionTool(
                name = "readFile",
                description = "读取指定路径的文件内容",
                parameters = mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "path" to mapOf(
                            "type" to "string",
                            "description" to "文件路径"
                        )
                    ),
                    "required" to listOf("path")
                )
            ),
            FunctionTool(
                name = "saveFile",
                description = "保存或创建文件",
                parameters = mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "path" to mapOf(
                            "type" to "string",
                            "description" to "文件路径"
                        ),
                        "content" to mapOf(
                            "type" to "string",
                            "description" to "文件内容"
                        )
                    ),
                    "required" to listOf("path", "content")
                )
            ),
            FunctionTool(
                name = "githubCommitChanges",
                description = "将修改提交到 GitHub 仓库",
                parameters = mapOf(
                    "type" to "object",
                    "properties" to mapOf(
                        "files" to mapOf(
                            "type" to "array",
                            "items" to mapOf("type" to "string"),
                            "description" to "要提交的文件路径列表"
                        ),
                        "message" to mapOf(
                            "type" to "string",
                            "description" to "提交信息"
                        )
                    ),
                    "required" to listOf("files", "message")
                )
            )
        )
    }
    
    /**
     * 创建新对话
     */
    fun createNewConversation(title: String = "新对话") {
        viewModelScope.launch {
            val currentConversation = _uiState.value.conversation
            if (currentConversation.hasMessages) {
                _uiState.update { currentState ->
                    currentState.copy(
                        conversations = currentState.conversations + currentConversation,
                        conversation = ChatConversation.create(title = title)
                    )
                }
            } else {
                _uiState.update { it.copy(conversation = ChatConversation.create(title = title)) }
            }
            _events.emit(ChatEvent.ShowMessage("已创建新对话"))
        }
    }
    
    /**
     * 切换到指定对话
     */
    fun switchConversation(conversationId: String) {
        val conversation = _uiState.value.conversations.find { it.id == conversationId }
        if (conversation != null) {
            _uiState.update { it.copy(conversation = conversation, showConversationList = false) }
        }
    }
    
    /**
     * 显示/隐藏对话列表
     */
    fun toggleConversationList() {
        _uiState.update { it.copy(showConversationList = !it.showConversationList) }
    }
    
    /**
     * 删除对话
     */
    fun deleteConversation(conversationId: String) {
        _uiState.update { currentState ->
            currentState.copy(
                conversations = currentState.conversations.filter { it.id != conversationId }
            )
        }
    }
    
    /**
     * 清除当前对话
     */
    fun clearCurrentConversation() {
        _uiState.update { currentState ->
            currentState.copy(
                conversation = currentState.conversation.clearMessages()
            )
        }
        viewModelScope.launch {
            _events.emit(ChatEvent.ShowMessage("对话已清空"))
        }
    }
    
    /**
     * 清除所有对话历史
     */
    fun clearAllConversations() {
        _uiState.update { 
            it.copy(
                conversations = emptyList(),
                conversation = ChatConversation.create()
            )
        }
        viewModelScope.launch {
            _events.emit(ChatEvent.ShowMessage("所有对话历史已清除"))
        }
    }
    
    /**
     * 更新 AI 设置
     */
    fun updateAISettings(settings: AISettings) {
        _uiState.update { it.copy(aiSettings = settings) }
    }
    
    /**
     * 停止生成
     */
    fun stopGenerating() {
        _uiState.update { currentState ->
            val updatedConversation = if (currentState.isStreaming) {
                currentState.conversation.updateLastMessage { msg ->
                    msg.finishStreaming()
                }
            } else {
                currentState.conversation
            }
            currentState.copy(
                conversation = updatedConversation,
                isGenerating = false,
                isStreaming = false,
                streamingContent = ""
            )
        }
    }
    
    /**
     * 重新生成最后一条响应
     */
    fun regenerateLastResponse() {
        val conversation = _uiState.value.conversation
        if (conversation.messages.isEmpty()) return
        
        // 移除最后一条助手消息
        val updatedMessages = conversation.messages.toMutableList()
        if (updatedMessages.lastOrNull()?.isAssistant == true) {
            updatedMessages.removeAt(updatedMessages.size - 1)
        }
        
        _uiState.update { currentState ->
            currentState.copy(
                conversation = currentState.conversation.copy(messages = updatedMessages)
            )
        }
        
        // 重新生成
        generateResponse()
    }
    
    /**
     * 编辑并重新发送消息
     */
    fun editAndResend(messageId: String, newContent: String) {
        val conversation = _uiState.value.conversation
        val messageIndex = conversation.messages.indexOfFirst { it.id == messageId }
        
        if (messageIndex >= 0 && conversation.messages[messageIndex].isUser) {
            // 移除该消息之后的所有消息
            val updatedMessages = conversation.messages.subList(0, messageIndex + 1).toMutableList()
            
            // 更新该消息的内容
            updatedMessages[messageIndex] = updatedMessages[messageIndex].copy(content = newContent)
            
            _uiState.update { currentState ->
                currentState.copy(
                    conversation = currentState.conversation.copy(messages = updatedMessages)
                )
            }
            
            // 重新生成
            generateResponse()
        }
    }
    
    /**
     * 复制消息内容
     */
    fun copyMessage(messageId: String): String? {
        return _uiState.value.conversation.messages.find { it.id == messageId }?.content
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * 获取对话摘要
     */
    fun getConversationSummary(): String {
        return _uiState.value.conversation.summary
    }
}