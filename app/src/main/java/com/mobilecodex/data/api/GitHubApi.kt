package com.mobilecodex.data.api

import retrofit2.Response
import retrofit2.http.*

/**
 * GitHub REST API 接口
 */
interface GitHubApi {
    
    /**
     * 获取当前用户信息
     */
    @GET("user")
    suspend fun getCurrentUser(): Response<GitHubUserResponse>
    
    /**
     * 获取用户仓库列表
     */
    @GET("user/repos")
    suspend fun getUserRepositories(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30,
        @Query("sort") sort: String = "updated",
        @Query("direction") direction: String = "desc"
    ): Response<List<RepositoryResponse>>
    
    /**
     * 获取仓库信息
     */
    @GET("repos/{owner}/{repo}")
    suspend fun getRepository(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): Response<RepositoryResponse>
    
    /**
     * 获取文件树
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
     * 获取引用信息
     */
    @GET("repos/{owner}/{repo}/git/ref/{ref}")
    suspend fun getRef(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("ref") ref: String
    ): Response<RefResponse>
    
    /**
     * 获取提交信息
     */
    @GET("repos/{owner}/{repo}/git/commits/{sha}")
    suspend fun getCommit(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("sha") sha: String
    ): Response<CommitResponse>
    
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
     * 更新引用
     */
    @PATCH("repos/{owner}/{repo}/git/ref/{ref}")
    suspend fun updateRef(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("ref") ref: String,
        @Body body: UpdateRefRequest
    ): Response<RefResponse>
}

// ==================== GitHub API 响应数据类 ====================

/**
 * GitHub 用户响应
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
    val following: Int
)

/**
 * 仓库响应
 */
data class RepositoryResponse(
    val id: Long,
    val name: String,
    val full_name: String,
    val owner: OwnerResponse,
    val description: String?,
    val default_branch: String,
    val private: Boolean,
    val fork: Boolean,
    val language: String?,
    val stargazers_count: Int,
    val watchers_count: Int,
    val forks_count: Int,
    val updated_at: String,
    val html_url: String
)

/**
 * 所有者响应
 */
data class OwnerResponse(
    val login: String,
    val id: Long,
    val avatar_url: String,
    val type: String
)

/**
 * 文件树响应
 */
data class TreeResponse(
    val sha: String,
    val url: String,
    val tree: List<TreeNodeResponse>,
    val truncated: Boolean
)

/**
 * 树节点响应
 */
data class TreeNodeResponse(
    val path: String,
    val mode: String,
    val type: String,
    val sha: String,
    val size: Long?,
    val url: String
)

/**
 * 文件内容响应
 */
data class FileContentResponse(
    val name: String,
    val path: String,
    val sha: String,
    val size: Long,
    val url: String,
    val html_url: String,
    val git_url: String,
    val download_url: String,
    val type: String,
    val content: String?,
    val encoding: String?
)

/**
 * 提交响应
 */
data class CommitResponse(
    val sha: String,
    val url: String,
    val author: CommitAuthorResponse,
    val committer: CommitAuthorResponse,
    val message: String,
    val tree: TreeRefResponse,
    val parents: List<TreeRefResponse>
)

/**
 * 提交作者响应
 */
data class CommitAuthorResponse(
    val name: String,
    val email: String,
    val date: String
)

/**
 * Tree 引用响应
 */
data class TreeRefResponse(
    val sha: String,
    val url: String
)