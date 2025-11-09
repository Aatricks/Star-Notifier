package io.aatricks.starnotifier.data.repository

import android.util.Log
import io.aatricks.starnotifier.data.local.SharedPreferencesStorage
import io.aatricks.starnotifier.data.model.GitHubRepoResponse
import io.aatricks.starnotifier.data.model.Repository
import io.aatricks.starnotifier.data.model.TrafficEntry
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

private data class TrafficData(
    val views: Int,
    val clones: Int,
    val lifetimeViews: Int,
    val lifetimeClones: Int,
    val twoWeekViews: Int,
    val twoWeekClones: Int,
    val viewsData: List<TrafficEntry>,
    val clonesData: List<TrafficEntry>
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
                                    val previousViews = storedRepo?.totalViews ?: 0
                                    val previousClones = storedRepo?.totalClones ?: 0
                                    
                                    // Calculate two-week totals from the views array
                                    val twoWeekViewsTotal = viewsData.views.sumOf { it.count }
                                    val twoWeekClonesTotal = clonesData.clones.sumOf { it.count }
                                    
                                    // Calculate lifetime totals
                                    val updatedLifetimeViews: Int
                                    val updatedLifetimeClones: Int
                                    val newViews: Int
                                    val newClones: Int
                                    
                                    if (storedRepo == null) {
                                        // First time tracking this repo - initialize lifetime with current 2-week total
                                        updatedLifetimeViews = twoWeekViewsTotal
                                        updatedLifetimeClones = twoWeekClonesTotal
                                        newViews = twoWeekViewsTotal
                                        newClones = twoWeekClonesTotal
                                        Log.d("GitHubRepository", "${apiRepo.full_name}: First tracking - initializing lifetime from 2-week data")
                                    } else {
                                        // Calculate new data since last check
                                        newViews = maxOf(0, viewsData.count - previousViews)
                                        newClones = maxOf(0, clonesData.count - previousClones)
                                        
                                        // Accumulate lifetime totals
                                        updatedLifetimeViews = storedRepo.lifetimeViews + newViews
                                        updatedLifetimeClones = storedRepo.lifetimeClones + newClones
                                    }
                                    
                                    Log.d("GitHubRepository", "${apiRepo.full_name}: new views=$newViews, new clones=$newClones, lifetime views=$updatedLifetimeViews, lifetime clones=$updatedLifetimeClones, 2wk views=$twoWeekViewsTotal, 2wk clones=$twoWeekClonesTotal")
                                    
                                    TrafficData(
                                        viewsData.count, 
                                        clonesData.count, 
                                        updatedLifetimeViews, 
                                        updatedLifetimeClones,
                                        twoWeekViewsTotal,
                                        twoWeekClonesTotal,
                                        viewsData.views,
                                        clonesData.clones
                                    )
                                } catch (e: Exception) {
                                    // Traffic data requires push access, may fail for some repos
                                    Log.e("GitHubRepository", "Failed to fetch traffic for ${apiRepo.full_name}: ${e.message}")
                                    val storedRepo = storage?.getRepositoryData(apiRepo.full_name)?.getOrNull()
                                    TrafficData(
                                        0, 
                                        0, 
                                        storedRepo?.lifetimeViews ?: 0, 
                                        storedRepo?.lifetimeClones ?: 0,
                                        0,
                                        0,
                                        emptyList(),
                                        emptyList()
                                    )
                                }
                            } else {
                                Log.d("GitHubRepository", "No token provided, skipping traffic data")
                                val storedRepo = storage?.getRepositoryData(apiRepo.full_name)?.getOrNull()
                                TrafficData(
                                    0, 
                                    0, 
                                    storedRepo?.lifetimeViews ?: 0, 
                                    storedRepo?.lifetimeClones ?: 0,
                                    0,
                                    0,
                                    emptyList(),
                                    emptyList()
                                )
                            }
                            
                            val repo = Repository(
                                name = apiRepo.full_name,
                                currentStars = apiRepo.stargazers_count,
                                currentForks = apiRepo.forks_count,
                                totalViews = trafficData.views,
                                totalClones = trafficData.clones,
                                lifetimeViews = trafficData.lifetimeViews,
                                lifetimeClones = trafficData.lifetimeClones,
                                twoWeekViews = trafficData.twoWeekViews,
                                twoWeekClones = trafficData.twoWeekClones,
                                viewsData = trafficData.viewsData,
                                clonesData = trafficData.clonesData,
                                lastChecked = System.currentTimeMillis(),
                                isSelected = selectedRepos.contains(apiRepo.full_name)
                            )
                            
                            // Save repository data to persist lifetime tracking
                            storage?.saveRepositoryData(repo)
                            
                            repo
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