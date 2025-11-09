package io.aatricks.starnotifier.data.model

data class Repository(
    val name: String, // e.g., "owner/repo"
    val currentStars: Int,
    val currentForks: Int,
    val totalViews: Int = 0,
    val totalClones: Int = 0,
    val lifetimeViews: Int = 0, // Accumulated views over time
    val lifetimeClones: Int = 0, // Accumulated clones over time
    val lastChecked: Long, // timestamp
    val isSelected: Boolean = false
)