package com.dev.myapplication.domain

import com.dev.myapplication.data.monitor.NetworkState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

data class NetworkDiagnosticReport(
    val latencyMs: Long = 28L,
    val dnsStatus: String = "OK", // OK, FAIL, TIMEOUT
    val dropCountToday: Int = 0,
    val diagnosticStatus: String = "HEALTHY", // HEALTHY, HIGH_LATENCY, DNS_FAILURE, DISCONNECTED
    val rootCauseExplanation: String = "Network reachable with optimal latency and valid DNS resolution.",
    val testedEndpoint: String = "1.1.1.1 (Cloudflare DNS)"
)

class NetworkDiagnosticsEngine {

    suspend fun runDiagnostics(
        networkState: NetworkState,
        targetHost: String = "1.1.1.1"
    ): NetworkDiagnosticReport = withContext(Dispatchers.IO) {
        if (!networkState.isConnected) {
            return@withContext NetworkDiagnosticReport(
                latencyMs = -1L,
                dnsStatus = "UNREACHABLE",
                diagnosticStatus = "DISCONNECTED",
                rootCauseExplanation = "Transport interface is disconnected. Check Wi-Fi / Cellular toggle.",
                testedEndpoint = targetHost
            )
        }

        // Test 1: Measure Socket Connection Latency to Port 53 (DNS Service)
        var latency = -1L
        val startTime = System.currentTimeMillis()
        var socketConnected = false

        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(targetHost, 53), 2000) // 2 second timeout
                latency = System.currentTimeMillis() - startTime
                socketConnected = true
            }
        } catch (_: Exception) {
            latency = -1L
        }

        // Test 2: DNS Resolution Probe
        var dnsHealthy = false
        val dnsStartTime = System.currentTimeMillis()
        try {
            val address = InetAddress.getByName("api.ipify.org")
            dnsHealthy = address != null
        } catch (_: Exception) {
            dnsHealthy = false
        }

        val dnsStatusStr = if (dnsHealthy) "OK" else "FAIL"

        return@withContext when {
            socketConnected && latency in 0..150 && dnsHealthy -> NetworkDiagnosticReport(
                latencyMs = latency,
                dnsStatus = dnsStatusStr,
                diagnosticStatus = "HEALTHY",
                rootCauseExplanation = "Reachable with low latency (${latency}ms) and valid DNS resolution.",
                testedEndpoint = targetHost
            )
            socketConnected && latency > 150 && dnsHealthy -> NetworkDiagnosticReport(
                latencyMs = latency,
                dnsStatus = dnsStatusStr,
                diagnosticStatus = "HIGH_LATENCY",
                rootCauseExplanation = "High network latency (${latency}ms) detected on ${networkState.transportType} transport.",
                testedEndpoint = targetHost
            )
            !dnsHealthy && socketConnected -> NetworkDiagnosticReport(
                latencyMs = latency,
                dnsStatus = "FAIL",
                diagnosticStatus = "DNS_FAILURE",
                rootCauseExplanation = "IP route reachable (${latency}ms), but DNS name resolution failed.",
                testedEndpoint = targetHost
            )
            else -> NetworkDiagnosticReport(
                latencyMs = -1L,
                dnsStatus = "FAIL",
                diagnosticStatus = "UNREACHABLE",
                rootCauseExplanation = "Target host ($targetHost) unreachable. High packet drop or network isolation.",
                testedEndpoint = targetHost
            )
        }
    }
}
