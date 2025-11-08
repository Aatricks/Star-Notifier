package io.aatricks.starnotifier.data.model

data class Repository(
    val name: String, // e.g., "owner/repo"
    val currentStars: Int,
    val currentForks: Int,
    val lastChecked: Long, // timestamp
    val isSelected: Boolean = false
)