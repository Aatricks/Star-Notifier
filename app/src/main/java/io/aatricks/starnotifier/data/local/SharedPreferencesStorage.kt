package io.aatricks.starnotifier.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import io.aatricks.starnotifier.data.model.Repository
import io.aatricks.starnotifier.data.model.UserConfig

class SharedPreferencesStorage(
    private val context: Context,
    private val gson: Gson = Gson(),
    private val useEncryption: Boolean = true
) : LocalStorageRepository {

    private val prefs: SharedPreferences by lazy {
        if (!useEncryption) {
             context.getSharedPreferences("star_notifier_prefs", Context.MODE_PRIVATE)
        } else {
            try {
                val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

                EncryptedSharedPreferences.create(
                    "secret_star_notifier_prefs",
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Exception) {
                // Fallback to standard prefs if encryption is not supported or fails
                context.getSharedPreferences("star_notifier_prefs", Context.MODE_PRIVATE)
            }
        }
    }

    companion object {
        private const val KEY_USER_CONFIG = "user_config"
        private const val KEY_REPO_PREFIX = "repo_"
    }

    override suspend fun saveRepositoryData(repo: Repository): Result<Unit> {
        return try {
            val json = gson.toJson(repo)
            prefs.edit().putString("$KEY_REPO_PREFIX${repo.name}", json).commit()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRepositoryData(repoName: String): Result<Repository?> {
        return try {
            val json = prefs.getString("$KEY_REPO_PREFIX$repoName", null)
            val repo = json?.let { gson.fromJson(it, Repository::class.java) }
            Result.success(repo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAllSelectedRepos(): Result<List<String>> {
        return try {
            val config = getUserConfig().getOrNull()
            val selectedRepos = config?.selectedRepos ?: emptyList()
            Result.success(selectedRepos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveUserConfig(config: UserConfig): Result<Unit> {
        return try {
            val json = gson.toJson(config)
            prefs.edit().putString(KEY_USER_CONFIG, json).commit()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserConfig(): Result<UserConfig?> {
        return try {
            val json = prefs.getString(KEY_USER_CONFIG, null)
            val config = json?.let { gson.fromJson(it, UserConfig::class.java) }
            Result.success(config)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}