package io.aatricks.starnotifier.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestWorkerBuilder
import io.aatricks.starnotifier.data.local.LocalStorageRepository
import io.aatricks.starnotifier.data.repository.GitHubRepository
import io.aatricks.starnotifier.notification.NotificationHelper
import io.aatricks.starnotifier.data.model.Repository
import io.aatricks.starnotifier.data.model.UserConfig
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.times

@RunWith(AndroidJUnit4::class)
class GitHubCheckWorkerTest {

    private lateinit var context: Context

    @Mock
    private lateinit var mockGitHubRepository: GitHubRepository

    @Mock
    private lateinit var mockLocalStorageRepository: LocalStorageRepository

    @Mock
    private lateinit var mockNotificationHelper: NotificationHelper

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `doWork should return success when no user config`() {
        // Given
        val worker = TestWorkerBuilder<GitHubCheckWorker>(context)
            .setWorkerFactory { appContext, workerParams ->
                GitHubCheckWorker(
                    appContext,
                    workerParams,
                    mockGitHubRepository,
                    mockLocalStorageRepository,
                    mockNotificationHelper
                )
            }
            .build()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun `doWork should send star notification when star increases`() {
        // Given
        val username = "Aatricks"
        val repoName = "Aatricks/Star-Notifier"
        val storedRepo = Repository(
            name = repoName,
            currentStars = 0,
            currentForks = 0,
            totalViews = 0,
            totalClones = 0,
            lifetimeViews = 0,
            lifetimeClones = 0,
            twoWeekViews = 0,
            twoWeekClones = 0,
            viewsData = emptyList(),
            clonesData = emptyList(),
            lastChecked = System.currentTimeMillis(),
            isSelected = true
        )
        val currentRepo = storedRepo.copy(currentStars = storedRepo.currentStars + 1)

        whenever(mockLocalStorageRepository.getUserConfig()).thenReturn(
            Result.success(
                UserConfig(
                    username,
                    listOf(repoName),
                    null
                )
            )
        )
        whenever(mockLocalStorageRepository.getAllSelectedRepos()).thenReturn(Result.success(listOf(repoName)))
        whenever(mockLocalStorageRepository.getRepositoryData(repoName)).thenReturn(Result.success(storedRepo))
        whenever(
            mockGitHubRepository.getUserRepositories(
                username,
                null
            )
        ).thenReturn(Result.success(listOf(currentRepo)))

        val worker = TestWorkerBuilder<GitHubCheckWorker>(context)
            .setWorkerFactory { appContext, workerParams ->
                GitHubCheckWorker(
                    appContext,
                    workerParams,
                    mockGitHubRepository,
                    mockLocalStorageRepository,
                    mockNotificationHelper
                )
            }
            .build()

        // When
        val result = worker.doWork()

        // Then
        assertEquals(ListenableWorker.Result.success(), result)
        verify(mockNotificationHelper, times(1)).sendStarNotification(repoName, currentRepo.currentStars)
    }
}
