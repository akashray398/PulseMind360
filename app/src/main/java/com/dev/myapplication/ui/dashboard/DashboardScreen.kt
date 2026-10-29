package com.dev.myapplication.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
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
import com.dev.myapplication.ui.theme.StatusError
import com.dev.myapplication.ui.theme.StatusGood
import com.dev.myapplication.ui.theme.StatusWarning
import com.dev.myapplication.ui.theme.VioletSecondary
import com.dev.myapplication.util.MemoryOptimizer
import com.dev.myapplication.util.NetworkThroughputState
import com.dev.myapplication.util.ReportGenerator

@Composable
fun DashboardScreen(
    onOpenDeskStand: () -> Unit = {},
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
        TitleBanner()

        // Hero System Health Overview Card
        SystemHealthHeroCard(
            healthScore = healthScore,
            batteryLevel = batteryState.level,
            isCharging = batteryState.isCharging,
            networkConnected = networkState.isConnected,
            hasAnomaly = anomaly.hasAnomaly
        )

        // Quick Action Chips Row
        val context = LocalContext.current
        QuickActionsRow(
            onFetchNetwork = { viewModel.fetchNetworkInfo() },
            onTriggerSync = { viewModel.triggerSyncWorker() },
            onExportReport = {
                ReportGenerator.generateAndSharePdfReport(
                    context = context,
                    healthScore = healthScore,
                    batteryState = batteryState,
                    networkState = networkState,
                    anomalyReport = anomaly,
                    frameState = frameState,
                    redactMetadata = true
                )
            },
            onOpenDeskStand = onOpenDeskStand
        )

        // PulseMind Sentinel Predictive AI Intelligence Card
        SentinelIntelligenceCard(sentinelReport)

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
                    BatteryMetricsSection(batteryState, insights)
                    BatteryTrendSection(insights.trendHistory)
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SmartAIAdvisorySection(insights.healthAdvisories)
                    UnifiedNetworkSection(
                        networkState = networkState,
                        throughputState = throughputState,
                        networkInfoState = networkInfoState,
                        onFetchInfo = { viewModel.fetchNetworkInfo() }
                    )
                    MemoryOptimizationSection()
                    DeveloperDiagnosticsSection(
                        frameState = frameState,
                        workerState = workerState,
                        onRunWorker = { viewModel.triggerSyncWorker() }
                    )
                }
            }
        } else {
            // Single Column Stack for Phones
            BatteryMetricsSection(batteryState, insights)
            BatteryTrendSection(insights.trendHistory)
            SmartAIAdvisorySection(insights.healthAdvisories)
            UnifiedNetworkSection(
                networkState = networkState,
                throughputState = throughputState,
                networkInfoState = networkInfoState,
                onFetchInfo = { viewModel.fetchNetworkInfo() }
            )
            MemoryOptimizationSection()
            DeveloperDiagnosticsSection(
                frameState = frameState,
                workerState = workerState,
                onRunWorker = { viewModel.triggerSyncWorker() }
            )
        }
    }
}

@Composable
private fun TitleBanner() {
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
}

