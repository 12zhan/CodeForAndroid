package com.mobilecodex.data.api

import retrofit2.Response
import retrofit2.http.*

/**
 * GitHub REST API 接口
 * 用于仓库操作、文件管理等功能
 */
interface GitHubApi {

    // ==================== 用户 ====================

    /**
     * 获取当前认证用户信息
     */
    @GET("user")
    suspend fun getCurrentUser(): Response<GitHubUserResponse>

    // ==================== 仓库 ====================

    /**
     * 获取当前用户的仓库列表
     */
    @GET("user/repos")
    suspend fun getUserRepositories(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
        @Query("sort") sort: String = "updated",
        @Query("direction") direction: String = "desc"
    ): Response<List<RepositoryResponse>>

    /**
     * 获取单个仓库详细信息
     */
    @GET("repos/{owner}/{repo}")
    suspend fun getRepository(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<RepositoryDetailResponse>

    // ==================== Git 数据 ====================

    /**
     * 获取 Git 树
     */
    @GET("repos/{owner}/{repo}/git/trees/{branch}")
    suspend fun getTree(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("branch") branch: String,
        @Query("recursive") recursive: Int? = null
    ): Response<TreeResponse>

    /**
     * 获取文件内容
     */
    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getFileContent(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Query("ref") ref: String? = null
    ): Response<FileContentResponse>

    /**
     * 创建 Blob
     */
    @POST("repos/{owner}/{repo}/git/blobs")
    suspend fun createBlob(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body body: CreateBlobRequest
    ): Response<CreateBlobResponse>

    /**
     * 创建 Tree
     */
    @POST("repos/{owner}/{repo}/git/trees")
    suspend fun createTree(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body body: CreateTreeRequest
    ): Response<CreateTreeResponse>

    /**
     * 创建 Commit
     */
    @POST("repos/{owner}/{repo}/git/commits")
    suspend fun createCommit(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body body: CreateCommitRequest
    ): Response<CreateCommitResponse>

    /**
     * 获取 Ref
     */
    @GET("repos/{owner}/{repo}/git/ref/{ref}")
    suspend fun getRef(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("ref") ref: String
    ): Response<RefResponse>

    /**
     * 更新 Ref
     */
    @PATCH("repos/{owner}/{repo}/git/refs/{ref}")
    suspend fun updateRef(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("ref") ref: String,
        @Body body: UpdateRefRequest
    ): Response<RefResponse>

    /**
     * 获取 Commit 详情
     */
    @GET("repos/{owner}/{repo}/git/commits/{sha}")
    suspend fun getCommit(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("sha") sha: String
    ): Response<CommitDetailResponse>
}

// ==================== 请求数据类 ====================

/**
 * 创建 Blob 请求
 */
data class CreateBlobRequest(
    val content: String,
    val encoding: String = "utf-8"
)

/**
 * 创建 Blob 响应
 */
data class CreateBlobResponse(
    val sha: String,
    val url: String
)

/**
 * Tree 节点（用于创建 Tree 请求）
 */
data class TreeEntry(
    val path: String,
    val mode: String,
    val type: String,
    val sha: String,
    val size: Long? = null
)

/**
 * 创建 Tree 请求
 */
data class CreateTreeRequest(
    val base_tree: String?,
    val tree: List<TreeEntry>
)

/**
 * 创建 Tree 响应
 */
data class CreateTreeResponse(
    val sha: String,
    val url: String
)

/**
 * Commit 作者
 */
data class CommitAuthor(
    val name: String,
    val email: String,
    val date: String? = null
)

/**
 * 创建 Commit 请求
 */
data class CreateCommitRequest(
    val message: String,
    val tree: String,
    val parents: List<String>,
    val author: CommitAuthor? = null,
    val committer: CommitAuthor? = null
)

/**
 * Tree 引用
 */
data class TreeRef(
    val sha: String,
    val url: String
)

/**
 * 创建 Commit 响应
 */
data class CreateCommitResponse(
    val sha: String,
    val url: String,
    val author: CommitAuthor,
    val committer: CommitAuthor,
    val message: String,
    val tree: TreeRef,
    val parents: List<TreeRef>
)

/**
 * 更新 Ref 请求
 */
data class UpdateRefRequest(
    val sha: String,
    val force: Boolean = false
)

// ==================== 响应数据类 ====================

/**
 * GitHub 用户 API 响应
 */
data class GitHubUserResponse(
    val login: String,
    val id: Long,
    val avatar_url: String,
    val name: String?,
    val email: String?,
    val bio: String?,
    val public_repos: Int,
    val followers: Int,
    val following: Int,
    val html_url: String = ""
)

/**
 * 仓库 API 响应
 */
data class RepositoryResponse(
    val id: Long,
    val name: String,
    val full_name: String,
    val description: String?,
    val `private`: Boolean,
    val fork: Boolean,
    val html_url: String,
    val default_branch: String,
    val language: String?,
    val stargazers_count: Int,
    val forks_count: Int,
    val updated_at: String
)

/**
 * 仓库详情 API 响应
 */
data class RepositoryDetailResponse(
    val id: Long,
    val name: String,
    val full_name: String,
    val description: String?,
    val `private`: Boolean,
    val fork: Boolean,
    val html_url: String,
    val default_branch: String,
    val language: String?,
    val stargazers_count: Int,
    val forks_count: Int,
    val updated_at: String
)

/**
 * Tree API 响应
 */
data class TreeResponse(
    val sha: String,
    val url: String,
    val tree: List<TreeNodeResponse>,
    val truncated: Boolean
)

/**
 * Tree 节点 API 响应
 */
data class TreeNodeResponse(
    val path: String,
    val mode: String,
    val type: String,
    val sha: String,
    val size: Long?,
    val url: String?
)

/**
 * 文件内容 API 响应
 */
data class FileContentResponse(
    val name: String,
    val path: String,
    val sha: String,
    val size: Long,
    val content: String,
    val encoding: String,
    val type: String
)

/**
 * Ref API 响应
 */
data class RefResponse(
    val ref: String,
    val node_id: String,
    val url: String,
    val `object`: RefObject
)

/**
 * Ref 对象
 */
data class RefObject(
    val sha: String,
    val type: String,
    val url: String
)

/**
 * Commit 详情 API 响应
 */
data class CommitDetailResponse(
    val sha: String,
    val url: String,
    val author: CommitAuthor,
    val committer: CommitAuthor,
    val message: String,
    val tree: TreeRef,
    val parents: List<TreeRef>
)
