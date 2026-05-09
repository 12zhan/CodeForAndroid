package com.mobilecodex.model

import com.google.gson.annotations.SerializedName

/**
 * Git 树节点数据模型
 * 对应 GitHub API 的 Tree 对象中的节点
 */
data class GitTreeNode(
    val path: String,
    val mode: String,
    val type: String, // "blob" 或 "tree"
    val sha: String,
    val size: Long?,
    val url: String
) {
    /**
     * 判断是否为文件（blob）
     */
    val isFile: Boolean get() = type == "blob"
    
    /**
     * 判断是否为目录（tree）
     */
    val isDirectory: Boolean get() = type == "tree"
    
    /**
     * 获取文件扩展名（如果是文件）
     */
    val extension: String?
        get() {
            if (!isFile) return null
            val lastDot = path.lastIndexOf('.')
            return if (lastDot > 0 && lastDot < path.length - 1) {
                path.substring(lastDot + 1).lowercase()
            } else {
                null
            }
        }
    
    /**
     * 判断是否为文本文件（基于扩展名）
     */
    val isTextFile: Boolean
        get() {
            if (!isFile) return false
            val ext = extension ?: return true // 无扩展名默认为文本文件
            return ext !in GitHubRepository.BINARY_EXTENSIONS
        }
    
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
            return if (lastSlash > 0) {
                path.substring(0, lastSlash)
            } else {
                null
            }
        }
}