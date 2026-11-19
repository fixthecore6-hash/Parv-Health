package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DoctorEntity
import com.example.safety.EmergencyLocationProvider
import com.example.safety.EmergencyLocationStatus

private val EmergencyDeepGreen = Color(0xFF0B3D2E)
private val EmergencyBrightGreen = Color(0xFF1FA86B)
private val EmergencyMintGreen = Color(0xFF53E0A0)
private val EmergencyBlack = Color(0xFF000000)
private val EmergencyWhite = Color(0xFFFFFFFF)

@Composable
fun EmergencyCard(
    emergencyNumber: String,
    primaryDoctor: DoctorEntity? = null,
    locationStatus: EmergencyLocationStatus = EmergencyLocationStatus.UNAVAILABLE,
    hasLocationPermission: Boolean = false,
    onTriggerSos: () -> Unit = {},
    onOpenLocationSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMissingNumberMessage by remember { mutableStateOf(false) }

    val cardShape = RoundedCornerShape(24.dp)
    val buttonShape = RoundedCornerShape(16.dp)

    val cardBackgroundBrush = Brush.linearGradient(
        colors = listOf(EmergencyDeepGreen, EmergencyBrightGreen),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    val diagonalShineBrush = Brush.linearGradient(
        colorStops = arrayOf(
            0.00f to Color.Transparent,
            0.38f to Color.Transparent,
            0.50f to EmergencyWhite.copy(alpha = 0.13f),
            0.62f to Color.Transparent,
            1.00f to Color.Transparent
        ),
        start = Offset.Zero,
        end = Offset(1000f, 1000f)
    )

    val primaryButtonBrush = Brush.horizontalGradient(
        colors = listOf(EmergencyBrightGreen, EmergencyMintGreen)
    )

    val secondaryBorderBrush = Brush.horizontalGradient(
        colors = listOf(EmergencyBrightGreen, EmergencyMintGreen)
    )

    val locationStatusText = when (locationStatus) {
        EmergencyLocationStatus.READY -> stringResource(R.string.location_status_ready)
        EmergencyLocationStatus.OFF -> stringResource(R.string.location_status_off)
        EmergencyLocationStatus.UNAVAILABLE -> stringResource(R.string.location_status_unavailable)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .testTag("emergency_card")
            .drawBehind {
                val radiusPx = 24.dp.toPx()
                val glowSpecs = listOf(
                    Triple(10.dp.toPx(), 6.dp.toPx(), 0.10f),
                    Triple(6.dp.toPx(), 4.dp.toPx(), 0.16f),
                    Triple(3.dp.toPx(), 2.dp.toPx(), 0.20f)
                )
                glowSpecs.forEach { (spread, yOffset, alpha) ->
                    drawRoundRect(
                        color = EmergencyBrightGreen.copy(alpha = alpha),
                        topLeft = Offset(-spread / 2f, yOffset),
                        size = Size(size.width + spread, size.height + spread / 2f),
                        cornerRadius = CornerRadius(radiusPx + spread / 2f, radiusPx + spread / 2f)
                    )
                }
            }
            .clip(cardShape)
            .background(cardBackgroundBrush)
            .background(diagonalShineBrush)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.emergency_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = EmergencyWhite,
                    modifier = Modifier.testTag("emergency_title")
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(EmergencyBlack.copy(alpha = 0.35f))
                        .border(1.dp, EmergencyMintGreen.copy(alpha = 0.5f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("emergency_location_status"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = if (locationStatus == EmergencyLocationStatus.READY) {
                            Icons.Rounded.LocationOn
                        } else {
                            Icons.Rounded.LocationOff
                        },
                        contentDescription = null,
                        tint = if (locationStatus == EmergencyLocationStatus.READY) {
                            EmergencyMintGreen
                        } else {
                            EmergencyWhite
                        },
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = locationStatusText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = EmergencyWhite
                    )
                }
            }

            if (locationStatus == EmergencyLocationStatus.OFF) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clip(buttonShape)
                        .background(EmergencyBlack)
                        .border(BorderStroke(1.5.dp, EmergencyMintGreen), buttonShape)
                        .clickable {
                            onOpenLocationSettings()
                            runCatching {
                                context.startActivity(EmergencyLocationProvider.buildLocationSettingsIntent())
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("emergency_open_location_settings_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null,
                            tint = EmergencyMintGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.location_open_settings),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = EmergencyWhite
                        )
                    }
                }
            } else if (!hasLocationPermission) {
                Text(
                    text = stringResource(R.string.location_denied_affected_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = EmergencyWhite.copy(alpha = 0.85f),
                    modifier = Modifier.testTag("emergency_location_affected_note")
                )
            }

            // Primary Button: Call emergency contact
            val primaryInteraction = remember { MutableInteractionSource() }
            val isPrimaryPressed by primaryInteraction.collectIsPressedAsState()
            val primaryScale by animateFloatAsState(
                targetValue = if (isPrimaryPressed) 0.97f else 1f,
                animationSpec = tween(durationMillis = 150),
                label = "emergency_primary_press"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .graphicsLayer {
                        scaleX = primaryScale
                        scaleY = primaryScale
                    }
                    .clip(buttonShape)
                    .background(primaryButtonBrush)
                    .clickable(
                        interactionSource = primaryInteraction,
                        indication = null
                    ) {
                        val trimmed = emergencyNumber.trim()
                        if (trimmed.isEmpty()) {
                            showMissingNumberMessage = true
                        } else {
                            showMissingNumberMessage = false
                            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${Uri.encode(trimmed)}")
                            }
                            runCatching { context.startActivity(dialIntent) }
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("emergency_call_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Call,
                        contentDescription = null,
                        tint = EmergencyBlack,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.emergency_call_contact),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = EmergencyBlack
                    )
                }
            }

            AnimatedVisibility(
                visible = showMissingNumberMessage,
                enter = fadeIn(animationSpec = tween(durationMillis = 200)),
                exit = fadeOut(animationSpec = tween(durationMillis = 200))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmergencyWhite.copy(alpha = 0.14f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("emergency_missing_number_message")
                ) {
                    Text(
                        text = stringResource(R.string.emergency_no_number_message),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = EmergencyWhite
                    )
                }
            }

            // SOS Alert Countdown Trigger Button
            val sosInteraction = remember { MutableInteractionSource() }
            val isSosPressed by sosInteraction.collectIsPressedAsState()
            val sosScale by animateFloatAsState(
                targetValue = if (isSosPressed) 0.97f else 1f,
                animationSpec = tween(durationMillis = 150),
                label = "emergency_sos_press"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .graphicsLayer {
                        scaleX = sosScale
                        scaleY = sosScale
                    }
                    .clip(buttonShape)
                    .background(EmergencyBlack)
                    .border(
                        border = BorderStroke(1.5.dp, secondaryBorderBrush),
                        shape = buttonShape
                    )
                    .clickable(
                        interactionSource = sosInteraction,
                        indication = null,
                        onClick = onTriggerSos
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("emergency_sos_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = null,
                        tint = EmergencyMintGreen,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.emergency_sos_button),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = EmergencyWhite
                    )
                }
            }

            // Primary Doctor Call Button (shown when a primary doctor is configured)
            if (primaryDoctor != null && primaryDoctor.phoneNumber.isNotBlank()) {
                val docInteraction = remember { MutableInteractionSource() }
                val isDocPressed by docInteraction.collectIsPressedAsState()
                val docScale by animateFloatAsState(
                    targetValue = if (isDocPressed) 0.97f else 1f,
                    animationSpec = tween(durationMillis = 150),
                    label = "emergency_doctor_press"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .graphicsLayer {
                            scaleX = docScale
                            scaleY = docScale
                        }
                        .clip(buttonShape)
                        .background(EmergencyBlack)
                        .border(
                            border = BorderStroke(1.5.dp, secondaryBorderBrush),
                            shape = buttonShape
                        )
                        .clickable(
                            interactionSource = docInteraction,
                            indication = null
                        ) {
                            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${Uri.encode(primaryDoctor.phoneNumber.trim())}")
                            }
                            runCatching { context.startActivity(dialIntent) }
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .testTag("emergency_call_primary_doctor_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MedicalServices,
                            contentDescription = null,
                            tint = EmergencyMintGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.emergency_call_doctor, primaryDoctor.name),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = EmergencyWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Secondary Button: Find nearby hospitals
            val secondaryInteraction = remember { MutableInteractionSource() }
            val isSecondaryPressed by secondaryInteraction.collectIsPressedAsState()
            val secondaryScale by animateFloatAsState(
                targetValue = if (isSecondaryPressed) 0.97f else 1f,
                animationSpec = tween(durationMillis = 150),
                label = "emergency_secondary_press"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .graphicsLayer {
                        scaleX = secondaryScale
                        scaleY = secondaryScale
                    }
                    .clip(buttonShape)
                    .background(EmergencyWhite)
                    .border(
                        border = BorderStroke(2.dp, secondaryBorderBrush),
                        shape = buttonShape
                    )
                    .clickable(
                        interactionSource = secondaryInteraction,
                        indication = null
                    ) {
                        val mapsGeoIntent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("geo:0,0?q=hospitals")
                        )
                        val launched = runCatching {
                            context.startActivity(mapsGeoIntent)
                            true
                        }.getOrDefault(false)
                        if (!launched) {
                            val webMapsIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.google.com/maps/search/?api=1&query=hospitals+near+me")
                            )
                            runCatching { context.startActivity(webMapsIntent) }
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("emergency_hospitals_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocalHospital,
                        contentDescription = null,
                        tint = EmergencyBlack,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.emergency_find_hospitals),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = EmergencyBlack
                    )
                }
            }

            Text(
                text = stringResource(R.string.location_privacy_note),
                style = MaterialTheme.typography.labelSmall,
                color = EmergencyWhite.copy(alpha = 0.80f),
                modifier = Modifier.testTag("emergency_location_privacy_note")
            )

            Text(
                text = stringResource(R.string.emergency_footer),
                style = MaterialTheme.typography.labelSmall,
                color = EmergencyWhite.copy(alpha = 0.70f),
                modifier = Modifier.testTag("emergency_footer")
            )
        }
    }
}
