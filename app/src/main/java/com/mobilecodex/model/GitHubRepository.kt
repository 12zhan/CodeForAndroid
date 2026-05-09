package com.mobilecodex.model

import com.google.gson.annotations.SerializedName

/**
 * GitHub 仓库数据模型
 * 对应 GitHub API 的 Repository 对象
 */
data class GitHubRepository(
    val id: Long,
    val name: String,
    @SerializedName("full_name") val fullName: String,
    val owner: GitHubUser,
    val description: String?,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("default_branch") val defaultBranch: String,
    @SerializedName("private") val isPrivate: Boolean,
    @SerializedName("fork") val isFork: Boolean,
    @SerializedName("stargazers_count") val stargazersCount: Int,
    @SerializedName("watchers_count") val watchersCount: Int,
    @SerializedName("forks_count") val forksCount: Int,
    @SerializedName("open_issues_count") val openIssuesCount: Int,
    @SerializedName("language") val language: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("pushed_at") val pushedAt: String,
    val topics: List<String>?,
    val visibility: String
) {
    /**
     * 获取仓库的所有者和名称，用于 API 调用
     */
    val ownerName: String get() = owner.login
    
    /**
     * 判断是否为文本文件（用于文件树过滤）
     */
    companion object {
        // 常见二进制文件扩展名，应从文件树中过滤
        val BINARY_EXTENSIONS = setOf(
            "png", "jpg", "jpeg", "gif", "bmp", "ico", "svg", "webp",
            "mp3", "mp4", "avi", "mov", "wmv", "flv", "webm",
            "zip", "rar", "7z", "tar", "gz", "bz2",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "exe", "dll", "so", "dylib", "bin", "dat",
            "ttf", "otf", "woff", "woff2", "eot"
        )
    }
}