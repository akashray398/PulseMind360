package com.dev.myapplication.ui.focustimer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dev.myapplication.data.local.entity.FocusSessionEntity
import com.dev.myapplication.ui.components.GlassCard
import com.dev.myapplication.ui.theme.FuturisticGradient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FocusTimerScreen(
    viewModel: FocusTimerViewModel = hiltViewModel()
) {
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val sessions by viewModel.sessionsState.collectAsStateWithLifecycle()

    val presets = remember {
        listOf(
            15 to "Short Focus (15m)",
            25 to "Pomodoro (25m)",
            45 to "Deep Work (45m)"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "FOCUS MODE",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Productivity Session & Persistence",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Focus Habit Streak & Analytics Card
        FocusAnalyticsCard(sessions = sessions)

        // Preset Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            presets.forEach { (mins, label) ->
                val isSelected = timerState.totalSeconds == mins * 60 && !timerState.isRunning
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectPreset(mins, label) },
                    label = { Text("${mins}m", fontSize = 12.sp) }
                )
            }
        }

        // Circular Timer Gauge Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            hasGlowBorder = timerState.isRunning
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = timerState.currentPresetLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Animated Circular Progress Indicator
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
                            color = outlineColor.copy(alpha = 0.2f),
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
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (timerState.isCompleted) {
                            Text(
                                text = "COMPLETED!",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Timer Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!timerState.isRunning) {
                        Button(
                            onClick = { viewModel.startTimer() },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Focus")
                        }
                    } else if (timerState.isPaused) {
                        Button(
                            onClick = { viewModel.resumeTimer() }
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Resume")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resume")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.pauseTimer() }
                        ) {
                            Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pause")
                        }
                    }

                    OutlinedButton(onClick = { viewModel.resetTimer() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset")
                    }
                }
            }
        }

        // Recent Completed Sessions List
        Text(
            text = "Recent Completed Sessions",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        if (sessions.isEmpty()) {
            Text(
                text = "No completed focus sessions yet.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sessions, key = { it.id }) { session ->
                    FocusSessionItem(session = session)
                }
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
