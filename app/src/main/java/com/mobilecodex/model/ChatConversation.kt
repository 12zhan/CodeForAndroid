package com.mobilecodex.model

import java.util.UUID

/**
 * 聊天对话数据模型
 * 表示一个完整的对话会话
 */
data class ChatConversation(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "新对话",
    val messages: List<ChatMessage> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val workspaceId: String? = null, // 关联的工作区 ID
    val metadata: Map<String, String> = emptyMap()
) {
    /**
     * 获取消息数量
     */
    val messageCount: Int get() = messages.size
    
    /**
     * 判断是否有消息
     */
    val hasMessages: Boolean get() = messages.isNotEmpty()
    
    /**
     * 获取最后一条消息
     */
    val lastMessage: ChatMessage? get() = messages.lastOrNull()
    
    /**
     * 获取最后一条助手消息
     */
    val lastAssistantMessage: ChatMessage?
        get() = messages.lastOrNull { it.isAssistant }
    
    /**
     * 获取最后一条用户消息
     */
    val lastUserMessage: ChatMessage?
        get() = messages.lastOrNull { it.isUser }
    
    /**
     * 判断是否正在等待响应（最后一条是用户消息）
     */
    val isWaitingForResponse: Boolean
        get() {
            val last = lastMessage ?: return false
            return last.isUser && !last.isStreaming
        }
    
    /**
     * 判断是否正在流式传输
     */
    val isStreaming: Boolean
        get() = messages.any { it.isStreaming }
    
    /**
     * 添加消息
     */
    fun withMessage(message: ChatMessage): ChatConversation {
        return copy(
            messages = messages + message,
            updatedAt = System.currentTimeMillis(),
            title = if (messages.isEmpty() && message.isUser) {
                // 使用第一条用户消息作为标题（截取前50个字符）
                message.content.take(50).let { 
                    if (it.length < message.content.length) "$it..." else it 
                }
            } else {
                title
            }
        )
    }
    
    /**
     * 更新最后一条消息（用于流式传输）
     */
    fun updateLastMessage(update: (ChatMessage) -> ChatMessage): ChatConversation {
        if (messages.isEmpty()) return this
        val updatedMessages = messages.toMutableList()
        updatedMessages[updatedMessages.size - 1] = update(updatedMessages.last())
        return copy(messages = updatedMessages, updatedAt = System.currentTimeMillis())
    }
    
    /**
     * 清除所有消息
     */
    fun clearMessages(): ChatConversation {
        return copy(
            messages = emptyList(),
            title = "新对话",
            updatedAt = System.currentTimeMillis()
        )
    }
    
    /**
     * 获取对话的简短摘要
     */
    val summary: String
        get() {
            if (!hasMessages) return "空对话"
            val userMessages = messages.count { it.isUser }
            val assistantMessages = messages.count { it.isAssistant }
            return "$userMessages 条用户消息, $assistantMessages 条助手回复"
        }
    
    /**
     * 判断是否关联到工作区
     */
    val hasWorkspace: Boolean get() = workspaceId != null
    
    companion object {
        /**
         * 创建新的空对话
         */
        fun create(title: String = "新对话", workspaceId: String? = null): ChatConversation {
            return ChatConversation(
                title = title,
                workspaceId = workspaceId
            )
        }
    }
}