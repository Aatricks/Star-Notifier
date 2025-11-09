package io.aatricks.starnotifier.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.aatricks.starnotifier.data.local.LocalStorageRepository
import io.aatricks.starnotifier.data.model.Repository
import io.aatricks.starnotifier.data.model.UserConfig
import io.aatricks.starnotifier.data.repository.GitHubRepository
import io.aatricks.starnotifier.worker.GitHubCheckWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SettingsViewModel(
    private val gitHubRepository: GitHubRepository,
    private val localStorageRepository: LocalStorageRepository,
    private val workManager: WorkManager
) : ViewModel() {

    private val _repositories = MutableLiveData<List<Repository>>()
    val repositories: LiveData<List<Repository>> = _repositories

    private val _userConfig = MutableLiveData<UserConfig?>()
    val userConfig: LiveData<UserConfig?> = _userConfig

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _totalsText = MutableLiveData<String>()
    val totalsText: LiveData<String> = _totalsText
    
    private val _totalStars = MutableLiveData<Int>()
    val totalStars: LiveData<Int> = _totalStars
    
    private val _totalForks = MutableLiveData<Int>()
    val totalForks: LiveData<Int> = _totalForks
    
    private val _totalViews = MutableLiveData<Int>()
    val totalViews: LiveData<Int> = _totalViews
    
    private val _totalClones = MutableLiveData<Int>()
    val totalClones: LiveData<Int> = _totalClones
    
    private val _isLifetimeMode = MutableLiveData<Boolean>(true)
    val isLifetimeMode: LiveData<Boolean> = _isLifetimeMode

    init {
        loadUserConfig()
    }

    private fun loadUserConfig() {
        viewModelScope.launch {
            val config = localStorageRepository.getUserConfig().getOrNull()
            _userConfig.value = config
            config?.let { loadRepositories(it.username, it.personalAccessToken) }
        }
    }

    private fun loadRepositories(username: String, token: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            val selectedRepos = _userConfig.value?.selectedRepos ?: emptyList()
            val result = gitHubRepository.getUserRepositories(username, token, selectedRepos)
            result.onSuccess { repos ->
                val sortedRepos = repos.sortedByDescending { it.currentStars }
                _repositories.value = sortedRepos
                updateTotals(sortedRepos)
            }.onFailure {
                // Handle error
                _repositories.value = emptyList()
                updateTotals(emptyList())
            }
            _isLoading.value = false
        }
    }

    fun saveUserConfig(username: String, token: String?) {
        val currentSelectedRepos = _userConfig.value?.selectedRepos ?: emptyList()
        val config = UserConfig(username, currentSelectedRepos, token)
        viewModelScope.launch {
            localStorageRepository.saveUserConfig(config).onSuccess {
                _userConfig.value = config
                loadRepositories(username, token)
                scheduleGitHubChecks()
            }
        }
    }

    fun updateRepositorySelection(repoName: String, isSelected: Boolean) {
        val currentConfig = _userConfig.value ?: return
        val currentRepos = _repositories.value ?: return
        val currentSelected = currentConfig.selectedRepos.toMutableList()

        if (isSelected) {
            if (!currentSelected.contains(repoName)) {
                currentSelected.add(repoName)
            }
        } else {
            currentSelected.remove(repoName)
        }

        val updatedConfig = currentConfig.copy(selectedRepos = currentSelected)
        val updatedRepos = currentRepos.map { repo ->
            if (repo.name == repoName) {
                repo.copy(isSelected = isSelected)
            } else {
                repo
            }
        }
        
        viewModelScope.launch {
            localStorageRepository.saveUserConfig(updatedConfig).onSuccess {
                _userConfig.value = updatedConfig
                _repositories.value = updatedRepos
                updateTotals(updatedRepos)
                scheduleGitHubChecks()
            }
        }
    }

    fun toggleSelectAllRepositories() {
        val currentRepos = _repositories.value ?: return
        val currentConfig = _userConfig.value ?: return

        val allSelected = currentRepos.all { it.isSelected }
        val newSelectedRepos = if (allSelected) {
            // Deselect all
            emptyList<String>()
        } else {
            // Select all
            currentRepos.map { it.name }
        }

        val updatedConfig = currentConfig.copy(selectedRepos = newSelectedRepos)
        val updatedRepos = currentRepos.map { repo ->
            repo.copy(isSelected = !allSelected)
        }

        viewModelScope.launch {
            localStorageRepository.saveUserConfig(updatedConfig).onSuccess {
                _userConfig.value = updatedConfig
                _repositories.value = updatedRepos
                updateTotals(updatedRepos)
                scheduleGitHubChecks()
            }
        }
    }

    private fun updateTotals(repos: List<Repository>) {
        val selectedRepos = repos.filter { it.isSelected }
        val totalStarsValue = selectedRepos.sumOf { it.currentStars }
        val totalForksValue = selectedRepos.sumOf { it.currentForks }
        
        val isLifetime = _isLifetimeMode.value ?: true
        val totalViewsValue = if (isLifetime) {
            selectedRepos.sumOf { it.lifetimeViews }
        } else {
            selectedRepos.sumOf { it.twoWeekViews }
        }
        val totalClonesValue = if (isLifetime) {
            selectedRepos.sumOf { it.lifetimeClones }
        } else {
            selectedRepos.sumOf { it.twoWeekClones }
        }
        
        _totalStars.value = totalStarsValue
        _totalForks.value = totalForksValue
        _totalViews.value = totalViewsValue
        _totalClones.value = totalClonesValue
        _totalsText.value = "Total: $totalStarsValue ⭐ | $totalForksValue 🍴"
    }
    
    fun setTrafficMode(isLifetime: Boolean) {
        _isLifetimeMode.value = isLifetime
        _repositories.value?.let { updateTotals(it) }
    }

    private fun scheduleGitHubChecks() {
        val workRequest = PeriodicWorkRequestBuilder<GitHubCheckWorker>(
            15, TimeUnit.MINUTES
            //1, TimeUnit.MINUTES  // TEST: Changed to 1 minute for testing
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "GitHubCheckWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}