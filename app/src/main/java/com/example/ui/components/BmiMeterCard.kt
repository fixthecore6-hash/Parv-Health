package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.BmiCategory
import com.example.data.UserProfile
import com.example.ui.theme.LocalAccentColors
import com.example.ui.theme.LocalIsDarkTheme
import java.util.Locale

@Composable
fun BmiMeterCard(
    userProfile: UserProfile,
    onAddProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current
    val bmiValue = userProfile.bmi
    val category = userProfile.bmiCategory

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bmi_card"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = if (isDark) 2.dp else 4.dp
    ) {
        if (bmiValue == null || category == null) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accents.greenTint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MonitorWeight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.bmi_empty_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.bmi_card_title),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.bmi_empty_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onAddProfileClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("bmi_add_profile_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.bmi_add_profile_cta),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Text(
                    text = stringResource(R.string.bmi_disclaimer),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val categoryLabel = when (category) {
                BmiCategory.UNDERWEIGHT -> stringResource(R.string.bmi_underweight)
                BmiCategory.HEALTHY -> stringResource(R.string.bmi_healthy)
                BmiCategory.OVERWEIGHT -> stringResource(R.string.bmi_overweight)
                BmiCategory.OBESE -> stringResource(R.string.bmi_obese)
            }

            val minScale = 14.0f
            val maxScale = 38.0f
            val normalizedMarker = ((bmiValue - minScale) / (maxScale - minScale)).coerceIn(0.02f, 0.98f)
            val animatedMarker by animateFloatAsState(
                targetValue = normalizedMarker,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                ),
                label = "bmi_marker"
            )

            val markerFill = MaterialTheme.colorScheme.onSurface
            val markerRing = MaterialTheme.colorScheme.surface

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.bmi_card_title),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f", bmiValue),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("bmi_value_text")
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(accents.greenTint)
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = categoryLabel,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("bmi_category_text")
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(26.dp)
                    ) {
                        val barHeight = 12.dp.toPx()
                        val barTop = (size.height - barHeight) / 2f
                        val gap = 3.dp.toPx()
                        val totalSpan = maxScale - minScale

                        val f1 = (18.5f - minScale) / totalSpan
                        val f2 = (25.0f - minScale) / totalSpan
                        val f3 = (30.0f - minScale) / totalSpan

                        val x0 = 0f
                        val x1 = size.width * f1
                        val x2 = size.width * f2
                        val x3 = size.width * f3
                        val x4 = size.width

                        val corner = CornerRadius(6.dp.toPx(), 6.dp.toPx())

                        drawRoundRect(
                            color = accents.bmiUnderweight,
                            topLeft = Offset(x0, barTop),
                            size = Size((x1 - x0 - gap).coerceAtLeast(4f), barHeight),
                            cornerRadius = corner
                        )
                        drawRoundRect(
                            color = accents.bmiHealthy,
                            topLeft = Offset(x1 + gap / 2f, barTop),
                            size = Size((x2 - x1 - gap).coerceAtLeast(4f), barHeight),
                            cornerRadius = corner
                        )
                        drawRoundRect(
                            color = accents.bmiOverweight,
                            topLeft = Offset(x2 + gap / 2f, barTop),
                            size = Size((x3 - x2 - gap).coerceAtLeast(4f), barHeight),
                            cornerRadius = corner
                        )
                        drawRoundRect(
                            color = accents.bmiObese,
                            topLeft = Offset(x3 + gap / 2f, barTop),
                            size = Size((x4 - x3 - gap / 2f).coerceAtLeast(4f), barHeight),
                            cornerRadius = corner
                        )

                        val markerX = size.width * animatedMarker
                        val markerCenter = Offset(markerX, size.height / 2f)
                        drawCircle(
                            color = markerRing,
                            radius = 10.dp.toPx(),
                            center = markerCenter
                        )
                        drawCircle(
                            color = markerFill,
                            radius = 6.5.dp.toPx(),
                            center = markerCenter
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "<18.5",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "18.5–24.9",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "25–29.9",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "30+",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.bmi_disclaimer),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