@Composable
private fun SystemHealthHeroCard(
    healthScore: Int,
    batteryLevel: Int,
    isCharging: Boolean,
    networkConnected: Boolean,
    hasAnomaly: Boolean
) {
    val animatedScore by animateIntAsState(
        targetValue = healthScore,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "HealthScoreAnimation"
    )

    val (scoreColor, scoreLabel) = when {
        animatedScore >= 85 -> StatusGood to "OPTIMAL HEALTH"
        animatedScore >= 65 -> StatusWarning to "STABLE"
        else -> StatusError to "ACTION REQUIRED"
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hasGlowBorder = true
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Radial Progress Indicator Score Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(84.dp)
            ) {
                CircularProgressIndicator(
                    progress = { animatedScore / 100f },
                    modifier = Modifier.size(80.dp),
                    color = scoreColor,
                    strokeWidth = 7.dp,
                    trackColor = scoreColor.copy(alpha = 0.15f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$animatedScore%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "SCORE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = scoreColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, scoreColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = scoreLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = scoreColor,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = "Contextual System Health",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = "Battery",
                            tint = if (isCharging) StatusGood else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "$batteryLevel%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (networkConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                            contentDescription = "Network",
                            tint = if (networkConnected) StatusGood else StatusError,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (networkConnected) "Online" else "Offline",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (hasAnomaly) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = "Anomaly",
                            tint = if (hasAnomaly) StatusError else StatusGood,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (hasAnomaly) "Alert" else "Clear",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsRow(
    onFetchNetwork: () -> Unit,
    onTriggerSync: () -> Unit,
    onExportReport: () -> Unit,
    onOpenDeskStand: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onFetchNetwork() },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "IP Context",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "IP Context",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTriggerSync() },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Run Sync",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sync Worker",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onExportReport() },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = "Audit PDF",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Export PDF Report",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenDeskStand() },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Nightlight,
                        contentDescription = "Desk Monitor",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ambient Desk Monitor",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun BatteryMetricsSection(
    batteryState: com.dev.myapplication.data.monitor.BatteryState,
    insights: com.dev.myapplication.domain.SystemInsights
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hasGlowBorder = batteryState.isCharging
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Battery & Thermal AI Forecast", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (insights.isSmartChargeGuardActive) {
                            Surface(
                                color = StatusWarning.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, StatusWarning.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = "80% GUARD",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusWarning,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(text = "Status: ${batteryState.status} (${batteryState.plugType})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "Estimate: ${insights.timeRemainingWithMargin}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    Text(text = "Rate Velocity: ${insights.dischargeRatePctPerHour}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Text(text = "Thermal Velocity: ${insights.thermalVelocityText}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (insights.isBaselineDeviation) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Text(
                    text = insights.baselineDeviationText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = StatusWarning
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Text(
                text = "ℹ️ ${insights.disclaimerText}",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
private fun SentinelIntelligenceCard(sentinelReport: com.dev.myapplication.domain.SentinelReport) {
    val (statusColor, statusLabel) = when (sentinelReport.severity) {
        "CRITICAL" -> StatusError to "CRITICAL SENTINEL SPIKE"
        "WARNING" -> StatusWarning to "PREDICTIVE ANOMALY"
        else -> StatusGood to "SENTINEL AI NOMINAL"
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hasGlowBorder = sentinelReport.hasAnomaly
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = "Sentinel Intelligence",
                tint = statusColor,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sentinelReport.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = statusColor
                )
                Text(
                    text = "${sentinelReport.confidenceScore}% Confidence · Self-Learning Baseline",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
            ) {
                Text(
                    text = statusLabel,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Root Cause: ${sentinelReport.rootCauseExplanation}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Evidence: ${sentinelReport.supportingEvidence}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Action: ${sentinelReport.recommendedAction}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        Text(
            text = sentinelReport.baselineSummary,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UnifiedNetworkSection(
    networkState: com.dev.myapplication.data.monitor.NetworkState,
    throughputState: com.dev.myapplication.util.NetworkThroughputState,
    networkInfoState: Resource<com.dev.myapplication.data.remote.NetworkInfoResponse>,
    onFetchInfo: () -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (networkState.isConnected) Icons.Default.Wifi else Icons.Default.WifiOff,
                contentDescription = "Network Status",
                tint = if (networkState.isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Network & Bandwidth Hub", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    if (throughputState.isHighBandwidthActive) {
                        Surface(
                            color = StatusWarning.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, StatusWarning.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "HIGH TRAFFIC",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusWarning,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = if (networkState.isConnected) "Connected (${networkState.transportType}) · ${if (networkState.isMetered) "Metered" else "Unmetered"}" else "Disconnected",
                    fontSize = 12.sp,
                    color = if (networkState.isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )

                // Live Speed Throughput Indicators
                val dlSpeedStr = if (throughputState.downloadKbps >= 1024f) {
                    "%.1f MB/s".format(throughputState.downloadKbps / 1024f)
                } else {
                    "%.0f KB/s".format(throughputState.downloadKbps)
                }

                val ulSpeedStr = if (throughputState.uploadKbps >= 1024f) {
                    "%.1f MB/s".format(throughputState.uploadKbps / 1024f)
                } else {
                    "%.0f KB/s".format(throughputState.uploadKbps)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Download",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "DL: $dlSpeedStr", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Upload",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "UL: $ulSpeedStr", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            OutlinedButton(
                onClick = onFetchInfo,
                contentPadding = ButtonDefaults.ContentPadding,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = "Fetch IP",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "IP Info", fontSize = 11.sp)
            }
        }

        when (networkInfoState) {
            is Resource.Idle -> { }
            is Resource.Loading -> {
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Querying public network endpoint...", fontSize = 12.sp)
                }
            }
            is Resource.Success -> {
                val info = networkInfoState.data
                val locationParts = listOfNotNull(info.city, info.region, info.country).filter { it.isNotBlank() }
                val locationStr = if (locationParts.isNotEmpty()) locationParts.joinToString(", ") else "Global IP Network"

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = "Public IP: ${info.ip}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text(text = "Location: $locationStr", fontSize = 11.sp)
                    if (!info.org.isNull0OrBlank()) {
                        Text(text = "ISP / Org: ${info.org}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            is Resource.Error -> {
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Text(text = "Error: ${networkInfoState.message}", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun MemoryOptimizationSection() {
    val context = LocalContext.current
    var memState by remember { mutableStateOf(MemoryOptimizer.getMemoryState(context)) }
    var freedMsg by remember { mutableStateOf<String?>(null) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = "RAM Optimizer",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "RAM & Cache Memory Diagnostic", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                val usedGbStr = "%.1f".format(memState.usedMemMb / 1024f)
                val totalGbStr = "%.1f".format(memState.totalMemMb / 1024f)
                Text(
                    text = "RAM: ${usedGbStr}GB / ${totalGbStr}GB (${memState.usagePct}% Used)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = {
                    val freed = MemoryOptimizer.trimMemoryCaches(context)
                    memState = MemoryOptimizer.getMemoryState(context)
                    freedMsg = "Freed ${freed} MB cache memory!"
                },
                contentPadding = ButtonDefaults.ContentPadding
            ) {
                Text(text = "Trim RAM", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val progressColor = when {
            memState.usagePct >= 85 -> StatusError
            memState.usagePct >= 70 -> StatusWarning
            else -> StatusGood
        }

        LinearProgressIndicator(
            progress = { memState.usagePct / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = progressColor,
            trackColor = progressColor.copy(alpha = 0.15f)
        )

        freedMsg?.let { msg ->
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = msg,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = StatusGood
            )
        }
    }
}

@Composable
private fun DeveloperDiagnosticsSection(
    frameState: com.dev.myapplication.domain.FramePerformanceState,
    workerState: String,
    onRunWorker: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
        ) {
            Icon(
                imageVector = Icons.Default.DeveloperMode,
                contentDescription = "Developer Telemetry",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Developer & System Telemetry",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                // Frame Telemetry
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "FPS",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "UI Frame Render Telemetry", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "${frameState.currentFps} FPS | ${frameState.frameDurationMs}ms frame time (${frameState.status})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // WorkManager
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Worker",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Background Worker Task", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "WorkManager Status: $workerState",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Button(
                        onClick = onRunWorker,
                        contentPadding = ButtonDefaults.ContentPadding
                    ) {
                        Text(text = "Run Worker", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

private fun String?.isNull0OrBlank(): Boolean = this == null || this.isBlank() || this == "N/A"
