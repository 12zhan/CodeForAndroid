package com.mobilecodex.data.api

import com.mobilecodex.model.GitHubContentItem
import com.mobilecodex.model.GitHubRepository
import com.mobilecodex.model.GitHubUser
import com.mobilecodex.model.GitTreeNode
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.*

/**
 * GitHub REST API
 */
interface GitHubApi {

    // --- 用户 ---

    @GET("user")
    suspend fun getAuthenticatedUser(
        @Header("Authorization") authorization: String
    ): Response<GitHubUser>

    @GET("users/{username}")
    suspend fun getUser(
        @Path("username") username: String,
        @Header("Authorization") authorization: String
    ): Response<GitHubUser>

    // --- 仓库 ---

    @GET("user/repos")
    suspend fun listMyRepos(
        @Header("Authorization") authorization: String,
        @Query("sort") sort: String = "updated",
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): Response<List<GitHubRepository>>

    @GET("users/{username}/repos")
    suspend fun listUserRepos(
        @Path("username") username: String,
        @Header("Authorization") authorization: String,
        @Query("sort") sort: String = "updated",
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1
    ): Response<List<GitHubRepository>>

    @GET("repos/{owner}/{repo}")
    suspend fun getRepository(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Header("Authorization") authorization: String
    ): Response<GitHubRepository>

    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Header("Authorization") authorization: String,
        @Query("sort") sort: String = "stars",
        @Query("per_page") perPage: Int = 20
    ): Response<SearchRepoResponse>

    // --- 目录 & 文件 ---

    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getContents(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Header("Authorization") authorization: String,
        @Query("ref") ref: String? = null
    ): Response<List<GitHubContentItem>>

    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getFileContent(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Header("Authorization") authorization: String,
        @Query("ref") ref: String? = null
    ): Response<GitHubContentItem>

    @GET("repos/{owner}/{repo}/git/trees/{sha}")
    suspend fun getTree(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("sha") sha: String,
        @Header("Authorization") authorization: String,
        @Query("recursive") recursive: Int = 1
    ): Response<GitTreeResponse>

    // --- 分支 ---

    @GET("repos/{owner}/{repo}/branches")
    suspend fun listBranches(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Header("Authorization") authorization: String
    ): Response<List<BranchDto>>

    @GET("repos/{owner}/{repo}")
    suspend fun getRepoWithBranch(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Header("Authorization") authorization: String
    ): Response<GitHubRepository>

    // --- 文件操作 ---

    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun createOrUpdateFile(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Header("Authorization") authorization: String,
        @Body body: CreateFileRequest
    ): Response<FileOperationResponse>

    @DELETE("repos/{owner}/{repo}/contents/{path}")
    suspend fun deleteFile(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Header("Authorization") authorization: String,
        @Body body: DeleteFileRequest
    ): Response<FileOperationResponse>
}

// --- Response DTOs ---

data class SearchRepoResponse(
    @SerializedName("total_count") val totalCount: Int,
    val items: List<GitHubRepository>
)

data class GitTreeResponse(
    val sha: String,
    val url: String,
    val tree: List<GitTreeNode>,
    val truncated: Boolean
)

data class BranchDto(
    val name: String,
    @SerializedName("commit") val commit: BranchCommitDto,
    @SerializedName("protected") val isProtected: Boolean
)

data class BranchCommitDto(
    val sha: String,
    val url: String
)

// --- 文件操作 DTOs ---

data class CreateFileRequest(
    val message: String,
    val content: String,           // Base64 编码的文件内容
    val sha: String? = null,       // 更新文件时需要提供 blob SHA
    val branch: String? = null,
    val committer: CommitterDto? = null
)

data class DeleteFileRequest(
    val message: String,
    val sha: String,               // 删除文件时必须提供 blob SHA
    val branch: String? = null
)

data class CommitterDto(
    val name: String,
    val email: String
)

data class FileOperationResponse(
    val content: FileOperationContent? = null,
    val commit: FileOperationCommit? = null
)

data class FileOperationContent(
    val name: String,
    val path: String,
    val sha: String,
    val size: Long?,
    val url: String?,
    @com.google.gson.annotations.SerializedName("html_url") val htmlUrl: String?,
    @com.google.gson.annotations.SerializedName("git_url") val gitUrl: String?
)

data class FileOperationCommit(
    val sha: String,
    @com.google.gson.annotations.SerializedName("html_url") val htmlUrl: String?,
    val author: CommitAuthorDto?,
    val committer: CommitAuthorDto?,
    val message: String?
)

data class CommitAuthorDto(
    val name: String,
    val email: String,
    val date: String?
)
