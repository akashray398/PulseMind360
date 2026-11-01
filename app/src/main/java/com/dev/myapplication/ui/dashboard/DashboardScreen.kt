package com.dev.myapplication.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.myapplication.data.remote.Resource
import com.dev.myapplication.ui.components.AnimatedBatteryGauge
import com.dev.myapplication.ui.components.BatteryTrendVisualizer
import com.dev.myapplication.ui.components.DiagnosticBundleDialog
import com.dev.myapplication.ui.components.GlassCard
import com.dev.myapplication.ui.theme.StatusError
import com.dev.myapplication.ui.theme.StatusGood
import com.dev.myapplication.ui.theme.StatusWarning
import com.dev.myapplication.util.MemoryOptimizer

@Composable
fun DashboardScreen(
    onOpenDeskStand: () -> Unit = {},
    onOpenFocus: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val batteryState by viewModel.batteryState.collectAsStateWithLifecycle()
    val networkState by viewModel.networkState.collectAsStateWithLifecycle()
    val insights by viewModel.insightsState.collectAsStateWithLifecycle()
    val anomaly by viewModel.anomalyState.collectAsStateWithLifecycle()
    val frameState by viewModel.framePerformanceState.collectAsStateWithLifecycle()
    val networkInfoState by viewModel.networkInfoState.collectAsStateWithLifecycle()
    val workerState by viewModel.workerState.collectAsStateWithLifecycle()
    val healthScore by viewModel.systemHealthScore.collectAsStateWithLifecycle()
    val throughputState by viewModel.networkThroughputState.collectAsStateWithLifecycle()
    val sentinelReport by viewModel.sentinelReportState.collectAsStateWithLifecycle()
    val networkDiagnostic by viewModel.networkDiagnosticState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val memoryState = remember(batteryState, networkState) { MemoryOptimizer.getMemoryState(context) }
    val scrollState = rememberScrollState()
    var showSharingDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Sleek Clean Title Header
        DashboardHeader(
            onExportReport = { showSharingDialog = true },
            onOpenDeskStand = onOpenDeskStand,
            onOpenFocus = onOpenFocus,
            onOpenHistory = onOpenHistory
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Main Dashboard Content Stack
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Executive System Health Card
            SystemHealthCommandCard(
                healthScore = healthScore,
                batteryLevel = batteryState.level,
                isCharging = batteryState.isCharging,
                batteryTemp = batteryState.temperature,
                networkConnected = networkState.isConnected,
                networkTransport = networkState.transportType,
                ramUsagePct = memoryState.usagePct,
                hasAnomaly = anomaly.hasAnomaly
            )

            // 2. Sentinel AI & Smart Advisory Block
            SentinelAndAdvisoryCard(
                sentinelReport = sentinelReport,
                advisories = insights.healthAdvisories
            )

            // 3. Battery & Thermal Forecast + Trend Curve
            BatteryThermalCard(
                batteryState = batteryState,
                insights = insights
            )

            // 4. Network & Bandwidth Hub
            NetworkHubCard(
                networkState = networkState,
                throughputState = throughputState,
                networkDiagnostic = networkDiagnostic,
                networkInfoState = networkInfoState,
                onFetchInfo = { viewModel.fetchNetworkInfo() },
                onRunDiagnostics = { viewModel.runNetworkDiagnostics() }
            )

            // 5. Memory & RAM Diagnostic
            MemoryCard()

            // 6. Developer Telemetry (Collapsible)
            DeveloperTelemetryCard(
                frameState = frameState,
                workerState = workerState,
                onRunWorker = { viewModel.triggerSyncWorker() }
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showSharingDialog) {
        DiagnosticBundleDialog(
            healthScore = healthScore,
            batteryState = batteryState,
            networkState = networkState,
            anomalyReport = anomaly,
            frameState = frameState,
            biometricVaultEnabled = false,
            onDismiss = { showSharingDialog = false }
        )
    }
}

@Composable
private fun DashboardHeader(
    onExportReport: () -> Unit,
    onOpenDeskStand: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenHistory: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "PULSEMIND 360",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(StatusGood)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIVE SYSTEM MONITOR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusGood
                    )
                }
            }
        }

        // Action Toolbar Buttons
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onOpenFocus) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Focus Timer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onOpenHistory) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Timeline",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onOpenDeskStand) {
                Icon(
                    imageVector = Icons.Default.Nightlight,
                    contentDescription = "Desk Mode",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = onExportReport) {
                Icon(
                    imageVector = Icons.Default.Assessment,
                    contentDescription = "PDF Report",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SystemHealthCommandCard(
    healthScore: Int,
    batteryLevel: Int,
    isCharging: Boolean,
    batteryTemp: Float,
    networkConnected: Boolean,
    networkTransport: String,
    ramUsagePct: Int,
    hasAnomaly: Boolean
) {
    val animatedScore by animateIntAsState(
        targetValue = healthScore,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "HealthScore"
    )

    val (statusColor, statusText) = when {
        animatedScore >= 85 -> StatusGood to "OPTIMAL HEALTH"
        animatedScore >= 65 -> StatusWarning to "STABLE"
        else -> StatusError to "ATTENTION REQUIRED"
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hasGlowBorder = false
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$animatedScore% · $statusText",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }
                }

                Text(
                    text = if (hasAnomaly) "⚠️ Anomaly Flagged" else "✓ Systems Clear",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (hasAnomaly) StatusWarning else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 4-Vitals Metric Chips Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VitalChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.BatteryChargingFull,
                    value = "$batteryLevel%",
                    subtext = if (isCharging) "Charging" else "Discharging",
                    color = MaterialTheme.colorScheme.primary
                )
                VitalChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Thermostat,
                    value = "${batteryTemp}°C",
                    subtext = if (batteryTemp > 38f) "Warm" else "Normal",
                    color = if (batteryTemp > 38f) StatusError else StatusGood
                )
                VitalChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Wifi,
                    value = if (networkConnected) networkTransport else "Offline",
                    subtext = if (networkConnected) "Connected" else "No Data",
                    color = MaterialTheme.colorScheme.secondary
                )
                VitalChip(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Memory,
                    value = "$ramUsagePct%",
                    subtext = "RAM Used",
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun VitalChip(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    subtext: String,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtext, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SentinelAndAdvisoryCard(
    sentinelReport: com.dev.myapplication.domain.SentinelReport,
    advisories: List<String>
) {
    var isExpanded by remember { mutableStateOf(sentinelReport.hasAnomaly) }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hasGlowBorder = sentinelReport.hasAnomaly
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = sentinelReport.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Sentinel AI · ${sentinelReport.confidenceScore}% Confidence",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Text(
                        text = "• ${sentinelReport.rootCauseExplanation}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    advisories.take(2).forEach { advisory ->
                        Text(
                            text = "• $advisory",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BatteryThermalCard(
    batteryState: com.dev.myapplication.data.monitor.BatteryState,
    insights: com.dev.myapplication.domain.SystemInsights
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedBatteryGauge(
                        level = batteryState.level,
                        isCharging = batteryState.isCharging
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Battery & Thermal Status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = insights.timeRemainingWithMargin,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = insights.dischargeRatePctPerHour,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            // Trend Visualizer Line Chart
            BatteryTrendVisualizer(dataPoints = insights.trendHistory)
        }
    }
}

@Composable
private fun NetworkHubCard(
    networkState: com.dev.myapplication.data.monitor.NetworkState,
    throughputState: com.dev.myapplication.util.NetworkThroughputState,
    networkDiagnostic: com.dev.myapplication.domain.NetworkDiagnosticReport,
    networkInfoState: Resource<com.dev.myapplication.data.remote.NetworkInfoResponse>,
    onFetchInfo: () -> Unit,
    onRunDiagnostics: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (networkState.isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Network & Bandwidth",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (networkState.isConnected) "Connected (${networkState.transportType})" else "Offline",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Uniformly Formatted Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = onRunDiagnostics,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Ping", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = onFetchInfo,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "IP Info", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "DL: ${throughputState.downloadKbps.toInt()} KB/s", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "UL: ${throughputState.uploadKbps.toInt()} KB/s", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
                Text(
                    text = "Latency: ${if (networkDiagnostic.latencyMs >= 0) "${networkDiagnostic.latencyMs}ms" else "N/A"}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            when (networkInfoState) {
                is Resource.Success -> {
                    val info = networkInfoState.data
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Text(text = "Public IP: ${info.ip} (${info.city ?: "Global"}, ${info.country ?: ""})", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
                is Resource.Loading -> {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Fetching IP info...", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun MemoryCard() {
    val context = LocalContext.current
    var memState by remember { mutableStateOf(MemoryOptimizer.getMemoryState(context)) }
    var freedMsg by remember { mutableStateOf<String?>(null) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "RAM Memory", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    val usedGb = "%.1f".format(memState.usedMemMb / 1024f)
                    val totalGb = "%.1f".format(memState.totalMemMb / 1024f)
                    Text(text = "${usedGb}GB / ${totalGb}GB (${memState.usagePct}% Used)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            FilledTonalButton(
                onClick = {
                    val freed = MemoryOptimizer.trimMemoryCaches(context)
                    memState = MemoryOptimizer.getMemoryState(context)
                    freedMsg = "Freed ${freed} MB"
                },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text(text = freedMsg ?: "Trim RAM", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DeveloperTelemetryCard(
    frameState: com.dev.myapplication.domain.FramePerformanceState,
    workerState: String,
    onRunWorker: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DeveloperMode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "System Telemetry & WorkManager",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Render FPS: ${frameState.currentFps} (${frameState.frameDurationMs}ms)", fontSize = 11.sp)
                        Button(onClick = onRunWorker, contentPadding = ButtonDefaults.ContentPadding) {
                            Text(text = "Sync Worker", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
