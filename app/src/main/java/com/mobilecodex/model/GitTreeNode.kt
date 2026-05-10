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
)
