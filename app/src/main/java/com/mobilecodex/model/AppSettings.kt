package com.mobilecodex.model

/**
 * 应用综合设置
 */
data class AppSettings(
    val aiSettings: AISettings = AISettings(),
    val gitHubSettings: GitHubSettings = GitHubSettings(),
    val editorSettings: EditorSettings = EditorSettings(),
    val isDarkTheme: Boolean = true,
    val selectedLanguage: String = "zh"
)
