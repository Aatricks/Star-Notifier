package io.aatricks.starnotifier.data.repository

import io.aatricks.starnotifier.data.model.GitHubRepoResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface GitHubApiService {
    @GET("users/{username}/repos")
    suspend fun getUserRepositories(
        @Path("username") username: String,
        @Header("Authorization") token: String? = null
    ): List<GitHubRepoResponse>
}