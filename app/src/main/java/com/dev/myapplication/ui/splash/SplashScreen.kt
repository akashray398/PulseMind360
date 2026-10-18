package com.dev.myapplication.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.myapplication.ui.theme.CyanPrimary
import com.dev.myapplication.ui.theme.TechDarkBackground
import com.dev.myapplication.ui.theme.VioletSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var loadingText by remember { mutableStateOf("Initializing Core Telemetry...") }

    LaunchedEffect(Unit) {
        delay(700L)
        loadingText = "Connecting Neural Monitors..."
        delay(800L)
        loadingText = "System Nominal. Launching..."
        delay(700L)
        onSplashFinished()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "MotionPosterSplash")

    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Ring1"
    )

    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Alpha1"
    )

    val rotateAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(TechDarkBackground, Color(0xFF04060A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background Motion Poster Pulse Rings
        Canvas(modifier = Modifier.size(320.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.width / 2f

            // Outer Pulse Ring 1
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(CyanPrimary.copy(alpha = pulseAlpha1), Color.Transparent),
                    center = center,
                    radius = maxRadius * pulseScale1
                ),
                center = center,
                radius = maxRadius * pulseScale1
            )

            // Inner Rotating Accent Arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(CyanPrimary, VioletSecondary, Color.Transparent)
                ),
                startAngle = rotateAngle,
                sweepAngle = 240f,
                useCenter = false,
                style = Stroke(width = 4.dp.toPx())
            )
        }

        // Central Motion Emblem & Branding
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(100.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "PulseMind Logo",
                    tint = CyanPrimary,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "PULSEMIND 360",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CyanPrimary,
                letterSpacing = 2.sp
            )

            Text(
                text = "INTELLIGENT SYSTEM COMPANION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = VioletSecondary,
                letterSpacing = 2.5.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = loadingText,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
