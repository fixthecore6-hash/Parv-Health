package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DailyStepsEntity
import com.example.data.DateUtils
import com.example.ui.theme.LocalAccentColors
import java.text.NumberFormat

@Composable
fun WeeklyStepChart(
    days: List<DailyStepsEntity>,
    goal: Int,
    modifier: Modifier = Modifier
) {
    if (days.isEmpty()) return

    var selectedIndex by remember(days.size) { mutableIntStateOf(days.lastIndex) }
    val selectedDay = days.getOrNull(selectedIndex) ?: days.last()
    val maxSteps = maxOf(goal, days.maxOfOrNull { it.steps } ?: goal, 1000)

    val accents = LocalAccentColors.current
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val goalLineColor = accents.goalLine.copy(alpha = 0.65f)
    val numberFormatter = NumberFormat.getIntegerInstance()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = DateUtils.formatDisplayDate(
                    selectedDay.date,
                    stringResource(R.string.label_today),
                    stringResource(R.string.label_yesterday)
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(accents.greenTint)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${numberFormatter.format(selectedDay.steps)} steps",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(156.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val goalRatio = (goal.toFloat() / maxSteps.toFloat()).coerceIn(0.05f, 1f)
                val goalY = size.height * (1f - goalRatio)
                drawLine(
                    color = goalLineColor,
                    start = Offset(0f, goalY),
                    end = Offset(size.width, goalY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                days.forEachIndexed { index, day ->
                    val targetRatio = (day.steps.toFloat() / maxSteps.toFloat()).coerceIn(0f, 1f)
                    val animatedRatio by animateFloatAsState(
                        targetValue = targetRatio,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "bar_$index"
                    )
                    val isSelected = index == selectedIndex
                    val barBrush = Brush.verticalGradient(
                        colors = if (isSelected || day.goalReached) {
                            listOf(accents.gradientEnd, accents.gradientStart)
                        } else {
                            listOf(
                                accents.gradientEnd.copy(alpha = 0.72f),
                                accents.gradientStart.copy(alpha = 0.72f)
                            )
                        }
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("weekly_chart_bar_$index")
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { selectedIndex = index },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth(0.62f)
                                .fillMaxHeight()
                        ) {
                            val cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            drawRoundRect(
                                color = trackColor.copy(alpha = if (isSelected) 0.85f else 0.45f),
                                topLeft = Offset.Zero,
                                size = size,
                                cornerRadius = cornerRadius
                            )
                            val minBarPx = if (day.steps > 0) 6.dp.toPx() else 0f
                            val barHeight = (size.height * animatedRatio).coerceAtLeast(minBarPx)
                            if (barHeight > 0f) {
                                drawRoundRect(
                                    brush = barBrush,
                                    topLeft = Offset(0f, size.height - barHeight),
                                    size = Size(size.width, barHeight),
                                    cornerRadius = cornerRadius
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            days.forEachIndexed { index, day ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = DateUtils.shortWeekday(day.date),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}
