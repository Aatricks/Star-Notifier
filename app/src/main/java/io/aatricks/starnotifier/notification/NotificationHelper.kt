package io.aatricks.starnotifier.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.aatricks.starnotifier.R

class NotificationHelper(
    private val context: Context
) {

    companion object {
        private const val CHANNEL_ID = "github_channel"
        private const val CHANNEL_NAME = "GitHub Notifications"
        private const val STAR_NOTIFICATION_ID = 1001
        private const val FORK_NOTIFICATION_ID = 1002
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for GitHub star and fork changes"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendStarNotification(repoName: String, newCount: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_star)
            .setContentTitle("⭐ New star on $repoName")
            .setContentText("Repository now has $newCount stars")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(STAR_NOTIFICATION_ID, notification)
    }

    fun sendForkNotification(repoName: String, newCount: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_fork)
            .setContentTitle("🍴 New fork on $repoName")
            .setContentText("Repository now has $newCount forks")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(FORK_NOTIFICATION_ID, notification)
    }
}