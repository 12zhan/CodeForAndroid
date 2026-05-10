package com.mobilecodex.model

/**
 * AI 服务设置
 */
data class AISettings(
    val apiKey: String = "",
    val baseUrl: String = "https://api.openai.com/v1",
    val modelId: String = "gpt-4-turbo",
    val maxTokens: Int = 4096,
    val temperature: Float = 0.7f,
    val topP: Float = 1.0f,
    val systemPrompt: String = "你是一个专业的编程助手，帮助用户编写、审查和优化代码。请用中文回答，代码示例除外。",
    val isConfigured: Boolean get() = apiKey.isNotBlank()
)
