package io.aatricks.starnotifier.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import io.aatricks.starnotifier.data.local.LocalStorageRepository
import io.aatricks.starnotifier.data.repository.GitHubRepository

class SettingsViewModelFactory(
    private val gitHubRepository: GitHubRepository,
    private val localStorageRepository: LocalStorageRepository,
    private val workManager: WorkManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(gitHubRepository, localStorageRepository, workManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}