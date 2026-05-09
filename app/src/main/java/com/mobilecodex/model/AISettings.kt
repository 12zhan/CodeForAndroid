package com.mobilecodex.model

/**
 * AI 服务提供商枚举
 */
enum class AIProvider(val displayName: String, val defaultEndpoint: String) {
    OPENAI("OpenAI", "https://api.openai.com/v1"),
    ANTHROPIC("Anthropic", "https://api.anthropic.com/v1"),
    GOOGLE("Google AI", "https://generativelanguage.googleapis.com/v1"),
    AZURE("Azure OpenAI", ""),
    CUSTOM("自定义", "")
}

/**
 * AI 设置数据模型
 * 用于配置 AI 服务提供商的参数
 */
data class AISettings(
    val provider: AIProvider = AIProvider.OPENAI,
    val apiKey: String = "",
    val endpoint: String = provider.defaultEndpoint,
    val model: String = "gpt-4",
    val systemPrompt: String = DEFAULT_SYSTEM_PROMPT,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 4096,
    val topP: Float = 1.0f,
    val frequencyPenalty: Float = 0.0f,
    val presencePenalty: Float = 0.0f,
    val streamResponse: Boolean = true,
    val enableFunctionCalling: Boolean = true
) {
    /**
     * 判断 API Key 是否已配置
     */
    val isConfigured: Boolean get() = apiKey.isNotBlank()
    
    /**
     * 判断是否使用自定义端点
     */
    val isCustomEndpoint: Boolean get() = provider == AIProvider.CUSTOM
    
    /**
     * 获取实际使用的端点 URL
     */
    val effectiveEndpoint: String
        get() = when {
            endpoint.isNotBlank() -> endpoint
            else -> provider.defaultEndpoint
        }
    
    /**
     * 创建更新后的设置副本
     */
    fun withProvider(provider: AIProvider): AISettings {
        return copy(
            provider = provider,
            endpoint = if (provider != AIProvider.CUSTOM) provider.defaultEndpoint else endpoint,
            model = provider.defaultModel
        )
    }
    
    /**
     * 创建更新 API Key 的副本
     */
    fun withApiKey(key: String): AISettings {
        return copy(apiKey = key)
    }
    
    /**
     * 创建更新模型的副本
     */
    fun withModel(model: String): AISettings {
        return copy(model = model)
    }
    
    /**
     * 创建更新系统提示词的副本
     */
    fun withSystemPrompt(prompt: String): AISettings {
        return copy(systemPrompt = prompt)
    }
    
    /**
     * 创建更新温度的副本
     */
    fun withTemperature(temperature: Float): AISettings {
        return copy(temperature = temperature.coerceIn(0f, 2f))
    }
    
    /**
     * 创建更新最大 token 数的副本
     */
    fun withMaxTokens(maxTokens: Int): AISettings {
        return copy(maxTokens = maxTokens.coerceIn(1, 128000))
    }
    
    companion object {
        const val DEFAULT_SYSTEM_PROMPT = """你是一个专业的编程助手，擅长帮助用户理解和修改代码。

你可以：
1. 阅读和分析代码文件
2. 提供代码修改建议
3. 解释代码逻辑
4. 帮助调试和修复问题
5. 直接修改代码文件并保存

请使用中文回复，保持专业和友好的语气。在提供代码修改时，请解释你的修改原因和影响。"""
        
        /**
         * 创建默认设置
         */
        fun default() = AISettings()
    }
}

/**
 * 获取提供商支持的默认模型
 */
val AIProvider.defaultModel: String
    get() = when (this) {
        AIProvider.OPENAI -> "gpt-4"
        AIProvider.ANTHROPIC -> "claude-3-sonnet-20240229"
        AIProvider.GOOGLE -> "gemini-pro"
        AIProvider.AZURE -> "gpt-4"
        AIProvider.CUSTOM -> ""
    }

/**
 * 获取提供商支持的模型列表
 */
val AIProvider.availableModels: List<String>
    get() = when (this) {
        AIProvider.OPENAI -> listOf(
            "gpt-4-turbo-preview",
            "gpt-4",
            "gpt-4-32k",
            "gpt-3.5-turbo",
            "gpt-3.5-turbo-16k"
        )
        AIProvider.ANTHROPIC -> listOf(
            "claude-3-opus-20240229",
            "claude-3-sonnet-20240229",
            "claude-3-haiku-20240307",
            "claude-2.1",
            "claude-2.0"
        )
        AIProvider.GOOGLE -> listOf(
            "gemini-pro",
            "gemini-pro-vision",
            "gemini-ultra"
        )
        AIProvider.AZURE -> listOf(
            "gpt-4",
            "gpt-4-32k",
            "gpt-35-turbo",
            "gpt-35-turbo-16k"
        )
        AIProvider.CUSTOM -> emptyList()
    }