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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dev.myapplication.ui.theme.CyanPrimary

data class ForecastChartDataPoint(
    val label: String,
    val value: Float,
    val isFuturePrediction: Boolean = false
)

@Composable
fun ForecastTrendChart(
    title: String,
    historicalPoints: List<ForecastChartDataPoint>,
    predictedPoints: List<ForecastChartDataPoint>,
    unit: String = "%",
    modifier: Modifier = Modifier
) {
    val allPoints = historicalPoints + predictedPoints
    if (allPoints.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)

            // Legend
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = CyanPrimary.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(CyanPrimary, RoundedCornerShape(3.dp)))
                        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                        Text("Historical", fontSize = 9.sp, color = CyanPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Surface(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(3.dp)))
                        Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                        Text("Predicted (+30m)", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        ) {
            val width = size.width
            val totalHeight = size.height
            val chartBottom = totalHeight - 18.dp.toPx()
            val chartTop = 10.dp.toPx()
            val chartHeight = chartBottom - chartTop

            val totalCount = allPoints.size
            val xSpacing = if (totalCount > 1) width / (totalCount - 1) else width

            val maxVal = allPoints.maxOf { it.value }.coerceAtLeast(100f)
            val minVal = allPoints.minOf { it.value }.coerceAtLeast(0f)
            val range = (maxVal - minVal).coerceAtLeast(1f)

            // Grid lines
            val gridColor = Color.Gray.copy(alpha = 0.12f)
            drawLine(gridColor, Offset(0f, chartTop), Offset(width, chartTop), strokeWidth = 1.dp.toPx())
            drawLine(gridColor, Offset(0f, chartBottom), Offset(width, chartBottom), strokeWidth = 1.dp.toPx())

            val solidPath = Path()
            val dashedPath = Path()

            val pointOffsets = mutableListOf<Offset>()

            allPoints.forEachIndexed { index, point ->
                val x = index * xSpacing
                val normY = (point.value - minVal) / range
                val y = chartBottom - (normY * chartHeight)
                pointOffsets.add(Offset(x, y))

                if (!point.isFuturePrediction) {
                    if (index == 0) solidPath.moveTo(x, y) else solidPath.lineTo(x, y)
                } else {
                    if (index > 0 && !allPoints[index - 1].isFuturePrediction) {
                        dashedPath.moveTo(pointOffsets[index - 1].x, pointOffsets[index - 1].y)
                    }
                    dashedPath.lineTo(x, y)
                }
            }

            // Draw solid historical stroke
            drawPath(path = solidPath, color = CyanPrimary, style = Stroke(width = 2.5.dp.toPx()))

            // Draw dashed predicted stroke
            drawPath(
                path = dashedPath,
                color = Color(0xFFD500F9),
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )

            // Draw nodes and x-axis labels
            allPoints.forEachIndexed { index, point ->
                val offset = pointOffsets[index]
                val dotColor = if (point.isFuturePrediction) Color(0xFFD500F9) else CyanPrimary

                drawCircle(color = dotColor, radius = 3.5.dp.toPx(), center = offset)

                val timeLayout = textMeasurer.measure(point.label, style = labelStyle)
                val timeX = (offset.x - timeLayout.size.width / 2f).coerceIn(0f, width - timeLayout.size.width)
                drawText(textLayoutResult = timeLayout, topLeft = Offset(timeX, totalHeight - timeLayout.size.height))
            }
        }
    }
}
