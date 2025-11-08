package io.aatricks.starnotifier.data.local

import io.aatricks.starnotifier.data.model.Repository
import io.aatricks.starnotifier.data.model.UserConfig

interface LocalStorageRepository {
    suspend fun saveRepositoryData(repo: Repository): Result<Unit>
    suspend fun getRepositoryData(repoName: String): Result<Repository?>
    suspend fun getAllSelectedRepos(): Result<List<String>>
    suspend fun saveUserConfig(config: UserConfig): Result<Unit>
    suspend fun getUserConfig(): Result<UserConfig?>
}