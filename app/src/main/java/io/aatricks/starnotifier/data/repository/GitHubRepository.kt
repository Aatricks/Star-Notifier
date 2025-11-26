package io.aatricks.starnotifier.data.repository

import io.aatricks.starnotifier.data.model.Repository

interface GitHubRepository {
    suspend fun getUserRepositories(
        username: String,
        token: String?,
        selectedRepos: List<String> = emptyList()
    ): Result<List<Repository>>
}