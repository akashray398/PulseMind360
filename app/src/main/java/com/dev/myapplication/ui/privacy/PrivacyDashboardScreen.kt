package com.dev.myapplication.ui.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.myapplication.data.local.entity.ActivityLogEntity
import com.dev.myapplication.ui.components.GlassCard
import com.dev.myapplication.ui.settings.SettingsViewModel
import com.dev.myapplication.ui.timeline.TimelineViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PrivacyDashboardScreen(
    onClose: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    timelineViewModel: TimelineViewModel = hiltViewModel()
) {
    val prefs by settingsViewModel.userPreferencesState.collectAsStateWithLifecycle()
    val logs by timelineViewModel.logsState.collectAsStateWithLifecycle()

    val securityAuditLogs = remember(logs) {
        logs.filter { it.category == "SECURITY" || it.category == "PRIVACY_AUDIT" || it.category == "BATTERY_GUARD" }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "PRIVACY DASHBOARD",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Per-Feature Controls, Audit Logs & Encrypted Storage",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        }

        // Panel 1: Per-Feature Collection Controls
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Per-Feature Telemetry Collection Controls", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CollectionToggleRow(
                        title = "Battery & Power Monitor",
                        description = "Observes battery discharge %, voltage & temperature",
                        icon = Icons.Default.BatteryChargingFull,
                        checked = prefs.collectBatteryTelemetry,
                        onCheckedChange = { settingsViewModel.setCollectBatteryTelemetry(it) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    CollectionToggleRow(
                        title = "Network Traffic & Throughput Inspector",
                        description = "Monitors upload/download speed velocity via TrafficStats",
                        icon = Icons.Default.Public,
                        checked = prefs.collectNetworkTelemetry,
                        onCheckedChange = { settingsViewModel.setCollectNetworkTelemetry(it) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    CollectionToggleRow(
                        title = "UI Frame Render Telemetry",
                        description = "Monitors rendering frame rates (FPS) and jank duration",
                        icon = Icons.Default.Speed,
                        checked = prefs.collectFramePerformance,
                        onCheckedChange = { settingsViewModel.setCollectFramePerformance(it) }
                    )
                }
            }
        }

        // Panel 2: Data Item Inventory Audit Matrix
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Real-Time Data Inventory Audit Matrix", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(10.dp))

                listOf(
                    Triple("Battery Vitals", "Room DB & DataStore", "Last Polling: Just Now"),
                    Triple("Network Traffic", "In-Memory Polling", "Last Polling: 1s ago"),
                    Triple("Public IP Context", "Retrofit Cache", "Last Polling: On-Demand"),
                    Triple("KeyStore Vault Tokens", "Android KeyStore AES-256", "Hardware Encrypted")
                ).forEach { (item, location, lastTime) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column {
                            Text(text = item, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = "Storage: $location", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(text = lastTime, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Panel 3: Protected-Data Access Audit Log
        item {
            Text(
                text = "Protected-Data Access Audit Log",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        if (securityAuditLogs.isEmpty()) {
            item {
                Text(
                    text = "No security audit events recorded yet.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(securityAuditLogs, key = { it.id }) { log ->
                AuditLogItem(log = log)
            }
        }
    }
}

@Composable
fun CollectionToggleRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = description, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun AuditLogItem(log: ActivityLogEntity) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault()) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Audit Event",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = log.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = log.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text(
                text = dateFormat.format(Date(log.timestamp)),
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
