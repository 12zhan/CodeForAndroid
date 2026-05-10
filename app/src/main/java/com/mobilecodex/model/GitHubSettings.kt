package com.mobilecodex.model

/**
 * GitHub 设置数据模型
 * 用于配置 GitHub 连接参数
 */
data class GitHubSettings(
    val token: String = "",                // GitHub Personal Access Token
    val username: String = "",             // GitHub 用户名
    val autoCommit: Boolean = true,        // 是否自动提交
    val commitMessageTemplate: String = "Update via CodeForAndroid",  // 提交信息模板
    val defaultBranch: String = "main"     // 默认分支
) {
    /**
     * 判断 Token 是否已配置
     */
    val isConfigured: Boolean get() = token.isNotBlank()

    companion object {
        fun default() = GitHubSettings()
    }
}
