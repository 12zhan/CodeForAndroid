package com.mobilecodex.model

import com.google.gson.annotations.SerializedName

/**
 * GitHub 用户数据模型
 * 对应 GitHub API 的 User 对象
 */
data class GitHubUser(
    val login: String,
    val id: Long,
    @SerializedName("avatar_url") val avatarUrl: String,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("repos_url") val reposUrl: String,
    @SerializedName("type") val type: String,
    @SerializedName("site_admin") val siteAdmin: Boolean,
    val name: String?,
    val company: String?,
    val blog: String?,
    val location: String?,
    val email: String?,
    val bio: String?,
    @SerializedName("twitter_username") val twitterUsername: String?,
    @SerializedName("public_repos") val publicRepos: Int,
    @SerializedName("public_gists") val publicGists: Int,
    val followers: Int,
    val following: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
) {
    /**
     * 获取显示名称，优先使用真实姓名，否则使用登录名
     */
    val displayName: String get() = name ?: login
    
    /**
     * 判断是否为组织账户
     */
    val isOrganization: Boolean get() = type == "Organization"
}