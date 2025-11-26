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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_star)
            .setContentTitle("⭐ New star on $repoName")
            .setContentText("Repository now has $newCount stars")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Use a unique ID based on repo name and type (star)
        val notificationId = (repoName + "star").hashCode()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun sendForkNotification(repoName: String, newCount: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_fork)
            .setContentTitle("🍴 New fork on $repoName")
            .setContentText("Repository now has $newCount forks")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Use a unique ID based on repo name and type (fork)
        val notificationId = (repoName + "fork").hashCode()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }
}