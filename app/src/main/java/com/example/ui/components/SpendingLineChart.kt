package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalBorder
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.CharcoalText
import com.example.ui.theme.CoralRed
import com.example.ui.theme.GreenPrimary
import com.example.viewmodel.DailySpendPoint

@Composable
fun SpendingLineChart(
    dailyPoints: List<DailySpendPoint>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    var selectedPoint by remember { mutableStateOf<DailySpendPoint?>(null) }
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(dailyPoints) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(700))
    }

    val maxSpend = (dailyPoints.maxOfOrNull { it.spendAmount } ?: 100.0).coerceAtLeast(100.0)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Spending Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CharcoalText
                    )
                    Text(
                        text = if (selectedPoint != null) {
                            "Day ${selectedPoint!!.dayOfMonth}: ${CurrencyFormatter.format(selectedPoint!!.spendAmount, currencySymbol)}"
                        } else {
                            "Tap any point to inspect"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selectedPoint != null) CoralRed else CharcoalMuted,
                        fontWeight = if (selectedPoint != null) FontWeight.SemiBold else FontWeight.Normal
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CoralRed)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Expense",
                        style = MaterialTheme.typography.labelSmall,
                        color = CharcoalMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Line Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                        .pointerInput(dailyPoints) {
                            detectTapGestures { offset ->
                                if (dailyPoints.isEmpty()) return@detectTapGestures
                                val chartWidth = size.width
                                val stepX = chartWidth / (dailyPoints.size - 1).coerceAtLeast(1)
                                val index = (offset.x / stepX).toInt().coerceIn(0, dailyPoints.size - 1)
                                selectedPoint = dailyPoints[index]
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height
                    val bottomPadding = 20.dp.toPx()
                    val topPadding = 12.dp.toPx()
                    val usableHeight = h - bottomPadding - topPadding

                    // Draw subtle grid lines
                    val gridLines = 3
                    for (i in 0..gridLines) {
                        val y = topPadding + (usableHeight / gridLines) * i
                        drawLine(
                            color = CharcoalBorder.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    if (dailyPoints.size < 2) return@Canvas

                    val stepX = w / (dailyPoints.size - 1)
                    val points = dailyPoints.mapIndexed { i, p ->
                        val normY = (p.spendAmount / maxSpend).toFloat().coerceIn(0f, 1f)
                        val y = topPadding + usableHeight * (1f - (normY * animationProgress.value))
                        Offset(i * stepX, y)
                    }

                    // Build smooth cubic bezier path
                    val path = Path()
                    val fillPath = Path()

                    path.moveTo(points[0].x, points[0].y)
                    fillPath.moveTo(points[0].x, points[0].y)

                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                        val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)
                        path.cubicTo(
                            controlPoint1.x, controlPoint1.y,
                            controlPoint2.x, controlPoint2.y,
                            p1.x, p1.y
                        )
                        fillPath.cubicTo(
                            controlPoint1.x, controlPoint1.y,
                            controlPoint2.x, controlPoint2.y,
                            p1.x, p1.y
                        )
                    }

                    // Close fillPath to bottom
                    val lastX = points.last().x
                    val baseY = topPadding + usableHeight
                    fillPath.lineTo(lastX, baseY)
                    fillPath.lineTo(points.first().x, baseY)
                    fillPath.close()

                    // Draw Gradient Fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                CoralRed.copy(alpha = 0.25f),
                                CoralRed.copy(alpha = 0.02f)
                            ),
                            startY = topPadding,
                            endY = baseY
                        )
                    )

                    // Draw Line Path
                    drawPath(
                        path = path,
                        color = CoralRed,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw selected point or peak indicator
                    selectedPoint?.let { sel ->
                        val index = dailyPoints.indexOf(sel)
                        if (index in points.indices) {
                            val pt = points[index]
                            // Highlight vertical guide
                            drawLine(
                                color = CoralRed.copy(alpha = 0.4f),
                                start = Offset(pt.x, topPadding),
                                end = Offset(pt.x, baseY),
                                strokeWidth = 1.5.dp.toPx()
                            )
                            // Outer ring
                            drawCircle(
                                color = Color.White,
                                radius = 7.dp.toPx(),
                                center = pt
                            )
                            // Inner dot
                            drawCircle(
                                color = CoralRed,
                                radius = 4.5.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }
            }

            // X-axis Day labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val step = (dailyPoints.size / 5).coerceAtLeast(1)
                dailyPoints.filterIndexed { index, _ -> index % step == 0 || index == dailyPoints.lastIndex }
                    .take(6)
                    .forEach { point ->
                        Text(
                            text = "d${point.dayOfMonth}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CharcoalMuted,
                            fontSize = 10.sp
                        )
                    }
            }
        }
    }
}
