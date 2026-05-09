package com.mobilecodex.model

/**
 * GitHub 设置数据模型
 * 用于配置 GitHub API 访问参数
 */
data class GitHubSettings(
    val token: String = "",
    val username: String = "",
    val defaultBranch: String = "main",
    val apiBaseUrl: String = "https://api.github.com",
    val pageSize: Int = 30, // 每页加载的仓库数量
    val includePrivateRepos: Boolean = true,
    val includeForkedRepos: Boolean = true,
    val autoFetchOnStartup: Boolean = true,
    val cacheTimeout: Long = 300_000 // 缓存超时时间（5分钟）
) {
    /**
     * 判断是否已配置 Token
     */
    val isConfigured: Boolean get() = token.isNotBlank()
    
    /**
     * 获取认证头
     */
    val authorizationHeader: String
        get() = "Bearer $token"
    
    /**
     * 判断是否使用自定义 API 基础 URL
     */
    val isCustomApiUrl: Boolean
        get() = apiBaseUrl != "https://api.github.com"
    
    /**
     * 创建更新 Token 的副本
     */
    fun withToken(token: String): GitHubSettings {
        return copy(token = token.trim())
    }
    
    /**
     * 创建更新用户名的副本
     */
    fun withUsername(username: String): GitHubSettings {
        return copy(username = username.trim())
    }
    
    /**
     * 创建更新默认分支的副本
     */
    fun withDefaultBranch(branch: String): GitHubSettings {
        return copy(defaultBranch = branch.trim())
    }
    
    /**
     * 创建更新 API 基础 URL 的副本
     */
    fun withApiBaseUrl(url: String): GitHubSettings {
        return copy(apiBaseUrl = url.trimEnd('/'))
    }
    
    /**
     * 创建更新分页大小的副本
     */
    fun withPageSize(size: Int): GitHubSettings {
        return copy(pageSize = size.coerceIn(1, 100))
    }
    
    /**
     * 创建更新包含私有仓库选项的副本
     */
    fun withIncludePrivateRepos(include: Boolean): GitHubSettings {
        return copy(includePrivateRepos = include)
    }
    
    /**
     * 创建更新包含 Fork 仓库选项的副本
     */
    fun withIncludeForkedRepos(include: Boolean): GitHubSettings {
        return copy(includeForkedRepos = include)
    }
    
    /**
     * 创建更新启动时自动获取选项的副本
     */
    fun withAutoFetchOnStartup(autoFetch: Boolean): GitHubSettings {
        return copy(autoFetchOnStartup = autoFetch)
    }
    
    /**
     * 创建更新缓存超时时间的副本
     */
    fun withCacheTimeout(timeout: Long): GitHubSettings {
        return copy(cacheTimeout = timeout.coerceIn(60_000, 3600_000))
    }
    
    /**
     * 构建仓库列表 API 端点
     */
    fun buildReposEndpoint(page: Int = 1): String {
        val visibility = if (includePrivateRepos) "all" else "public"
        val type = if (includeForkedRepos) "all" else "owner"
        return "$apiBaseUrl/user/repos?page=$page&per_page=$pageSize&visibility=$visibility&type=$type&sort=updated"
    }
    
    /**
     * 构建文件树 API 端点
     */
    fun buildTreeEndpoint(owner: String, repo: String, branch: String): String {
        return "$apiBaseUrl/repos/$owner/$repo/git/trees/$branch?recursive=1"
    }
    
    /**
     * 构建文件内容 API 端点
     */
    fun buildContentEndpoint(owner: String, repo: String, path: String): String {
        return "$apiBaseUrl/repos/$owner/$repo/contents/$path"
    }
    
    /**
     * 构建创建 Blob API 端点
     */
    fun buildCreateBlobEndpoint(owner: String, repo: String): String {
        return "$apiBaseUrl/repos/$owner/$repo/git/blobs"
    }
    
    /**
     * 构建创建 Tree API 端点
     */
    fun buildCreateTreeEndpoint(owner: String, repo: String): String {
        return "$apiBaseUrl/repos/$owner/$repo/git/trees"
    }
    
    /**
     * 构建创建 Commit API 端点
     */
    fun buildCreateCommitEndpoint(owner: String, repo: String): String {
        return "$apiBaseUrl/repos/$owner/$repo/git/commits"
    }
    
    /**
     * 构建更新 Ref API 端点
     */
    fun buildUpdateRefEndpoint(owner: String, repo: String, ref: String): String {
        return "$apiBaseUrl/repos/$owner/$repo/git/refs/$ref"
    }
    
    companion object {
        /**
         * 创建默认设置
         */
        fun default() = GitHubSettings()
        
        /**
         * 从环境变量或配置文件加载设置
         */
        fun fromEnvironment(): GitHubSettings {
            val token = System.getenv("GITHUB_TOKEN") ?: ""
            val username = System.getenv("GITHUB_USERNAME") ?: ""
            return GitHubSettings(
                token = token,
                username = username
            )
        }
    }
}