package com.dev.myapplication.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.myapplication.ui.theme.CyanPrimary
import com.dev.myapplication.ui.theme.VioletSecondary

data class BatteryDataPoint(
    val timeLabel: String,
    val level: Int, // 0 - 100
    val temp: Float // e.g. 20.0 - 45.0
)

@Composable
fun BatteryTrendVisualizer(
    dataPoints: List<BatteryDataPoint>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val latestPoint = dataPoints.lastOrNull()

    val labelStyle = TextStyle(
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold
    )
    val timeStyle = TextStyle(
        fontSize = 9.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Header with legend and latest values badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Live Battery & Thermal Curve",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (latestPoint != null) {
                    val tempStr = "%.1f".format(latestPoint.temp)
                    Text(
                        text = "Current: ${latestPoint.level}% · ${tempStr}°C",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Legend Chips
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = CyanPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(CyanPrimary, RoundedCornerShape(3.dp)))
                        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                        Text("Level (%)", fontSize = 10.sp, color = CyanPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(
                    color = VioletSecondary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(VioletSecondary, RoundedCornerShape(3.dp)))
                        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                        Text("Temp (°C)", fontSize = 10.sp, color = VioletSecondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Chart Canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val width = size.width
            val totalHeight = size.height
            val chartBottom = totalHeight - 22.dp.toPx() // Leave room for time x-axis
            val chartTop = 16.dp.toPx() // Leave room for top value labels
            val chartHeight = chartBottom - chartTop

            val maxPoints = dataPoints.size
            val xSpacing = if (maxPoints > 1) width / (maxPoints - 1) else width

            // Draw Background Grid Lines
            val gridColor = Color.Gray.copy(alpha = 0.12f)
            val gridSteps = listOf(0.0f, 0.5f, 1.0f)
            gridSteps.forEach { ratio ->
                val y = chartTop + chartHeight * ratio
                drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 1.dp.toPx())
            }

            val levelPath = Path()
            val levelFillPath = Path()
            val tempPath = Path()

            val levelPoints = mutableListOf<Offset>()
            val tempPoints = mutableListOf<Offset>()

            dataPoints.forEachIndexed { index, dp ->
                val x = index * xSpacing

                // Map Level (0..100) -> chartBottom..chartTop
                val levelY = chartBottom - (dp.level / 100f * chartHeight)

                // Map Temp (20°C..45°C) -> chartBottom..chartTop
                val clampedTemp = dp.temp.coerceIn(20f, 45f)
                val tempY = chartBottom - ((clampedTemp - 20f) / 25f * chartHeight)

                levelPoints.add(Offset(x, levelY))
                tempPoints.add(Offset(x, tempY))

                if (index == 0) {
                    levelPath.moveTo(x, levelY)
                    levelFillPath.moveTo(x, chartBottom)
                    levelFillPath.lineTo(x, levelY)
                    tempPath.moveTo(x, tempY)
                } else {
                    val prevX = (index - 1) * xSpacing
                    val prevLevelY = levelPoints[index - 1].y
                    val prevTempY = tempPoints[index - 1].y

                    val controlX1 = prevX + (xSpacing / 2f)
                    val controlX2 = prevX + (xSpacing / 2f)

                    levelPath.cubicTo(controlX1, prevLevelY, controlX2, levelY, x, levelY)
                    levelFillPath.cubicTo(controlX1, prevLevelY, controlX2, levelY, x, levelY)
                    tempPath.cubicTo(controlX1, prevTempY, controlX2, tempY, x, tempY)
                }

                if (index == dataPoints.size - 1) {
                    levelFillPath.lineTo(x, chartBottom)
                    levelFillPath.close()
                }
            }

            // Draw Gradient Area below Battery Level Curve
            drawPath(
                path = levelFillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(CyanPrimary.copy(alpha = 0.20f), Color.Transparent),
                    startY = chartTop,
                    endY = chartBottom
                )
            )

            // Draw Curved Stroke Lines
            drawPath(path = levelPath, color = CyanPrimary, style = Stroke(width = 2.5.dp.toPx()))
            drawPath(path = tempPath, color = VioletSecondary, style = Stroke(width = 2.dp.toPx()))

            // Draw Dots, Value Labels, and X-Axis Time Labels
            dataPoints.forEachIndexed { index, dp ->
                val (x, levelY) = levelPoints[index]
                val (_, tempY) = tempPoints[index]

                // Draw circles for data points
                drawCircle(color = CyanPrimary, radius = 3.5.dp.toPx(), center = Offset(x, levelY))
                drawCircle(color = VioletSecondary, radius = 3.dp.toPx(), center = Offset(x, tempY))

                // Draw Numerical Battery Value Label above point
                val levelLabel = "${dp.level}%"
                val levelTextLayout = textMeasurer.measure(levelLabel, style = labelStyle.copy(color = CyanPrimary))
                val levelTextX = (x - levelTextLayout.size.width / 2f).coerceIn(0f, width - levelTextLayout.size.width)
                val levelTextY = (levelY - levelTextLayout.size.height - 3.dp.toPx()).coerceAtLeast(0f)

                drawText(
                    textLayoutResult = levelTextLayout,
                    topLeft = Offset(levelTextX, levelTextY)
                )

                // Draw Numerical Temp Value Label below/above point
                val tempLabel = "${"%.0f".format(dp.temp)}°"
                val tempTextLayout = textMeasurer.measure(tempLabel, style = labelStyle.copy(color = VioletSecondary))
                val tempTextX = (x - tempTextLayout.size.width / 2f).coerceIn(0f, width - tempTextLayout.size.width)
                val tempTextY = (tempY + 3.dp.toPx()).coerceAtMost(chartBottom - tempTextLayout.size.height)

                drawText(
                    textLayoutResult = tempTextLayout,
                    topLeft = Offset(tempTextX, tempTextY)
                )

                // Draw X-Axis Time Label below graph line
                val timeLayout = textMeasurer.measure(dp.timeLabel, style = timeStyle)
                val timeX = (x - timeLayout.size.width / 2f).coerceIn(0f, width - timeLayout.size.width)
                drawText(
                    textLayoutResult = timeLayout,
                    topLeft = Offset(timeX, totalHeight - timeLayout.size.height)
                )
            }
        }
    }
}
