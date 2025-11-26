package io.aatricks.starnotifier.notification

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationHelperTest {

    private lateinit var notificationHelper: NotificationHelper

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        notificationHelper = NotificationHelper(context)
    }

    @Test
    fun sendStarNotification_shouldNotCrash() {
        // Given
        val repoName = "test/repo"
        val newCount = 42

        // When
        notificationHelper.sendStarNotification(repoName, newCount)

        // Then
        // No exception thrown
    }

    @Test
    fun sendForkNotification_shouldNotCrash() {
        // Given
        val repoName = "test/repo"
        val newCount = 7

        // When
        notificationHelper.sendForkNotification(repoName, newCount)

        // Then
        // No exception thrown
    }
}