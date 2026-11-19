package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AlertLocationSource
import com.example.data.DoctorEntity
import com.example.data.EmergencyAlertType
import com.example.safety.EmergencyLocationProvider
import com.example.safety.LocationResolutionResult
import com.example.ui.ActiveFallAlertState
import com.example.ui.ActiveShakeAlertState
import com.example.ui.SafetyHealthViewModel
import com.example.ui.theme.GreenBright
import com.example.ui.theme.GreenDeep
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FallAlertOverlay(
    alertState: ActiveFallAlertState,
    emergencyNumber: String,
    primaryDoctor: DoctorEntity?,
    onImOk: () -> Unit,
    onFalseAlarm: () -> Unit,
    onDismissTimeout: () -> Unit
) {
    val context = LocalContext.current
    BackHandler(enabled = true) {
        if (alertState.timedOutNoResponse) {
            onDismissTimeout()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PureBlack, GreenDeep)
                )
            )
            .safeDrawingPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .testTag("fall_alert_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 520.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = GreenBright.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, GreenBright)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.HealthAndSafety,
                        contentDescription = null,
                        tint = GreenBright,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (alertState.alertType == EmergencyAlertType.FALL) {
                            stringResource(R.string.fall_sensitivity_disclaimer)
                        } else {
                            stringResource(R.string.location_privacy_note)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = PureWhite
                    )
                }
            }

            if (!alertState.timedOutNoResponse) {
                val total = alertState.totalSeconds.coerceAtLeast(1)
                val fraction = (alertState.secondsRemaining.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier.size(164.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(164.dp)) {
                        val stroke = 14.dp.toPx()
                        drawArc(
                            color = PureWhite.copy(alpha = 0.18f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            brush = Brush.sweepGradient(listOf(GreenPrimary, GreenBright, GreenPrimary)),
                            startAngle = -90f,
                            sweepAngle = 360f * fraction,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${alertState.secondsRemaining}s",
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                            color = PureWhite,
                            modifier = Modifier.testTag("fall_countdown_text")
                        )
                        if (alertState.alertType == EmergencyAlertType.FALL && alertState.peakG > 0f) {
                            Text(
                                text = String.format(Locale.getDefault(), "%.1fg", alertState.peakG),
                                style = MaterialTheme.typography.labelMedium,
                                color = GreenBright
                            )
                        } else {
                            Text(
                                text = alertState.alertType.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = GreenBright
                            )
                        }
                    }
                }

                Text(
                    text = if (alertState.alertType == EmergencyAlertType.SOS) {
                        stringResource(R.string.sos_alert_title)
                    } else {
                        stringResource(R.string.fall_alert_title)
                    },
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = PureWhite,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (alertState.alertType == EmergencyAlertType.SOS) {
                        stringResource(R.string.sos_alert_countdown, alertState.secondsRemaining)
                    } else {
                        stringResource(R.string.fall_alert_countdown, alertState.secondsRemaining)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = PureWhite.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                // Large "I'm OK" Button
                Button(
                    onClick = onImOk,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .testTag("fall_im_ok_button"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenBright,
                        contentColor = PureBlack
                    )
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.fall_alert_im_ok),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                OutlinedButton(
                    onClick = onFalseAlarm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("fall_false_alarm_button"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, PureWhite.copy(alpha = 0.7f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite)
                ) {
                    Text(
                        text = if (alertState.alertType == EmergencyAlertType.SOS) {
                            stringResource(R.string.dialog_cancel)
                        } else {
                            stringResource(R.string.fall_alert_false_alarm)
                        },
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            } else {
                // Post-countdown Alert Screen: Location Card + Emergency Contact, Doctor & SMS Options
                val titleText = when (alertState.alertType) {
                    EmergencyAlertType.SOS -> stringResource(R.string.sos_alert_title)
                    EmergencyAlertType.SHAKE -> stringResource(R.string.shake_no_response_title)
                    EmergencyAlertType.FALL -> stringResource(R.string.fall_alert_no_response_title)
                }
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = PureWhite,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.fall_alert_no_response_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PureWhite.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                // Alert Location Card (post-countdown only)
                AlertLocationCard(
                    isResolving = alertState.isResolvingLocation,
                    locationResult = alertState.locationResult,
                    onOpenMaps = { mapsLink ->
                        runCatching {
                            context.startActivity(EmergencyLocationProvider.buildOpenMapsIntent(mapsLink))
                        }
                    }
                )

                val trimmedEmergency = emergencyNumber.trim()
                Button(
                    onClick = {
                        if (trimmedEmergency.isNotEmpty()) {
                            runCatching {
                                context.startActivity(SafetyHealthViewModel.buildDialIntent(trimmedEmergency))
                            }
                        }
                    },
                    enabled = trimmedEmergency.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("fall_timeout_call_emergency"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenBright,
                        contentColor = PureBlack
                    )
                ) {
                    Icon(Icons.Rounded.Call, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (trimmedEmergency.isNotEmpty()) {
                            "${stringResource(R.string.emergency_call_contact)} ($trimmedEmergency)"
                        } else {
                            stringResource(R.string.emergency_no_number_message)
                        },
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (primaryDoctor != null && primaryDoctor.phoneNumber.isNotBlank()) {
                    Button(
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    SafetyHealthViewModel.buildDialIntent(primaryDoctor.phoneNumber)
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("fall_timeout_call_doctor"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PureWhite,
                            contentColor = PureBlack
                        )
                    ) {
                        Icon(Icons.Rounded.MedicalServices, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.emergency_call_doctor, primaryDoctor.name),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                val effectiveLocation = alertState.locationResult
                    ?: LocationResolutionResult(
                        source = AlertLocationSource.UNAVAILABLE,
                        timestampMs = alertState.alertTimestampMs
                    )
                OutlinedButton(
                    onClick = {
                        val target = trimmedEmergency.ifBlank { primaryDoctor?.phoneNumber?.trim().orEmpty() }
                        val msg = EmergencyLocationProvider.buildEmergencySmsBody(
                            alertLabel = alertState.alertType.label,
                            timestampMs = alertState.alertTimestampMs,
                            location = effectiveLocation
                        )
                        runCatching {
                            context.startActivity(SafetyHealthViewModel.buildEmergencySmsIntent(target, msg))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("fall_timeout_send_sms"),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, GreenBright),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, tint = GreenBright)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.fall_alert_send_sms))
                }

                if (alertState.alertType == EmergencyAlertType.FALL) {
                    OutlinedButton(
                        onClick = onFalseAlarm,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("fall_timeout_false_alarm"),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, PureWhite.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite)
                    ) {
                        Text(stringResource(R.string.fall_alert_false_alarm))
                    }
                }

                TextButton(
                    onClick = onDismissTimeout,
                    modifier = Modifier.testTag("fall_timeout_done_button")
                ) {
                    Text(stringResource(R.string.fall_alert_dismiss), color = PureWhite.copy(alpha = 0.85f))
                }
            }

            Text(
                text = stringResource(R.string.safety_disclaimer_general),
                style = MaterialTheme.typography.labelSmall,
                color = PureWhite.copy(alpha = 0.72f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AlertLocationCard(
    isResolving: Boolean,
    locationResult: LocationResolutionResult?,
    onOpenMaps: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alert_location_card"),
        shape = RoundedCornerShape(18.dp),
        color = PureBlack.copy(alpha = 0.65f),
        border = BorderStroke(1.5.dp, GreenBright)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = GreenBright,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.location_card_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = PureWhite
                    )
                }

                if (locationResult != null && locationResult.isAvailable) {
                    Box(
                        modifier = Modifier
                            .background(GreenBright.copy(alpha = 0.22f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (locationResult.source == AlertLocationSource.FRESH) {
                                stringResource(R.string.location_source_fresh)
                            } else {
                                stringResource(R.string.location_source_last_known)
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GreenBright
                        )
                    }
                }
            }

            if (isResolving) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = GreenBright,
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = stringResource(R.string.location_resolving),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PureWhite.copy(alpha = 0.88f)
                    )
                }
            } else if (locationResult != null && locationResult.isAvailable) {
                val coords = locationResult.formattedCoordinates().orEmpty()
                val acc = locationResult.formattedAccuracy()
                val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault())
                    .format(Date(locationResult.timestampMs))
                Text(
                    text = coords,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = PureWhite,
                    modifier = Modifier.testTag("alert_location_coordinates")
                )
                Text(
                    text = listOfNotNull(
                        acc?.let { "Accuracy $it" },
                        "Captured $timeStr"
                    ).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = PureWhite.copy(alpha = 0.8f)
                )
                Button(
                    onClick = { onOpenMaps(locationResult.mapsLink) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("alert_open_in_maps_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenBright,
                        contentColor = PureBlack
                    )
                ) {
                    Icon(Icons.Rounded.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.location_open_in_maps),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.location_unavailable_text),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PureWhite.copy(alpha = 0.85f),
                    modifier = Modifier.testTag("alert_location_unavailable_text")
                )
            }
        }
    }
}

@Composable
fun ShakeCountdownOverlay(
    alertState: ActiveShakeAlertState,
    emergencyNumber: String,
    onCancel: () -> Unit
) {
    BackHandler(enabled = true, onBack = onCancel)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PureBlack, GreenDeep)
                )
            )
            .safeDrawingPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .testTag("shake_countdown_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            val fraction = (alertState.secondsRemaining / 5f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier.size(152.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(152.dp)) {
                    val stroke = 14.dp.toPx()
                    drawArc(
                        color = PureWhite.copy(alpha = 0.18f),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(listOf(GreenPrimary, GreenBright, GreenPrimary)),
                        startAngle = -90f,
                        sweepAngle = 360f * fraction,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${alertState.secondsRemaining}s",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = PureWhite,
                    modifier = Modifier.testTag("shake_countdown_text")
                )
            }

            Text(
                text = stringResource(R.string.shake_alert_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = PureWhite,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (emergencyNumber.isNotBlank()) {
                    "${stringResource(R.string.shake_alert_countdown, alertState.secondsRemaining)} • $emergencyNumber"
                } else {
                    stringResource(R.string.shake_no_emergency_number)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = PureWhite.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .testTag("shake_cancel_button"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = PureBlack
                )
            ) {
                Text(
                    text = stringResource(R.string.shake_alert_cancel),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Text(
                text = stringResource(R.string.safety_disclaimer_general),
                style = MaterialTheme.typography.labelSmall,
                color = PureWhite.copy(alpha = 0.72f),
                textAlign = TextAlign.Center
            )
        }
    }
}
