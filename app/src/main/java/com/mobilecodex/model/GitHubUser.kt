package com.mobilecodex.model

import com.google.gson.annotations.SerializedName

/**
 * GitHub 用户信息
 */
data class GitHubUser(
    val login: String,
    val id: Long,
    @SerializedName("avatar_url") val avatarUrl: String,
    @SerializedName("html_url") val htmlUrl: String,
    val name: String? = null,
    val bio: String? = null,
    @SerializedName("public_repos") val publicRepos: Int = 0,
    @SerializedName("followers") val followers: Int = 0,
    @SerializedName("following") val following: Int = 0,
    val email: String? = null,
    val company: String? = null,
    val location: String? = null,
    val blog: String? = null
)
