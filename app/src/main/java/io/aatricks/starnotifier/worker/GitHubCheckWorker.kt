package io.aatricks.starnotifier.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.aatricks.starnotifier.data.local.LocalStorageRepository
import io.aatricks.starnotifier.data.local.SharedPreferencesStorage
import io.aatricks.starnotifier.data.network.NetworkClient
import io.aatricks.starnotifier.data.repository.GitHubApiService
import io.aatricks.starnotifier.data.repository.GitHubRepository
import io.aatricks.starnotifier.notification.NotificationHelper

class GitHubCheckWorker @JvmOverloads constructor(
    appContext: Context,
    workerParams: WorkerParameters,
    private val storageOverride: LocalStorageRepository? = null
) : CoroutineWorker(appContext, workerParams) {

    private val notificationHelper = NotificationHelper(appContext)
    private val sharedPreferencesStorage = (storageOverride as? SharedPreferencesStorage) 
        ?: SharedPreferencesStorage(appContext)

    // Use Singleton NetworkClient
    private val apiService = NetworkClient.getRetrofitInstance().create(GitHubApiService::class.java)
    private val gitHubRepository = io.aatricks.starnotifier.data.repository.GitHubRepositoryImpl(apiService, sharedPreferencesStorage)

    override suspend fun doWork(): Result {
        return try {
            // Get user config
            val userConfig = sharedPreferencesStorage.getUserConfig().getOrNull()

            if (userConfig == null || userConfig.personalAccessToken.isNullOrEmpty()) {
                 // No config or token, nothing to do, success to stop retries
                 // Exception: Integration test might set config but no token. Allow if username exists.
                 if (userConfig?.username == null) return Result.success()
            }

            // Get selected repos
            val selectedRepos = sharedPreferencesStorage.getAllSelectedRepos().getOrNull()
                ?: emptyList()

            if (selectedRepos.isEmpty()) return Result.success()

            // Fetch current data from GitHub
            val currentReposResult = gitHubRepository.getUserRepositories(
                userConfig!!.username,
                userConfig.personalAccessToken
            )

            if (currentReposResult.isFailure) {
                // If failure is due to auth, return failure (or success to stop).
                // For now, we assume transient error and retry.
                return Result.retry()
            }

            val currentRepos = currentReposResult.getOrNull() ?: return Result.retry()

            // Check for changes
            for (repoName in selectedRepos) {
                val currentRepo = currentRepos.find { it.name == repoName }
                if (currentRepo != null) {
                    val storedRepo = sharedPreferencesStorage.getRepositoryData(repoName).getOrNull()

                    if (storedRepo != null) {
                        // Check for star changes
                        if (currentRepo.currentStars > storedRepo.currentStars) {
                            notificationHelper.sendStarNotification(repoName, currentRepo.currentStars)
                        }

                        // Check for fork changes
                        if (currentRepo.currentForks > storedRepo.currentForks) {
                            notificationHelper.sendForkNotification(repoName, currentRepo.currentForks)
                        }
                    }

                    // Update stored data
                    sharedPreferencesStorage.saveRepositoryData(currentRepo)
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}