package io.aatricks.starnotifier.data.model

data class UserConfig(
    val username: String,
    val selectedRepos: List<String>,
    val personalAccessToken: String? = null,
    val checkIntervalMinutes: Int = 30
)