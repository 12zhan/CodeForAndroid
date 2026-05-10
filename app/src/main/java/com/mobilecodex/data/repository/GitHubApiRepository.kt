package com.mobilecodex.data.repository

import android.util.Base64
import com.mobilecodex.data.api.GitHubApi
import com.mobilecodex.model.FileContent
import com.mobilecodex.model.GitHubContentItem
import com.mobilecodex.model.GitHubRepository
import com.mobilecodex.model.GitHubSettings
import com.mobilecodex.model.GitHubUser
import com.mobilecodex.model.GitTreeNode
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * GitHub API 数据仓库 — 封装所有 GitHub REST API 调用。
 * 注意：与 model.GitHubRepository（数据类）不同，这是 API 访问层。
 */
@ViewModelScoped
class GitHubApiRepository @Inject constructor(
    private val gitHubApi: GitHubApi
) {
    private fun authHeader(settings: GitHubSettings) = "Bearer ${settings.accessToken}"

    // --- 用户 ---

    suspend fun getAuthenticatedUser(settings: GitHubSettings): Result<GitHubUser> =
        withContext(Dispatchers.IO) {
            try {
                val response = gitHubApi.getAuthenticatedUser(authHeader(settings))
                if (response.isSuccessful)
                    Result.success(response.body()!!)
                else
                    Result.failure(Exception("获取用户信息失败: ${response.code()} ${response.message()}"))
            } catch (e: Exception) {
                Result.failure(Exception("网络错误: ${e.message}", e))
            }
        }

    // --- 仓库 ---

    suspend fun listMyRepos(
        settings: GitHubSettings, page: Int = 1
    ): Result<List<GitHubRepository>> = withContext(Dispatchers.IO) {
        try {
            val response = gitHubApi.listMyRepos(authHeader(settings), page = page)
            if (response.isSuccessful)
                Result.success(response.body() ?: emptyList())
            else
                Result.failure(Exception("获取仓库列表失败: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}", e))
        }
    }

    suspend fun getRepository(
        settings: GitHubSettings, owner: String, repo: String
    ): Result<GitHubRepository> = withContext(Dispatchers.IO) {
        try {
            val response = gitHubApi.getRepository(owner, repo, authHeader(settings))
            if (response.isSuccessful)
                Result.success(response.body()!!)
            else
                Result.failure(Exception("获取仓库信息失败: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}", e))
        }
    }

    suspend fun searchRepositories(
        settings: GitHubSettings, query: String
    ): Result<List<GitHubRepository>> = withContext(Dispatchers.IO) {
        try {
            val response = gitHubApi.searchRepositories(query, authHeader(settings))
            if (response.isSuccessful)
                Result.success(response.body()?.items ?: emptyList())
            else
                Result.failure(Exception("搜索仓库失败: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}", e))
        }
    }

    // --- 文件树 ---

    suspend fun getFileTree(
        settings: GitHubSettings, owner: String, repo: String, branch: String = "main"
    ): Result<List<GitTreeNode>> = withContext(Dispatchers.IO) {
        try {
            val repoResponse = gitHubApi.getRepository(owner, repo, authHeader(settings))
            if (!repoResponse.isSuccessful)
                return@withContext Result.failure(Exception("获取仓库信息失败"))

            val defaultBranch = repoResponse.body()?.defaultBranch ?: branch
            val branchesResponse = gitHubApi.listBranches(owner, repo, authHeader(settings))
            val branchSha = if (branchesResponse.isSuccessful) {
                branchesResponse.body()?.find { it.name == defaultBranch }?.commit?.sha ?: defaultBranch
            } else defaultBranch

            val treeResponse = gitHubApi.getTree(owner, repo, branchSha, authHeader(settings))
            if (treeResponse.isSuccessful) {
                Result.success(treeResponse.body()!!.tree.sortedBy { it.type })
            } else {
                Result.failure(Exception("获取文件树失败: ${treeResponse.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}", e))
        }
    }

    // --- 文件内容 ---

    suspend fun getFileContent(
        settings: GitHubSettings, owner: String, repo: String, path: String, ref: String? = null
    ): Result<FileContent> = withContext(Dispatchers.IO) {
        try {
            val response = gitHubApi.getFileContent(owner, repo, path, authHeader(settings), ref)
            if (response.isSuccessful) {
                val item = response.body()!!
                val decodedContent = when {
                    item.isFile && item.content != null -> decodeBase64(item.content)
                    item.isFile && item.downloadUrl != null ->
                        getRawFileContent(settings, item.downloadUrl).getOrDefault("")
                    else -> ""
                }
                Result.success(FileContent(
                    path = item.path, name = item.name, content = decodedContent,
                    sha = item.sha, url = item.htmlUrl, size = item.size
                ))
            } else {
                Result.failure(Exception("获取文件内容失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}", e))
        }
    }

    suspend fun getRawFileContent(
        settings: GitHubSettings, downloadUrl: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val client = okhttp3.OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(downloadUrl)
                .header("Authorization", authHeader(settings))
                .build()
            val resp = client.newCall(request).execute()
            val bodyStr = resp.body?.string() ?: ""
            if (resp.isSuccessful) Result.success(bodyStr)
            else Result.failure(Exception("获取原始文件失败: ${resp.code}"))
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}", e))
        }
    }

    // --- 目录内容 ---

    suspend fun getDirectoryContents(
        settings: GitHubSettings, owner: String, repo: String, path: String, ref: String? = null
    ): Result<List<GitHubContentItem>> = withContext(Dispatchers.IO) {
        try {
            val response = gitHubApi.getContents(owner, repo, path, authHeader(settings), ref)
            if (response.isSuccessful) {
                val items = response.body() ?: emptyList()
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
        settings: GitHubSettings, owner: String, repo: String, path: String,
        content: String, commitMessage: String, sha: String? = null, branch: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val base64Content = Base64.encodeToString(
                content.toByteArray(Charsets.UTF_8), Base64.NO_WRAP
            )
            val request = com.mobilecodex.data.api.CreateFileRequest(
                message = commitMessage, content = base64Content, sha = sha, branch = branch
            )
            val response = gitHubApi.createOrUpdateFile(
                owner, repo, path, authHeader(settings), request
            )
            if (response.isSuccessful) {
                Result.success(response.body()?.content?.sha ?: "")
            } else {
                val errorBody = response.errorBody()?.string() ?: ""
                Result.failure(Exception("提交失败 ${response.code()}: $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}", e))
        }
    }

    suspend fun deleteFile(
        settings: GitHubSettings, owner: String, repo: String, path: String,
        sha: String, commitMessage: String, branch: String? = null
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val request = com.mobilecodex.data.api.DeleteFileRequest(
                message = commitMessage, sha = sha, branch = branch
            )
            val response = gitHubApi.deleteFile(owner, repo, path, authHeader(settings), request)
            if (response.isSuccessful) Result.success(true)
            else Result.failure(Exception("删除失败 ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(Exception("网络错误: ${e.message}", e))
        }
    }

    private fun decodeBase64(encoded: String): String = try {
        String(Base64.decode(encoded, Base64.DEFAULT), Charsets.UTF_8)
    } catch (_: Exception) { "" }
}
