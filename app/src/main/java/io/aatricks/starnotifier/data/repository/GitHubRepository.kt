package io.aatricks.starnotifier.data.repository

import android.util.Log
import io.aatricks.starnotifier.data.model.GitHubRepoResponse
import io.aatricks.starnotifier.data.model.Repository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class GitHubRepository(
    private val apiService: GitHubApiService
) {
    suspend fun getUserRepositories(username: String, token: String?, selectedRepos: List<String> = emptyList()): Result<List<Repository>> {
        return try {
            val authToken = token?.let { "token $it" }
            val response = apiService.getUserRepositories(username, authToken)
            
            val repositories = coroutineScope {
                response
                    .filter { !it.private } // Only public repos
                    .map { apiRepo ->
                        async {
                            // Fetch traffic data for each repository (requires token)
                            val (views, clones) = if (token != null) {
                                try {
                                    val owner = apiRepo.full_name.substringBefore('/')
                                    val repo = apiRepo.full_name.substringAfter('/')
                                    Log.d("GitHubRepository", "Fetching traffic for ${apiRepo.full_name}")
                                    val viewsData = apiService.getRepoViews(owner, repo, authToken)
                                    val clonesData = apiService.getRepoClones(owner, repo, authToken)
                                    Log.d("GitHubRepository", "${apiRepo.full_name}: views=${viewsData.count}, clones=${clonesData.count}")
                                    Pair(viewsData.count, clonesData.count)
                                } catch (e: Exception) {
                                    // Traffic data requires push access, may fail for some repos
                                    Log.e("GitHubRepository", "Failed to fetch traffic for ${apiRepo.full_name}: ${e.message}")
                                    Pair(0, 0)
                                }
                            } else {
                                Log.d("GitHubRepository", "No token provided, skipping traffic data")
                                Pair(0, 0)
                            }
                            
                            Repository(
                                name = apiRepo.full_name,
                                currentStars = apiRepo.stargazers_count,
                                currentForks = apiRepo.forks_count,
                                totalViews = views,
                                totalClones = clones,
                                lastChecked = System.currentTimeMillis(),
                                isSelected = selectedRepos.contains(apiRepo.full_name)
                            )
                        }
                    }
                    .awaitAll()
            }
            
            Result.success(repositories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}