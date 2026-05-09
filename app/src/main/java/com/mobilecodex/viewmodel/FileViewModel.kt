package com.mobilecodex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilecodex.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 文件 UI 状态
 */
data class FileUiState(
    val fileTree: List<GitTreeNode> = emptyList(),
    val virtualFiles: Map<String, VirtualFile> = emptyMap(),
    val selectedFile: VirtualFile? = null,
    val selectedFilePath: String? = null,
    val expandedFolders: Set<String> = emptySet(),
    val isLoadingFile: Boolean = false,
    val isLoadingTree: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val searchResults: List<GitTreeNode> = emptyList(),
    val isEditing: Boolean = false,
    val editContent: String = ""
)

/**
 * 文件事件
 */
sealed class FileEvent {
    data class ShowError(val error: String) : FileEvent()
    data class ShowMessage(val message: String) : FileEvent()
    data class FileSelected(val path: String) : FileEvent()
    data class FileSaved(val path: String) : FileEvent()
    object TreeLoaded : FileEvent()
}

/**
 * 文件 ViewModel
 * 管理文件树浏览、文件内容编辑和 VFS 状态
 */
@HiltViewModel
class FileViewModel @Inject constructor(
    // 注入的仓库将在这里添加
    // private val githubRepository: GitHubRepository,
    // private val fileRepository: FileRepository
) : ViewModel() {
    
    // 文件 UI 状态
    private val _uiState = MutableStateFlow(FileUiState())
    val uiState: StateFlow<FileUiState> = _uiState.asStateFlow()
    
    // 一次性事件
    private val _events = MutableSharedFlow<FileEvent>()
    val events: SharedFlow<FileEvent> = _events.asSharedFlow()
    
    // 文件树
    val fileTree: StateFlow<List<GitTreeNode>> = _uiState
        .map { it.fileTree }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 虚拟文件
    val virtualFiles: StateFlow<Map<String, VirtualFile>> = _uiState
        .map { it.virtualFiles }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
    
    // 选中的文件
    val selectedFile: StateFlow<VirtualFile?> = _uiState
        .map { it.selectedFile }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    // 已修改的文件列表
    val modifiedFiles: StateFlow<List<VirtualFile>> = _uiState
        .map { it.virtualFiles.values.filter { file -> file.isModified } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 已修改文件数量
    val modifiedCount: StateFlow<Int> = _uiState
        .map { it.virtualFiles.values.count { file -> file.isModified } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    // 加载状态
    val isLoading: StateFlow<Boolean> = _uiState
        .map { it.isLoadingFile || it.isLoadingTree }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    // 错误状态
    val error: StateFlow<String?> = _uiState
        .map { it.error }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    // 搜索查询
    val searchQuery: StateFlow<String> = _uiState
        .map { it.searchQuery }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    
    // 搜索结果
    val searchResults: StateFlow<List<GitTreeNode>> = _uiState
        .map { it.searchResults }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 是否正在编辑
    val isEditing: StateFlow<Boolean> = _uiState
        .map { it.isEditing }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    /**
     * 加载文件树
     */
    fun loadFileTree(
        token: String,
        owner: String,
        repo: String,
        branch: String = "main"
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTree = true, error = null) }
            
            try {
                // TODO: 调用 GitHub API 获取文件树
                // val tree = githubRepository.getTree(token, owner, repo, branch)
                // 
                // // 过滤掉二进制文件
                // val filteredTree = tree.filter { node ->
                //     node.isDirectory || (node.isFile && node.isTextFile)
                // }
                // 
                // _uiState.update { it.copy(fileTree = filteredTree, isLoadingTree = false) }
                // _events.emit(FileEvent.TreeLoaded)
                
                // 临时模拟
                _uiState.update { it.copy(isLoadingTree = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingTree = false, error = e.message) }
                _events.emit(FileEvent.ShowError("加载文件树失败: ${e.message}"))
            }
        }
    }
    
    /**
     * 设置文件树（从外部传入）
     */
    fun setFileTree(tree: List<GitTreeNode>) {
        _uiState.update { it.copy(fileTree = tree) }
    }
    
    /**
     * 选择文件
     */
    fun selectFile(path: String) {
        viewModelScope.launch {
            val existingFile = _uiState.value.virtualFiles[path]
            
            if (existingFile != null && existingFile.isLoaded) {
                // 文件已加载，直接显示
                _uiState.update { 
                    it.copy(
                        selectedFile = existingFile,
                        selectedFilePath = path,
                        editContent = existingFile.content ?: "",
                        isEditing = false
                    )
                }
            } else {
                // 文件未加载，需要从 GitHub 获取
                _uiState.update { it.copy(isLoadingFile = true, selectedFilePath = path) }
                
                // TODO: 通知 GitHubViewModel 加载文件内容
                // 这里应该通过共享的 ViewModel 或事件来触发加载
                
                _events.emit(FileEvent.FileSelected(path))
            }
        }
    }
    
    /**
     * 设置文件内容（从 GitHub 加载后调用）
     */
    fun setFileContent(path: String, content: String, sha: String? = null, size: Long? = null) {
        val virtualFile = VirtualFile(
            path = path,
            content = content,
            isLoaded = true,
            sha = sha,
            size = size
        )
        
        _uiState.update { currentState ->
            currentState.copy(
                virtualFiles = currentState.virtualFiles + (path to virtualFile),
                selectedFile = virtualFile,
                editContent = content,
                isLoadingFile = false,
                isEditing = false
            )
        }
    }
    
    /**
     * 开始编辑文件
     */
    fun startEditing() {
        val selectedFile = _uiState.value.selectedFile ?: return
        _uiState.update { 
            it.copy(
                isEditing = true,
                editContent = selectedFile.content ?: ""
            )
        }
    }
    
    /**
     * 更新编辑内容
     */
    fun updateEditContent(content: String) {
        _uiState.update { it.copy(editContent = content) }
    }
    
    /**
     * 保存文件到 VFS
     */
    fun saveFile() {
        val selectedFilePath = _uiState.value.selectedFilePath ?: return
        val editContent = _uiState.value.editContent
        val existingFile = _uiState.value.virtualFiles[selectedFilePath]
        
        val updatedFile = if (existingFile != null) {
            existingFile.withModification(editContent)
        } else {
            VirtualFile(
                path = selectedFilePath,
                content = editContent,
                isModified = true,
                isLoaded = true
            )
        }
        
        _uiState.update { currentState ->
            currentState.copy(
                virtualFiles = currentState.virtualFiles + (selectedFilePath to updatedFile),
                selectedFile = updatedFile,
                isEditing = false
            )
        }
        
        viewModelScope.launch {
            _events.emit(FileEvent.FileSaved(selectedFilePath))
            _events.emit(FileEvent.ShowMessage("文件已保存"))
        }
    }
    
    /**
     * 撤销编辑
     */
    fun revertEdit() {
        val selectedFile = _uiState.value.selectedFile ?: return
        _uiState.update { 
            it.copy(
                editContent = selectedFile.content ?: "",
                isEditing = false
            )
        }
    }
    
    /**
     * 关闭文件
     */
    fun closeFile() {
        _uiState.update { 
            it.copy(
                selectedFile = null,
                selectedFilePath = null,
                isEditing = false,
                editContent = ""
            )
        }
    }
    
    /**
     * 切换文件夹展开/折叠状态
     */
    fun toggleFolder(folderPath: String) {
        _uiState.update { currentState ->
            val expanded = currentState.expandedFolders
            val newExpanded = if (folderPath in expanded) {
                expanded - folderPath
            } else {
                expanded + folderPath
            }
            currentState.copy(expandedFolders = newExpanded)
        }
    }
    
    /**
     * 展开所有文件夹
     */
    fun expandAllFolders() {
        val folders = _uiState.value.fileTree
            .filter { it.isDirectory }
            .map { it.path }
            .toSet()
        _uiState.update { it.copy(expandedFolders = folders) }
    }
    
    /**
     * 折叠所有文件夹
     */
    fun collapseAllFolders() {
        _uiState.update { it.copy(expandedFolders = emptySet()) }
    }
    
    /**
     * 搜索文件
     */
    fun searchFiles(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }
        
        val results = _uiState.value.fileTree.filter { node ->
            node.path.contains(query, ignoreCase = true) ||
            node.fileName.contains(query, ignoreCase = true)
        }
        
        _uiState.update { it.copy(searchResults = results) }
    }
    
    /**
     * 清除搜索
     */
    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "", searchResults = emptyList()) }
    }
    
    /**
     * 获取文件图标
     */
    fun getFileIcon(node: GitTreeNode): String {
        return when {
            node.isDirectory -> "📁"
            node.extension == "kt" -> "🟣"
            node.extension == "java" -> "☕"
            node.extension == "xml" -> "📄"
            node.extension == "json" -> "📋"
            node.extension == "md" -> "📝"
            node.extension == "txt" -> "📃"
            node.extension == "py" -> "🐍"
            node.extension == "js" -> "💛"
            node.extension == "ts" -> "💙"
            node.extension == "html" -> "🌐"
            node.extension == "css" -> "🎨"
            node.extension == "gradle" -> "🐘"
            node.extension == "properties" -> "⚙️"
            node.extension == "yml" || node.extension == "yaml" -> "📋"
            node.extension == "sh" -> "💻"
            else -> "📄"
        }
    }
    
    /**
     * 获取文件树的层级结构
     */
    fun getFileTreeHierarchy(): List<FileTreeNode> {
        val tree = _uiState.value.fileTree
        val expanded = _uiState.value.expandedFolders
        
        return buildFileTree(tree, "", expanded)
    }
    
    /**
     * 递归构建文件树
     */
    private fun buildFileTree(
        tree: List<GitTreeNode>,
        parentPath: String,
        expanded: Set<String>
    ): List<FileTreeNode> {
        val nodes = tree.filter { node ->
            val nodeParent = node.parentPath ?: ""
            nodeParent == parentPath
        }.sortedWith(compareByDescending<GitTreeNode> { it.isDirectory }.thenBy { it.fileName })
        
        return nodes.map { node ->
            FileTreeNode(
                node = node,
                isExpanded = node.isDirectory && node.path in expanded,
                children = if (node.isDirectory && node.path in expanded) {
                    buildFileTree(tree, node.path, expanded)
                } else {
                    emptyList()
                },
                level = node.path.count { it == '/' }
            )
        }
    }
    
    /**
     * 获取已修改文件的内容映射（用于提交）
     */
    fun getModifiedFilesContent(): Map<String, String> {
        return _uiState.value.virtualFiles
            .filter { (_, file) -> file.isModified }
            .mapValues { (_, file) -> file.content ?: "" }
    }
    
    /**
     * 重置所有修改状态
     */
    fun resetAllModifications() {
        _uiState.update { currentState ->
            val updatedFiles = currentState.virtualFiles.mapValues { (_, file) ->
                file.resetModification()
            }
            currentState.copy(virtualFiles = updatedFiles)
        }
    }
    
    /**
     * 清除所有虚拟文件
     */
    fun clearVirtualFiles() {
        _uiState.update { 
            it.copy(
                virtualFiles = emptyMap(),
                selectedFile = null,
                selectedFilePath = null,
                isEditing = false,
                editContent = ""
            )
        }
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * 文件树节点（UI 用）
 */
data class FileTreeNode(
    val node: GitTreeNode,
    val isExpanded: Boolean,
    val children: List<FileTreeNode>,
    val level: Int
) {
    val isDirectory: Boolean get() = node.isDirectory
    val path: String get() = node.path
    val fileName: String get() = node.fileName
}