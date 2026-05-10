package com.mobilecodex.model

/**
 * 工作区选项（导航用）
 */
enum class WorkspaceOption(val displayName: String, val icon: String) {
    CHAT("AI 对话", "chat"),
    REPOSITORIES("仓库列表", "folder"),
    FILES("文件浏览", "description"),
    SETTINGS("设置", "settings")
}
