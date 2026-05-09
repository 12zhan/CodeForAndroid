package com.mobilecodex.model

import java.util.UUID

/**
 * 聊天消息角色枚举
 */
enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM,
    FUNCTION
}

/**
 * 聊天消息数据模型
 * 表示对话中的单条消息
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachedFiles: List<String> = emptyList(), // 附件文件路径列表
    val functionName: String? = null, // Function Calling 时的函数名
    val functionCallId: String? = null, // Function Calling 时的调用 ID
    val isStreaming: Boolean = false, // 是否正在流式传输
    val isError: Boolean = false, // 是否为错误消息
    val metadata: Map<String, String> = emptyMap() // 额外元数据
) {
    /**
     * 判断是否为用户消息
     */
    val isUser: Boolean get() = role == MessageRole.USER
    
    /**
     * 判断是否为助手消息
     */
    val isAssistant: Boolean get() = role == MessageRole.ASSISTANT
    
    /**
     * 判断是否为系统消息
     */
    val isSystem: Boolean get() = role == MessageRole.SYSTEM
    
    /**
     * 判断是否为函数调用消息
     */
    val isFunctionCall: Boolean get() = role == MessageRole.FUNCTION
    
    /**
     * 判断是否包含附件
     */
    val hasAttachments: Boolean get() = attachedFiles.isNotEmpty()
    
    /**
     * 获取格式化的时间戳
     */
    val formattedTime: String
        get() {
            val date = java.util.Date(timestamp)
            val format = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            return format.format(date)
        }
    
    /**
     * 创建流式传输中的消息副本
     */
    fun withStreamingContent(content: String): ChatMessage {
        return copy(content = content, isStreaming = true)
    }
    
    /**
     * 完成流式传输
     */
    fun finishStreaming(): ChatMessage {
        return copy(isStreaming = false)
    }
    
    /**
     * 创建错误消息
     */
    fun withError(error: String): ChatMessage {
        return copy(content = error, isError = true, isStreaming = false)
    }
    
    /**
     * 创建带附件的消息副本
     */
    fun withAttachments(files: List<String>): ChatMessage {
        return copy(attachedFiles = files)
    }
    
    companion object {
        /**
         * 创建用户消息
         */
        fun user(content: String, attachedFiles: List<String> = emptyList()): ChatMessage {
            return ChatMessage(
                role = MessageRole.USER,
                content = content,
                attachedFiles = attachedFiles
            )
        }
        
        /**
         * 创建助手消息
         */
        fun assistant(content: String): ChatMessage {
            return ChatMessage(
                role = MessageRole.ASSISTANT,
                content = content
            )
        }
        
        /**
         * 创建系统消息
         */
        fun system(content: String): ChatMessage {
            return ChatMessage(
                role = MessageRole.SYSTEM,
                content = content
            )
        }
        
        /**
         * 创建函数调用消息
         */
        fun functionCall(name: String, callId: String, arguments: String): ChatMessage {
            return ChatMessage(
                role = MessageRole.FUNCTION,
                content = arguments,
                functionName = name,
                functionCallId = callId
            )
        }
    }
}