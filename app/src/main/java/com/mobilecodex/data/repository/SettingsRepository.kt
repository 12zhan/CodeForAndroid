package com.mobilecodex.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.mobilecodex.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

@ViewModelScoped
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val API_KEY = stringPreferencesKey("ai_api_key")
        val AI_BASE_URL = stringPreferencesKey("ai_base_url")
        val AI_MODEL = stringPreferencesKey("ai_model_id")
        val AI_MAX_TOKENS = intPreferencesKey("ai_max_tokens")
        val AI_TEMPERATURE = floatPreferencesKey("ai_temperature")
        val AI_TOP_P = floatPreferencesKey("ai_top_p")
        val AI_SYSTEM_PROMPT = stringPreferencesKey("ai_system_prompt")

        val GH_TOKEN = stringPreferencesKey("gh_access_token")
        val GH_USERNAME = stringPreferencesKey("gh_username")

        val EDITOR_FONT_SIZE = intPreferencesKey("editor_font_size")
        val EDITOR_TAB_SIZE = intPreferencesKey("editor_tab_size")
        val EDITOR_USE_SPACES = booleanPreferencesKey("editor_use_spaces")
        val EDITOR_SHOW_LINE_NUMBERS = booleanPreferencesKey("editor_show_line_numbers")
        val EDITOR_WORD_WRAP = booleanPreferencesKey("editor_word_wrap")

        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            aiSettings = AISettings(
                apiKey = prefs[Keys.API_KEY] ?: "",
                baseUrl = prefs[Keys.AI_BASE_URL] ?: "https://api.openai.com/v1",
                modelId = prefs[Keys.AI_MODEL] ?: "gpt-4-turbo",
                maxTokens = prefs[Keys.AI_MAX_TOKENS] ?: 4096,
                temperature = prefs[Keys.AI_TEMPERATURE] ?: 0.7f,
                topP = prefs[Keys.AI_TOP_P] ?: 1.0f,
                systemPrompt = prefs[Keys.AI_SYSTEM_PROMPT]
                    ?: "你是一个专业的编程助手，帮助用户编写、审查和优化代码。"
            ),
            gitHubSettings = GitHubSettings(
                accessToken = prefs[Keys.GH_TOKEN] ?: "",
                username = prefs[Keys.GH_USERNAME] ?: ""
            ),
            editorSettings = EditorSettings(
                fontSize = prefs[Keys.EDITOR_FONT_SIZE] ?: 14,
                tabSize = prefs[Keys.EDITOR_TAB_SIZE] ?: 4,
                useSpaces = prefs[Keys.EDITOR_USE_SPACES] ?: true,
                showLineNumbers = prefs[Keys.EDITOR_SHOW_LINE_NUMBERS] ?: true,
                wordWrap = prefs[Keys.EDITOR_WORD_WRAP] ?: false
            ),
            isDarkTheme = prefs[Keys.DARK_THEME] ?: true
        )
    }

    suspend fun getSettings(): AppSettings {
        return settingsFlow.first()
    }

    suspend fun updateAISettings(settings: AISettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.API_KEY] = settings.apiKey
            prefs[Keys.AI_BASE_URL] = settings.baseUrl
            prefs[Keys.AI_MODEL] = settings.modelId
            prefs[Keys.AI_MAX_TOKENS] = settings.maxTokens
            prefs[Keys.AI_TEMPERATURE] = settings.temperature
            prefs[Keys.AI_TOP_P] = settings.topP
            prefs[Keys.AI_SYSTEM_PROMPT] = settings.systemPrompt
        }
    }

    suspend fun updateGitHubSettings(settings: GitHubSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.GH_TOKEN] = settings.accessToken
            prefs[Keys.GH_USERNAME] = settings.username
        }
    }

    suspend fun updateEditorSettings(settings: EditorSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.EDITOR_FONT_SIZE] = settings.fontSize
            prefs[Keys.EDITOR_TAB_SIZE] = settings.tabSize
            prefs[Keys.EDITOR_USE_SPACES] = settings.useSpaces
            prefs[Keys.EDITOR_SHOW_LINE_NUMBERS] = settings.showLineNumbers
            prefs[Keys.EDITOR_WORD_WRAP] = settings.wordWrap
        }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DARK_THEME] = isDark
        }
    }
}
