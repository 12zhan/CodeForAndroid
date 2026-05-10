package com.mobilecodex.data.repository

import com.mobilecodex.data.api.*
import com.mobilecodex.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GitHub 仓库
 * 封装 GitHub API 调用
 */
@Singleton
class GitHubRepository @Inject constructor(
    private val gitHubApi: GitHubApi,
    private val settingsRepository: SettingsRepository
) {
    /**
     * 获取当前用户信息
     */
    suspend fun getCurrentUser(): GitHubUser {
        return withContext(Dispatchers.IO) {
            val response = gitHubApi.getCurrentUser()
            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("获取用户信息失败：响应体为空")
                body.toDomainModel()
            } else {
                throw Exception("获取用户信息失败：${response.code()} ${response.message()}")
            }
        }
    }

    /**
     * 获取用户仓库列表
     * @param page 页码（从1开始）
     * @param perPage 每页数量
     * @param includePrivate 是否包含私有仓库
     * @param includeForked 是否包含 Fork 仓库
     */
    suspend fun getUserRepositories(
        page: Int = 1,
        perPage: Int = 30,
        includePrivate: Boolean = true,
        includeForked: Boolean = true
    ): List<com.mobilecodex.model.GitHubRepository> {
        return withContext(Dispatchers.IO) {
            val response = gitHubApi.getUserRepositories(
                page = page,
                perPage = perPage,
                sort = "updated",
                direction = "desc"
            )

            if (response.isSuccessful) {
                val repos = response.body() ?: emptyList()

                // 过滤仓库并映射到领域模型
                repos.filter { repo ->
                    val privateFilter = if (includePrivate) true else !repo.`private`
                    val forkFilter = if (includeForked) true else !repo.fork
                    privateFilter && forkFilter
                }.map { it.toDomainModel() }
            } else {
                throw Exception("获取仓库列表失败：${response.code()} ${response.message()}")
            }
        }
    }

    /**
     * 获取仓库文件树
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param branch 分支名称
     * @param recursive 是否递归获取
     */
    suspend fun getTree(
        owner: String,
        repo: String,
        branch: String,
        recursive: Boolean = true
    ): List<GitTreeNode> {
        return withContext(Dispatchers.IO) {
            val response = gitHubApi.getTree(
                owner = owner,
                repo = repo,
                branch = branch,
                recursive = if (recursive) 1 else null
            )

            if (response.isSuccessful) {
                val treeResponse = response.body()
                treeResponse?.tree?.map { it.toDomainModel() } ?: emptyList()
            } else {
                throw Exception("获取文件树失败：${response.code()} ${response.message()}")
            }
        }
    }

    /**
     * 获取文件内容
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param path 文件路径
     * @param ref 分支或提交SHA
     */
    suspend fun getFileContent(
        owner: String,
        repo: String,
        path: String,
        ref: String? = null
    ): FileContent {
        return withContext(Dispatchers.IO) {
            val response = gitHubApi.getFileContent(
                owner = owner,
                repo = repo,
                path = path,
                ref = ref
            )

            if (response.isSuccessful) {
                val body = response.body() ?: throw Exception("获取文件内容失败：响应体为空")
                body.toDomainModel()
            } else {
                throw Exception("获取文件内容失败：${response.code()} ${response.message()}")
            }
        }
    }

    /**
     * 创建 Blob
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param content 文件内容
     * @param encoding 编码（base64 或 utf-8）
     * @return Blob SHA
     */
    suspend fun createBlob(
        owner: String,
        repo: String,
        content: String,
        encoding: String = "utf-8"
    ): String {
        return withContext(Dispatchers.IO) {
            val blobRequest = CreateBlobRequest(content = content, encoding = encoding)
            val response = gitHubApi.createBlob(owner = owner, repo = repo, body = blobRequest)

            if (response.isSuccessful) {
                response.body()?.sha ?: throw Exception("创建 Blob 失败：响应体为空")
            } else {
                throw Exception("创建 Blob 失败：${response.code()} ${response.message()}")
            }
        }
    }

    /**
     * 创建 Tree
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param baseTree 基础树 SHA（可选）
     * @param tree 树节点列表
     * @return Tree SHA
     */
    suspend fun createTree(
        owner: String,
        repo: String,
        baseTree: String?,
        tree: List<TreeEntry>
    ): String {
        return withContext(Dispatchers.IO) {
            val treeRequest = CreateTreeRequest(base_tree = baseTree, tree = tree)
            val response = gitHubApi.createTree(owner = owner, repo = repo, body = treeRequest)

            if (response.isSuccessful) {
                response.body()?.sha ?: throw Exception("创建 Tree 失败：响应体为空")
            } else {
                throw Exception("创建 Tree 失败：${response.code()} ${response.message()}")
            }
        }
    }

    /**
     * 创建 Commit
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param message 提交信息
     * @param tree Tree SHA
     * @param parents 父提交 SHA 列表
     * @return Commit SHA
     */
    suspend fun createCommit(
        owner: String,
        repo: String,
        message: String,
        tree: String,
        parents: List<String>
    ): String {
        return withContext(Dispatchers.IO) {
            val commitRequest = CreateCommitRequest(
                message = message,
                tree = tree,
                parents = parents
            )
            val response = gitHubApi.createCommit(owner = owner, repo = repo, body = commitRequest)

            if (response.isSuccessful) {
                response.body()?.sha ?: throw Exception("创建 Commit 失败：响应体为空")
            } else {
                throw Exception("创建 Commit 失败：${response.code()} ${response.message()}")
            }
        }
    }

    /**
     * 更新 Ref
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param ref 引用路径（如 "heads/main"）
     * @param sha 新的 SHA
     * @param force 是否强制更新
     */
    suspend fun updateRef(
        owner: String,
        repo: String,
        ref: String,
        sha: String,
        force: Boolean = false
    ) {
        return withContext(Dispatchers.IO) {
            val refRequest = UpdateRefRequest(sha = sha, force = force)
            val response = gitHubApi.updateRef(owner = owner, repo = repo, ref = ref, body = refRequest)

            if (!response.isSuccessful) {
                throw Exception("更新 Ref 失败：${response.code()} ${response.message()}")
            }
        }
    }

    /**
     * 获取仓库默认分支
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @return 默认分支名称
     */
    suspend fun getDefaultBranch(owner: String, repo: String): String {
        return withContext(Dispatchers.IO) {
            val response = gitHubApi.getRepository(owner = owner, repo = repo)

            if (response.isSuccessful) {
                response.body()?.default_branch ?: "main"
            } else {
                throw Exception("获取仓库信息失败：${response.code()} ${response.message()}")
            }
        }
    }

    /**
     * 提交文件更改
     * @param owner 仓库所有者
     * @param repo 仓库名称
     * @param branch 分支名称
     * @param commitMessage 提交信息
     * @param files 文件更改列表（路径 -> 内容）
     * @return 提交 SHA
     */
    suspend fun commitChanges(
        owner: String,
        repo: String,
        branch: String,
        commitMessage: String,
        files: Map<String, String>
    ): String {
        return withContext(Dispatchers.IO) {
            // 1. 获取当前分支的 HEAD 提交
            val refResponse = gitHubApi.getRef(owner, repo, "heads/$branch")
            if (!refResponse.isSuccessful) {
                throw Exception("获取分支引用失败：${refResponse.code()} ${refResponse.message()}")
            }
            val headSha = refResponse.body()?.`object`?.sha ?: throw Exception("无法获取 HEAD SHA")

            // 2. 获取当前 Tree
            val commitResponse = gitHubApi.getCommit(owner, repo, headSha)
            if (!commitResponse.isSuccessful) {
                throw Exception("获取提交信息失败：${commitResponse.code()} ${commitResponse.message()}")
            }
            val baseTreeSha = commitResponse.body()?.tree?.sha

            // 3. 为每个文件创建 Blob
            val treeEntries = mutableListOf<TreeEntry>()
            for ((path, content) in files) {
                val blobSha = createBlob(owner, repo, content)
                treeEntries.add(
                    TreeEntry(
                        path = path,
                        mode = "100644",
                        type = "blob",
                        sha = blobSha
                    )
                )
            }

            // 4. 创建新的 Tree
            val newTreeSha = createTree(owner, repo, baseTreeSha, treeEntries)

            // 5. 创建新的 Commit
            val newCommitSha = createCommit(
                owner, repo, commitMessage, newTreeSha, listOf(headSha)
            )

            // 6. 更新分支引用
            updateRef(owner, repo, "heads/$branch", newCommitSha)

            newCommitSha
        }
    }
}

