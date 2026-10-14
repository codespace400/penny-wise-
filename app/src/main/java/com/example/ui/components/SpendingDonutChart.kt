package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.CharcoalText
import com.example.viewmodel.CategorySpendItem
import kotlin.math.atan2
import kotlin.math.sqrt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpendingDonutChart(
    spendItems: List<CategorySpendItem>,
    totalSpent: Double,
    currencySymbol: String,
    onCategoryClick: ((CategorySpendItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedItem by remember { mutableStateOf<CategorySpendItem?>(null) }
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(spendItems) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (spendItems.isEmpty() || totalSpent <= 0.0) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No spending\nrecorded yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = CharcoalMuted,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Donut Canvas with Center Text
            Box(
                modifier = Modifier.size(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(210.dp)
                        .pointerInput(spendItems, totalSpent) {
                            detectTapGestures { offset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val dx = offset.x - center.x
                                val dy = offset.y - center.y
                                val dist = sqrt(dx * dx + dy * dy)
                                val outerR = size.width / 2f
                                val innerR = outerR - 40.dp.toPx()

                                if (dist in innerR..outerR) {
                                    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                    if (angle < 0) angle += 360f
                                    val chartAngle = (angle + 90f) % 360f

                                    var currentAngle = 0f
                                    for (item in spendItems) {
                                        val sweep = (item.totalAmount / totalSpent).toFloat() * 360f
                                        if (chartAngle in currentAngle..(currentAngle + sweep)) {
                                            selectedItem = if (selectedItem == item) null else item
                                            onCategoryClick?.invoke(item)
                                            break
                                        }
                                        currentAngle += sweep
                                    }
                                } else {
                                    selectedItem = null
                                }
                            }
                        }
                ) {
                    val strokeWidth = 32.dp.toPx()
                    val padding = strokeWidth / 2f
                    val arcSize = Size(size.width - padding * 2, size.height - padding * 2)
                    val arcOffset = Offset(padding, padding)

                    var startAngle = -90f
                    val totalProgress = animatedProgress.value

                    spendItems.forEach { item ->
                        val sliceSweep = (item.totalAmount / totalSpent).toFloat() * 360f * totalProgress
                        val isSelected = selectedItem == item
                        val drawStrokeWidth = if (isSelected) strokeWidth + 6.dp.toPx() else strokeWidth

                        drawArc(
                            color = item.category.color,
                            startAngle = startAngle + 1f,
                            sweepAngle = (sliceSweep - 2f).coerceAtLeast(0f),
                            useCenter = false,
                            topLeft = arcOffset,
                            size = arcSize,
                            style = Stroke(width = drawStrokeWidth, cap = StrokeCap.Round)
                        )
                        startAngle += sliceSweep
                    }
                }

                // Center Label
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = if (selectedItem != null) selectedItem!!.category.name else "Total Spent",
                        style = MaterialTheme.typography.labelMedium,
                        color = CharcoalMuted,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = CurrencyFormatter.format(
                            selectedItem?.totalAmount ?: totalSpent,
                            currencySymbol
                        ),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = CharcoalText,
                        textAlign = TextAlign.Center
                    )
                    if (selectedItem != null) {
                        Text(
                            text = "${(selectedItem!!.percentage * 100).toInt()}% of total",
                            style = MaterialTheme.typography.labelSmall,
                            color = selectedItem!!.category.color,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Legend pills
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                spendItems.take(6).forEach { item ->
                    val isSelected = selectedItem == item
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) item.category.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                selectedItem = if (isSelected) null else item
                                onCategoryClick?.invoke(item)
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(item.category.color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.category.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = CharcoalText,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${(item.percentage * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = CharcoalMuted
                            )
                        }
                    }
                }
            }
        }
    }
}
