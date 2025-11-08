package io.aatricks.starnotifier.data.repository

import io.aatricks.starnotifier.data.model.GitHubRepoResponse
import io.aatricks.starnotifier.data.model.GitHubTrafficViews
import io.aatricks.starnotifier.data.model.GitHubTrafficClones
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface GitHubApiService {
    @GET("users/{username}/repos")
    suspend fun getUserRepositories(
        @Path("username") username: String,
        @Header("Authorization") token: String? = null
    ): List<GitHubRepoResponse>
    
    @GET("repos/{owner}/{repo}/traffic/views")
    suspend fun getRepoViews(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Header("Authorization") token: String? = null
    ): GitHubTrafficViews
    
    @GET("repos/{owner}/{repo}/traffic/clones")
    suspend fun getRepoClones(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Header("Authorization") token: String? = null
    ): GitHubTrafficClones
}