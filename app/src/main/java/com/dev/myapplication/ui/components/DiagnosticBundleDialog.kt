package com.dev.myapplication.ui.components

import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.domain.AnomalyReport
import com.dev.myapplication.domain.FramePerformanceState
import com.dev.myapplication.util.BiometricHelper
import com.dev.myapplication.util.ReportGenerator

@Composable
fun DiagnosticBundleDialog(
    healthScore: Int,
    batteryState: BatteryState,
    networkState: NetworkState,
    anomalyReport: AnomalyReport,
    frameState: FramePerformanceState,
    biometricVaultEnabled: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val biometricHelper = remember { BiometricHelper(context) }

    var includeHealth by remember { mutableStateOf(true) }
    var includeBattery by remember { mutableStateOf(true) }
    var includeFrameRates by remember { mutableStateOf(true) }
    var includeNetworkIp by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val livePreviewText = remember(includeHealth, includeBattery, includeFrameRates, includeNetworkIp, healthScore, batteryState) {
        buildString {
            appendLine("=== PULSEMIND 360 DIAGNOSTIC BUNDLE PREVIEW ===")
            appendLine("Generated: Native Android Client")
            appendLine("Privacy Policy: 100% Offline-First")
            appendLine()
            if (includeHealth) {
                appendLine("[+] Health Score: $healthScore% (${if (healthScore >= 85) "Optimal" else "Action Required"})")
            }
            if (includeBattery) {
                appendLine("[+] Battery Level: ${batteryState.level}% | Temp: ${batteryState.temperature}°C | Voltage: ${batteryState.voltage}mV")
            }
            if (includeFrameRates) {
                appendLine("[+] UI Frame Rate: ${frameState.currentFps} FPS | Render Duration: ${frameState.frameDurationMs}ms")
            }
            if (includeNetworkIp) {
                appendLine("[!] Public Network Metadata: Un-redacted Requested")
            } else {
                appendLine("[-] Public IP & Network Metadata: [REDACTED FOR PRIVACY]")
            }
            appendLine()
            appendLine("=== EXCLUDED SECRETS & PROTECTED FIELDS ===")
            appendLine("x Public IP & ISP Organization (Redacted by default)")
            appendLine("x KeyStore AES-256 Secret Keys (Never exported)")
            appendLine("x Hardware MAC & Serial Identifiers (Never exported)")
            appendLine("x Local Private Notes & Database History (Never exported)")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Secure Share",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Secure Diagnostic Sharing", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Configure fields to include in the diagnostic bundle. Sensitive data and secret tokens are sanitized by default.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Field Selector Checkboxes
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = includeHealth, onCheckedChange = { includeHealth = it })
                        Text("Include System Health Score", fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = includeBattery, onCheckedChange = { includeBattery = it })
                        Text("Include Battery & Thermal Telemetry", fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = includeFrameRates, onCheckedChange = { includeFrameRates = it })
                        Text("Include UI Frame Rate Metrics", fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = includeNetworkIp,
                            onCheckedChange = { checked ->
                                if (checked && biometricVaultEnabled && context is FragmentActivity) {
                                    biometricHelper.promptBiometricAuth(
                                        activity = context,
                                        title = "Un-redact Network Metadata",
                                        subtitle = "Authenticate fingerprint/PIN to include IP metadata in report",
                                        onSuccess = { includeNetworkIp = true },
                                        onError = { includeNetworkIp = false }
                                    )
                                } else {
                                    includeNetworkIp = checked
                                }
                            }
                        )
                        Text(
                            text = "Un-redact Public IP & Location (Requires Biometric Auth)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Explicit Excluded Fields Matrix Box
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "🛡️ Explicitly Excluded Fields (Guaranteed Privacy):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "• Public IP Address & ISP Name (Redacted by default)", fontSize = 10.sp)
                        Text(text = "• Android KeyStore AES-256 Hardware Secret Keys", fontSize = 10.sp)
                        Text(text = "• Local Private Database Activity Logs & Notes", fontSize = 10.sp)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Live Text Preview Box
                Text(text = "Live Bundle Content Preview:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = livePreviewText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    ReportGenerator.generateAndSharePdfReport(
                        context = context,
                        healthScore = healthScore,
                        batteryState = batteryState,
                        networkState = networkState,
                        anomalyReport = anomalyReport,
                        frameState = frameState,
                        redactMetadata = !includeNetworkIp
                    )
                    onDismiss()
                }
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Share PDF")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share PDF Report", fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
