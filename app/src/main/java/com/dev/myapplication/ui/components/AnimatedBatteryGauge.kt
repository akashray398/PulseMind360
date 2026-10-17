package com.dev.myapplication.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.myapplication.ui.theme.BatteryGradientCharging
import com.dev.myapplication.ui.theme.BatteryGradientLow
import com.dev.myapplication.ui.theme.BatteryGradientNormal

@Composable
fun AnimatedBatteryGauge(
    level: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val animatedLevel by animateFloatAsState(
        targetValue = level.coerceIn(0, 100) / 100f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "BatteryLevelAnimation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "ChargingPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val gradient = when {
        isCharging -> BatteryGradientCharging
        level <= 20 -> BatteryGradientLow
        else -> BatteryGradientNormal
    }

    val outlineColor = MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = this.size.width
            val height = this.size.height

            val batteryWidth = width * 0.7f
            val batteryHeight = height * 0.85f
            val left = (width - batteryWidth) / 2f
            val top = height * 0.12f

            val strokeWidth = 6.dp.toPx()
            val corner = 12.dp.toPx()

            // Draw Battery Nub / Tip
            val nubWidth = batteryWidth * 0.35f
            val nubHeight = top * 0.6f
            val nubLeft = (width - nubWidth) / 2f
            drawRoundRect(
                color = outlineColor,
                topLeft = Offset(nubLeft, top - nubHeight),
                size = Size(nubWidth, nubHeight + strokeWidth),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Draw Battery Outer Body Outline
            drawRoundRect(
                color = outlineColor,
                topLeft = Offset(left, top),
                size = Size(batteryWidth, batteryHeight),
                cornerRadius = CornerRadius(corner, corner),
                style = Stroke(width = strokeWidth)
            )

            // Calculate Fill Height
            val padding = strokeWidth + 4.dp.toPx()
            val maxFillHeight = batteryHeight - (padding * 2)
            val fillHeight = maxFillHeight * animatedLevel
            val fillWidth = batteryWidth - (padding * 2)
            val fillTop = top + padding + (maxFillHeight - fillHeight)
            val fillLeft = left + padding

            if (fillHeight > 0) {
                drawRoundRect(
                    brush = gradient,
                    topLeft = Offset(fillLeft, fillTop),
                    size = Size(fillWidth, fillHeight),
                    cornerRadius = CornerRadius(corner / 2, corner / 2),
                    alpha = if (isCharging) pulseAlpha else 1.0f
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$level%",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (isCharging) {
                Text(
                    text = "CHARGING",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E676)
                )
            }
        }
    }
}
