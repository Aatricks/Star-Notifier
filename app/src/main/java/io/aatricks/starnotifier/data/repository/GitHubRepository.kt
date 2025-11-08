package io.aatricks.starnotifier.data.repository

import io.aatricks.starnotifier.data.model.GitHubRepoResponse
import io.aatricks.starnotifier.data.model.Repository

class GitHubRepository(
    private val apiService: GitHubApiService
) {
    suspend fun getUserRepositories(username: String, token: String?, selectedRepos: List<String> = emptyList()): Result<List<Repository>> {
        return try {
            val response = apiService.getUserRepositories(username, token?.let { "token $it" })
            val repositories = response
                .filter { !it.private } // Only public repos
                .map { apiRepo ->
                    Repository(
                        name = apiRepo.full_name,
                        currentStars = apiRepo.stargazers_count,
                        currentForks = apiRepo.forks_count,
                        lastChecked = System.currentTimeMillis(),
                        isSelected = selectedRepos.contains(apiRepo.full_name)
                    )
                }
            Result.success(repositories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}