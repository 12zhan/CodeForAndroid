package com.mobilecodex.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.mobilecodex.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// DataStore 实例扩展
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * 设置仓库
 * 使用 DataStore 持久化应用设置
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    // DataStore 实例
    private val dataStore = context.dataStore
    
    // 设置键
    private object Keys {
        val AI_SETTINGS = stringPreferencesKey("ai_settings")
        val GITHUB_SETTINGS = stringPreferencesKey("github_settings")
        val APP_SETTINGS = stringPreferencesKey("app_settings")
    }
    
    // AI 设置
    val aiSettings: Flow<AISettings> = dataStore.data.map { preferences ->
        val json = preferences[Keys.AI_SETTINGS]
        if (json != null) {
            try {
                gson.fromJson(json, AISettings::class.java) ?: AISettings.default()
            } catch (e: Exception) {
                AISettings.default()
            }
        } else {
            AISettings.default()
        }
    }
    
    // GitHub 设置
    val githubSettings: Flow<GitHubSettings> = dataStore.data.map { preferences ->
        val json = preferences[Keys.GITHUB_SETTINGS]
        if (json != null) {
            try {
                gson.fromJson(json, GitHubSettings::class.java) ?: GitHubSettings.default()
            } catch (e: Exception) {
                GitHubSettings.default()
            }
        } else {
            GitHubSettings.default()
        }
    }
    
    // 应用设置
    val appSettings: Flow<AppSettings> = dataStore.data.map { preferences ->
        val json = preferences[Keys.APP_SETTINGS]
        if (json != null) {
            try {
                gson.fromJson(json, AppSettings::class.java) ?: AppSettings.default()
            } catch (e: Exception) {
                AppSettings.default()
            }
        } else {
            AppSettings.default()
        }
    }
    
    /**
     * 保存 AI 设置
     */
    suspend fun saveAISettings(settings: AISettings) {
        dataStore.edit { preferences ->
            preferences[Keys.AI_SETTINGS] = gson.toJson(settings)
        }
    }
    
    /**
     * 保存 GitHub 设置
     */
    suspend fun saveGitHubSettings(settings: GitHubSettings) {
        dataStore.edit { preferences ->
            preferences[Keys.GITHUB_SETTINGS] = gson.toJson(settings)
        }
    }
    
    /**
     * 保存应用设置
     */
    suspend fun saveAppSettings(settings: AppSettings) {
        dataStore.edit { preferences ->
            preferences[Keys.APP_SETTINGS] = gson.toJson(settings)
        }
    }
    
    /**
     * 清除所有设置
     */
    suspend fun clearAllSettings() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    /**
     * 清除聊天历史（这里只清除设置，实际聊天历史由 ChatRepository 管理）
     */
    suspend fun clearChatHistory() {
        // 聊天历史由 ChatRepository 管理
        // 这里可以清除相关设置（如最近对话ID等）
    }
    
    /**
     * 清除工作区缓存
     */
    suspend fun clearWorkspaceCache() {
        // 工作区缓存由 FileRepository 管理
        // 这里可以清除相关设置
    }
    
    /**
     * 获取 AI 提供商配置
     */
    fun getAIProviderConfig(provider: AIProvider): AIProviderConfig {
        return AIProviderConfig(
            provider = provider,
            baseUrl = provider.baseUrl,
            models = provider.availableModels
        )
    }
}

/**
 * AI 提供商配置
 */
data class AIProviderConfig(
    val provider: AIProvider,
    val baseUrl: String,
    val models: List<String>
)