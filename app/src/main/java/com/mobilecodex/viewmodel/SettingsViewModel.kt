package com.mobilecodex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilecodex.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 设置 UI 状态
 */
data class SettingsUiState(
    val aiSettings: AISettings = AISettings.default(),
    val githubSettings: GitHubSettings = GitHubSettings.default(),
    val appSettings: AppSettings = AppSettings.default(),
    val githubUser: GitHubUser? = null,
    val isTestingConnection: Boolean = false,
    val connectionTestResult: ConnectionTestResult? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
    val showClearChatDialog: Boolean = false,
    val showClearWorkspaceDialog: Boolean = false,
    val showClearAllDialog: Boolean = false
)

/**
 * 连接测试结果
 */
data class ConnectionTestResult(
    val isSuccess: Boolean,
    val message: String,
    val details: String? = null
)

/**
 * 设置事件
 */
sealed class SettingsEvent {
    data class ShowError(val error: String) : SettingsEvent()
    data class ShowMessage(val message: String) : SettingsEvent()
    data class AISettingsUpdated(val settings: AISettings) : SettingsEvent()
    data class GitHubSettingsUpdated(val settings: GitHubSettings) : SettingsEvent()
    data class AppSettingsUpdated(val settings: AppSettings) : SettingsEvent()
    object ChatHistoryCleared : SettingsEvent()
    object WorkspaceCleared : SettingsEvent()
    object AllDataCleared : SettingsEvent()
}

