package io.aatricks.starnotifier.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GitHubCheckWorkerTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testDoWork_NoConfig_ReturnsSuccess() {
        // Given
        val worker = TestListenableWorkerBuilder<GitHubCheckWorker>(context).build()

        // When
        val result = runBlocking { worker.doWork() }

        // Then
        // Without config, it should return success (to stop retrying)
        assertEquals(ListenableWorker.Result.success(), result)
    }
}