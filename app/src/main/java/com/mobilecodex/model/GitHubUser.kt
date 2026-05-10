package com.mobilecodex.model

/**
 * GitHub 用户领域模型
 */
data class GitHubUser(
    val login: String,
    val id: Long,
    val avatarUrl: String,
    val name: String?,
    val email: String?,
    val bio: String?,
    val publicRepos: Int,
    val followers: Int,
    val following: Int,
    val htmlUrl: String = ""
)
