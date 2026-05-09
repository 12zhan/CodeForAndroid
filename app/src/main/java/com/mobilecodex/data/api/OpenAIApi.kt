package com.mobilecodex.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * OpenAI 兼容 API 接口
 * 支持 OpenAI、自定义端点等
 */
interface OpenAIApi {
    
    /**
     * 创建对话完成（非流式）
     */
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Body request: ChatCompletionRequest,
        @Header("Authorization") authorization: String
    ): Response<ChatCompletionResponse>
    
    /**
     * 创建对话完成（流式）
     */
    @Streaming
    @POST("chat/completions")
    suspend fun createChatCompletionStream(
        @Body request: ChatCompletionRequest,
        @Header("Authorization") authorization: String
    ): Response<ResponseBody>
}

// ==================== 请求数据类 ====================

/**
 * 对话完成请求
 */
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessageRequest>,
    val temperature: Float = 0.7f,
    val max_tokens: Int = 4096,
    val top_p: Float = 1.0f,
    val frequency_penalty: Float = 0.0f,
    val presence_penalty: Float = 0.0f,
    val stream: Boolean = false,
    val tools: List<Tool>? = null,
    val tool_choice: String? = null
)

/**
 * 消息请求
 */
data class ChatMessageRequest(
    val role: String,
    val content: String,
    val name: String? = null
)

/**
 * 工具定义（用于 Function Calling）
 */
data class Tool(
    val type: String = "function",
    val function: FunctionDefinition
)

/**
 * 函数定义
 */
data class FunctionDefinition(
    val name: String,
    val description: String,
    val parameters: Map<String, Any>
)

// ==================== 响应数据类 ====================

/**
 * 对话完成响应
 */
data class ChatCompletionResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage?
)

/**
 * 选择项
 */
data class Choice(
    val index: Int,
    val message: ChatMessageResponse,
    val finish_reason: String?
)

/**
 * 消息响应
 */
data class ChatMessageResponse(
    val role: String,
    val content: String?
)

/**
 * 使用统计
 */
data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

/**
 * 流式响应块
 */
data class StreamChunk(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<StreamChoice>
)

/**
 * 流式选择项
 */
data class StreamChoice(
    val index: Int,
    val delta: Delta,
    val finish_reason: String?
)

/**
 * 增量内容
 */
data class Delta(
    val role: String? = null,
    val content: String? = null
)