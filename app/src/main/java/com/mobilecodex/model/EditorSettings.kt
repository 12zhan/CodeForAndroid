package com.mobilecodex.model

/**
 * 编辑器设置
 */
data class EditorSettings(
    val fontSize: Int = 14,
    val fontFamily: String = "monospace",
    val tabSize: Int = 4,
    val useSpaces: Boolean = true,
    val showLineNumbers: Boolean = true,
    val wordWrap: Boolean = false,
    val syntaxHighlighting: Boolean = true,
    val autoSave: Boolean = true,
    val autoSaveDelayMs: Long = 2000L,
    val theme: EditorTheme = EditorTheme.DARCULA
)

enum class EditorTheme(val displayName: String) {
    DEFAULT("Default"),
    DARCULA("Darcula"),
    MONOKAI("Monokai"),
    GITHUB("GitHub"),
    SOLARIZED_DARK("Solarized Dark"),
    SOLARIZED_LIGHT("Solarized Light")
}
