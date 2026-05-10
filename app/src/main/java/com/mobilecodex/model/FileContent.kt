package com.mobilecodex.model

/**
 * 文件内容领域模型
 */
data class FileContent(
    val name: String,
    val path: String,
    val sha: String,
    val size: Long,
    val content: String,
    val encoding: String,
    val type: String
)
