package com.dev.myapplication.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.domain.AnomalyReport
import com.dev.myapplication.domain.FramePerformanceState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportGenerator {

    fun generateAndSharePdfReport(
        context: Context,
        healthScore: Int,
        batteryState: BatteryState,
        networkState: NetworkState,
        anomalyReport: AnomalyReport,
        frameState: FramePerformanceState,
        redactMetadata: Boolean = true
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard dimensions
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())

        // Header Background
        paint.color = Color.parseColor("#0F172A") // Tech Dark Background
        canvas.drawRect(0f, 0f, 595f, 110f, paint)

        // App Title Banner
        paint.color = Color.parseColor("#00E5FF") // Cyan Primary
        paint.textSize = 24f
        paint.isFakeBoldText = true
        canvas.drawText("PULSEMIND 360", 30f, 45f, paint)

        paint.color = Color.parseColor("#D500F9") // Violet Secondary
        paint.textSize = 12f
        paint.isFakeBoldText = false
        canvas.drawText("SYSTEM DIAGNOSTIC & HEALTH AUDIT REPORT", 30f, 65f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        canvas.drawText("Generated: $timestamp ${if (redactMetadata) "· [PRIVACY REDACTION ACTIVE]" else ""}", 30f, 88f, paint)

        // Health Score Card Section
        val scoreColor = when {
            healthScore >= 85 -> Color.parseColor("#00E676")
            healthScore >= 65 -> Color.parseColor("#FFAB00")
            else -> Color.parseColor("#FF1744")
        }

        paint.color = scoreColor
        paint.textSize = 28f
        paint.isFakeBoldText = true
        canvas.drawText("$healthScore% SCORE", 30f, 155f, paint)

        paint.color = Color.DKGRAY
        paint.textSize = 11f
        paint.isFakeBoldText = false
        val healthStatusText = when {
            healthScore >= 85 -> "SYSTEM STATUS: OPTIMAL HEALTH"
            healthScore >= 65 -> "SYSTEM STATUS: STABLE"
            else -> "SYSTEM STATUS: ACTION REQUIRED"
        }
        canvas.drawText(healthStatusText, 200f, 155f, paint)

        // Divider Line
        paint.color = Color.LTGRAY
        paint.strokeWidth = 1f
        canvas.drawLine(30f, 175f, 565f, 175f, paint)

        var yOffset = 205f

        fun drawSectionHeader(title: String) {
            paint.color = Color.parseColor("#00838F")
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas.drawText(title, 30f, yOffset, paint)
            yOffset += 20f
        }

        fun drawMetricRow(label: String, value: String) {
            paint.color = Color.BLACK
            paint.textSize = 11f
            paint.isFakeBoldText = true
            canvas.drawText("$label:", 40f, yOffset, paint)

            paint.isFakeBoldText = false
            paint.color = Color.DKGRAY
            canvas.drawText(value, 200f, yOffset, paint)
            yOffset += 18f
        }

        // Section 1: Battery & Thermal Dynamics
        drawSectionHeader("1. BATTERY & THERMAL TELEMETRY")
        drawMetricRow("Battery Level", "${batteryState.level}%")
        drawMetricRow("Charging Status", if (batteryState.isCharging) "Charging (${batteryState.plugType})" else "Discharging")
        drawMetricRow("Battery Health", batteryState.health)
        drawMetricRow("Temperature", "${batteryState.temperature}°C")
        drawMetricRow("Voltage", "${batteryState.voltage} mV")
        yOffset += 10f

        // Section 2: Network Connectivity & Speed
        drawSectionHeader("2. NETWORK & GEOGRAPHIC DIAGNOSTICS")
        drawMetricRow("Connection Status", if (networkState.isConnected) "Connected (${networkState.transportType})" else "Disconnected")
        drawMetricRow("Metered Connection", if (networkState.isMetered) "Yes" else "No")
        drawMetricRow("Downstream Link Speed", "${networkState.linkDownstreamKbps / 1000} Mbps")
        if (redactMetadata) {
            drawMetricRow("Public IP Metadata", "[REDACTED FOR PRIVACY SANITIZATION]")
        } else {
            drawMetricRow("Public IP Metadata", "Un-redacted Explicit Telemetry")
        }
        yOffset += 10f

        // Section 3: Anomaly & System Health Audit
        drawSectionHeader("3. ANOMALY & THREAT DETECTOR")
        drawMetricRow("Anomaly Active", if (anomalyReport.hasAnomaly) "YES (${anomalyReport.severity})" else "NONE (All Systems Nominal)")
        drawMetricRow("Audit Summary", anomalyReport.title)
        drawMetricRow("Details", anomalyReport.description)
        yOffset += 10f

        // Section 4: UI Frame Rendering Telemetry
        drawSectionHeader("4. UI FRAME RENDER TELEMETRY")
        drawMetricRow("Current Frame Rate", "${frameState.currentFps} FPS")
        drawMetricRow("Frame Render Duration", "${frameState.frameDurationMs} ms")
        drawMetricRow("Performance Status", frameState.status)
        yOffset += 20f

        // Footer Divider & Disclaimer
        canvas.drawLine(30f, 780f, 565f, 780f, paint)
        paint.color = Color.GRAY
        paint.textSize = 9f
        paint.isFakeBoldText = false
        canvas.drawText("PulseMind 360 Security & Telemetry Audit • Offline-First Native Diagnostics", 30f, 800f, paint)

        pdfDocument.finishPage(page)

        // Save PDF to cache directory
        val cachePath = File(context.cacheDir, "reports")
        cachePath.mkdirs()
        val outputFile = File(cachePath, "PulseMind360_Health_Report.pdf")

        try {
            val fileOutputStream = FileOutputStream(outputFile)
            pdfDocument.writeTo(fileOutputStream)
            fileOutputStream.close()
            pdfDocument.close()

            // Generate FileProvider Content URI & Fire Share Intent
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                outputFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "PulseMind 360 - Diagnostic System Audit Report")
                putExtra(Intent.EXTRA_TEXT, "Attached is the PulseMind 360 System Health Audit Report generated on $timestamp.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Diagnostic Health Report PDF")
            context.startActivity(chooser)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
