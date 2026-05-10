package com.mobilecodex.model

/**
 * 虚拟文件系统节点（用于本地编辑）
 */
data class VirtualFile(
    val id: String,
    val name: String,
    val path: String,
    val isDirectory: Boolean = false,
    val children: List<VirtualFile> = emptyList(),
    val content: String? = null,
    val originalContent: String? = null,
    val sha: String? = null,
    val language: String = FileContent.inferLanguage(name),
    val isModified: Boolean = false,
    val isExpanded: Boolean = false
)
