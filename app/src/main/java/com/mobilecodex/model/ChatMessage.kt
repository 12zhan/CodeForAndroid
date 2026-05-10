package com.mobilecodex.model

import com.google.gson.annotations.SerializedName

/**
 * 聊天消息角色
 */
enum class MessageRole {
    @SerializedName("system") SYSTEM,
    @SerializedName("user") USER,
    @SerializedName("assistant") ASSISTANT;
}

/**
 * 单条聊天消息
 */
data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val tokensUsed: Int = 0
)
