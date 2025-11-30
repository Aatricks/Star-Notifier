package io.aatricks.starnotifier.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import io.aatricks.starnotifier.data.local.LocalStorageRepository
import io.aatricks.starnotifier.data.local.SharedPreferencesStorage
import io.aatricks.starnotifier.data.repository.GitHubApiService
import io.aatricks.starnotifier.data.repository.GitHubRepository
import io.aatricks.starnotifier.notification.NotificationHelper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.HttpException
import java.io.IOException

class GitHubCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val gitHubRepositoryParam: GitHubRepository? = null,
    private val localStorageRepositoryParam: LocalStorageRepository? = null,
    private val notificationHelperParam: NotificationHelper? = null
) : CoroutineWorker(appContext, workerParams) {

    private val notificationHelper = notificationHelperParam ?: NotificationHelper(appContext)
    private val sharedPreferencesStorage: LocalStorageRepository =
        localStorageRepositoryParam ?: SharedPreferencesStorage(appContext)
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
    private val gitHubRepository =
        gitHubRepositoryParam ?: GitHubRepository(apiService, sharedPreferencesStorage as? SharedPreferencesStorage)

    companion object {
        private const val TAG = "GitHubCheckWorker"
    }

    init {
        Log.d(
            TAG,
            "Initialized. useInjectedGitHubRepository=${gitHubRepositoryParam != null}, useInjectedLocalStorage=${localStorageRepositoryParam != null}, useInjectedNotificationHelper=${notificationHelperParam != null}"
        )
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting doWork")

        return try {
            // Get user config
            val userConfig = sharedPreferencesStorage.getUserConfig().getOrNull()
            if (userConfig == null) {
                Log.d(TAG, "No user config found - skipping work")
                return Result.success() // No config, nothing to do
            }

            // Get selected repos
            val selectedRepos = sharedPreferencesStorage.getAllSelectedRepos().getOrNull()
                ?: emptyList()

            if (selectedRepos.isEmpty()) {
                Log.d(TAG, "No selected repositories found - skipping work")
                return Result.success()
            }

            Log.d(
                TAG,
                "Found ${selectedRepos.size} selected repositories: ${selectedRepos.joinToString(",")}"
            )

            // Fetch current data from GitHub
            val repoResult = gitHubRepository.getUserRepositories(
                userConfig.username,
                userConfig.personalAccessToken
            )

            val currentRepos = repoResult.getOrNull()
            if (currentRepos == null) {
                val ex = repoResult.exceptionOrNull()
                ex?.let {
                    Log.e(TAG, "Failed to fetch repositories from GitHub: ${it.message}", it)
                } ?: Log.e(TAG, "Failed to fetch repositories from GitHub for unknown reason")

                // Choose retry vs permanent failure based on the exception type / HTTP code
                return when (ex) {
                    is IOException -> {
                        Log.d(TAG, "Retrying due to network error")
                        Result.retry()
                    }

                    is HttpException -> {
                        val code = ex.code()
                        if (code == 429 || code >= 500) {
                            Log.d(TAG, "Retrying due to server error (HTTP $code)")
                            Result.retry()
                        } else {
                            Log.d(TAG, "Failing due to client HTTP error (HTTP $code)")
                            Result.failure()
                        }
                    }

                    else -> {
                        Log.d(TAG, "Retrying due to unknown error (default to retry)")
                        Result.retry()
                    }
                }
            }

            Log.d(TAG, "Fetched ${currentRepos.size} repository entries from GitHub")

            // Check for changes
            for (repoName in selectedRepos) {
                Log.d(TAG, "Checking selected repo: $repoName")
                val currentRepo = currentRepos.find { it.name == repoName }
                if (currentRepo != null) {
                    Log.d(
                        TAG,
                        "Current data for ${currentRepo.name}: stars=${currentRepo.currentStars}, forks=${currentRepo.currentForks}"
                    )

                    val storedRepo = sharedPreferencesStorage.getRepositoryData(repoName).getOrNull()
                    if (storedRepo != null) {
                        Log.d(
                            TAG,
                            "Stored data for ${repoName}: stars=${storedRepo.currentStars}, forks=${storedRepo.currentForks}"
                        )

                        // Check for star changes
                        if (currentRepo.currentStars > storedRepo.currentStars) {
                            Log.d(
                                "GitHubCheckWorker",
                                "Star increase detected for ${repoName}: ${storedRepo.currentStars} -> ${currentRepo.currentStars}"
                            )
                            notificationHelper.sendStarNotification(repoName, currentRepo.currentStars)
                        } else {
                            Log.d(TAG, "No star change for ${repoName}")
                        }

                        // Check for fork changes
                        if (currentRepo.currentForks > storedRepo.currentForks) {
                            Log.d(
                                "GitHubCheckWorker",
                                "Fork increase detected for ${repoName}: ${storedRepo.currentForks} -> ${currentRepo.currentForks}"
                            )
                            notificationHelper.sendForkNotification(repoName, currentRepo.currentForks)
                        } else {
                            Log.d(TAG, "No fork change for ${repoName}")
                        }
                    } else {
                        Log.d(TAG, "No stored data for ${repoName}; initializing stored entry")
                    }

                    // Update stored data
                    sharedPreferencesStorage.saveRepositoryData(currentRepo)
                    Log.d(TAG, "Saved updated data for ${currentRepo.name}")
                } else {
                    Log.d(TAG, "Selected repo ${repoName} not found in fetched repo list")
                }
            }

            Log.d(TAG, "All selected repositories processed successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error while running doWork: ${e.message}", e)
            return when (e) {
                is IOException -> {
                    Log.d(TAG, "Retrying due to IO exception: ${e.message}")
                    Result.retry()
                }

                is HttpException -> {
                    val code = e.code()
                    if (code == 429 || code >= 500) {
                        Log.d(TAG, "Retrying due to HTTP server error: $code")
                        Result.retry()
                    } else {
                        Log.d(TAG, "Permanent HTTP error (HTTP $code), failing")
                        Result.failure()
                    }
                }

                else -> {
                    Log.d(TAG, "Retrying due to unknown exception type (defaulting to retry).")
                    Result.retry()
                }
            }
        }
    }
}
