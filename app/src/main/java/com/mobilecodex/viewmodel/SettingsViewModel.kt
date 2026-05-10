package com.mobilecodex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilecodex.model.*
import com.mobilecodex.data.repository.ChatRepository
import com.mobilecodex.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val aiSettings: AISettings = AISettings(),
    val gitHubSettings: GitHubSettings = GitHubSettings(),
    val editorSettings: EditorSettings = EditorSettings(),
    val isDarkTheme: Boolean = true,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    // 表单状态
    val aiApiKeyInput: String = "",
    val aiBaseUrlInput: String = "",
    val aiModelInput: String = "",
    val aiMaxTokensInput: String = "",
    val aiTemperatureInput: String = "",
    val aiSystemPromptInput: String = "",
    val ghTokenInput: String = "",
    val editorFontSizeInput: String = "",
    val editorTabSizeInput: String = "",
    // 模型列表
    val availableModels: List<String> = emptyList(),
    val isLoadingModels: Boolean = false,
    val showModelDropdown: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _uiState.update {
                    it.copy(
                        aiSettings = settings.aiSettings,
                        gitHubSettings = settings.gitHubSettings,
                        editorSettings = settings.editorSettings,
                        isDarkTheme = settings.isDarkTheme,
                        aiApiKeyInput = settings.aiSettings.apiKey,
                        aiBaseUrlInput = settings.aiSettings.baseUrl,
                        aiModelInput = settings.aiSettings.modelId,
                        aiMaxTokensInput = settings.aiSettings.maxTokens.toString(),
                        aiTemperatureInput = settings.aiSettings.temperature.toString(),
                        aiSystemPromptInput = settings.aiSettings.systemPrompt,
                        ghTokenInput = settings.gitHubSettings.accessToken,
                        editorFontSizeInput = settings.editorSettings.fontSize.toString(),
                        editorTabSizeInput = settings.editorSettings.tabSize.toString()
                    )
                }
            }
        }
    }

    // --- 获取当前设置（供其他 ViewModel 使用）---

    fun getCurrentAISettings(): AISettings = _uiState.value.aiSettings
    fun getCurrentGitHubSettings(): GitHubSettings = _uiState.value.gitHubSettings
    fun getCurrentEditorSettings(): EditorSettings = _uiState.value.editorSettings

    // --- 输入更新 ---

    fun updateAiApiKey(value: String) {
        _uiState.update { it.copy(aiApiKeyInput = value) }
    }

    fun updateAiBaseUrl(value: String) {
        _uiState.update { it.copy(aiBaseUrlInput = value) }
    }

    fun updateAiModel(value: String) {
        _uiState.update { it.copy(aiModelInput = value) }
    }

    fun updateAiMaxTokens(value: String) {
        _uiState.update { it.copy(aiMaxTokensInput = value) }
    }

    fun updateAiTemperature(value: String) {
        _uiState.update { it.copy(aiTemperatureInput = value) }
    }

    fun updateAiSystemPrompt(value: String) {
        _uiState.update { it.copy(aiSystemPromptInput = value) }
    }

    fun updateGhToken(value: String) {
        _uiState.update { it.copy(ghTokenInput = value) }
    }

    fun updateEditorFontSize(value: String) {
        _uiState.update { it.copy(editorFontSizeInput = value) }
    }

    fun updateEditorTabSize(value: String) {
        _uiState.update { it.copy(editorTabSizeInput = value) }
    }

    // --- 保存设置 ---

    fun saveAISettings() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true, saveSuccess = false, error = null) }
                val settings = AISettings(
                    apiKey = _uiState.value.aiApiKeyInput,
                    baseUrl = _uiState.value.aiBaseUrlInput.ifBlank { "https://api.openai.com/v1" },
                    modelId = _uiState.value.aiModelInput.ifBlank { "gpt-4-turbo" },
                    maxTokens = _uiState.value.aiMaxTokensInput.toIntOrNull() ?: 4096,
                    temperature = _uiState.value.aiTemperatureInput.toFloatOrNull() ?: 0.7f,
                    systemPrompt = _uiState.value.aiSystemPromptInput
                )
                settingsRepository.updateAISettings(settings)
                _uiState.update {
                    it.copy(
                        aiSettings = settings,
                        isSaving = false,
                        saveSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = "保存失败: ${e.message}")
                }
            }
        }
    }

    fun saveGitHubSettings() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true, saveSuccess = false, error = null) }
                val settings = GitHubSettings(
                    accessToken = _uiState.value.ghTokenInput
                )
                settingsRepository.updateGitHubSettings(settings)
                // 获取用户信息更新 username
                _uiState.update {
                    it.copy(
                        gitHubSettings = settings,
                        isSaving = false,
                        saveSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = "保存失败: ${e.message}")
                }
            }
        }
    }

    fun saveEditorSettings() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSaving = true, saveSuccess = false, error = null) }
                val settings = EditorSettings(
                    fontSize = _uiState.value.editorFontSizeInput.toIntOrNull() ?: 14,
                    tabSize = _uiState.value.editorTabSizeInput.toIntOrNull() ?: 4
                )
                settingsRepository.updateEditorSettings(settings)
                _uiState.update {
                    it.copy(
                        editorSettings = settings,
                        isSaving = false,
                        saveSuccess = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = "保存失败: ${e.message}")
                }
            }
        }
    }

    fun toggleDarkTheme() {
        viewModelScope.launch {
            val newValue = !_uiState.value.isDarkTheme
            settingsRepository.setDarkTheme(newValue)
            _uiState.update { it.copy(isDarkTheme = newValue) }
        }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    // --- 模型列表 ---

    fun fetchModels() {
        val apiKey = _uiState.value.aiApiKeyInput.trim()
        val baseUrl = _uiState.value.aiBaseUrlInput.trim().ifBlank { "https://api.openai.com/v1" }

        if (apiKey.isBlank()) {
            _uiState.update { it.copy(error = "请先输入 API Key") }
            return
        }

        _uiState.update { it.copy(isLoadingModels = true, error = null) }

        viewModelScope.launch {
            val settings = AISettings(
                apiKey = apiKey,
                baseUrl = baseUrl
            )
            chatRepository.fetchModels(settings)
                .fold(
                    onSuccess = { models ->
                        _uiState.update {
                            it.copy(
                                availableModels = models,
                                isLoadingModels = false,
                                showModelDropdown = models.isNotEmpty()
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoadingModels = false,
                                error = "获取模型列表失败: ${error.message}"
                            )
                        }
                    }
                )
        }
    }

    fun selectModel(modelId: String) {
        _uiState.update {
            it.copy(
                aiModelInput = modelId,
                showModelDropdown = false
            )
        }
    }

    fun toggleModelDropdown() {
        _uiState.update { it.copy(showModelDropdown = !it.showModelDropdown) }
    }

    fun dismissModelDropdown() {
        _uiState.update { it.copy(showModelDropdown = false) }
    }
}
