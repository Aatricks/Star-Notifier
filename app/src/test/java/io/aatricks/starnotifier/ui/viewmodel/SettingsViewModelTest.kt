package io.aatricks.starnotifier.ui.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import io.aatricks.starnotifier.data.local.LocalStorageRepository
import io.aatricks.starnotifier.data.model.Repository
import io.aatricks.starnotifier.data.model.UserConfig
import io.aatricks.starnotifier.data.repository.GitHubRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class SettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var gitHubRepository: FakeGitHubRepository
    private lateinit var localStorageRepository: FakeLocalStorageRepository

    @Mock
    private lateinit var workManager: WorkManager

    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() = runTest {
        MockitoAnnotations.openMocks(this@SettingsViewModelTest)
        Dispatchers.setMain(testDispatcher)

        gitHubRepository = FakeGitHubRepository()
        localStorageRepository = FakeLocalStorageRepository()

        // Default config
        localStorageRepository.configToReturn = Result.success(UserConfig("user", emptyList(), "token"))
        
        viewModel = SettingsViewModel(gitHubRepository, localStorageRepository, workManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `scheduleGitHubChecks should enqueue unique periodic work`() = runTest {
        // Given
        val username = "testuser"
        val token = "testtoken"
        
        // When
        viewModel.saveUserConfig(username, token)

        // Then
        verify(workManager).enqueueUniquePeriodicWork(
            eq("GitHubCheckWork"),
            eq(ExistingPeriodicWorkPolicy.UPDATE),
            any()
        )
    }
    
    @Test
    fun `saveUserConfig without token should cancel work`() = runTest {
        // Given
        val username = "testuser"
        val token = "" // Empty token
        
        // When
        viewModel.saveUserConfig(username, token)

        // Then
        verify(workManager).cancelUniqueWork("GitHubCheckWork")
    }
}

// Fakes
class FakeGitHubRepository : GitHubRepository {
    var reposToReturn: Result<List<Repository>> = Result.success(emptyList())

    override suspend fun getUserRepositories(
        username: String,
        token: String?,
        selectedRepos: List<String>
    ): Result<List<Repository>> {
        return reposToReturn
    }
}

class FakeLocalStorageRepository : LocalStorageRepository {
    var configToReturn: Result<UserConfig?> = Result.success(null)
    var saveResult: Result<Unit> = Result.success(Unit)

    override suspend fun saveRepositoryData(repo: Repository): Result<Unit> {
        return Result.success(Unit)
    }

    override suspend fun getRepositoryData(repoName: String): Result<Repository?> {
        return Result.success(null)
    }

    override suspend fun getAllSelectedRepos(): Result<List<String>> {
        return Result.success(emptyList())
    }

    override suspend fun getUserConfig(): Result<UserConfig?> {
        return configToReturn
    }

    override suspend fun saveUserConfig(config: UserConfig): Result<Unit> {
        configToReturn = Result.success(config)
        return saveResult
    }
}