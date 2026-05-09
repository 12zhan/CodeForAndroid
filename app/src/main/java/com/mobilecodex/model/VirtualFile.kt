package com.mobilecodex.model

/**
 * 虚拟文件系统（VFS）中的文件模型
 * 用于在内存中缓存和管理文件状态
 */
data class VirtualFile(
    val path: String,
    val content: String? = null,
    val isModified: Boolean = false,
    val isLoaded: Boolean = false,
    val sha: String? = null,
    val size: Long? = null,
    val lastModified: Long = System.currentTimeMillis()
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
     * 获取文件扩展名
     */
    val extension: String?
        get() {
            val lastDot = path.lastIndexOf('.')
            return if (lastDot > 0 && lastDot < path.length - 1) {
                path.substring(lastDot + 1).lowercase()
            } else {
                null
            }
        }
    
    /**
     * 判断是否为文本文件
     */
    val isTextFile: Boolean
        get() {
            val ext = extension ?: return true
            return ext !in GitHubRepository.BINARY_EXTENSIONS
        }
    
    /**
     * 创建已加载内容的副本
     */
    fun withContent(content: String, sha: String? = null): VirtualFile {
        return copy(
            content = content,
            isLoaded = true,
            sha = sha ?: this.sha,
            lastModified = System.currentTimeMillis()
        )
    }
    
    /**
     * 创建已修改的副本
     */
    fun withModification(content: String): VirtualFile {
        return copy(
            content = content,
            isModified = true,
            lastModified = System.currentTimeMillis()
        )
    }
    
    /**
     * 重置修改状态（保存后调用）
     */
    fun resetModification(): VirtualFile {
        return copy(
            isModified = false,
            lastModified = System.currentTimeMillis()
        )
    }
    
    /**
     * 判断内容是否为空或未加载
     */
    val isEmpty: Boolean get() = content.isNullOrBlank()
    
    /**
     * 获取内容行数
     */
    val lineCount: Int
        get() = content?.lines()?.size ?: 0
}