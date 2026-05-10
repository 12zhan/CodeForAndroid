package com.mobilecodex.model

/**
 * 工作区状态
 */
data class WorkspaceState(
    val currentRepo: GitHubRepository? = null,
    val currentBranch: String = "main",
    val fileTree: List<GitTreeNode> = emptyList(),
    val openedFiles: List<VirtualFile> = emptyList(),
    val activeFileId: String? = null,
    val isLoadingRepo: Boolean = false,
    val isLoadingFile: Boolean = false,
    val repoError: String? = null
)
