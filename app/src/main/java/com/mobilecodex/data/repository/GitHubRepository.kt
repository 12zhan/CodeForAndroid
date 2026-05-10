package com.mobilecodex.data.repository

import android.util.Base64
import com.mobilecodex.data.api.GitHubApi
import com.mobilecodex.model.*
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class GitHubRepository @Inject constructor(
    private val gitHubApi: GitHubApi
) {
    private fun authHeader(settings: GitHubSettings): String {
        return "Bearer ${settings.accessToken}"
    }

    // --- 用户 ---

    suspend fun getAuthenticatedUser(settings: GitHubSettings): Result<GitHubUser> =
        withContext(Dispatchers.IO) {
            try {
                val response = gitHubApi.getAuthenticatedUser(authHeader(settings))
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("获取用户信息失败: ${response.code()} ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(Exception("网络错误: ${e.message}", e))
            }
        }

    // --- 仓库 ---

    suspend fun listMyRepos(
        settings: GitHubSettings,
        page: Int = 1
    ): Result<List<GitHubRepository>> = withContext(Dispatchers.IO) {
        try {
            val response = gitHubApi.listMyRepos(authHeader(settings), page = page)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("获取仓库列表失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}", e))
        }
    }

    suspend fun getRepository(
        settings: GitHubSettings,
        owner: String,
        repo: String
    ): Result<GitHubRepository> = withContext(Dispatchers.IO) {
        try {
            val response = gitHubApi.getRepository(owner, repo, authHeader(settings))
            if (response.isSuccessful) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("获取仓库信息失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}", e))
        }
    }

    suspend fun searchRepositories(
        settings: GitHubSettings,
        query: String
    ): Result<List<GitHubRepository>> = withContext(Dispatchers.IO) {
        try {
            val response = gitHubApi.searchRepositories(query, authHeader(settings))
            if (response.isSuccessful) {
                Result.success(response.body()?.items ?: emptyList())
            } else {
                Result.failure(Exception("搜索仓库失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}", e))
        }
    }

    // --- 文件树 ---

    suspend fun getFileTree(
        settings: GitHubSettings,
        owner: String,
        repo: String,
        branch: String = "main"
    ): Result<List<GitTreeNode>> = withContext(Dispatchers.IO) {
        try {
            // 先获取仓库信息拿到 tree sha
            val repoResponse = gitHubApi.getRepository(owner, repo, authHeader(settings))
            if (!repoResponse.isSuccessful) {
                return@withContext Result.failure(Exception("获取仓库信息失败"))
            }

            val defaultBranch = repoResponse.body()?.defaultBranch ?: branch

            // 获取分支信息
            val branchesResponse = gitHubApi.listBranches(owner, repo, authHeader(settings))
            val branchSha = if (branchesResponse.isSuccessful) {
                branchesResponse.body()?.find { it.name == defaultBranch }?.commit?.sha
                    ?: defaultBranch
            } else {
                defaultBranch
            }

            // 递归获取文件树
            val treeResponse = gitHubApi.getTree(owner, repo, branchSha, authHeader(settings))
            if (treeResponse.isSuccessful) {
                val tree = treeResponse.body()!!
                if (tree.truncated) {
                    // 如果被截断，至少返回已有的
                    Result.success(tree.tree.sortedBy { it.type })  // 目录在前
                } else {
                    Result.success(tree.tree.sortedBy { it.type })
                }
            } else {
                Result.failure(Exception("获取文件树失败: ${treeResponse.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}", e))
        }
    }

    // --- 文件内容 ---

    suspend fun getFileContent(
        settings: GitHubSettings,
        owner: String,
        repo: String,
        path: String,
        ref: String? = null
    ): Result<FileContent> = withContext(Dispatchers.IO) {
        try {
            val response = gitHubApi.getFileContent(owner, repo, path, authHeader(settings), ref)
            if (response.isSuccessful) {
                val item = response.body()!!

                // GitHub 单文件 API 返回的 content 字段是 Base64 编码的
                val decodedContent = if (item.isFile && item.content != null) {
                    decodeBase64(item.content)
                } else if (item.isFile && item.downloadUrl != null) {
                    // 降级方案：通过 download_url 获取
                    getRawFileContent(settings, item.downloadUrl).getOrDefault("")
                } else {
                    ""
                }

                Result.success(
                    FileContent(
                        path = item.path,
                        name = item.name,
                        content = decodedContent,
                        sha = item.sha,
                        url = item.htmlUrl,
                        size = item.size
                    )
                )
            } else {
                Result.failure(Exception("获取文件内容失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}", e))
        }
    }

    /**
     * 通过 download_url 直接获取原始文件内容
     */
    suspend fun getRawFileContent(
        settings: GitHubSettings,
        downloadUrl: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val okHttpClient = okhttp3.OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(downloadUrl)
                .header("Authorization", authHeader(settings))
                .build()
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                Result.success(response.body()?.string() ?: "")
            } else {
                Result.failure(Exception("获取原始文件失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}", e))
        }
    }

    // --- 目录内容 ---

    suspend fun getDirectoryContents(
        settings: GitHubSettings,
        owner: String,
        repo: String,
        path: String,
        ref: String? = null
    ): Result<List<GitHubContentItem>> = withContext(Dispatchers.IO) {
        try {
            val response = gitHubApi.getContents(owner, repo, path, authHeader(settings), ref)
            if (response.isSuccessful) {
                val items = response.body() ?: emptyList()
                // 按类型排序：目录在前
                Result.success(items.sortedBy { if (it.isDirectory) 0 else 1 })
            } else {
                Result.failure(Exception("获取目录内容失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}", e))
        }
    }

    // --- 文件操作 ---

    suspend fun createOrUpdateFile(
        settings: GitHubSettings,
        owner: String,
        repo: String,
        path: String,
        content: String,
        commitMessage: String,
        sha: String? = null,
        branch: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val base64Content = Base64.encodeToString(
                content.toByteArray(Charsets.UTF_8),
                Base64.NO_WRAP
            )
            val request = com.mobilecodex.data.api.CreateFileRequest(
                message = commitMessage,
                content = base64Content,
                sha = sha,
                branch = branch
            )
            val response = gitHubApi.createOrUpdateFile(
                owner, repo, path, authHeader(settings), request
            )
            if (response.isSuccessful) {
                val newSha = response.body()?.content?.sha ?: ""
                Result.success(newSha)
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                Result.failure(Exception("提交失败 ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}", e))
        }
    }

    suspend fun deleteFile(
        settings: GitHubSettings,
        owner: String,
        repo: String,
        path: String,
        sha: String,
        commitMessage: String,
        branch: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = com.mobilecodex.data.api.DeleteFileRequest(
                message = commitMessage,
                sha = sha,
                branch = branch
            )
            val response = gitHubApi.deleteFile(
                owner, repo, path, authHeader(settings), request
            )
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("删除失败 ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}", e))
        }
    }

    private fun decodeBase64(encoded: String): String {
        return try {
            String(Base64.decode(encoded, Base64.DEFAULT), Charsets.UTF_8)
        } catch (e: Exception) {
            ""
        }
    }
}
