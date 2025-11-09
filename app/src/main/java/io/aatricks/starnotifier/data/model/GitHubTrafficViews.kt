package io.aatricks.starnotifier.data.model

data class GitHubTrafficViews(
    val count: Int,
    val uniques: Int,
    val views: List<TrafficEntry> = emptyList()
)

data class TrafficEntry(
    val timestamp: String,
    val count: Int,
    val uniques: Int
)
