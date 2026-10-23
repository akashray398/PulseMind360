package com.dev.myapplication.util

import android.net.TrafficStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class NetworkThroughputState(
    val downloadKbps: Float = 0f,
    val uploadKbps: Float = 0f,
    val totalRxMb: Float = 0f,
    val totalTxMb: Float = 0f,
    val isHighBandwidthActive: Boolean = false
)

object TrafficInspector {

    fun observeNetworkThroughput(sampleIntervalMs: Long = 1000L): Flow<NetworkThroughputState> = flow {
        var prevRx = TrafficStats.getTotalRxBytes()
        var prevTx = TrafficStats.getTotalTxBytes()

        while (true) {
            delay(sampleIntervalMs)

            val currentRx = TrafficStats.getTotalRxBytes()
            val currentTx = TrafficStats.getTotalTxBytes()

            val rxDiff = if (prevRx != TrafficStats.UNSUPPORTED.toLong() && currentRx >= prevRx) {
                currentRx - prevRx
            } else 0L

            val txDiff = if (prevTx != TrafficStats.UNSUPPORTED.toLong() && currentTx >= prevTx) {
                currentTx - prevTx
            } else 0L

            prevRx = currentRx
            prevTx = currentTx

            // Convert bytes per interval to KB/s
            val downloadKbps = (rxDiff / 1024f) / (sampleIntervalMs / 1000f)
            val uploadKbps = (txDiff / 1024f) / (sampleIntervalMs / 1000f)

            val totalRxMb = if (currentRx > 0) currentRx / (1024f * 1024f) else 0f
            val totalTxMb = if (currentTx > 0) currentTx / (1024f * 1024f) else 0f

            // High bandwidth active if download > 1.5 MB/s (1500 KB/s)
            val isHighBandwidth = downloadKbps > 1500f

            emit(
                NetworkThroughputState(
                    downloadKbps = downloadKbps,
                    uploadKbps = uploadKbps,
                    totalRxMb = totalRxMb,
                    totalTxMb = totalTxMb,
                    isHighBandwidthActive = isHighBandwidth
                )
            )
        }
    }
}
