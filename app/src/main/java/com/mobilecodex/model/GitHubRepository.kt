package com.mobilecodex.model

import com.google.gson.annotations.SerializedName

/**
 * GitHub 仓库
 */
data class GitHubRepository(
    val id: Long,
    val name: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("html_url") val htmlUrl: String,
    val description: String? = null,
    @SerializedName("private") val isPrivate: Boolean = false,
    @SerializedName("fork") val isFork: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("pushed_at") val pushedAt: String? = null,
    @SerializedName("default_branch") val defaultBranch: String = "main",
    val language: String? = null,
    @SerializedName("stargazers_count") val stars: Int = 0,
    @SerializedName("forks_count") val forks: Int = 0,
    @SerializedName("open_issues_count") val openIssues: Int = 0,
    @SerializedName("watchers_count") val watchers: Int = 0,
    @SerializedName("size") val size: Int = 0,
    @SerializedName("owner") val owner: GitHubUser? = null
)
