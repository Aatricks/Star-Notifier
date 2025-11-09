package io.aatricks.starnotifier.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.aatricks.starnotifier.notification.NotificationHelper

class TestBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("TestBroadcastReceiver", "Received broadcast: ${intent.action}")
        when (intent.action) {
            "io.aatricks.starnotifier.TEST_STAR_NOTIFICATION" -> {
                Log.d("TestBroadcastReceiver", "Sending star notification")
                val notificationHelper = NotificationHelper(context)
                notificationHelper.sendStarNotification("test/repo", 100)
                Log.d("TestBroadcastReceiver", "Star notification sent")
            }
            "io.aatricks.starnotifier.TEST_FORK_NOTIFICATION" -> {
                Log.d("TestBroadcastReceiver", "Sending fork notification")
                val notificationHelper = NotificationHelper(context)
                notificationHelper.sendForkNotification("test/repo", 50)
                Log.d("TestBroadcastReceiver", "Fork notification sent")
            }
        }
    }
}
