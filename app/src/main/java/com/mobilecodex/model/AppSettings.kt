package com.mobilecodex.model

/**
 * 应用主题模式枚举
 */
enum class ThemeMode(val displayName: String) {
    LIGHT("浅色模式"),
    DARK("深色模式"),
    SYSTEM("跟随系统")
}

/**
 * 应用语言枚举
 */
enum class AppLanguage(val displayName: String, val code: String) {
    CHINESE("中文", "zh"),
    ENGLISH("English", "en"),
    SYSTEM("跟随系统", "")
}

/**
 * 代码编辑器设置
 */
data class EditorSettings(
    val fontSize: Int = 14,
    val tabSize: Int = 4,
    val showLineNumbers: Boolean = true,
    val wordWrap: Boolean = true,
    val autoIndent: Boolean = true,
    val syntaxHighlighting: Boolean = true,
    val highlightCurrentLine: Boolean = true,
    val showWhitespace: Boolean = false,
    val fontFamily: String = "monospace"
) {
    fun withFontSize(size: Int): EditorSettings {
        return copy(fontSize = size.coerceIn(8, 32))
    }
    
    fun withTabSize(size: Int): EditorSettings {
        return copy(tabSize = size.coerceIn(1, 8))
    }
    
    fun withShowLineNumbers(show: Boolean): EditorSettings {
        return copy(showLineNumbers = show)
    }
    
    fun withWordWrap(wrap: Boolean): EditorSettings {
        return copy(wordWrap = wrap)
    }
    
    fun withAutoIndent(indent: Boolean): EditorSettings {
        return copy(autoIndent = indent)
    }
    
    fun withSyntaxHighlighting(highlight: Boolean): EditorSettings {
        return copy(syntaxHighlighting = highlight)
    }
    
    fun withHighlightCurrentLine(highlight: Boolean): EditorSettings {
        return copy(highlightCurrentLine = highlight)
    }
    
    fun withShowWhitespace(show: Boolean): EditorSettings {
        return copy(showWhitespace = show)
    }
    
    fun withFontFamily(family: String): EditorSettings {
        return copy(fontFamily = family)
    }
    
    companion object {
        fun default() = EditorSettings()
    }
}

/**
 * 应用设置数据模型
 * 用于配置应用的全局参数
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val editorSettings: EditorSettings = EditorSettings.default(),
    val autoSave: Boolean = true,
    val autoSaveInterval: Long = 30_000, // 自动保存间隔（30秒）
    val showNotifications: Boolean = true,
    val hapticFeedback: Boolean = true,
    val crashReporting: Boolean = true,
    val analyticsEnabled: Boolean = false,
    val maxRecentFiles: Int = 10,
    val maxChatHistory: Int = 100,
    val enableAnimations: Boolean = true,
    val compactMode: Boolean = false
) {
    /**
     * 创建更新主题模式的副本
     */
    fun withThemeMode(mode: ThemeMode): AppSettings {
        return copy(themeMode = mode)
    }
    
    /**
     * 创建更新语言的副本
     */
    fun withLanguage(language: AppLanguage): AppSettings {
        return copy(language = language)
    }
    
    /**
     * 创建更新编辑器设置的副本
     */
    fun withEditorSettings(settings: EditorSettings): AppSettings {
        return copy(editorSettings = settings)
    }
    
    /**
     * 创建更新自动保存选项的副本
     */
    fun withAutoSave(autoSave: Boolean): AppSettings {
        return copy(autoSave = autoSave)
    }
    
    /**
     * 创建更新自动保存间隔的副本
     */
    fun withAutoSaveInterval(interval: Long): AppSettings {
        return copy(autoSaveInterval = interval.coerceIn(5_000, 300_000))
    }
    
    /**
     * 创建更新显示通知选项的副本
     */
    fun withShowNotifications(show: Boolean): AppSettings {
        return copy(showNotifications = show)
    }
    
    /**
     * 创建更新触觉反馈选项的副本
     */
    fun withHapticFeedback(haptic: Boolean): AppSettings {
        return copy(hapticFeedback = haptic)
    }
    
    /**
     * 创建更新崩溃报告选项的副本
     */
    fun withCrashReporting(report: Boolean): AppSettings {
        return copy(crashReporting = report)
    }
    
    /**
     * 创建更新分析选项的副本
     */
    fun withAnalyticsEnabled(enabled: Boolean): AppSettings {
        return copy(analyticsEnabled = enabled)
    }
    
    /**
     * 创建更新最大最近文件数的副本
     */
    fun withMaxRecentFiles(max: Int): AppSettings {
        return copy(maxRecentFiles = max.coerceIn(5, 50))
    }
    
    /**
     * 创建更新最大聊天历史数的副本
     */
    fun withMaxChatHistory(max: Int): AppSettings {
        return copy(maxChatHistory = max.coerceIn(10, 1000))
    }
    
    /**
     * 创建更新动画选项的副本
     */
    fun withEnableAnimations(enable: Boolean): AppSettings {
        return copy(enableAnimations = enable)
    }
    
    /**
     * 创建更新紧凑模式选项的副本
     */
    fun withCompactMode(compact: Boolean): AppSettings {
        return copy(compactMode = compact)
    }
    
    /**
     * 获取实际的语言代码
     */
    val effectiveLanguageCode: String
        get() = if (language == AppLanguage.SYSTEM) {
            java.util.Locale.getDefault().language
        } else {
            language.code
        }
    
    companion object {
        /**
         * 创建默认设置
         */
        fun default() = AppSettings()
    }
}