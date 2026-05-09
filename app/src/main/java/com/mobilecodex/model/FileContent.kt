package com.mobilecodex.model

import com.google.gson.annotations.SerializedName

/**
 * 文件内容数据模型
 * 对应 GitHub API 的 Content 对象
 */
data class FileContent(
    val name: String,
    val path: String,
    val sha: String,
    val size: Long,
    val url: String,
    @SerializedName("html_url") val htmlUrl: String?,
    @SerializedName("git_url") val gitUrl: String?,
    @SerializedName("download_url") val downloadUrl: String?,
    val type: String, // "file", "dir", "symlink", "submodule"
    val content: String?,
    val encoding: String?, // "base64" 或 "none"
    @SerializedName("_links") val links: Links?
) {
    data class Links(
        val self: String,
        val git: String?,
        val html: String?
    )
    
    /**
     * 解码 Base64 内容为字符串
     */
    fun decodedContent(): String? {
        if (content == null || encoding != "base64") return content
        return try {
            String(android.util.Base64.decode(content, android.util.Base64.DEFAULT))
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 判断是否为文件
     */
    val isFile: Boolean get() = type == "file"
    
    /**
     * 判断是否为目录
     */
    val isDirectory: Boolean get() = type == "dir"
}