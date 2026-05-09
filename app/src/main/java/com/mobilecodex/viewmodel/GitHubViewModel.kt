package com.mobilecodex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilecodex.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * GitHub UI 状态
 */
data class GitHubUiState(
    val repositories: List<GitHubRepository> = emptyList(),
    val currentUser: GitHubUser? = null,
    val selectedRepository: GitHubRepository? = null,
    val fileTree: List<GitTreeNode> = emptyList(),
    val isLoadingRepos: Boolean = false,
    val isLoadingTree: Boolean = false,
    val isLoadingUser: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = true
)

/**
 * GitHub 事件
 */
sealed class GitHubEvent {
    data class ShowError(val error: String) : GitHubEvent()
    data class ShowMessage(val message: String) : GitHubEvent()
    data class RepositorySelected(val repository: GitHubRepository) : GitHubEvent()
    object ReposLoaded : GitHubEvent()
    object TreeLoaded : GitHubEvent()
}

/**
 * GitHub ViewModel
 * 管理 GitHub 相关的所有操作
 */
@HiltViewModel
class GitHubViewModel @Inject constructor(
    // 注入的仓库将在这里添加
    // private val githubRepository: GitHubRepository
) : ViewModel() {
    
    // GitHub UI 状态
    private val _uiState = MutableStateFlow(GitHubUiState())
    val uiState: StateFlow<GitHubUiState> = _uiState.asStateFlow()
    
    // 一次性事件
    private val _events = MutableSharedFlow<GitHubEvent>()
    val events: SharedFlow<GitHubEvent> = _events.asSharedFlow()
    
    // 仓库列表
    val repositories: StateFlow<List<GitHubRepository>> = _uiState
        .map { it.repositories }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 当前用户
    val currentUser: StateFlow<GitHubUser?> = _uiState
        .map { it.currentUser }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    // 文件树
    val fileTree: StateFlow<List<GitTreeNode>> = _uiState
        .map { it.fileTree }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // 加载状态
    val isLoading: StateFlow<Boolean> = _uiState
        .map { it.isLoadingRepos || it.isLoadingTree || it.isLoadingUser }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    // 错误状态
    val error: StateFlow<String?> = _uiState
        .map { it.error }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    /**
     * 获取当前用户信息
     */
    fun fetchCurrentUser(token: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingUser = true, error = null) }
            
            try {
                // TODO: 调用 GitHub API 获取用户信息
                // val user = githubRepository.getCurrentUser(token)
                // _uiState.update { it.copy(currentUser = user, isLoadingUser = false) }
                // _events.emit(GitHubEvent.ShowMessage("用户信息加载成功"))
                
                // 临时模拟
                _uiState.update { it.copy(isLoadingUser = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingUser = false, error = e.message) }
                _events.emit(GitHubEvent.ShowError("获取用户信息失败: ${e.message}"))
            }
        }
    }
    
    /**
     * 获取用户的仓库列表
     */
    fun fetchRepositories(token: String, page: Int = 1, refresh: Boolean = false) {
        if (_uiState.value.isLoadingRepos) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRepos = true, error = null) }
            
            try {
                // TODO: 调用 GitHub API 获取仓库列表
                // val repos = githubRepository.getUserRepositories(token, page)
                // 
                // _uiState.update { currentState ->
                //     val updatedRepos = if (refresh || page == 1) {
                //         repos
                //     } else {
                //         currentState.repositories + repos
                //     }
                //     currentState.copy(
                //         repositories = updatedRepos,
                //         isLoadingRepos = false,
                //         page = page,
                //         hasMore = repos.size >= 30 // 假设每页30个
                //     )
                // }
                // 
                // _events.emit(GitHubEvent.ReposLoaded)
                
                // 临时模拟
                _uiState.update { it.copy(isLoadingRepos = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingRepos = false, error = e.message) }
                _events.emit(GitHubEvent.ShowError("获取仓库列表失败: ${e.message}"))
            }
        }
    }
    
    /**
     * 加载下一页仓库
     */
    fun loadNextPage(token: String) {
        val currentState = _uiState.value
        if (!currentState.hasMore || currentState.isLoadingRepos) return
        
        fetchRepositories(token, page = currentState.page + 1)
    }
    
    /**
     * 刷新仓库列表
     */
    fun refreshRepositories(token: String) {
        fetchRepositories(token, page = 1, refresh = true)
    }
    
    /**
     * 选择仓库
     */
    fun selectRepository(repository: GitHubRepository) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedRepository = repository) }
            _events.emit(GitHubEvent.RepositorySelected(repository))
        }
    }
    
    /**
     * 加载仓库文件树
     */
    fun loadRepositoryTree(
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
                // _events.emit(GitHubEvent.TreeLoaded)
                
                // 临时模拟
                _uiState.update { it.copy(isLoadingTree = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingTree = false, error = e.message) }
                _events.emit(GitHubEvent.ShowError("加载文件树失败: ${e.message}"))
            }
        }
    }
    
    /**
     * 获取文件内容
     */
    suspend fun getFileContent(
        token: String,
        owner: String,
        repo: String,
        path: String
    ): FileContent? {
        return try {
            // TODO: 调用 GitHub API 获取文件内容
            // githubRepository.getFileContent(token, owner, repo, path)
            null
        } catch (e: Exception) {
            _events.emit(GitHubEvent.ShowError("获取文件内容失败: ${e.message}"))
            null
        }
    }
    
    /**
     * 创建 Blob
     */
    suspend fun createBlob(
        token: String,
        owner: String,
        repo: String,
        content: String,
        encoding: String = "utf-8"
    ): String? {
        return try {
            // TODO: 调用 GitHub API 创建 Blob
            // githubRepository.createBlob(token, owner, repo, content, encoding)
            null
        } catch (e: Exception) {
            _events.emit(GitHubEvent.ShowError("创建 Blob 失败: ${e.message}"))
            null
        }
    }
    
    /**
     * 创建 Tree
     */
    suspend fun createTree(
        token: String,
        owner: String,
        repo: String,
        baseTree: String,
        treeItems: List<TreeItem>
    ): String? {
        return try {
            // TODO: 调用 GitHub API 创建 Tree
            // githubRepository.createTree(token, owner, repo, baseTree, treeItems)
            null
        } catch (e: Exception) {
            _events.emit(GitHubEvent.ShowError("创建 Tree 失败: ${e.message}"))
            null
        }
    }
    
    /**
     * 创建 Commit
     */
    suspend fun createCommit(
        token: String,
        owner: String,
        repo: String,
        message: String,
        treeSha: String,
        parentSha: String
    ): String? {
        return try {
            // TODO: 调用 GitHub API 创建 Commit
            // githubRepository.createCommit(token, owner, repo, message, treeSha, parentSha)
            null
        } catch (e: Exception) {
            _events.emit(GitHubEvent.ShowError("创建 Commit 失败: ${e.message}"))
            null
        }
    }
    
    /**
     * 更新 Ref
     */
    suspend fun updateRef(
        token: String,
        owner: String,
        repo: String,
        ref: String,
        sha: String,
        force: Boolean = false
    ): Boolean {
        return try {
            // TODO: 调用 GitHub API 更新 Ref
            // githubRepository.updateRef(token, owner, repo, ref, sha, force)
            true
        } catch (e: Exception) {
            _events.emit(GitHubEvent.ShowError("更新 Ref 失败: ${e.message}"))
            false
        }
    }
    
    /**
     * 提交文件更改
     */
    fun commitChanges(
        token: String,
        owner: String,
        repo: String,
        branch: String,
        files: Map<String, String>, // path -> content
        message: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingTree = true, error = null) }
            
            try {
                // 1. 获取当前分支的最新 commit
                // val latestCommit = githubRepository.getLatestCommit(token, owner, repo, branch)
                // val baseTreeSha = latestCommit.tree.sha
                
                // 2. 为每个修改的文件创建 Blob
                // val treeItems = files.map { (path, content) ->
                //     val blobSha = createBlob(token, owner, repo, content) ?: throw Exception("创建 Blob 失败")
                //     TreeItem(path = path, mode = "100644", type = "blob", sha = blobSha)
                // }
                
                // 3. 创建新的 Tree
                // val treeSha = createTree(token, owner, repo, baseTreeSha, treeItems) 
                //     ?: throw Exception("创建 Tree 失败")
                
                // 4. 创建 Commit
                // val commitSha = createCommit(token, owner, repo, message, treeSha, latestCommit.sha)
                //     ?: throw Exception("创建 Commit 失败")
                
                // 5. 更新 Ref
                // val success = updateRef(token, owner, repo, "heads/$branch", commitSha)
                // if (!success) throw Exception("更新 Ref 失败")
                
                _uiState.update { it.copy(isLoadingTree = false) }
                _events.emit(GitHubEvent.ShowMessage("成功提交 ${files.size} 个文件的更改"))
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingTree = false, error = e.message) }
                _events.emit(GitHubEvent.ShowError("提交失败: ${e.message}"))
            }
        }
    }
    
    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * 清除文件树
     */
    fun clearFileTree() {
        _uiState.update { it.copy(fileTree = emptyList()) }
    }
    
    /**
     * 获取仓库的分支列表
     */
    fun fetchBranches(
        token: String,
        owner: String,
        repo: String
    ) {
        viewModelScope.launch {
            try {
                // TODO: 调用 GitHub API 获取分支列表
                // val branches = githubRepository.getBranches(token, owner, repo)
                // 处理分支列表...
            } catch (e: Exception) {
                _events.emit(GitHubEvent.ShowError("获取分支列表失败: ${e.message}"))
            }
        }
    }
    
    /**
     * 获取仓库的默认分支
     */
    fun getDefaultBranch(repository: GitHubRepository): String {
        return repository.defaultBranch
    }
    
    /**
     * 构建工作区选项
     */
    fun buildWorkspaceOption(repository: GitHubRepository): WorkspaceOption {
        return WorkspaceOption.fromRepository(repository)
    }
}

/**
 * Tree Item 数据类
 * 用于创建 Git Tree
 */
data class TreeItem(
    val path: String,
    val mode: String, // "100644" (file), "100755" (executable), "040000" (subdirectory), "160000" (submodule), "120000" (symlink)
    val type: String, // "blob", "tree", "commit"
    val sha: String?
)