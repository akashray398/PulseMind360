package com.dev.myapplication.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.domain.AnomalyReport
import com.dev.myapplication.domain.FramePerformanceState

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            "ACTION_EXPORT_PDF_REPORT" -> {
                ReportGenerator.generateAndSharePdfReport(
                    context = context,
                    healthScore = 95,
                    batteryState = BatteryState(level = 80, isCharging = true, plugType = "AC"),
                    networkState = NetworkState(isConnected = true, transportType = "WIFI"),
                    anomalyReport = AnomalyReport(),
                    frameState = FramePerformanceState()
                )
            }
        }
    }
}
