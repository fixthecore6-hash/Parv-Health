package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.data.ExerciseVisualType
import com.example.ui.theme.LocalAccentColors

@Composable
fun ExerciseIllustration(
    visualType: ExerciseVisualType,
    modifier: Modifier = Modifier
) {
    val accents = LocalAccentColors.current
    val bodyStrokeColor = MaterialTheme.colorScheme.onSurface
    val accentColor = accents.greenPrimary
    val bgColor = accents.greenTint

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val stroke = (w * 0.08f).coerceIn(2.5.dp.toPx(), 5.dp.toPx())
            val headR = w * 0.085f

            // Ground line
            drawLine(
                color = accentColor.copy(alpha = 0.45f),
                start = Offset(w * 0.08f, h * 0.86f),
                end = Offset(w * 0.92f, h * 0.86f),
                strokeWidth = stroke * 0.6f,
                cap = StrokeCap.Round
            )

            when (visualType) {
                ExerciseVisualType.PUSHUPS -> {
                    // Angled plank-pushup body
                    drawCircle(color = accentColor, radius = headR, center = Offset(w * 0.78f, h * 0.44f))
                    drawLine(bodyStrokeColor, Offset(w * 0.16f, h * 0.80f), Offset(w * 0.72f, h * 0.52f), stroke, StrokeCap.Round)
                    drawLine(accentColor, Offset(w * 0.70f, h * 0.53f), Offset(w * 0.70f, h * 0.82f), stroke, StrokeCap.Round)
                }
                ExerciseVisualType.SQUATS -> {
                    // Bent-knee squat silhouette
                    drawCircle(color = accentColor, radius = headR, center = Offset(w * 0.46f, h * 0.24f))
                    drawLine(bodyStrokeColor, Offset(w * 0.46f, h * 0.34f), Offset(w * 0.36f, h * 0.58f), stroke, StrokeCap.Round)
                    drawLine(accentColor, Offset(w * 0.36f, h * 0.58f), Offset(w * 0.62f, h * 0.60f), stroke, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.62f, h * 0.60f), Offset(w * 0.54f, h * 0.84f), stroke, StrokeCap.Round)
                    drawLine(accentColor, Offset(w * 0.45f, h * 0.40f), Offset(w * 0.72f, h * 0.44f), stroke * 0.8f, StrokeCap.Round)
                }
                ExerciseVisualType.LUNGES -> {
                    // Split lunge pose
                    drawCircle(color = accentColor, radius = headR, center = Offset(w * 0.48f, h * 0.20f))
                    drawLine(bodyStrokeColor, Offset(w * 0.48f, h * 0.30f), Offset(w * 0.48f, h * 0.55f), stroke, StrokeCap.Round)
                    // Front leg
                    drawLine(accentColor, Offset(w * 0.48f, h * 0.55f), Offset(w * 0.68f, h * 0.62f), stroke, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.68f, h * 0.62f), Offset(w * 0.68f, h * 0.84f), stroke, StrokeCap.Round)
                    // Back leg
                    drawLine(bodyStrokeColor, Offset(w * 0.48f, h * 0.55f), Offset(w * 0.30f, h * 0.74f), stroke, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.30f, h * 0.74f), Offset(w * 0.20f, h * 0.84f), stroke, StrokeCap.Round)
                }
                ExerciseVisualType.PLANK -> {
                    // Horizontal forearm plank
                    drawCircle(color = accentColor, radius = headR, center = Offset(w * 0.80f, h * 0.56f))
                    drawLine(bodyStrokeColor, Offset(w * 0.16f, h * 0.78f), Offset(w * 0.72f, h * 0.64f), stroke, StrokeCap.Round)
                    drawLine(accentColor, Offset(w * 0.68f, h * 0.65f), Offset(w * 0.68f, h * 0.82f), stroke, StrokeCap.Round)
                    drawLine(accentColor, Offset(w * 0.68f, h * 0.82f), Offset(w * 0.79f, h * 0.82f), stroke, StrokeCap.Round)
                }
                ExerciseVisualType.GLUTE_BRIDGES -> {
                    // Supine hip bridge
                    drawCircle(color = accentColor, radius = headR, center = Offset(w * 0.20f, h * 0.74f))
                    drawLine(bodyStrokeColor, Offset(w * 0.28f, h * 0.78f), Offset(w * 0.55f, h * 0.54f), stroke, StrokeCap.Round)
                    drawLine(accentColor, Offset(w * 0.55f, h * 0.54f), Offset(w * 0.74f, h * 0.64f), stroke, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.74f, h * 0.64f), Offset(w * 0.76f, h * 0.84f), stroke, StrokeCap.Round)
                }
                ExerciseVisualType.WALL_SITS -> {
                    // Wall line + 90-deg seated pose
                    drawLine(
                        color = bodyStrokeColor.copy(alpha = 0.35f),
                        start = Offset(w * 0.24f, h * 0.14f),
                        end = Offset(w * 0.24f, h * 0.86f),
                        strokeWidth = stroke * 0.8f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = accentColor, radius = headR, center = Offset(w * 0.35f, h * 0.26f))
                    drawLine(bodyStrokeColor, Offset(w * 0.33f, h * 0.36f), Offset(w * 0.33f, h * 0.60f), stroke, StrokeCap.Round)
                    drawLine(accentColor, Offset(w * 0.33f, h * 0.60f), Offset(w * 0.64f, h * 0.60f), stroke, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.64f, h * 0.60f), Offset(w * 0.64f, h * 0.84f), stroke, StrokeCap.Round)
                }
                ExerciseVisualType.SITUPS -> {
                    // Curled sit-up pose
                    drawCircle(color = accentColor, radius = headR, center = Offset(w * 0.34f, h * 0.42f))
                    drawLine(bodyStrokeColor, Offset(w * 0.36f, h * 0.52f), Offset(w * 0.48f, h * 0.78f), stroke, StrokeCap.Round)
                    drawLine(accentColor, Offset(w * 0.48f, h * 0.78f), Offset(w * 0.68f, h * 0.60f), stroke, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.68f, h * 0.60f), Offset(w * 0.78f, h * 0.83f), stroke, StrokeCap.Round)
                }
                ExerciseVisualType.JUMPING_JACKS -> {
                    // Star-jump pose
                    drawCircle(color = accentColor, radius = headR, center = Offset(w * 0.50f, h * 0.20f))
                    drawLine(bodyStrokeColor, Offset(w * 0.50f, h * 0.30f), Offset(w * 0.50f, h * 0.58f), stroke, StrokeCap.Round)
                    // Raised arms
                    drawLine(accentColor, Offset(w * 0.50f, h * 0.38f), Offset(w * 0.24f, h * 0.22f), stroke, StrokeCap.Round)
                    drawLine(accentColor, Offset(w * 0.50f, h * 0.38f), Offset(w * 0.76f, h * 0.22f), stroke, StrokeCap.Round)
                    // Wide legs
                    drawLine(bodyStrokeColor, Offset(w * 0.50f, h * 0.58f), Offset(w * 0.30f, h * 0.84f), stroke, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.50f, h * 0.58f), Offset(w * 0.70f, h * 0.84f), stroke, StrokeCap.Round)
                    // Motion arc
                    drawArc(
                        color = accentColor.copy(alpha = 0.5f),
                        startAngle = 200f,
                        sweepAngle = 140f,
                        useCenter = false,
                        style = Stroke(width = stroke * 0.45f, cap = StrokeCap.Round)
                    )
                }
                ExerciseVisualType.CHEST_PRESS -> {
                    // Bench / floor press silhouette + upward bar
                    drawCircle(color = accentColor, radius = headR, center = Offset(w * 0.24f, h * 0.68f))
                    drawLine(bodyStrokeColor, Offset(w * 0.30f, h * 0.72f), Offset(w * 0.76f, h * 0.72f), stroke, StrokeCap.Round)
                    drawLine(accentColor, Offset(w * 0.42f, h * 0.70f), Offset(w * 0.42f, h * 0.34f), stroke, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.28f, h * 0.34f), Offset(w * 0.56f, h * 0.34f), stroke * 1.1f, StrokeCap.Round)
                }
                ExerciseVisualType.BACK_ROW -> {
                    // Bent-over row pose
                    drawCircle(color = accentColor, radius = headR, center = Offset(w * 0.68f, h * 0.30f))
                    drawLine(bodyStrokeColor, Offset(w * 0.40f, h * 0.50f), Offset(w * 0.62f, h * 0.36f), stroke, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.40f, h * 0.50f), Offset(w * 0.36f, h * 0.84f), stroke, StrokeCap.Round)
                    drawLine(accentColor, Offset(w * 0.56f, h * 0.42f), Offset(w * 0.56f, h * 0.68f), stroke, StrokeCap.Round)
                    drawCircle(color = bodyStrokeColor, radius = headR * 0.65f, center = Offset(w * 0.56f, h * 0.72f))
                }
                ExerciseVisualType.BAND_PULL -> {
                    // Standing band pull / press pose
                    drawCircle(color = accentColor, radius = headR, center = Offset(w * 0.46f, h * 0.20f))
                    drawLine(bodyStrokeColor, Offset(w * 0.46f, h * 0.30f), Offset(w * 0.46f, h * 0.58f), stroke, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.46f, h * 0.58f), Offset(w * 0.36f, h * 0.84f), stroke, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.46f, h * 0.58f), Offset(w * 0.58f, h * 0.84f), stroke, StrokeCap.Round)
                    drawLine(accentColor, Offset(w * 0.24f, h * 0.40f), Offset(w * 0.76f, h * 0.40f), stroke * 0.8f, StrokeCap.Round)
                }
                ExerciseVisualType.PULL_UP -> {
                    // Overhead bar + hanging pull silhouette
                    drawLine(bodyStrokeColor, Offset(w * 0.16f, h * 0.16f), Offset(w * 0.84f, h * 0.16f), stroke, StrokeCap.Round)
                    drawLine(accentColor, Offset(w * 0.36f, h * 0.18f), Offset(w * 0.42f, h * 0.38f), stroke, StrokeCap.Round)
                    drawLine(accentColor, Offset(w * 0.64f, h * 0.18f), Offset(w * 0.58f, h * 0.38f), stroke, StrokeCap.Round)
                    drawCircle(color = accentColor, radius = headR, center = Offset(w * 0.50f, h * 0.30f))
                    drawLine(bodyStrokeColor, Offset(w * 0.50f, h * 0.40f), Offset(w * 0.50f, h * 0.64f), stroke, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.50f, h * 0.64f), Offset(w * 0.42f, h * 0.82f), stroke, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.50f, h * 0.64f), Offset(w * 0.58f, h * 0.82f), stroke, StrokeCap.Round)
                }
                ExerciseVisualType.SHOULDER_PRESS -> {
                    // Standing overhead press pose
                    drawCircle(color = accentColor, radius = headR, center = Offset(w * 0.50f, h * 0.28f))
                    drawLine(bodyStrokeColor, Offset(w * 0.50f, h * 0.38f), Offset(w * 0.50f, h * 0.62f), stroke, StrokeCap.Round)
                    drawLine(accentColor, Offset(w * 0.34f, h * 0.42f), Offset(w * 0.34f, h * 0.18f), stroke, StrokeCap.Round)
                    drawLine(accentColor, Offset(w * 0.66f, h * 0.42f), Offset(w * 0.66f, h * 0.18f), stroke, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.34f, h * 0.42f), Offset(w * 0.66f, h * 0.42f), stroke * 0.9f, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.50f, h * 0.62f), Offset(w * 0.38f, h * 0.84f), stroke, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.50f, h * 0.62f), Offset(w * 0.62f, h * 0.84f), stroke, StrokeCap.Round)
                }
                ExerciseVisualType.ARM_CURL -> {
                    // Standing arm curl pose with dumbbell highlight
                    drawCircle(color = accentColor, radius = headR, center = Offset(w * 0.48f, h * 0.20f))
                    drawLine(bodyStrokeColor, Offset(w * 0.48f, h * 0.30f), Offset(w * 0.48f, h * 0.58f), stroke, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.48f, h * 0.58f), Offset(w * 0.40f, h * 0.84f), stroke, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.48f, h * 0.58f), Offset(w * 0.58f, h * 0.84f), stroke, StrokeCap.Round)
                    drawLine(accentColor, Offset(w * 0.50f, h * 0.38f), Offset(w * 0.64f, h * 0.48f), stroke, StrokeCap.Round)
                    drawLine(accentColor, Offset(w * 0.64f, h * 0.48f), Offset(w * 0.72f, h * 0.32f), stroke, StrokeCap.Round)
                    drawCircle(color = bodyStrokeColor, radius = headR * 0.55f, center = Offset(w * 0.72f, h * 0.30f))
                }
                ExerciseVisualType.CARDIO_RUN -> {
                    // Dynamic stride running silhouette
                    drawCircle(color = accentColor, radius = headR, center = Offset(w * 0.56f, h * 0.22f))
                    drawLine(bodyStrokeColor, Offset(w * 0.54f, h * 0.32f), Offset(w * 0.46f, h * 0.56f), stroke, StrokeCap.Round)
                    drawLine(accentColor, Offset(w * 0.52f, h * 0.38f), Offset(w * 0.72f, h * 0.44f), stroke, StrokeCap.Round)
                    drawLine(accentColor, Offset(w * 0.52f, h * 0.38f), Offset(w * 0.32f, h * 0.46f), stroke, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.46f, h * 0.56f), Offset(w * 0.68f, h * 0.64f), stroke, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.68f, h * 0.64f), Offset(w * 0.74f, h * 0.84f), stroke, StrokeCap.Round)
                    drawLine(bodyStrokeColor, Offset(w * 0.46f, h * 0.56f), Offset(w * 0.28f, h * 0.76f), stroke, StrokeCap.Round)
                }
            }
        }
    }
}
