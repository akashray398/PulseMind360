package com.dev.myapplication.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dev.myapplication.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ALERTS = "companion_alerts"
        const val CHANNEL_FOCUS = "focus_channel"
        const val CHANNEL_SYNC = "sync_channel"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "Device Alerts & Automations",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for battery alerts and automated rules"
            }

            val focusChannel = NotificationChannel(
                CHANNEL_FOCUS,
                "Focus Mode Session",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for focus timer sessions"
            }

            val syncChannel = NotificationChannel(
                CHANNEL_SYNC,
                "Background Task Sync",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for WorkManager background tasks"
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(
                listOf(alertsChannel, focusChannel, syncChannel)
            )
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showNotification(
        id: Int,
        channelId: String,
        title: String,
        message: String
    ) {
        if (!hasNotificationPermission()) return

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(id, builder.build())
        } catch (_: SecurityException) {}
    }
}
