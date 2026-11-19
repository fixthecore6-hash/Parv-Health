package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalAccentColors
import java.text.NumberFormat
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun StepProgressRing(
    steps: Int,
    goal: Int,
    modifier: Modifier = Modifier
) {
    val rawProgress = if (goal > 0) (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "step_progress_ring"
    )

    val accents = LocalAccentColors.current
    val gradStart = accents.gradientStart
    val gradEnd = accents.gradientEnd
    val midGradient = lerp(gradStart, gradEnd, 0.5f)
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
    val innerGlowColor = accents.greenPrimary.copy(alpha = 0.12f)

    val numberFormatter = NumberFormat.getIntegerInstance()
    val percentage = ((steps.toFloat() / goal.coerceAtLeast(1)) * 100f).roundToInt()
    val goalReached = goal > 0 && steps >= goal

    Box(
        modifier = modifier.size(244.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()
            val inset = 22.dp.toPx()
            val arcDiameter = size.minDimension - inset * 2f
            val topLeft = Offset(
                x = (size.width - arcDiameter) / 2f,
                y = (size.height - arcDiameter) / 2f
            )
            val arcSize = Size(arcDiameter, arcDiameter)
            val startAngle = 135f
            val totalSweep = 270f
            val center = Offset(size.width / 2f, size.height / 2f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(innerGlowColor, innerGlowColor.copy(alpha = 0f)),
                    center = center,
                    radius = arcDiameter * 0.48f
                ),
                radius = arcDiameter * 0.48f,
                center = center
            )

            val tickCount = 32
            val outerTickRadius = arcDiameter / 2f + 16.dp.toPx()
            val innerTickRadius = arcDiameter / 2f + 11.dp.toPx()
            for (i in 0..tickCount) {
                val fraction = i.toFloat() / tickCount.toFloat()
                val angleDeg = startAngle + totalSweep * fraction
                val angleRad = angleDeg * (PI / 180.0)
                val cosA = cos(angleRad).toFloat()
                val sinA = sin(angleRad).toFloat()
                val isTickActive = fraction <= animatedProgress && animatedProgress > 0f
                val activeTickColor = lerp(gradStart, gradEnd, fraction).copy(alpha = 0.85f)
                drawLine(
                    color = if (isTickActive) activeTickColor else tickColor,
                    start = Offset(center.x + innerTickRadius * cosA, center.y + innerTickRadius * sinA),
                    end = Offset(center.x + outerTickRadius * cosA, center.y + outerTickRadius * sinA),
                    strokeWidth = if (i % 4 == 0) 2.5.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            if (animatedProgress > 0.002f) {
                val sweep = totalSweep * animatedProgress
                val wrapBlend = lerp(gradStart, gradEnd, 0.83f)
                drawArc(
                    brush = Brush.sweepGradient(
                        0.00f to wrapBlend,
                        0.125f to gradEnd,
                        0.375f to gradStart,
                        0.75f to midGradient,
                        1.00f to wrapBlend,
                        center = center
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                val tipAngleRad = (startAngle + sweep) * (PI / 180.0)
                val ringRadius = arcDiameter / 2f
                val tipOffset = Offset(
                    x = center.x + ringRadius * cos(tipAngleRad).toFloat(),
                    y = center.y + ringRadius * sin(tipAngleRad).toFloat()
                )
                val tipColor = lerp(gradStart, gradEnd, animatedProgress)
                drawCircle(
                    color = tipColor.copy(alpha = 0.30f),
                    radius = 12.dp.toPx(),
                    center = tipOffset
                )
                drawCircle(
                    color = tipColor,
                    radius = 5.5.dp.toPx(),
                    center = tipOffset
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (goalReached) Icons.Rounded.CheckCircle else Icons.Rounded.DirectionsWalk,
                contentDescription = null,
                tint = accents.greenPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = numberFormatter.format(steps),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.goal_format, numberFormatter.format(goal)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (goalReached) {
                            accents.greenTint
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (goalReached) {
                        "${stringResource(R.string.goal_reached_badge)} • $percentage%"
                    } else {
                        "$percentage%"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
