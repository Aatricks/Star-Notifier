package io.aatricks.starnotifier.integration

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import io.aatricks.starnotifier.data.local.SharedPreferencesStorage
import io.aatricks.starnotifier.data.model.Repository
import io.aatricks.starnotifier.data.model.UserConfig
import io.aatricks.starnotifier.worker.GitHubCheckWorker
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LiveWorkerIntegrationTest {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(android.Manifest.permission.POST_NOTIFICATIONS)

    @Test
    fun testWorkerDetectsChangesForAatricks() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        // Use non-encrypted storage for test stability
        val storage = SharedPreferencesStorage(context, useEncryption = false)
        
        // 1. Setup: Configure User "Aatricks"
        val targetRepo = "Aatricks/EasyReader"
        val config = UserConfig(
            username = "Aatricks",
            selectedRepos = listOf(targetRepo),
            personalAccessToken = null
        )
        
        runBlocking {
            storage.saveUserConfig(config)
            
            // 2. Setup: Plant "old" data with 0 stars and 0 timestamp
            val oldRepoData = createEmptyRepo(targetRepo)
            storage.saveRepositoryData(oldRepoData)
        }

        // 3. Action: Run the Worker with injected storage
        val worker = TestListenableWorkerBuilder<GitHubCheckWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker? {
                    return GitHubCheckWorker(appContext, workerParameters, storage)
                }
            })
            .build()
            
        val result = runBlocking { worker.doWork() }

        // 4. Verify: Worker Succeeded
        assertEquals(ListenableWorker.Result.success(), result)

        // 5. Verify: Data was updated
        runBlocking {
            val updatedRepo = storage.getRepositoryData(targetRepo).getOrNull()
            
            assertNotNull("Repository data should exist", updatedRepo)
            println("Repo: ${updatedRepo!!.name}, Stars: ${updatedRepo.currentStars}, Last Checked: ${updatedRepo.lastChecked}")
            
            assertTrue("Repo timestamp should be updated", updatedRepo.lastChecked > 0)
        }
    }
    
    private fun createEmptyRepo(name: String): Repository {
        return Repository(
            name = name,
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
            lastChecked = 0,
            isSelected = true
        )
    }
}