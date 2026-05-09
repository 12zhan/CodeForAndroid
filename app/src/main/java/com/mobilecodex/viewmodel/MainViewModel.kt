package com.mobilecodex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilecodex.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 应用 UI 状态
 * 包含整个应用的全局状态
 */
data class AppUiState(
    val workspaceState: WorkspaceState = WorkspaceState.empty(),
    val workspaceOptions: List<WorkspaceOption> = WorkspaceOption.defaultList(),
    val currentConversation: ChatConversation = ChatConversation.create(),
    val conversations: List<ChatConversation> = emptyList(),
    val aiSettings: AISettings = AISettings.default(),
    val githubSettings: GitHubSettings = GitHubSettings.default(),
    val appSettings: AppSettings = AppSettings.default(),
    val githubUser: GitHubUser? = null,
    val repositories: List<GitHubRepository> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val snackbarMessage: String? = null
)

/**
 * 应用事件密封类
 * 用于处理 UI 事件
 */
sealed class AppEvent {
    data class ShowSnackbar(val message: String) : AppEvent()
    data class ShowError(val error: String) : AppEvent()
    object NavigateToSettings : AppEvent()
    object NavigateToChat : AppEvent()
    object NavigateToFiles : AppEvent()
}

/**
 * 主 ViewModel
 * 管理应用的全局状态和跨模块协调
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    // 注入的仓库将在这里添加
    // private val settingsRepository: SettingsRepository,
    // private val githubRepository: GitHubRepository,
    // private val chatRepository: ChatRepository,
    // private val fileRepository: FileRepository
) : ViewModel() {
    
    // 应用 UI 状态
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()
    
    // 一次性事件
    private val _events = MutableSharedFlow<AppEvent>()
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()
    
    // 当前工作区状态
    val workspaceState: StateFlow<WorkspaceState> = _uiState
        .map { it.workspaceState }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkspaceState.empty())
    
    // 工作区选项列表
    val workspaceOptions: StateFlow<List<WorkspaceOption>> = _uiState
        .map { it.workspaceOptions }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WorkspaceOption.defaultList())
    
    // 当前对话
    val currentConversation: StateFlow<ChatConversation> = _uiState
        .map { it.currentConversation }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChatConversation.create())
    
    // AI 设置
    val aiSettings: StateFlow<AISettings> = _uiState
        .map { it.aiSettings }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AISettings.default())
    
    // GitHub 设置
    val githubSettings: StateFlow<GitHubSettings> = _uiState
        .map { it.githubSettings }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GitHubSettings.default())
    
    // 应用设置
    val appSettings: StateFlow<AppSettings> = _uiState
        .map { it.appSettings }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings.default())
    
    // 仓库列表
    val repositories: StateFlow<List<GitHubRepository>> = _uiState
        .map { it.repositories }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 加载状态
    val isLoading: StateFlow<Boolean> = _uiState
        .map { it.isLoading }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    // 错误状态
    val error: StateFlow<String?> = _uiState
        .map { it.error }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    // 是否有未保存的更改
    val hasUnsavedChanges: StateFlow<Boolean> = _uiState
        .map { it.workspaceState.hasUnsavedChanges }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    // 已修改文件数量
    val modifiedFileCount: StateFlow<Int> = _uiState
        .map { it.workspaceState.modifiedCount }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    
    init {
        // 初始化加载设置
        loadSettings()
    }
    
    /**
     * 加载所有设置
     */
    private fun loadSettings() {
        viewModelScope.launch {
            // TODO: 从 DataStore 加载设置
            // settingsRepository.aiSettings.collect { settings ->
            //     _uiState.update { it.copy(aiSettings = settings) }
            // }
            // settingsRepository.githubSettings.collect { settings ->
            //     _uiState.update { it.copy(githubSettings = settings) }
            // }
            // settingsRepository.appSettings.collect { settings ->
            //     _uiState.update { it.copy(appSettings = settings) }
            // }
        }
    }
    
    /**
     * 切换工作区
     */
    fun switchWorkspace(workspace: WorkspaceOption) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    workspaceState = currentState.workspaceState.withWorkspace(workspace)
                )
            }
            
            // 如果是 GitHub 工作区，加载仓库文件树
            if (workspace.isGitHub) {
                loadRepositoryTree(workspace)
            }
            
            _events.emit(AppEvent.ShowSnackbar("已切换到 ${workspace.shortDisplayName}"))
        }
    }
    
    /**
     * 加载仓库文件树
     */
    private fun loadRepositoryTree(workspace: WorkspaceOption) {
        val owner = workspace.ownerName ?: return
        val repo = workspace.repoName ?: return
        val branch = workspace.effectiveBranch
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // TODO: 调用 GitHub API 获取文件树
                // val tree = githubRepository.getTree(owner, repo, branch)
                // _uiState.update { currentState ->
                //     currentState.copy(
                //         workspaceState = currentState.workspaceState.withFileTree(tree),
                //         isLoading = false
                //     )
                // }
                
                _events.emit(AppEvent.ShowSnackbar("文件树加载完成"))
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _events.emit(AppEvent.ShowError("加载文件树失败: ${e.message}"))
            }
        }
    }
    
    /**
     * 获取 GitHub 仓库列表
     */
    fun fetchRepositories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // TODO: 调用 GitHub API 获取仓库列表
                // val repos = githubRepository.getUserRepositories()
                // val options = repos.map { WorkspaceOption.fromRepository(it) }
                // _uiState.update { currentState ->
                //     currentState.copy(
                //         repositories = repos,
                //         workspaceOptions = WorkspaceOption.defaultList() + options,
                //         isLoading = false
                //     )
                // }
                
                _events.emit(AppEvent.ShowSnackbar("仓库列表加载完成"))
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _events.emit(AppEvent.ShowError("获取仓库列表失败: ${e.message}"))
            }
        }
    }
    
    /**
     * 获取 GitHub 用户信息
     */
    fun fetchGitHubUser() {
        viewModelScope.launch {
            try {
                // TODO: 调用 GitHub API 获取用户信息
                // val user = githubRepository.getCurrentUser()
                // _uiState.update { it.copy(githubUser = user) }
            } catch (e: Exception) {
                _events.emit(AppEvent.ShowError("获取用户信息失败: ${e.message}"))
            }
        }
    }
    
    /**
     * 更新 AI 设置
     */
    fun updateAISettings(settings: AISettings) {
        viewModelScope.launch {
            _uiState.update { it.copy(aiSettings = settings) }
            // TODO: 保存到 DataStore
            // settingsRepository.saveAISettings(settings)
            _events.emit(AppEvent.ShowSnackbar("AI 设置已更新"))
        }
    }
    
    /**
     * 更新 GitHub 设置
     */
    fun updateGitHubSettings(settings: GitHubSettings) {
        viewModelScope.launch {
            _uiState.update { it.copy(githubSettings = settings) }
            // TODO: 保存到 DataStore
            // settingsRepository.saveGitHubSettings(settings)
            
            // 如果 Token 已配置，获取用户信息和仓库列表
            if (settings.isConfigured) {
                fetchGitHubUser()
                fetchRepositories()
            }
            
            _events.emit(AppEvent.ShowSnackbar("GitHub 设置已更新"))
        }
    }
    
    /**
     * 更新应用设置
     */
    fun updateAppSettings(settings: AppSettings) {
        viewModelScope.launch {
            _uiState.update { it.copy(appSettings = settings) }
            // TODO: 保存到 DataStore
            // settingsRepository.saveAppSettings(settings)
            _events.emit(AppEvent.ShowSnackbar("应用设置已更新"))
        }
    }
    
    /**
     * 创建新对话
     */
    fun createNewConversation() {
        viewModelScope.launch {
            val newConversation = ChatConversation.create()
            _uiState.update { currentState ->
                currentState.copy(
                    currentConversation = newConversation,
                    conversations = currentState.conversations + currentState.currentConversation
                )
            }
            _events.emit(AppEvent.ShowSnackbar("已创建新对话"))
        }
    }
    
    /**
     * 切换到指定对话
     */
    fun switchConversation(conversationId: String) {
        viewModelScope.launch {
            val conversation = _uiState.value.conversations.find { it.id == conversationId }
            if (conversation != null) {
                _uiState.update { it.copy(currentConversation = conversation) }
            }
        }
    }
    
    /**
     * 清除当前对话
     */
    fun clearCurrentConversation() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    currentConversation = currentState.currentConversation.clearMessages()
                )
            }
            _events.emit(AppEvent.ShowSnackbar("对话已清空"))
        }
    }
    
    /**
     * 清除所有对话历史
     */
    fun clearAllConversations() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    conversations = emptyList(),
                    currentConversation = ChatConversation.create()
                )
            }
            _events.emit(AppEvent.ShowSnackbar("所有对话历史已清除"))
        }
    }
    
    /**
     * 加载文件内容（懒加载）
     */
    fun loadFileContent(path: String) {
        val workspace = _uiState.value.workspaceState.currentWorkspace ?: return
        
        if (workspace.isLocal) {
            // 本地工作区，不需要从 GitHub 加载
            return
        }
        
        val owner = workspace.ownerName ?: return
        val repo = workspace.repoName ?: return
        
        viewModelScope.launch {
            try {
                // TODO: 调用 GitHub API 获取文件内容
                // val content = githubRepository.getFileContent(owner, repo, path)
                // val virtualFile = VirtualFile(
                //     path = path,
                //     content = content.decodedContent(),
                //     isLoaded = true,
                //     sha = content.sha,
                //     size = content.size
                // )
                // _uiState.update { currentState ->
                //     currentState.copy(
                //         workspaceState = currentState.workspaceState.withFile(virtualFile)
                //     )
                // }
            } catch (e: Exception) {
                _events.emit(AppEvent.ShowError("加载文件内容失败: ${e.message}"))
            }
        }
    }
    
    /**
     * 保存文件内容到 VFS
     */
    fun saveFileContent(path: String, content: String) {
        viewModelScope.launch {
            val currentState = _uiState.value.workspaceState
            val existingFile = currentState.getFile(path)
            
            val updatedFile = if (existingFile != null) {
                existingFile.withModification(content)
            } else {
                VirtualFile(
                    path = path,
                    content = content,
                    isModified = true,
                    isLoaded = true
                )
            }
            
            _uiState.update { state ->
                state.copy(
                    workspaceState = state.workspaceState.withFile(updatedFile)
                )
            }
            
            _events.emit(AppEvent.ShowSnackbar("文件已保存到本地"))
        }
    }
    
    /**
     * 提交更改到 GitHub
     */
    fun commitChanges(commitMessage: String) {
        val workspace = _uiState.value.workspaceState.currentWorkspace
        if (workspace == null || !workspace.isGitHub) {
            viewModelScope.launch {
                _events.emit(AppEvent.ShowError("当前工作区不是 GitHub 仓库"))
            }
            return
        }
        
        val modifiedFiles = _uiState.value.workspaceState.modifiedFiles
        if (modifiedFiles.isEmpty()) {
            viewModelScope.launch {
                _events.emit(AppEvent.ShowSnackbar("没有需要提交的更改"))
            }
            return
        }
        
        val owner = workspace.ownerName ?: return
        val repo = workspace.repoName ?: return
        val branch = workspace.effectiveBranch
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // TODO: 实现 Git 提交流程
                // 1. 为每个修改的文件创建 Blob
                // 2. 创建新的 Tree
                // 3. 创建 Commit
                // 4. 更新 Ref
                
                // val fileChanges = modifiedFiles.map { file ->
                //     val blobSha = githubRepository.createBlob(owner, repo, file.content!!)
                //     FileChange(path = file.path, sha = blobSha, mode = "100644")
                // }
                // 
                // val treeSha = githubRepository.createTree(owner, repo, branch, fileChanges)
                // val commitSha = githubRepository.createCommit(
                //     owner, repo, commitMessage, treeSha, parentCommitSha
                // )
                // githubRepository.updateRef(owner, repo, "heads/$branch", commitSha)
                
                // 重置修改状态
                _uiState.update { currentState ->
                    val updatedFiles = currentState.workspaceState.virtualFiles.mapValues { (_, file) ->
                        file.resetModification()
                    }
                    currentState.copy(
                        workspaceState = currentState.workspaceState.copy(
                            virtualFiles = updatedFiles
                        ),
                        isLoading = false
                    )
                }
                
                _events.emit(AppEvent.ShowSnackbar("已成功提交 ${modifiedFiles.size} 个文件的更改"))
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
                _events.emit(AppEvent.ShowError("提交失败: ${e.message}"))
            }
        }
    }
    
    /**
     * 清除错误状态
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * 清除 Snackbar 消息
     */
    fun clearSnackbarMessage() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
    
    /**
     * 显示 Snackbar 消息
     */
    fun showSnackbar(message: String) {
        viewModelScope.launch {
            _events.emit(AppEvent.ShowSnackbar(message))
        }
    }
    
    /**
     * 刷新当前工作区
     */
    fun refreshWorkspace() {
        val workspace = _uiState.value.workspaceState.currentWorkspace ?: return
        
        if (workspace.isGitHub) {
            loadRepositoryTree(workspace)
        }
    }
    
    /**
     * 获取文件树（扁平化）
     */
    fun getFlatFileTree(): List<GitTreeNode> {
        return _uiState.value.workspaceState.fileTree
    }
    
    /**
     * 获取文件树（树形结构）
     */
    fun getFileTreeStructure(): Map<String, List<GitTreeNode>> {
        val tree = _uiState.value.workspaceState.fileTree
        return tree.groupBy { it.parentPath ?: "" }
    }
}