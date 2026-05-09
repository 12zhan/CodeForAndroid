package com.mobilecodex.model

/**
 * 工作区类型枚举
 */
enum class WorkspaceType(val displayName: String) {
    LOCAL("本地工作区"),
    GITHUB("GitHub 仓库")
}

/**
 * 工作区选项数据模型
 * 用于工作区下拉选择器
 */
data class WorkspaceOption(
    val type: WorkspaceType,
    val name: String,
    val id: String,
    val repository: GitHubRepository? = null,
    val branch: String? = null,
    val icon: String? = null,
    val isDefault: Boolean = false
) {
    /**
     * 判断是否为本地工作区
     */
    val isLocal: Boolean get() = type == WorkspaceType.LOCAL
    
    /**
     * 判断是否为 GitHub 工作区
     */
    val isGitHub: Boolean get() = type == WorkspaceType.GITHUB
    
    /**
     * 获取显示名称（包含分支信息）
     */
    val displayName: String
        get() = when (type) {
            WorkspaceType.LOCAL -> "📁 $name"
            WorkspaceType.GITHUB -> {
                val branchInfo = branch?.let { " ($it)" } ?: ""
                "🐙 ${repository?.fullName ?: name}$branchInfo"
            }
        }
    
    /**
     * 获取简短显示名称
     */
    val shortDisplayName: String
        get() = when (type) {
            WorkspaceType.LOCAL -> name
            WorkspaceType.GITHUB -> repository?.name ?: name
        }
    
    /**
     * 获取仓库所有者名称（仅 GitHub 工作区）
     */
    val ownerName: String?
        get() = repository?.owner?.login
    
    /**
     * 获取仓库名称（仅 GitHub 工作区）
     */
    val repoName: String?
        get() = repository?.name
    
    /**
     * 获取实际使用的分支名称
     */
    val effectiveBranch: String
        get() = branch ?: repository?.defaultBranch ?: "main"
    
    /**
     * 创建更新分支的副本
     */
    fun withBranch(branch: String): WorkspaceOption {
        return copy(branch = branch)
    }
    
    companion object {
        /**
         * 创建本地工作区选项
         */
        fun local(name: String = "本地工作区"): WorkspaceOption {
            return WorkspaceOption(
                type = WorkspaceType.LOCAL,
                name = name,
                id = "local_workspace",
                isDefault = true
            )
        }
        
        /**
         * 从 GitHub 仓库创建工作区选项
         */
        fun fromRepository(repository: GitHubRepository, branch: String? = null): WorkspaceOption {
            return WorkspaceOption(
                type = WorkspaceType.GITHUB,
                name = repository.name,
                id = "github_${repository.fullName}",
                repository = repository,
                branch = branch ?: repository.defaultBranch
            )
        }
        
        /**
         * 创建默认的工作区列表
         */
        fun defaultList(): List<WorkspaceOption> {
            return listOf(local())
        }
    }
}