package com.mobilecodex.model

import com.google.gson.annotations.SerializedName

/**
 * Git 树节点（文件/目录）
 */
data class GitTreeNode(
    val path: String,
    val mode: String? = null,
    val type: String,       // "blob" = 文件, "tree" = 目录
    val sha: String? = null,
    val size: Long? = null,
    val url: String? = null
) {
    val isDirectory: Boolean get() = type == "tree"
    val isFile: Boolean get() = type == "blob"
    val name: String get() = path.substringAfterLast("/")
    val parentPath: String get() = path.substringBeforeLast("/", "")
    val extension: String get() = if (isFile) name.substringAfterLast(".", "") else ""
}

/**
 * GitHub 目录内容 API 响应项
 * 当获取单个文件时 content 字段包含 Base64 编码的文件内容
 * 当获取目录列表时 content 为 null
 */
data class GitHubContentItem(
    val name: String,
    val path: String,
    val sha: String,
    val size: Long,
    val url: String,
    @SerializedName("html_url") val htmlUrl: String? = null,
    @SerializedName("git_url") val gitUrl: String? = null,
    @SerializedName("download_url") val downloadUrl: String? = null,
    val type: String,  // "file" or "dir"
    val content: String? = null,       // Base64 编码内容（仅单文件获取时）
    val encoding: String? = null       // "base64" 当 content 不为空时
) {
    val isDirectory: Boolean get() = type == "dir"
    val isFile: Boolean get() = type == "file"
}
