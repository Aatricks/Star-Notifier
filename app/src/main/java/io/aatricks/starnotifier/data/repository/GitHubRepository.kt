package io.aatricks.starnotifier.data.repository

import android.util.Log
import io.aatricks.starnotifier.data.local.SharedPreferencesStorage
import io.aatricks.starnotifier.data.model.GitHubRepoResponse
import io.aatricks.starnotifier.data.model.Repository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

private data class TrafficData(
    val views: Int,
    val clones: Int,
    val lifetimeViews: Int,
    val lifetimeClones: Int
)

class GitHubRepository(
    private val apiService: GitHubApiService,
    private val storage: SharedPreferencesStorage? = null
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
                            val trafficData = if (token != null) {
                                try {
                                    val owner = apiRepo.full_name.substringBefore('/')
                                    val repo = apiRepo.full_name.substringAfter('/')
                                    Log.d("GitHubRepository", "Fetching traffic for ${apiRepo.full_name}")
                                    val viewsData = apiService.getRepoViews(owner, repo, authToken)
                                    val clonesData = apiService.getRepoClones(owner, repo, authToken)
                                    Log.d("GitHubRepository", "${apiRepo.full_name}: views=${viewsData.count}, clones=${clonesData.count}")
                                    
                                    // Get stored data to accumulate lifetime totals
                                    val storedRepo = storage?.getRepositoryData(apiRepo.full_name)?.getOrNull()
                                    val previousLifetimeViews = storedRepo?.lifetimeViews ?: 0
                                    val previousLifetimeClones = storedRepo?.lifetimeClones ?: 0
                                    val previousViews = storedRepo?.totalViews ?: 0
                                    val previousClones = storedRepo?.totalClones ?: 0
                                    
                                    // Calculate new data since last check
                                    val newViews = maxOf(0, viewsData.count - previousViews)
                                    val newClones = maxOf(0, clonesData.count - previousClones)
                                    
                                    // Accumulate lifetime totals
                                    val updatedLifetimeViews = previousLifetimeViews + newViews
                                    val updatedLifetimeClones = previousLifetimeClones + newClones
                                    
                                    Log.d("GitHubRepository", "${apiRepo.full_name}: new views=$newViews, new clones=$newClones, lifetime views=$updatedLifetimeViews, lifetime clones=$updatedLifetimeClones")
                                    
                                    TrafficData(viewsData.count, clonesData.count, updatedLifetimeViews, updatedLifetimeClones)
                                } catch (e: Exception) {
                                    // Traffic data requires push access, may fail for some repos
                                    Log.e("GitHubRepository", "Failed to fetch traffic for ${apiRepo.full_name}: ${e.message}")
                                    val storedRepo = storage?.getRepositoryData(apiRepo.full_name)?.getOrNull()
                                    TrafficData(0, 0, storedRepo?.lifetimeViews ?: 0, storedRepo?.lifetimeClones ?: 0)
                                }
                            } else {
                                Log.d("GitHubRepository", "No token provided, skipping traffic data")
                                val storedRepo = storage?.getRepositoryData(apiRepo.full_name)?.getOrNull()
                                TrafficData(0, 0, storedRepo?.lifetimeViews ?: 0, storedRepo?.lifetimeClones ?: 0)
                            }
                            
                            Repository(
                                name = apiRepo.full_name,
                                currentStars = apiRepo.stargazers_count,
                                currentForks = apiRepo.forks_count,
                                totalViews = trafficData.views,
                                totalClones = trafficData.clones,
                                lifetimeViews = trafficData.lifetimeViews,
                                lifetimeClones = trafficData.lifetimeClones,
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