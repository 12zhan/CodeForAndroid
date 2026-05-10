package com.mobilecodex.model

/**
 * 编辑器设置数据模型
 */
data class EditorSettings(
    val fontSize: Int = 14,
    val tabSize: Int = 4,
    val useSoftTabs: Boolean = true,
    val showLineNumbers: Boolean = true,
    val wordWrap: Boolean = false,
    val highlightCurrentLine: Boolean = true,
    val autoIndent: Boolean = true,
    val codeCompletion: Boolean = true
) {
    companion object {
        fun default() = EditorSettings()
    }
}

/**
 * 主题模式枚举
 */
enum class ThemeMode(val displayName: String) {
    LIGHT("浅色"),
    DARK("深色"),
    SYSTEM("跟随系统")
}

/**
 * 应用语言枚举
 */
enum class AppLanguage(val displayName: String, val code: String) {
    CHINESE("中文", "zh"),
    ENGLISH("English", "en"),
    JAPANESE("日本語", "ja"),
    KOREAN("한국어", "ko")
}
