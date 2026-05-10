package com.mobilecodex.data.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * OpenAI Chat Completions API（含流式支持）
 */
interface OpenAIApi {

    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): Response<ChatCompletionResponse>

    @POST("chat/completions")
    @Streaming
    suspend fun createChatCompletionStream(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): Response<ResponseBody>

    @GET("models")
    suspend fun listModels(
        @Header("Authorization") authorization: String
    ): Response<ModelsListResponse>
}

// --- Request DTOs ---

data class ChatCompletionRequest(
    val model: String,
    val messages: List<MessageDto>,
    val temperature: Float = 0.7f,
    @com.google.gson.annotations.SerializedName("max_tokens") val maxTokens: Int = 4096,
    @com.google.gson.annotations.SerializedName("top_p") val topP: Float = 1.0f,
    val stream: Boolean = false
)

data class MessageDto(
    val role: String,
    val content: String
)

// --- Response DTOs ---

data class ChatCompletionResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<ChoiceDto>,
    val usage: UsageDto?
)

data class ChoiceDto(
    val index: Int,
    val message: MessageDto?,
    val delta: DeltaDto?,
    @com.google.gson.annotations.SerializedName("finish_reason") val finishReason: String?
)

data class DeltaDto(
    val role: String?,
    val content: String?
)

data class UsageDto(
    @com.google.gson.annotations.SerializedName("prompt_tokens") val promptTokens: Int,
    @com.google.gson.annotations.SerializedName("completion_tokens") val completionTokens: Int,
    @com.google.gson.annotations.SerializedName("total_tokens") val totalTokens: Int
)

data class ModelsListResponse(
    val `object`: String,
    val data: List<ModelDto>
)

data class ModelDto(
    val id: String,
    val `object`: String,
    val ownedBy: String
)
