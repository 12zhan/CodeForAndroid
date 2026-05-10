package com.mobilecodex.model

/**
 * GitHub 仓库领域模型
 */
data class GitHubRepository(
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String?,
    val isPrivate: Boolean,
    val isFork: Boolean,
    val htmlUrl: String,
    val defaultBranch: String,
    val language: String?,
    val stargazersCount: Int,
    val forksCount: Int,
    val updatedAt: String
)
