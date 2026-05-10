package com.mobilecodex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilecodex.model.*
import com.mobilecodex.data.repository.ChatRepository
import com.mobilecodex.data.repository.StreamResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatUiState(
    val conversations: List<ChatConversation> = emptyList(),
    val activeConversation: ChatConversation? = null,
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val isStreaming: Boolean = false,
    val error: String? = null,
    val inputText: String = ""
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val settingsViewModel: SettingsViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun updateInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun createNewConversation() {
        val newConv = ChatConversation(
            id = UUID.randomUUID().toString(),
            title = "新对话"
        )
        _uiState.update { state ->
            state.copy(
                conversations = listOf(newConv) + state.conversations,
                activeConversation = newConv,
                error = null
            )
        }
    }

    fun selectConversation(conversationId: String) {
        _uiState.update { state ->
            val conv = state.conversations.find { it.id == conversationId }
            state.copy(activeConversation = conv)
        }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty() || _uiState.value.isSending) return

        val settings = settingsViewModel.getCurrentAISettings()
        if (!settings.isConfigured) {
            _uiState.update { it.copy(error = "请先在设置中配置 OpenAI API Key") }
            return
        }

        // 确保有活跃对话
        val activeConv = _uiState.value.activeConversation
        val conversation = if (activeConv == null) {
            val newConv = ChatConversation(
                id = UUID.randomUUID().toString(),
                title = text.take(40)
            )
            _uiState.update { state ->
                state.copy(
                    conversations = listOf(newConv) + state.conversations,
                    activeConversation = newConv
                )
            }
            newConv
        } else {
            activeConv
        }

        // 添加用户消息
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = MessageRole.USER,
            content = text
        )

        updateConversationMessages(conversation.id) { messages -> messages + userMessage }
        _uiState.update { it.copy(inputText = "", isSending = true, isStreaming = true, error = null) }

        // 创建一个空占位消息用于流式更新
        val assistantMessageId = UUID.randomUUID().toString()
        val placeholderMessage = ChatMessage(
            id = assistantMessageId,
            role = MessageRole.ASSISTANT,
            content = "",
            isStreaming = true
        )
        updateConversationMessages(conversation.id) { messages -> messages + placeholderMessage }

        viewModelScope.launch {
            val currentMessages = _uiState.value.activeConversation?.messages?.filter {
                it.id != userMessage.id && it.id != assistantMessageId
            } ?: emptyList()

            val messagesToSend = chatRepository.buildMessages(
                systemPrompt = settings.systemPrompt,
                conversationMessages = currentMessages,
                newUserMessage = text
            )

            // 使用流式请求
            var hasError = false
            chatRepository.sendMessageStream(settings, messagesToSend).collect { result ->
                when (result) {
                    is StreamResult.Token -> {
                        // 逐 token 更新消息内容
                        updateMessageContent(assistantMessageId, result.fullContent)
                    }
                    is StreamResult.Done -> {
                        // 流式完成，标记为非流式
                        finalizeMessage(assistantMessageId, result.fullContent)
                        // 更新对话标题
                        if (currentMessages.isEmpty()) {
                            updateConversationTitle(conversation.id, text.take(40))
                        }
                        _uiState.update { it.copy(isSending = false, isStreaming = false) }
                    }
                    is StreamResult.Error -> {
                        hasError = true
                        // 移除占位消息和用户消息
                        updateConversationMessages(conversation.id) { messages ->
                            messages.filter { it.id != assistantMessageId && it.id != userMessage.id }
                        }
                        _uiState.update {
                            it.copy(
                                isSending = false,
                                isStreaming = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    fun stopStreaming() {
        // 当前无法真正中断 HTTP 流，但可以标记停止
        _uiState.update { it.copy(isSending = false, isStreaming = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun deleteConversation(conversationId: String) {
        _uiState.update { state ->
            val updatedList = state.conversations.filter { it.id != conversationId }
            state.copy(
                conversations = updatedList,
                activeConversation = if (state.activeConversation?.id == conversationId) {
                    updatedList.firstOrNull()
                } else {
                    state.activeConversation
                }
            )
        }
    }

    // --- 内部辅助方法 ---

    private fun updateMessageContent(messageId: String, newContent: String) {
        _uiState.update { state ->
            val updatedConversations = state.conversations.map { conv ->
                conv.copy(
                    messages = conv.messages.map { msg ->
                        if (msg.id == messageId) {
                            msg.copy(content = newContent, isStreaming = true)
                        } else msg
                    },
                    updatedAt = System.currentTimeMillis()
                )
            }
            val updatedActive = state.activeConversation?.let { active ->
                updatedConversations.find { it.id == active.id } ?: active
            }
            state.copy(conversations = updatedConversations, activeConversation = updatedActive)
        }
    }

    private fun finalizeMessage(messageId: String, finalContent: String) {
        _uiState.update { state ->
            val updatedConversations = state.conversations.map { conv ->
                conv.copy(
                    messages = conv.messages.map { msg ->
                        if (msg.id == messageId) {
                            msg.copy(content = finalContent, isStreaming = false)
                        } else msg
                    }
                )
            }
            val updatedActive = state.activeConversation?.let { active ->
                updatedConversations.find { it.id == active.id } ?: active
            }
            state.copy(conversations = updatedConversations, activeConversation = updatedActive)
        }
    }

    private fun updateConversationMessages(
        conversationId: String,
        transform: (List<ChatMessage>) -> List<ChatMessage>
    ) {
        _uiState.update { state ->
            val updatedConversations = state.conversations.map { conv ->
                if (conv.id == conversationId) {
                    conv.copy(
                        messages = transform(conv.messages),
                        updatedAt = System.currentTimeMillis()
                    )
                } else conv
            }
            val updatedActive = if (state.activeConversation?.id == conversationId) {
                updatedConversations.find { it.id == conversationId }
            } else state.activeConversation

            state.copy(
                conversations = updatedConversations,
                activeConversation = updatedActive
            )
        }
    }

    private fun updateConversationTitle(conversationId: String, title: String) {
        _uiState.update { state ->
            val updated = state.conversations.map { conv ->
                if (conv.id == conversationId) conv.copy(title = title) else conv
            }
            state.copy(conversations = updated)
        }
    }
}
