package com.mobilecodex.model

/**
 * 工作区状态模型
 * 管理当前工作区（本地或 GitHub 仓库）的状态
 */
data class WorkspaceState(
    val currentWorkspace: WorkspaceOption? = null,
    val fileTree: List<GitTreeNode> = emptyList(),
    val virtualFiles: Map<String, VirtualFile> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastRefresh: Long = System.currentTimeMillis()
) {
    /**
     * 获取已修改的文件列表
     */
    val modifiedFiles: List<VirtualFile>
        get() = virtualFiles.values.filter { it.isModified }
    
    /**
     * 获取已修改文件的数量
     */
    val modifiedCount: Int get() = modifiedFiles.size
    
    /**
     * 判断是否有未保存的修改
     */
    val hasUnsavedChanges: Boolean get() = modifiedCount > 0
    
    /**
     * 获取所有已加载的文件
     */
    val loadedFiles: List<VirtualFile>
        get() = virtualFiles.values.filter { it.isLoaded }
    
    /**
     * 获取所有未加载的文件（懒加载）
     */
    val unloadedFiles: List<GitTreeNode>
        get() = fileTree.filter { node ->
            node.isFile && node.isTextFile && !virtualFiles.containsKey(node.path)
        }
    
    /**
     * 获取指定路径的虚拟文件
     */
    fun getFile(path: String): VirtualFile? = virtualFiles[path]
    
    /**
     * 判断文件是否已加载
     */
    fun isFileLoaded(path: String): Boolean = virtualFiles[path]?.isLoaded == true
    
    /**
     * 判断文件是否已修改
     */
    fun isFileModified(path: String): Boolean = virtualFiles[path]?.isModified == true
    
    /**
     * 添加或更新虚拟文件
     */
    fun withFile(file: VirtualFile): WorkspaceState {
        return copy(
            virtualFiles = virtualFiles + (file.path to file),
            lastRefresh = System.currentTimeMillis()
        )
    }
    
    /**
     * 移除虚拟文件
     */
    fun withoutFile(path: String): WorkspaceState {
        return copy(
            virtualFiles = virtualFiles - path,
            lastRefresh = System.currentTimeMillis()
        )
    }
    
    /**
     * 更新文件树
     */
    fun withFileTree(tree: List<GitTreeNode>): WorkspaceState {
        return copy(
            fileTree = tree,
            lastRefresh = System.currentTimeMillis()
        )
    }
    
    /**
     * 切换工作区
     */
    fun withWorkspace(workspace: WorkspaceOption): WorkspaceState {
        return copy(
            currentWorkspace = workspace,
            fileTree = emptyList(),
            virtualFiles = emptyMap(),
            isLoading = false,
            error = null,
            lastRefresh = System.currentTimeMillis()
        )
    }
    
    /**
     * 设置加载状态
     */
    fun withLoading(isLoading: Boolean): WorkspaceState {
        return copy(isLoading = isLoading)
    }
    
    /**
     * 设置错误状态
     */
    fun withError(error: String?): WorkspaceState {
        return copy(error = error)
    }
    
    companion object {
        /**
         * 创建空的工作区状态
         */
        fun empty() = WorkspaceState()
        
        /**
         * 创建本地工作区状态
         */
        fun local() = WorkspaceState(
            currentWorkspace = WorkspaceOption.local()
        )
    }
}