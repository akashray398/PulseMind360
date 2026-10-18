package com.dev.myapplication.ui.dashboard

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.myapplication.data.remote.Resource
import com.dev.myapplication.ui.components.AnimatedBatteryGauge
import com.dev.myapplication.ui.components.BatteryTrendVisualizer
import com.dev.myapplication.ui.components.GlassCard
import com.dev.myapplication.ui.theme.StatusGood
import com.dev.myapplication.ui.theme.VioletSecondary

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val batteryState by viewModel.batteryState.collectAsStateWithLifecycle()
    val networkState by viewModel.networkState.collectAsStateWithLifecycle()
    val insights by viewModel.insightsState.collectAsStateWithLifecycle()
    val anomaly by viewModel.anomalyState.collectAsStateWithLifecycle()
    val frameState by viewModel.framePerformanceState.collectAsStateWithLifecycle()
    val networkInfoState by viewModel.networkInfoState.collectAsStateWithLifecycle()
    val workerState by viewModel.workerState.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current
    val isTabletOrLandscape = configuration.screenWidthDp >= 600

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Title Banner
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Pulsing Logo Emblem
                    val infiniteTransition = rememberInfiniteTransition(label = "HeaderLogoPulse")
                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.65f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "PulseAlpha"
                    )
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.95f,
                        targetValue = 1.08f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "PulseScale"
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .graphicsLayer {
                                scaleX = pulseScale
                                scaleY = pulseScale
                                alpha = pulseAlpha
                            }
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "PulseMind Emblem",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        val gradientOffset by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 600f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(3500, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "TitleGradient"
                        )

                        val titleBrush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                VioletSecondary,
                                MaterialTheme.colorScheme.primary
                            ),
                            start = Offset(gradientOffset, 0f),
                            end = Offset(gradientOffset + 300f, 300f)
                        )

                        Text(
                            text = "PULSEMIND 360",
                            style = TextStyle(
                                brush = titleBrush,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.5.sp
                            )
                        )
                        Text(
                            text = "Real-Time Contextual System Monitor",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Live Indicator Badge
                Surface(
                    color = StatusGood.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, StatusGood.copy(alpha = 0.3f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(StatusGood, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusGood,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        if (isTabletOrLandscape) {
            // Dual Column Adaptive Layout for Wide Screen / Tablet
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BatteryMetricsSection(batteryState, insights.timeRemainingEstimate)
                    BatteryTrendSection(insights.trendHistory)
                    AnomalySection(anomaly)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SmartAIAdvisorySection(insights.healthAdvisories)
                    FramePerformanceSection(frameState)
                    NetworkStatusSection(networkState)
                    NetworkApiSection(networkInfoState, onFetch = { viewModel.fetchNetworkInfo() })
                    WorkerTaskSection(workerState, onRunTask = { viewModel.triggerSyncWorker() })
                }
            }
        } else {
            // Single Column Stack for Phones
            BatteryMetricsSection(batteryState, insights.timeRemainingEstimate)
            if (anomaly.hasAnomaly) {
                AnomalySection(anomaly)
            }
            BatteryTrendSection(insights.trendHistory)
            SmartAIAdvisorySection(insights.healthAdvisories)
            FramePerformanceSection(frameState)
            NetworkStatusSection(networkState)
            NetworkApiSection(networkInfoState, onFetch = { viewModel.fetchNetworkInfo() })
            WorkerTaskSection(workerState, onRunTask = { viewModel.triggerSyncWorker() })
        }
    }
}

@Composable
private fun BatteryMetricsSection(batteryState: com.dev.myapplication.data.monitor.BatteryState, estimateStr: String) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hasGlowBorder = batteryState.isCharging
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            AnimatedBatteryGauge(
                level = batteryState.level,
                isCharging = batteryState.isCharging
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = "Battery Metrics", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "Status: ${batteryState.status}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Power Source: ${batteryState.plugType}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Health: ${batteryState.health}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "Temp: ${batteryState.temperature}°C  |  ${batteryState.voltage}mV", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                Text(text = "Estimate: $estimateStr", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun BatteryTrendSection(trendHistory: List<com.dev.myapplication.ui.components.BatteryDataPoint>) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        BatteryTrendVisualizer(dataPoints = trendHistory)
    }
}

@Composable
private fun AnomalySection(anomaly: com.dev.myapplication.domain.AnomalyReport) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hasGlowBorder = anomaly.hasAnomaly
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Anomaly",
                tint = if (anomaly.hasAnomaly) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = anomaly.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (anomaly.hasAnomaly) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Text(
                    text = anomaly.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun SmartAIAdvisorySection(advisories: List<String>) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hasGlowBorder = true
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI Insights",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Smart AI System Advisory",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            advisories.forEach { advisory ->
                Text(
                    text = "• $advisory",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun FramePerformanceSection(frameState: com.dev.myapplication.domain.FramePerformanceState) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = "Frame Telemetry",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "UI Frame Render Telemetry", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    text = "${frameState.currentFps} FPS | ${frameState.frameDurationMs}ms frame time | ${frameState.status}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun NetworkStatusSection(networkState: com.dev.myapplication.data.monitor.NetworkState) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (networkState.isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                contentDescription = "Network Status",
                tint = if (networkState.isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Network Connectivity", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = if (networkState.isConnected) "Connected (${networkState.transportType})" else "Disconnected",
                    fontSize = 13.sp,
                    color = if (networkState.isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
                if (networkState.isConnected) {
                    Text(
                        text = "Metered: ${if (networkState.isMetered) "Yes" else "No"} | Downstream: ${networkState.linkDownstreamKbps / 1000} Mbps",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkApiSection(networkInfoState: Resource<com.dev.myapplication.data.remote.NetworkInfoResponse>, onFetch: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Public,
                contentDescription = "Network Info API",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Public IP & Geo Diagnostics",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )

            OutlinedButton(
                onClick = onFetch,
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = "Fetch",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Fetch", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (networkInfoState) {
            is Resource.Idle -> {
                Text(text = "Tap 'Fetch' to retrieve external network context via Retrofit.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            is Resource.Loading -> {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Querying public network endpoint...", fontSize = 12.sp)
                }
            }
            is Resource.Success -> {
                val info = networkInfoState.data
                val locationParts = listOfNotNull(info.city, info.region, info.country).filter { it.isNotBlank() }
                val locationStr = if (locationParts.isNotEmpty()) locationParts.joinToString(", ") else "Global IP Network"

                Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(top = 4.dp)) {
                    Text(text = "Public IP: ${info.ip}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    Text(text = "Location: $locationStr", fontSize = 12.sp)
                    if (!info.org.isNull0OrBlank()) {
                        Text(text = "ISP / Org: ${info.org}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            is Resource.Error -> {
                Text(text = "Error: ${networkInfoState.message}", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun WorkerTaskSection(workerState: String, onRunTask: () -> Unit) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "WorkManager",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Background Worker Task", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = "WorkManager Status: $workerState",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(onClick = onRunTask) {
                Text(text = "Run Task", fontSize = 12.sp)
            }
        }
    }
}

private fun String?.isNull0OrBlank(): Boolean = this == null || this.isBlank() || this == "N/A"
