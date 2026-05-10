package com.mobilecodex.model

/**
 * 聊天对话会话
 */
data class ChatConversation(
    val id: String,
    val title: String,
    val messages: List<ChatMessage> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val modelId: String = "gpt-4-turbo",
    val isArchived: Boolean = false
) {
    val previewText: String
        get() = messages.lastOrNull()?.content?.take(80) ?: "空对话"

    val messageCount: Int
        get() = messages.count { it.role != MessageRole.SYSTEM }
}
