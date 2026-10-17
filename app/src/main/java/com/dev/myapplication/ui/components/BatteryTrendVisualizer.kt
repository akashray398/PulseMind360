package com.dev.myapplication.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.myapplication.ui.theme.CyanPrimary
import com.dev.myapplication.ui.theme.VioletSecondary

data class BatteryDataPoint(
    val timeLabel: String,
    val level: Int, // 0 - 100
    val temp: Float // e.g. 30.0 - 45.0
)

@Composable
fun BatteryTrendVisualizer(
    dataPoints: List<BatteryDataPoint>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Live Battery & Thermal Curve",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("● Level (%)", fontSize = 11.sp, color = CyanPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("● Temp (°C)", fontSize = 11.sp, color = VioletSecondary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            val width = size.width
            val height = size.height

            val maxPoints = dataPoints.size
            val xSpacing = if (maxPoints > 1) width / (maxPoints - 1) else width

            // Draw Background Grid Lines
            val gridColor = Color.Gray.copy(alpha = 0.15f)
            drawLine(gridColor, Offset(0f, height * 0.25f), Offset(width, height * 0.25f), strokeWidth = 1.dp.toPx())
            drawLine(gridColor, Offset(0f, height * 0.50f), Offset(width, height * 0.50f), strokeWidth = 1.dp.toPx())
            drawLine(gridColor, Offset(0f, height * 0.75f), Offset(width, height * 0.75f), strokeWidth = 1.dp.toPx())

            val levelPath = Path()
            val tempPath = Path()

            dataPoints.forEachIndexed { index, dp ->
                val x = index * xSpacing
                // Level curve (y maps 0..100 -> height..0)
                val levelY = height - (dp.level / 100f * height)

                // Temp curve (y maps 20°C..45°C -> height..0)
                val clampedTemp = dp.temp.coerceIn(20f, 45f)
                val tempY = height - ((clampedTemp - 20f) / 25f * height)

                if (index == 0) {
                    levelPath.moveTo(x, levelY)
                    tempPath.moveTo(x, tempY)
                } else {
                    val previousX = (index - 1) * xSpacing
                    val prevLevelY = height - (dataPoints[index - 1].level / 100f * height)
                    val prevTempClamped = dataPoints[index - 1].temp.coerceIn(20f, 45f)
                    val prevTempY = height - ((prevTempClamped - 20f) / 25f * height)

                    val controlX1 = previousX + (xSpacing / 2f)
                    val controlX2 = previousX + (xSpacing / 2f)

                    levelPath.cubicTo(controlX1, prevLevelY, controlX2, levelY, x, levelY)
                    tempPath.cubicTo(controlX1, prevTempY, controlX2, tempY, x, tempY)
                }

                // Draw Data Points
                drawCircle(color = CyanPrimary, radius = 3.dp.toPx(), center = Offset(x, levelY))
                drawCircle(color = VioletSecondary, radius = 3.dp.toPx(), center = Offset(x, tempY))
            }

            // Draw Paths
            drawPath(
                path = levelPath,
                color = CyanPrimary,
                style = Stroke(width = 2.5.dp.toPx())
            )
            drawPath(
                path = tempPath,
                color = VioletSecondary,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}
