package com.mobilecodex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilecodex.model.*
import com.mobilecodex.data.repository.GitHubApiRepository
import com.mobilecodex.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class FileUiState(
    val openedFiles: List<VirtualFile> = emptyList(),
    val activeFile: VirtualFile? = null,
    val currentPath: String = "",
    val directoryContents: List<GitHubContentItem> = emptyList(),
    val isLoadingFile: Boolean = false,
    val isLoadingDirectory: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val commitMessage: String = "",
    val error: String? = null
)

@HiltViewModel
class FileViewModel @Inject constructor(
    private val apiRepo: GitHubApiRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FileUiState())
    val uiState: StateFlow<FileUiState> = _uiState.asStateFlow()

    private var cachedGitHubSettings: GitHubSettings = GitHubSettings()

    init {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { appSettings ->
                cachedGitHubSettings = appSettings.gitHubSettings
            }
        }
    }

    fun openFile(
        owner: String, repo: String, path: String, name: String,
        downloadUrl: String? = null, sha: String? = null
    ) {
        val settings = cachedGitHubSettings
        if (!settings.isConfigured) return

        val existingFile = _uiState.value.openedFiles.find { it.path == path && it.name == name }
        if (existingFile != null) {
            _uiState.update { it.copy(activeFile = existingFile) }
            return
        }

        _uiState.update { it.copy(isLoadingFile = true, error = null) }

        viewModelScope.launch {
            val contentResult = if (downloadUrl != null) {
                apiRepo.getRawFileContent(settings, downloadUrl)
            } else {
                apiRepo.getFileContent(settings, owner, repo, path)
            }

            contentResult.fold(
                onSuccess = { result ->
                    val contentStr = when (result) {
                        is String -> result
                        is FileContent -> result.content
                        else -> ""
                    }
                    val virtualFile = VirtualFile(
                        id = UUID.randomUUID().toString(), name = name, path = path,
                        content = contentStr, originalContent = contentStr, sha = sha, isModified = false
                    )
                    _uiState.update { state ->
                        state.copy(
                            openedFiles = state.openedFiles + virtualFile,
                            activeFile = virtualFile, isLoadingFile = false
                        )
                    }
                },
                onFailure = { error ->
                    val virtualFile = VirtualFile(
                        id = UUID.randomUUID().toString(), name = name, path = path,
                        content = "// 无法加载文件内容\n// ${error.message}", sha = sha
                    )
                    _uiState.update { state ->
                        state.copy(
                            openedFiles = state.openedFiles + virtualFile,
                            activeFile = virtualFile, isLoadingFile = false, error = error.message
                        )
                    }
                }
            )
        }
    }

    fun closeFile(fileId: String) {
        _uiState.update { state ->
            val updatedFiles = state.openedFiles.filter { it.id != fileId }
            state.copy(
                openedFiles = updatedFiles,
                activeFile = if (state.activeFile?.id == fileId) updatedFiles.lastOrNull() else state.activeFile
            )
        }
    }

    fun selectFile(fileId: String) {
        _uiState.update { state ->
            state.copy(activeFile = state.openedFiles.find { it.id == fileId })
        }
    }

    fun updateFileContent(fileId: String, newContent: String) {
        _uiState.update { state ->
            val updatedFiles = state.openedFiles.map { file ->
                if (file.id == fileId)
                    file.copy(content = newContent, isModified = newContent != file.originalContent)
                else file
            }
            val updatedActive = state.activeFile?.let {
                if (it.id == fileId) updatedFiles.find { f -> f.id == fileId } ?: it else it
            }
            state.copy(openedFiles = updatedFiles, activeFile = updatedActive)
        }
    }

    fun loadDirectoryContents(owner: String, repo: String, path: String) {
        val settings = cachedGitHubSettings
        if (!settings.isConfigured) return
        _uiState.update { it.copy(isLoadingDirectory = true, currentPath = path) }

        viewModelScope.launch {
            apiRepo.getDirectoryContents(settings, owner, repo, path)
                .fold(
                    onSuccess = { contents ->
                        _uiState.update { it.copy(directoryContents = contents, isLoadingDirectory = false) }
                    },
                    onFailure = { error ->
                        _uiState.update { it.copy(isLoadingDirectory = false, error = error.message) }
                    }
                )
        }
    }

    fun navigateUp() {
        val currentPath = _uiState.value.currentPath
        val parentPath = currentPath.substringBeforeLast("/", "")
        _uiState.update {
            if (parentPath.isEmpty() && currentPath.isNotEmpty())
                it.copy(currentPath = "", directoryContents = emptyList())
            else if (parentPath != currentPath)
                it.copy(currentPath = parentPath)
            else it
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
    fun updateCommitMessage(message: String) { _uiState.update { it.copy(commitMessage = message) } }

    fun commitActiveFile(owner: String, repo: String, branch: String? = null) {
        val file = _uiState.value.activeFile ?: return
        val message = _uiState.value.commitMessage.ifBlank { "Update ${file.name}" }
        val settings = cachedGitHubSettings
        if (!settings.isConfigured) {
            _uiState.update { it.copy(error = "请先配置 GitHub Token") }
            return
        }

        _uiState.update { it.copy(isSaving = true, saveSuccess = false, error = null) }

        viewModelScope.launch {
            apiRepo.createOrUpdateFile(
                settings = settings, owner = owner, repo = repo, path = file.path,
                content = file.content.orEmpty(), commitMessage = message, sha = file.sha, branch = branch
            ).fold(
                onSuccess = { newSha ->
                    _uiState.update { state ->
                        val updatedFiles = state.openedFiles.map { f ->
                            if (f.id == file.id) f.copy(sha = newSha, originalContent = f.content, isModified = false)
                            else f
                        }
                        state.copy(
                            openedFiles = updatedFiles,
                            activeFile = state.activeFile?.let {
                                if (it.id == file.id) it.copy(sha = newSha, originalContent = it.content, isModified = false)
                                else it
                            },
                            isSaving = false, saveSuccess = true, commitMessage = ""
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSaving = false, error = "提交失败: ${error.message}") }
                }
            )
        }
    }

    fun clearSaveSuccess() { _uiState.update { it.copy(saveSuccess = false) } }
}
