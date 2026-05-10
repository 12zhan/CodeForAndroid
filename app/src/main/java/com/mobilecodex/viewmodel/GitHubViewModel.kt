package com.mobilecodex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilecodex.model.*
import com.mobilecodex.data.repository.GitHubApiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GitHubUiState(
    val repositories: List<com.mobilecodex.model.GitHubRepository> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedRepo: com.mobilecodex.model.GitHubRepository? = null,
    val fileTree: List<GitTreeNode> = emptyList(),
    val directoryContents: List<GitHubContentItem> = emptyList(),
    val currentPath: String = "",
    val isLoadingTree: Boolean = false,
    val isLoadingContent: Boolean = false
)

@HiltViewModel
class GitHubViewModel @Inject constructor(
    private val apiRepo: GitHubApiRepository,
    private val settingsViewModel: SettingsViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(GitHubUiState())
    val uiState: StateFlow<GitHubUiState> = _uiState.asStateFlow()

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun loadRepositories() {
        val settings = settingsViewModel.getCurrentGitHubSettings()
        if (!settings.isConfigured) {
            _uiState.update { it.copy(error = "请先在设置中配置 GitHub Token") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            apiRepo.listMyRepos(settings)
                .fold(
                    onSuccess = { repos ->
                        _uiState.update {
                            it.copy(repositories = repos, isLoading = false)
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "获取仓库列表失败"
                            )
                        }
                    }
                )
        }
    }

    fun searchRepositories() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isEmpty()) {
            loadRepositories()
            return
        }

        val settings = settingsViewModel.getCurrentGitHubSettings()
        if (!settings.isConfigured) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            apiRepo.searchRepositories(settings, query)
                .fold(
                    onSuccess = { repos ->
                        _uiState.update { it.copy(repositories = repos, isLoading = false) }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(isLoading = false, error = error.message ?: "搜索失败")
                        }
                    }
                )
        }
    }

    fun selectRepository(repo: com.mobilecodex.model.GitHubRepository) {
        _uiState.update {
            it.copy(
                selectedRepo = repo,
                fileTree = emptyList(),
                currentPath = "",
                directoryContents = emptyList()
            )
        }
        loadFileTree(repo)
    }

    fun loadFileTree(repo: com.mobilecodex.model.GitHubRepository? = _uiState.value.selectedRepo) {
        if (repo == null) return
        val settings = settingsViewModel.getCurrentGitHubSettings()
        if (!settings.isConfigured) return

        _uiState.update { it.copy(isLoadingTree = true) }

        viewModelScope.launch {
            val parts = repo.fullName.split("/")
            if (parts.size != 2) {
                _uiState.update {
                    it.copy(isLoadingTree = false, error = "无效的仓库路径")
                }
                return@launch
            }

            apiRepo.getFileTree(settings, parts[0], parts[1], repo.defaultBranch)
                .fold(
                    onSuccess = { tree ->
                        _uiState.update {
                            it.copy(fileTree = tree, isLoadingTree = false)
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(isLoadingTree = false, error = error.message)
                        }
                    }
                )
        }
    }

    fun navigateToDirectory(path: String) {
        val repo = _uiState.value.selectedRepo ?: return
        val settings = settingsViewModel.getCurrentGitHubSettings()
        if (!settings.isConfigured) return

        _uiState.update { it.copy(currentPath = path, isLoadingContent = true) }

        viewModelScope.launch {
            val parts = repo.fullName.split("/")
            if (parts.size != 2) return@launch

            apiRepo.getDirectoryContents(settings, parts[0], parts[1], path)
                .fold(
                    onSuccess = { contents ->
                        _uiState.update {
                            it.copy(
                                directoryContents = contents,
                                isLoadingContent = false
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(isLoadingContent = false, error = error.message)
                        }
                    }
                )
        }
    }

    fun goBackToRepositories() {
        _uiState.update {
            it.copy(
                selectedRepo = null,
                fileTree = emptyList(),
                directoryContents = emptyList(),
                currentPath = ""
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
        loadRepositories()
    }
}
