package com.mobilecodex.model

/**
 * 应用设置数据模型
 * 用于持久化应用级别的配置
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.CHINESE,
    val enableAnimations: Boolean = true,
    val compactMode: Boolean = false,
    val autoSave: Boolean = true,
    val autoSaveInterval: Long = 300,
    val showNotifications: Boolean = true,
    val hapticFeedback: Boolean = true,
    val editorSettings: EditorSettings = EditorSettings()
) {
    fun withEditorSettings(editorSettings: EditorSettings) = copy(editorSettings = editorSettings)
    fun withThemeMode(themeMode: ThemeMode) = copy(themeMode = themeMode)
    fun withLanguage(language: AppLanguage) = copy(language = language)
    fun withEnableAnimations(enable: Boolean) = copy(enableAnimations = enable)
    fun withCompactMode(compact: Boolean) = copy(compactMode = compact)
    fun withAutoSave(enabled: Boolean) = copy(autoSave = enabled)
    fun withAutoSaveInterval(interval: Long) = copy(autoSaveInterval = interval)
    fun withShowNotifications(enabled: Boolean) = copy(showNotifications = enabled)
    fun withHapticFeedback(enabled: Boolean) = copy(hapticFeedback = enabled)

    companion object {
        fun default() = AppSettings()
    }
}
