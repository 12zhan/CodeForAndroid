package com.mobilecodex.model

/**
 * GitHub 设置数据模型
 * 用于配置 GitHub 连接参数
 */
data class GitHubSettings(
    val token: String = "",
    val username: String = "",
    val apiBaseUrl: String = "https://api.github.com",
    val defaultBranch: String = "main",
    val pageSize: Int = 30,
    val includePrivateRepos: Boolean = true,
    val includeForkedRepos: Boolean = true,
    val autoFetchOnStartup: Boolean = true,
    val autoCommit: Boolean = true,
    val commitMessageTemplate: String = "Update via CodeForAndroid"
) {
    /**
     * 判断 Token 是否已配置
     */
    val isConfigured: Boolean get() = token.isNotBlank()

    fun withToken(token: String) = copy(token = token)
    fun withUsername(username: String) = copy(username = username)
    fun withApiBaseUrl(url: String) = copy(apiBaseUrl = url)
    fun withDefaultBranch(branch: String) = copy(defaultBranch = branch)
    fun withPageSize(size: Int) = copy(pageSize = size)
    fun withIncludePrivateRepos(include: Boolean) = copy(includePrivateRepos = include)
    fun withIncludeForkedRepos(include: Boolean) = copy(includeForkedRepos = include)
    fun withAutoFetchOnStartup(autoFetch: Boolean) = copy(autoFetchOnStartup = autoFetch)

    companion object {
        fun default() = GitHubSettings()
    }
}
