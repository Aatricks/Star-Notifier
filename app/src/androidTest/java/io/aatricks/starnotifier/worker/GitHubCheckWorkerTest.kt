package io.aatricks.starnotifier.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestWorkerBuilder
import io.aatricks.starnotifier.data.local.LocalStorageRepository
import io.aatricks.starnotifier.data.repository.GitHubRepository
import io.aatricks.starnotifier.notification.NotificationHelper
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations

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

    // Additional tests would mock the repositories and verify interactions
    // For now, this basic test ensures the worker can be instantiated
}