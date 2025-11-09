package io.aatricks.starnotifier.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import io.aatricks.starnotifier.data.local.SharedPreferencesStorage
import io.aatricks.starnotifier.data.repository.GitHubApiService
import io.aatricks.starnotifier.data.repository.GitHubRepository
import io.aatricks.starnotifier.notification.NotificationHelper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GitHubCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val notificationHelper = NotificationHelper(appContext)
    private val sharedPreferencesStorage = SharedPreferencesStorage(appContext)
    private val gson = Gson()
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
    private val apiService = retrofit.create(GitHubApiService::class.java)
    private val gitHubRepository = GitHubRepository(apiService, sharedPreferencesStorage)

    override suspend fun doWork(): Result {
        return try {
            // Get user config
            val userConfig = sharedPreferencesStorage.getUserConfig().getOrNull()
                ?: return Result.success() // No config, nothing to do

            // Get selected repos
            val selectedRepos = sharedPreferencesStorage.getAllSelectedRepos().getOrNull()
                ?: emptyList()

            if (selectedRepos.isEmpty()) return Result.success()

            // Fetch current data from GitHub
            val currentRepos = gitHubRepository.getUserRepositories(
                userConfig.username,
                userConfig.personalAccessToken
            ).getOrNull() ?: return Result.failure()

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
            Result.failure()
        }
    }
}