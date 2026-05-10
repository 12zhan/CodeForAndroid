package com.mobilecodex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilecodex.model.*
import com.mobilecodex.data.repository.GitHubRepository
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
    private val gitHubRepository: GitHubRepository,
    private val settingsViewModel: SettingsViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(FileUiState())
    val uiState: StateFlow<FileUiState> = _uiState.asStateFlow()

    fun openFile(
        owner: String,
        repo: String,
        path: String,
        name: String,
        downloadUrl: String? = null,
        sha: String? = null
    ) {
        val settings = settingsViewModel.getCurrentGitHubSettings()
        if (!settings.isConfigured) return

        // 检查是否已经打开
        val existingFile = _uiState.value.openedFiles.find {
            it.path == path && it.name == name
        }
        if (existingFile != null) {
            _uiState.update { it.copy(activeFile = existingFile) }
            return
        }

        _uiState.update { it.copy(isLoadingFile = true, error = null) }

        viewModelScope.launch {
            // 尝试从 download_url 获取原始内容
            val contentResult = if (downloadUrl != null) {
                gitHubRepository.getRawFileContent(settings, downloadUrl)
            } else {
                gitHubRepository.getFileContent(settings, owner, repo, path)
            }

            contentResult.fold(
                onSuccess = { content ->
                    val contentStr = if (content is FileContent) content.content else content as String
                    val virtualFile = VirtualFile(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        path = path,
                        content = contentStr,
                        originalContent = contentStr,
                        sha = sha,
                        isModified = false
                    )
                    _uiState.update { state ->
                        state.copy(
                            openedFiles = state.openedFiles + virtualFile,
                            activeFile = virtualFile,
                            isLoadingFile = false
                        )
                    }
                },
                onFailure = { error ->
                    // 创建空文件占位
                    val virtualFile = VirtualFile(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        path = path,
                        content = "// 无法加载文件内容\n// ${error.message}",
                        sha = sha
                    )
                    _uiState.update { state ->
                        state.copy(
                            openedFiles = state.openedFiles + virtualFile,
                            activeFile = virtualFile,
                            isLoadingFile = false,
                            error = error.message
                        )
                    }
                }
            )
        }
    }

    fun closeFile(fileId: String) {
        _uiState.update { state ->
            val updatedFiles = state.openedFiles.filter { it.id != fileId }
            val newActive = if (state.activeFile?.id == fileId) {
                updatedFiles.lastOrNull()
            } else {
                state.activeFile
            }
            state.copy(openedFiles = updatedFiles, activeFile = newActive)
        }
    }

    fun selectFile(fileId: String) {
        _uiState.update { state ->
            val file = state.openedFiles.find { it.id == fileId }
            state.copy(activeFile = file)
        }
    }

    fun updateFileContent(fileId: String, newContent: String) {
        _uiState.update { state ->
            val updatedFiles = state.openedFiles.map { file ->
                if (file.id == fileId) {
                    file.copy(
                        content = newContent,
                        isModified = newContent != file.originalContent
                    )
                } else file
            }
            val updatedActive = state.activeFile?.let {
                if (it.id == fileId) updatedFiles.find { f -> f.id == fileId } ?: it
                else it
            }
            state.copy(openedFiles = updatedFiles, activeFile = updatedActive)
        }
    }

    fun loadDirectoryContents(
        owner: String,
        repo: String,
        path: String
    ) {
        val settings = settingsViewModel.getCurrentGitHubSettings()
        if (!settings.isConfigured) return

        _uiState.update { it.copy(isLoadingDirectory = true, currentPath = path) }

        viewModelScope.launch {
            gitHubRepository.getDirectoryContents(settings, owner, repo, path)
                .fold(
                    onSuccess = { contents ->
                        _uiState.update {
                            it.copy(
                                directoryContents = contents,
                                isLoadingDirectory = false
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoadingDirectory = false,
                                error = error.message
                            )
                        }
                    }
                )
        }
    }

    fun navigateUp() {
        val currentPath = _uiState.value.currentPath
        val parentPath = currentPath.substringBeforeLast("/", "")
        if (parentPath.isEmpty() && currentPath.isNotEmpty()) {
            _uiState.update { it.copy(currentPath = "", directoryContents = emptyList()) }
        } else if (parentPath != currentPath) {
            val repo = settingsViewModel.getCurrentGitHubSettings()
            // 需要知道 owner/repo，这里从其他地方获取
            // 简化处理
            _uiState.update { it.copy(currentPath = parentPath) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun updateCommitMessage(message: String) {
        _uiState.update { it.copy(commitMessage = message) }
    }

    fun commitActiveFile(
        owner: String,
        repo: String,
        branch: String? = null
    ) {
        val file = _uiState.value.activeFile ?: return
        val message = _uiState.value.commitMessage.ifBlank {
            "Update ${file.name}"
        }

        val settings = settingsViewModel.getCurrentGitHubSettings()
        if (!settings.isConfigured) {
            _uiState.update { it.copy(error = "请先配置 GitHub Token") }
            return
        }

        _uiState.update { it.copy(isSaving = true, saveSuccess = false, error = null) }

        viewModelScope.launch {
            gitHubRepository.createOrUpdateFile(
                settings = settings,
                owner = owner,
                repo = repo,
                path = file.path,
                content = file.content ?: "",
                commitMessage = message,
                sha = file.sha,
                branch = branch
            ).fold(
                onSuccess = { newSha ->
                    // 更新文件的 sha 和 originalContent
                    _uiState.update { state ->
                        val updatedFiles = state.openedFiles.map { f ->
                            if (f.id == file.id) {
                                f.copy(
                                    sha = newSha,
                                    originalContent = f.content,
                                    isModified = false
                                )
                            } else f
                        }
                        val updatedActive = state.activeFile?.let {
                            if (it.id == file.id) {
                                it.copy(
                                    sha = newSha,
                                    originalContent = it.content,
                                    isModified = false
                                )
                            } else it
                        }
                        state.copy(
                            openedFiles = updatedFiles,
                            activeFile = updatedActive,
                            isSaving = false,
                            saveSuccess = true,
                            commitMessage = ""
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = "提交失败: ${error.message}"
                        )
                    }
                }
            )
        }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}
