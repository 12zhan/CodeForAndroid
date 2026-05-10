package com.mobilecodex.model

/**
 * GitHub 设置
 */
data class GitHubSettings(
    val accessToken: String = "",
    val username: String = "",
    val apiBaseUrl: String = "https://api.github.com",
    val isConfigured: Boolean get() = accessToken.isNotBlank()
)
