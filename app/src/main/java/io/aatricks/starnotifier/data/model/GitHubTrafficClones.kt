package io.aatricks.starnotifier.data.model

data class GitHubTrafficClones(
    val count: Int,
    val uniques: Int,
    val clones: List<TrafficEntry> = emptyList()
)
