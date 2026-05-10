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
    val updatedAt: String,
    val owner: GitHubUser? = null
) {
    companion object {
        /**
         * 二进制文件扩展名列表（不可编辑的文件类型）
         */
        val BINARY_EXTENSIONS = setOf(
            "png", "jpg", "jpeg", "gif", "bmp", "ico", "svg",
            "mp3", "wav", "ogg", "flac", "aac",
            "mp4", "avi", "mkv", "mov", "webm",
            "zip", "tar", "gz", "rar", "7z",
            "apk", "aab", "jar", "dex",
            "so", "dll", "dylib",
            "ttf", "otf", "woff", "woff2",
            "db", "sqlite", "sqlite3",
            "bin", "dat", "exe", "class",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx"
        )
    }
}
