package com.dev.myapplication.ui.predictive

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.myapplication.domain.predictive.MetricPrediction
import com.dev.myapplication.domain.predictive.PredictiveOverview
import com.dev.myapplication.ui.components.ForecastChartDataPoint
import com.dev.myapplication.ui.components.ForecastTrendChart
import com.dev.myapplication.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictiveIntelligenceScreen(
    viewModel: PredictiveIntelligenceViewModel = hiltViewModel()
) {
    val overview by viewModel.overview.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Predictive Intelligence",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Personalized Statistical Forecast Models",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadPredictions() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Predictions",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            overview?.let { ov ->
                // Hero Confidence Overview
                item {
                    HeroPredictiveCard(ov = ov)
                }

                // 1. Battery Horizon Forecast
                item {
                    BatteryForecastSection(ov = ov)
                }

                // 2. Thermal Rate-of-Change Forecast
                item {
                    ThermalForecastSection(ov = ov)
                }

                // 3. RAM Memory Pressure Trajectory
                item {
                    MemoryForecastSection(ov = ov)
                }

                // 4. Network Reliability & Disconnection Trend
                item {
                    NetworkForecastSection(ov = ov)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun HeroPredictiveCard(ov: PredictiveOverview) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Predictive System Model", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Personalized Historical Statistical Baseline", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "${ov.overallConfidence}% Confidence",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun BatteryForecastSection(ov: PredictiveOverview) {
    val bFc = ov.batteryForecast
    val historical = listOf(
        ForecastChartDataPoint("-1h", 75f),
        ForecastChartDataPoint("-45m", 71f),
        ForecastChartDataPoint("-30m", 67f),
        ForecastChartDataPoint("-15m", 64f),
        ForecastChartDataPoint("Now", 61f)
    )
    val predicted = listOf(
        ForecastChartDataPoint("+15m", 58f, isFuturePrediction = true),
        ForecastChartDataPoint("+30m", 54f, isFuturePrediction = true),
        ForecastChartDataPoint("+1h", 46f, isFuturePrediction = true)
    )

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ForecastTrendChart(
                title = "Battery Discharge Horizon",
                historicalPoints = historical,
                predictedPoints = predicted
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PredictionMetricPill("Discharge Velocity", "${"%.1f".format(bFc.dischargeRatePctPerHour)}%/hr")
                PredictionMetricPill("Horizon to 20%", "~${bFc.timeTo20PctMins} mins")
                PredictionMetricPill("Horizon to 0%", "~${bFc.timeToEmptyMins} mins")
            }

            PredictionDetailsCard(prediction = bFc.prediction)
        }
    }
}

@Composable
fun ThermalForecastSection(ov: PredictiveOverview) {
    val tFc = ov.thermalForecast
    val historical = listOf(
        ForecastChartDataPoint("-1h", 31f),
        ForecastChartDataPoint("-30m", 33f),
        ForecastChartDataPoint("Now", 35f)
    )
    val predicted = listOf(
        ForecastChartDataPoint("+15m", tFc.predictedTemp15m, isFuturePrediction = true),
        ForecastChartDataPoint("+30m", tFc.predictedTemp30m, isFuturePrediction = true)
    )

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ForecastTrendChart(
                title = "Thermal Slope & Overheat Trajectory",
                historicalPoints = historical,
                predictedPoints = predicted
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PredictionMetricPill("Rate of Change", "${"%.2f".format(tFc.rateOfChangePerMin)}°C/min")
                PredictionMetricPill("Predicted +15m", "${"%.1f".format(tFc.predictedTemp15m)}°C")
                PredictionMetricPill("Predicted +30m", "${"%.1f".format(tFc.predictedTemp30m)}°C")
            }

            PredictionDetailsCard(prediction = tFc.prediction)
        }
    }
}

@Composable
fun MemoryForecastSection(ov: PredictiveOverview) {
    val mFc = ov.memoryTrend

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "RAM Memory Pressure Trajectory", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PredictionMetricPill("RAM Usage Slope", "${"%.2f".format(mFc.ramUsageSlopePctPerMin)}%/min")
                PredictionMetricPill("Projected +30m", "${mFc.predictedRam30m}%")
            }

            PredictionDetailsCard(prediction = mFc.prediction)
        }
    }
}

@Composable
fun NetworkForecastSection(ov: PredictiveOverview) {
    val nFc = ov.networkTrend

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Network Reliability Trend", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PredictionMetricPill("Stability Score", "${nFc.stabilityScorePct}%")
                PredictionMetricPill("24h Drop Count", "${nFc.dropCount}")
            }

            PredictionDetailsCard(prediction = nFc.prediction)
        }
    }
}

@Composable
fun PredictionMetricPill(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun PredictionDetailsCard(prediction: MetricPrediction) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "📊 Algorithm: ${prediction.algorithmUsed}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(text = "🔍 Historical Evidence: ${prediction.historicalEvidence}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "⚠️ Limitations: ${prediction.limitations}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