/**
 * 设置 ViewModel
 * 管理应用的所有配置项
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    // 注入的仓库将在这里添加
    // private val settingsRepository: SettingsRepository,
    // private val githubRepository: GitHubRepository
) : ViewModel() {
    
    // 设置 UI 状态
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    // 一次性事件
    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()
    
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
    
    // GitHub 用户
    val githubUser: StateFlow<GitHubUser?> = _uiState
        .map { it.githubUser }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    // 连接测试结果
    val connectionTestResult: StateFlow<ConnectionTestResult?> = _uiState
        .map { it.connectionTestResult }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    // 加载状态
    val isLoading: StateFlow<Boolean> = _uiState
        .map { it.isTestingConnection || it.isSaving }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    // 错误状态
    val error: StateFlow<String?> = _uiState
        .map { it.error }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    
    init {
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
    
    // ==================== AI 设置 ====================
    
    /**
     * 更新 AI 提供商
     */
    fun updateAIProvider(provider: AIProvider) {
        _uiState.update { currentState ->
            currentState.copy(
                aiSettings = currentState.aiSettings.withProvider(provider),
                connectionTestResult = null
            )
        }
    }
    
    /**
     * 更新 AI API Key
     */
    fun updateAIApiKey(apiKey: String) {
        _uiState.update { currentState ->
            currentState.copy(
                aiSettings = currentState.aiSettings.withApiKey(apiKey),
                connectionTestResult = null
            )
        }
    }
    
    /**
     * 更新 AI Endpoint
     */
    fun updateAIEndpoint(endpoint: String) {
        _uiState.update { currentState ->
            currentState.copy(
                aiSettings = currentState.aiSettings.copy(endpoint = endpoint),
                connectionTestResult = null
            )
        }
    }
    
    /**
     * 更新 AI 模型
     */
    fun updateAIModel(model: String) {
        _uiState.update { currentState ->
            currentState.copy(
                aiSettings = currentState.aiSettings.withModel(model)
            )
        }
    }
    
    /**
     * 更新系统提示词
     */
    fun updateSystemPrompt(prompt: String) {
        _uiState.update { currentState ->
            currentState.copy(
                aiSettings = currentState.aiSettings.withSystemPrompt(prompt)
            )
        }
    }
    
    /**
     * 更新温度参数
     */
    fun updateTemperature(temperature: Float) {
        _uiState.update { currentState ->
            currentState.copy(
                aiSettings = currentState.aiSettings.withTemperature(temperature)
            )
        }
    }
    
    /**
     * 更新最大 Token 数
     */
    fun updateMaxTokens(maxTokens: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                aiSettings = currentState.aiSettings.withMaxTokens(maxTokens)
            )
        }
    }
    
    /**
     * 更新流式响应选项
     */
    fun updateStreamResponse(enabled: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                aiSettings = currentState.aiSettings.copy(streamResponse = enabled)
            )
        }
    }
    
    /**
     * 更新 Function Calling 选项
     */
    fun updateFunctionCalling(enabled: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                aiSettings = currentState.aiSettings.copy(enableFunctionCalling = enabled)
            )
        }
    }
    
    /**
     * 测试 AI 连接
     */
    fun testAIConnection() {
        val settings = _uiState.value.aiSettings
        
        if (!settings.isConfigured) {
            _uiState.update { 
                it.copy(connectionTestResult = ConnectionTestResult(
                    isSuccess = false,
                    message = "请先配置 API Key"
                ))
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingConnection = true, connectionTestResult = null) }
            
            try {
                // TODO: 测试 AI API 连接
                // val result = aiRepository.testConnection(settings)
                
                // 临时模拟
                kotlinx.coroutines.delay(1000)
                
                _uiState.update { 
                    it.copy(
                        isTestingConnection = false,
                        connectionTestResult = ConnectionTestResult(
                            isSuccess = true,
                            message = "连接成功",
                            details = "已成功连接到 ${settings.provider.displayName} API"
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isTestingConnection = false,
                        connectionTestResult = ConnectionTestResult(
                            isSuccess = false,
                            message = "连接失败",
                            details = e.message
                        )
                    )
                }
            }
        }
    }
    
    /**
     * 保存 AI 设置
     */
    fun saveAISettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                val settings = _uiState.value.aiSettings
                // TODO: 保存到 DataStore
                // settingsRepository.saveAISettings(settings)
                
                _uiState.update { it.copy(isSaving = false) }
                _events.emit(SettingsEvent.AISettingsUpdated(settings))
                _events.emit(SettingsEvent.ShowMessage("AI 设置已保存"))
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
                _events.emit(SettingsEvent.ShowError("保存 AI 设置失败: ${e.message}"))
            }
        }
    }
    
    // ==================== GitHub 设置 ====================
    
    /**
     * 更新 GitHub Token
     */
    fun updateGitHubToken(token: String) {
        _uiState.update { currentState ->
            currentState.copy(
                githubSettings = currentState.githubSettings.withToken(token),
                connectionTestResult = null
            )
        }
    }
    
    /**
     * 更新 GitHub 用户名
     */
    fun updateGitHubUsername(username: String) {
        _uiState.update { currentState ->
            currentState.copy(
                githubSettings = currentState.githubSettings.withUsername(username)
            )
        }
    }
    
    /**
     * 更新默认分支
     */
    fun updateDefaultBranch(branch: String) {
        _uiState.update { currentState ->
            currentState.copy(
                githubSettings = currentState.githubSettings.withDefaultBranch(branch)
            )
        }
    }
    
    /**
     * 更新 API 基础 URL
     */
    fun updateGitHubApiBaseUrl(url: String) {
        _uiState.update { currentState ->
            currentState.copy(
                githubSettings = currentState.githubSettings.withApiBaseUrl(url),
                connectionTestResult = null
            )
        }
    }
    
    /**
     * 更新分页大小
     */
    fun updatePageSize(size: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                githubSettings = currentState.githubSettings.withPageSize(size)
            )
        }
    }
    
    /**
     * 更新包含私有仓库选项
     */
    fun updateIncludePrivateRepos(include: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                githubSettings = currentState.githubSettings.withIncludePrivateRepos(include)
            )
        }
    }
    
    /**
     * 更新包含 Fork 仓库选项
     */
    fun updateIncludeForkedRepos(include: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                githubSettings = currentState.githubSettings.withIncludeForkedRepos(include)
            )
        }
    }
    
    /**
     * 更新启动时自动获取选项
     */
    fun updateAutoFetchOnStartup(autoFetch: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                githubSettings = currentState.githubSettings.withAutoFetchOnStartup(autoFetch)
            )
        }
    }
    
    /**
     * 测试 GitHub 连接
     */
    fun testGitHubConnection() {
        val settings = _uiState.value.githubSettings
        
        if (!settings.isConfigured) {
            _uiState.update { 
                it.copy(connectionTestResult = ConnectionTestResult(
                    isSuccess = false,
                    message = "请先配置 GitHub Token"
                ))
            }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isTestingConnection = true, connectionTestResult = null) }
            
            try {
                // TODO: 测试 GitHub API 连接
                // val user = githubRepository.getCurrentUser(settings.token)
                // _uiState.update { it.copy(githubUser = user) }
                
                // 临时模拟
                kotlinx.coroutines.delay(1000)
                
                _uiState.update { 
                    it.copy(
                        isTestingConnection = false,
                        connectionTestResult = ConnectionTestResult(
                            isSuccess = true,
                            message = "连接成功",
                            details = "已成功连接到 GitHub API"
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isTestingConnection = false,
                        connectionTestResult = ConnectionTestResult(
                            isSuccess = false,
                            message = "连接失败",
                            details = e.message
                        )
                    )
                }
            }
        }
    }
    
    /**
     * 保存 GitHub 设置
     */
    fun saveGitHubSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                val settings = _uiState.value.githubSettings
                // TODO: 保存到 DataStore
                // settingsRepository.saveGitHubSettings(settings)
                
                _uiState.update { it.copy(isSaving = false) }
                _events.emit(SettingsEvent.GitHubSettingsUpdated(settings))
                _events.emit(SettingsEvent.ShowMessage("GitHub 设置已保存"))
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
                _events.emit(SettingsEvent.ShowError("保存 GitHub 设置失败: ${e.message}"))
            }
        }
    }
    
    // ==================== 应用设置 ====================
    
    /**
     * 更新主题模式
     */
    fun updateThemeMode(mode: ThemeMode) {
        _uiState.update { currentState ->
            currentState.copy(
                appSettings = currentState.appSettings.withThemeMode(mode)
            )
        }
        saveAppSettings()
    }
    
    /**
     * 更新语言
     */
    fun updateLanguage(language: AppLanguage) {
        _uiState.update { currentState ->
            currentState.copy(
                appSettings = currentState.appSettings.withLanguage(language)
            )
        }
        saveAppSettings()
    }
    
    /**
     * 更新自动保存选项
     */
    fun updateAutoSave(enabled: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                appSettings = currentState.appSettings.withAutoSave(enabled)
            )
        }
        saveAppSettings()
    }
    
    /**
     * 更新自动保存间隔
     */
    fun updateAutoSaveInterval(interval: Long) {
        _uiState.update { currentState ->
            currentState.copy(
                appSettings = currentState.appSettings.withAutoSaveInterval(interval)
            )
        }
        saveAppSettings()
    }
    
    /**
     * 更新显示通知选项
     */
    fun updateShowNotifications(show: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                appSettings = currentState.appSettings.withShowNotifications(show)
            )
        }
        saveAppSettings()
    }
    
    /**
     * 更新触觉反馈选项
     */
    fun updateHapticFeedback(enabled: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                appSettings = currentState.appSettings.withHapticFeedback(enabled)
            )
        }
        saveAppSettings()
    }
    
    /**
     * 更新动画选项
     */
    fun updateAnimations(enabled: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                appSettings = currentState.appSettings.withEnableAnimations(enabled)
            )
        }
        saveAppSettings()
    }
    
    /**
     * 更新紧凑模式选项
     */
    fun updateCompactMode(enabled: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(
                appSettings = currentState.appSettings.withCompactMode(enabled)
            )
        }
        saveAppSettings()
    }
    
    /**
     * 更新编辑器设置
     */
    fun updateEditorSettings(editorSettings: EditorSettings) {
        _uiState.update { currentState ->
            currentState.copy(
                appSettings = currentState.appSettings.withEditorSettings(editorSettings)
            )
        }
        saveAppSettings()
    }
    
    /**
     * 保存应用设置
     */
    fun saveAppSettings() {
        viewModelScope.launch {
            try {
                val settings = _uiState.value.appSettings
                // TODO: 保存到 DataStore
                // settingsRepository.saveAppSettings(settings)
                _events.emit(SettingsEvent.AppSettingsUpdated(settings))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowError("保存应用设置失败: ${e.message}"))
            }
        }
    }
    
    // ==================== 数据操作 ====================
    
    /**
     * 显示清除聊天历史对话框
     */
    fun showClearChatDialog() {
        _uiState.update { it.copy(showClearChatDialog = true) }
    }
    
    /**
     * 隐藏清除聊天历史对话框
     */
    fun hideClearChatDialog() {
        _uiState.update { it.copy(showClearChatDialog = false) }
    }
    
    /**
     * 确认清除聊天历史
     */
    fun confirmClearChatHistory() {
        viewModelScope.launch {
            try {
                // TODO: 清除聊天历史
                // chatRepository.clearAllConversations()
                
                _uiState.update { it.copy(showClearChatDialog = false) }
                _events.emit(SettingsEvent.ChatHistoryCleared)
                _events.emit(SettingsEvent.ShowMessage("聊天历史已清除"))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowError("清除聊天历史失败: ${e.message}"))
            }
        }
    }
    
    /**
     * 显示清除工作区对话框
     */
    fun showClearWorkspaceDialog() {
        _uiState.update { it.copy(showClearWorkspaceDialog = true) }
    }
    
    /**
     * 隐藏清除工作区对话框
     */
    fun hideClearWorkspaceDialog() {
        _uiState.update { it.copy(showClearWorkspaceDialog = false) }
    }
    
    /**
     * 确认清除工作区
     */
    fun confirmClearWorkspace() {
        viewModelScope.launch {
            try {
                // TODO: 清除本地工作区
                // fileRepository.clearLocalWorkspace()
                
                _uiState.update { it.copy(showClearWorkspaceDialog = false) }
                _events.emit(SettingsEvent.WorkspaceCleared)
                _events.emit(SettingsEvent.ShowMessage("本地工作区已清除"))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowError("清除工作区失败: ${e.message}"))
            }
        }
    }
    
    /**
     * 显示清除所有数据对话框
     */
    fun showClearAllDialog() {
        _uiState.update { it.copy(showClearAllDialog = true) }
    }
    
    /**
     * 隐藏清除所有数据对话框
     */
    fun hideClearAllDialog() {
        _uiState.update { it.copy(showClearAllDialog = false) }
    }
    
    /**
     * 确认清除所有数据
     */
    fun confirmClearAllData() {
        viewModelScope.launch {
            try {
                // TODO: 清除所有数据
                // chatRepository.clearAllConversations()
                // fileRepository.clearLocalWorkspace()
                // settingsRepository.clearAllSettings()
                
                _uiState.update { 
                    it.copy(
                        showClearAllDialog = false,
                        aiSettings = AISettings.default(),
                        githubSettings = GitHubSettings.default(),
                        appSettings = AppSettings.default(),
                        githubUser = null
                    )
                }
                _events.emit(SettingsEvent.AllDataCleared)
                _events.emit(SettingsEvent.ShowMessage("所有数据已清除"))
            } catch (e: Exception) {
                _events.emit(SettingsEvent.ShowError("清除数据失败: ${e.message}"))
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
     * 清除连接测试结果
     */
    fun clearConnectionTestResult() {
        _uiState.update { it.copy(connectionTestResult = null) }
    }
    
    /**
     * 获取可用的 AI 模型列表
     */
    fun getAvailableAIModels(): List<String> {
        return _uiState.value.aiSettings.provider.availableModels
    }
    
    /**
     * 重置 AI 设置为默认值
     */
    fun resetAISettings() {
        _uiState.update { it.copy(aiSettings = AISettings.default()) }
        saveAISettings()
    }
    
    /**
     * 重置 GitHub 设置为默认值
     */
    fun resetGitHubSettings() {
        _uiState.update { 
            it.copy(
                githubSettings = GitHubSettings.default(),
                githubUser = null
            )
        }
        saveGitHubSettings()
    }
    
    /**
     * 重置应用设置为默认值
     */
    fun resetAppSettings() {
        _uiState.update { it.copy(appSettings = AppSettings.default()) }
        saveAppSettings()
    }
}