package io.aatricks.starnotifier.data.model

data class GitHubRepoResponse(
    val name: String,
    val full_name: String,
    val stargazers_count: Int,
    val forks_count: Int,
    val private: Boolean
)