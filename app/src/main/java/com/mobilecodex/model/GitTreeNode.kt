package com.mobilecodex.model

/**
 * Git 树节点领域模型
 */
data class GitTreeNode(
    val path: String,
    val mode: String,
    val type: String,
    val sha: String,
    val size: Long? = null,
    val url: String? = null
) {
    /**
     * 获取文件名（不含路径）
     */
    val fileName: String
        get() {
            val lastSlash = path.lastIndexOf('/')
            return if (lastSlash >= 0 && lastSlash < path.length - 1) {
                path.substring(lastSlash + 1)
            } else {
                path
            }
        }

    /**
     * 获取父目录路径
     */
    val parentPath: String?
        get() {
            val lastSlash = path.lastIndexOf('/')
            return if (lastSlash > 0) path.substring(0, lastSlash) else null
        }

    /**
     * 获取文件扩展名
     */
    val extension: String?
        get() {
            val name = fileName
            val lastDot = name.lastIndexOf('.')
            return if (lastDot > 0 && lastDot < name.length - 1) {
                name.substring(lastDot + 1).lowercase()
            } else {
                null
            }
        }

    /**
     * 判断是否为目录
     */
    val isDirectory: Boolean get() = type == "tree"

    /**
     * 判断是否为文件
     */
    val isFile: Boolean get() = type == "blob"

    /**
     * 判断是否为文本文件
     */
    val isTextFile: Boolean
        get() {
            if (isDirectory) return false
            val ext = extension ?: return true
            return ext !in TEXT_EXTENSIONS_BLACKLIST
        }

    companion object {
        /**
         * 二进制/非文本文件扩展名黑名单
         */
        val TEXT_EXTENSIONS_BLACKLIST = setOf(
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
