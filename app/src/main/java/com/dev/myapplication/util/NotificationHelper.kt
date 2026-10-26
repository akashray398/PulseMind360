package com.dev.myapplication.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dev.myapplication.MainActivity
import com.dev.myapplication.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ALERTS = "companion_alerts"
        const val CHANNEL_FOCUS = "focus_channel"
        const val CHANNEL_SYNC = "sync_channel"
        const val CHANNEL_BATTERY_GUARD = "battery_guard_channel"
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
                enableVibration(true)
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

            val guardChannel = NotificationChannel(
                CHANNEL_BATTERY_GUARD,
                "Smart Charge & Thermal Lifespan Guard",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for 80% charge limit and thermal overheating protection"
                enableVibration(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(
                listOf(alertsChannel, focusChannel, syncChannel, guardChannel)
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

    private fun getAppIconBitmap() = try {
        BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
    } catch (_: Exception) {
        null
    }

    private fun getAppContentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun showNotification(
        id: Int,
        channelId: String,
        title: String,
        message: String
    ) {
        if (!hasNotificationPermission()) return

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(getAppIconBitmap())
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(getAppContentIntent())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(id, builder.build())
        } catch (_: SecurityException) {}
    }

    fun showSmartChargeGuardNotification(level: Int) {
        if (!hasNotificationPermission()) return

        val reportIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "ACTION_EXPORT_PDF_REPORT"
        }
        val reportPendingIntent = PendingIntent.getBroadcast(
            context,
            101,
            reportIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_BATTERY_GUARD)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(getAppIconBitmap())
            .setContentTitle("⚡ 80% Smart Charge Limit Reached")
            .setContentText("Battery reached $level%. Unplug charger to preserve battery health.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Battery reached $level%. Unplug charger to preserve long-term battery cell health."))
            .setContentIntent(getAppContentIntent())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .addAction(0, "📄 Export PDF Report", reportPendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(8080, builder.build())
        } catch (_: SecurityException) {}
    }

    fun showThermalOverheatNotification(temp: Float) {
        if (!hasNotificationPermission()) return

        val tempStr = "%.1f".format(temp)
        val reportIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = "ACTION_EXPORT_PDF_REPORT"
        }
        val reportPendingIntent = PendingIntent.getBroadcast(
            context,
            102,
            reportIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_BATTERY_GUARD)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(getAppIconBitmap())
            .setContentTitle("🔥 High Thermal Overheat Warning")
            .setContentText("Battery temp reached ${tempStr}°C! Heavy tasks paused for hardware safety.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Battery temp reached ${tempStr}°C! Heavy background tasks paused for hardware safety."))
            .setContentIntent(getAppContentIntent())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .addAction(0, "📄 Export PDF Report", reportPendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(9090, builder.build())
        } catch (_: SecurityException) {}
    }
}
