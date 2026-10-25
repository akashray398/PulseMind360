package com.dev.myapplication.ui.focustimer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.myapplication.data.local.entity.FocusSessionEntity
import com.dev.myapplication.ui.components.GlassCard
import com.dev.myapplication.ui.theme.CyanPrimary
import com.dev.myapplication.ui.theme.FuturisticGradient
import com.dev.myapplication.ui.theme.StatusGood
import com.dev.myapplication.ui.theme.StatusWarning
import com.dev.myapplication.util.FocusSoundEngine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun FocusTimerScreen(
    viewModel: FocusTimerViewModel = hiltViewModel()
) {
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val sessions by viewModel.sessionsState.collectAsStateWithLifecycle()

    var selectedSound by remember { mutableStateOf("OFF") } // OFF, RAIN, CYBER
    var customDurationMins by remember { mutableFloatStateOf(25f) }

    // Control Ambient Audio Engine based on timer & selection
    DisposableEffect(timerState.isRunning, selectedSound) {
        if (timerState.isRunning && !timerState.isPaused) {
            FocusSoundEngine.startAmbientSound(selectedSound)
        } else {
            FocusSoundEngine.stopAmbientSound()
        }
        onDispose {
            FocusSoundEngine.stopAmbientSound()
        }
    }

    val presets = remember {
        listOf(
            15 to "Short Focus (15m)",
            25 to "Pomodoro (25m)",
            45 to "Deep Work (45m)"
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "FOCUS MODE & ANALYTICS",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Productivity Analytics, Custom Timer & Ambient Sounds",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Focus Habit Streak & Analytics Card
        item {
            FocusAnalyticsCard(sessions = sessions)
        }

        // 7-Day Weekly Focus Bar Chart
        item {
            FocusWeeklyBarChart(sessions = sessions)
        }

        // Timer Setup & Ambient Audio Configuration Card
        item {
            TimerConfigurationCard(
                presets = presets,
                totalSeconds = timerState.totalSeconds,
                isRunning = timerState.isRunning,
                customDurationMins = customDurationMins,
                selectedSound = selectedSound,
                onSelectPreset = { mins, label -> viewModel.selectPreset(mins, label) },
                onCustomDurationChange = { customDurationMins = it },
                onSelectSound = { selectedSound = it }
            )
        }

        // Circular Pomodoro Timer Gauge Card
        item {
            PomodoroTimerCard(
                timerState = timerState,
                selectedSound = selectedSound,
                onStart = { viewModel.startTimer() },
                onPause = { viewModel.pauseTimer() },
                onResume = { viewModel.resumeTimer() },
                onReset = { viewModel.resetTimer() }
            )
        }

        // Recent Completed Sessions List
        item {
            Text(
                text = "Recent Completed Sessions",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        if (sessions.isEmpty()) {
            item {
                Text(
                    text = "No completed focus sessions yet.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(sessions, key = { it.id }) { session ->
                FocusSessionItem(session = session)
            }
        }
    }
}

@Composable
private fun TimerConfigurationCard(
    presets: List<Pair<Int, String>>,
    totalSeconds: Int,
    isRunning: Boolean,
    customDurationMins: Float,
    selectedSound: String,
    onSelectPreset: (Int, String) -> Unit,
    onCustomDurationChange: (Float) -> Unit,
    onSelectSound: (String) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Timer & Ambient Setup",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Preset Duration Chips Row (Equal Width 3 Buttons)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            presets.forEach { (mins, label) ->
                val isSelected = totalSeconds == mins * 60 && !isRunning
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            onCustomDurationChange(mins.toFloat())
                            onSelectPreset(mins, label)
                        },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "${mins}m",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Custom Duration",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${customDurationMins.toInt()} mins",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = customDurationMins,
            onValueChange = {
                onCustomDurationChange(it)
                val mins = it.toInt()
                onSelectPreset(mins, "Custom Focus (${mins}m)")
            },
            valueRange = 5f..120f,
            steps = 22
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

        // Ambient Sound Selector (Equal Width 3 Buttons)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = "Ambient Sound",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ambient Sound Generator",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf("OFF" to "Off", "RAIN" to "🌧️ Rain", "CYBER" to "⚡ Cyber").forEach { (type, label) ->
                val isSelected = selectedSound == type
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectSound(type) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .padding(vertical = 10.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun PomodoroTimerCard(
    timerState: com.dev.myapplication.domain.FocusTimerState,
    selectedSound: String,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hasGlowBorder = timerState.isRunning
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Status Header Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = timerState.currentPresetLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val (badgeBg, badgeText, badgeColor) = when {
                    timerState.isCompleted -> Triple(MaterialTheme.colorScheme.primaryContainer, "COMPLETED", MaterialTheme.colorScheme.primary)
                    timerState.isRunning && !timerState.isPaused -> Triple(StatusGood.copy(alpha = 0.15f), "DEEP FOCUS", StatusGood)
                    timerState.isPaused -> Triple(StatusWarning.copy(alpha = 0.15f), "PAUSED", StatusWarning)
                    else -> Triple(MaterialTheme.colorScheme.surfaceVariant, "READY", MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Animated Circular Timer Progress Indicator
            val progress = if (timerState.totalSeconds > 0) {
                timerState.remainingSeconds.toFloat() / timerState.totalSeconds
            } else 0f

            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(durationMillis = 500),
                label = "TimerProgress"
            )

            val outlineColor = MaterialTheme.colorScheme.outline
            val primaryColor = MaterialTheme.colorScheme.primary

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 12.dp.toPx()
                    drawArc(
                        color = outlineColor.copy(alpha = 0.18f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = stroke)
                    )
                    drawArc(
                        brush = FuturisticGradient,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val mins = timerState.remainingSeconds / 60
                    val secs = timerState.remainingSeconds % 60
                    val timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", mins, secs)

                    Text(
                        text = timeFormatted,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 1.sp
                    )
                    if (selectedSound != "OFF") {
                        Text(
                            text = "🎵 Ambient Audio Active",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Timer Control Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!timerState.isRunning) {
                    Button(
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        modifier = Modifier.height(42.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Focus", fontWeight = FontWeight.Bold)
                    }
                } else if (timerState.isPaused) {
                    Button(
                        onClick = onResume,
                        modifier = Modifier.height(42.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Resume")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Resume", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onPause,
                        modifier = Modifier.height(42.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pause", fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reset")
                }
            }
        }
    }
}

@Composable
fun FocusWeeklyBarChart(sessions: List<FocusSessionEntity>) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
    val dayStyle = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Medium, color = Color.Gray)

    // Compute focus minutes per day for last 7 days
    val daysData = remember(sessions) {
        val days = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val minutesPerDay = IntArray(7) { 0 }

        val cal = Calendar.getInstance()
        sessions.forEach { session ->
            cal.timeInMillis = session.startTime
            var dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 2 // Mon = 0
            if (dayOfWeek < 0) dayOfWeek = 6
            if (dayOfWeek in 0..6) {
                minutesPerDay[dayOfWeek] += session.durationMinutes
            }
        }
        days.mapIndexed { index, dayName -> dayName to minutesPerDay[index] }
    }

    val maxMins = remember(daysData) { (daysData.maxOfOrNull { it.second } ?: 60).coerceAtLeast(30) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(text = "7-Day Focus Weekly Analytics", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            val width = size.width
            val totalHeight = size.height
            val chartBottom = totalHeight - 20.dp.toPx()
            val chartTop = 16.dp.toPx()
            val chartHeight = chartBottom - chartTop

            val barCount = daysData.size
            val slotWidth = width / barCount
            val barWidth = 18.dp.toPx()

            daysData.forEachIndexed { index, (dayLabel, mins) ->
                val xCenter = index * slotWidth + (slotWidth / 2f)
                val barLeft = xCenter - (barWidth / 2f)

                val barHeightRatio = mins.toFloat() / maxMins
                val barTopY = chartBottom - (barHeightRatio * chartHeight)

                // Draw Bar
                drawRoundRect(
                    color = if (mins > 0) CyanPrimary else Color.Gray.copy(alpha = 0.2f),
                    topLeft = Offset(barLeft, if (mins > 0) barTopY else chartBottom - 4.dp.toPx()),
                    size = Size(barWidth, if (mins > 0) chartBottom - barTopY else 4.dp.toPx()),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Draw Minute Label above bar
                if (mins > 0) {
                    val labelResult = textMeasurer.measure("${mins}m", style = labelStyle)
                    drawText(
                        textLayoutResult = labelResult,
                        topLeft = Offset(xCenter - labelResult.size.width / 2f, barTopY - labelResult.size.height - 2.dp.toPx())
                    )
                }

                // Draw Day Label below bar
                val dayResult = textMeasurer.measure(dayLabel, style = dayStyle)
                drawText(
                    textLayoutResult = dayResult,
                    topLeft = Offset(xCenter - dayResult.size.width / 2f, totalHeight - dayResult.size.height)
                )
            }
        }
    }
}

@Composable
fun FocusAnalyticsCard(sessions: List<FocusSessionEntity>) {
    val totalMins = remember(sessions) { sessions.sumOf { it.durationMinutes } }
    val totalCount = sessions.size
    val streakDays = remember(sessions) {
        if (sessions.isEmpty()) 0 else {
            val dayMillis = 24 * 60 * 60 * 1000L
            val uniqueDays = sessions.map { it.startTime / dayMillis }.distinct().sortedDescending()
            var streak = 1
            for (i in 0 until uniqueDays.size - 1) {
                if (uniqueDays[i] - uniqueDays[i + 1] == 1L) {
                    streak++
                } else {
                    break
                }
            }
            streak
        }
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Whatshot,
                contentDescription = "Focus Streak",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Habit & Productivity Streak",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "$streakDays Day Focus Streak · $totalCount Completed Sessions",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$totalMins mins", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Text(text = "TOTAL FOCUS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$totalCount", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                Text(text = "SESSIONS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$streakDays days", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.secondary)
                Text(text = "STREAK", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun FocusSessionItem(session: FocusSessionEntity) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = session.label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "Duration: ${session.durationMinutes} min", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text(
                text = dateFormat.format(Date(session.startTime)),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