// ==================== 响应 → 领域模型映射 ====================

/**
 * 将 API 用户响应映射为领域模型
 */
private fun GitHubUserResponse.toDomainModel() = GitHubUser(
    login = login,
    id = id,
    avatarUrl = avatar_url,
    name = name,
    email = email,
    bio = bio,
    publicRepos = public_repos,
    followers = followers,
    following = following,
    htmlUrl = html_url
)

/**
 * 将 API 仓库响应映射为领域模型
 */
private fun RepositoryResponse.toDomainModel() =
    com.mobilecodex.model.GitHubRepository(
        id = id,
        name = name,
        fullName = full_name,
        description = description,
        isPrivate = `private`,
        isFork = fork,
        htmlUrl = html_url,
        defaultBranch = default_branch,
        language = language,
        stargazersCount = stargazers_count,
        forksCount = forks_count,
        updatedAt = updated_at
    )

/**
 * 将 API 树节点响应映射为领域模型
 */
private fun TreeNodeResponse.toDomainModel() = GitTreeNode(
    path = path,
    mode = mode,
    type = type,
    sha = sha,
    size = size,
    url = url
)

/**
 * 将 API 文件内容响应映射为领域模型
 */
private fun FileContentResponse.toDomainModel() = FileContent(
    name = name,
    path = path,
    sha = sha,
    size = size,
    content = content,
    encoding = encoding,
    type = type
)
