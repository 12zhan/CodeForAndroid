package com.mobilecodex.model

/**
 * 应用设置数据模型
 * 用于持久化应用级别的配置
 */
data class AppSettings(
    val theme: String = "system",         // "light", "dark", "system"
    val language: String = "zh",           // 语言代码
    val autoSave: Boolean = true,          // 是否自动保存
    val codeFontSize: Int = 14,           // 代码字体大小
    val showLineNumbers: Boolean = true,   // 是否显示行号
    val tabSize: Int = 4,                 // Tab 缩进大小
    val useSoftTabs: Boolean = true       // 是否使用空格代替 Tab
) {
    companion object {
        fun default() = AppSettings()
    }
}
