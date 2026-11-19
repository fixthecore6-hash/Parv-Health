package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AlertLocationSource
import com.example.data.FallSensitivity
import com.example.data.ThemeMode
import com.example.safety.EmergencyLocationProvider
import com.example.safety.EmergencyLocationStatus
import com.example.ui.LocationTestUiState
import com.example.ui.SafetyHealthUiState
import com.example.ui.theme.LocalAccentColors
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsSheet(
    dailyGoal: Int,
    strideLengthCm: Int,
    themeMode: ThemeMode,
    safetyUiState: SafetyHealthUiState,
    backupMessage: String?,
    onUpdateGoal: (Int) -> Unit,
    onUpdateStrideLength: (Int) -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetFallDetectionEnabled: (Boolean) -> Unit,
    onSetFallSensitivity: (FallSensitivity) -> Unit,
    onSetShakeToCallEnabled: (Boolean) -> Unit,
    onToggleWaterReminders: (Boolean) -> Unit,
    onToggleMedicationNotifications: (Boolean) -> Unit,
    onEnableLocation: () -> Unit,
    onTestLocation: () -> Unit,
    onClearLocationData: () -> Unit,
    onExportJson: () -> Unit,
    onExportCsv: () -> Unit,
    onImportFromFile: () -> Unit,
    onClearHistory: () -> Unit,
    onClearFoodData: () -> Unit,
    onClearAllAppData: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val accents = LocalAccentColors.current
    var localGoal by remember(dailyGoal) { mutableIntStateOf(dailyGoal) }
    var localStride by remember(strideLengthCm) { mutableIntStateOf(strideLengthCm) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showClearFoodConfirm by remember { mutableStateOf(false) }
    var showClearAllConfirm by remember { mutableStateOf(false) }
    var showClearLocationConfirm by remember { mutableStateOf(false) }

    val formatter = NumberFormat.getIntegerInstance()
    val goalPresets = listOf(5000, 8000, 10000, 12000)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("settings_done_button")
                ) {
                    Text(stringResource(R.string.dialog_done))
                }
            }

            // Appearance (System / Bright / Dark)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.settings_theme_mode),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf(
                        Triple(ThemeMode.SYSTEM, stringResource(R.string.theme_system), Icons.Rounded.BrightnessAuto),
                        Triple(ThemeMode.BRIGHT, stringResource(R.string.theme_bright), Icons.Rounded.LightMode),
                        Triple(ThemeMode.DARK, stringResource(R.string.theme_dark), Icons.Rounded.DarkMode)
                    )
                    modes.forEach { (mode, label, icon) ->
                        FilterChip(
                            selected = themeMode == mode,
                            onClick = { onSetThemeMode(mode) },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null
                                )
                            },
                            label = { Text(label) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_mode_${mode.name.lowercase()}")
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            // Daily Step Goal Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_daily_goal),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${formatter.format(localGoal)} steps",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = localGoal.toFloat(),
                    onValueChange = { raw ->
                        val snapped = ((raw / 500f).roundToInt() * 500).coerceIn(2000, 20000)
                        localGoal = snapped
                    },
                    onValueChangeFinished = { onUpdateGoal(localGoal) },
                    valueRange = 2000f..20000f,
                    modifier = Modifier.testTag("goal_slider")
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    goalPresets.forEach { preset ->
                        FilterChip(
                            selected = localGoal == preset,
                            onClick = {
                                localGoal = preset
                                onUpdateGoal(preset)
                            },
                            label = { Text(formatter.format(preset)) },
                            modifier = Modifier.testTag("goal_preset_$preset")
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            // Stride Length Section
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_stride_length),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "$localStride cm",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = stringResource(R.string.settings_stride_helper),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = localStride.toFloat(),
                    onValueChange = { localStride = it.roundToInt().coerceIn(50, 105) },
                    onValueChangeFinished = { onUpdateStrideLength(localStride) },
                    valueRange = 50f..105f,
                    modifier = Modifier.testTag("stride_slider")
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            // Safety & Reminders Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.settings_safety_section),
                    style = MaterialTheme.typography.titleMedium
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accents.greenTint,
                    border = BorderStroke(1.dp, accents.greenPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.HealthAndSafety,
                            contentDescription = null,
                            tint = accents.greenPrimary
                        )
                        Text(
                            text = stringResource(R.string.safety_disclaimer_general),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Fall Detection Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = stringResource(R.string.fall_settings_title),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = if (safetyUiState.hasAccelerometer) {
                                stringResource(R.string.fall_settings_subtitle)
                            } else {
                                "No accelerometer sensor detected on this device."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = safetyUiState.fallDetectionEnabled,
                        onCheckedChange = onSetFallDetectionEnabled,
                        enabled = safetyUiState.hasAccelerometer,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = accents.greenPrimary
                        ),
                        modifier = Modifier.testTag("settings_fall_detection_switch")
                    )
                }

                // Fall Sensitivity Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.fall_sensitivity_label),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FallSensitivity.entries.forEach { sensitivity ->
                            FilterChip(
                                selected = safetyUiState.fallSensitivity == sensitivity,
                                onClick = { onSetFallSensitivity(sensitivity) },
                                label = { Text(sensitivity.label) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("fall_sensitivity_${sensitivity.name.lowercase()}")
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.fall_sensitivity_disclaimer),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("settings_fall_disclaimer")
                    )
                }

                // Shake to Call Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = stringResource(R.string.shake_settings_title),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = stringResource(R.string.shake_settings_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = safetyUiState.shakeToCallEnabled,
                        onCheckedChange = onSetShakeToCallEnabled,
                        enabled = safetyUiState.hasAccelerometer,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = accents.greenPrimary
                        ),
                        modifier = Modifier.testTag("settings_shake_to_call_switch")
                    )
                }

                // Water Reminders Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = stringResource(R.string.water_reminders_title),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = stringResource(R.string.water_reminders_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = safetyUiState.waterRemindersEnabled,
                        onCheckedChange = onToggleWaterReminders,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = accents.greenPrimary
                        ),
                        modifier = Modifier.testTag("settings_water_reminders_switch")
                    )
                }

                // Medication Notifications Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                        Text(
                            text = stringResource(R.string.meds_notifications_title),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = stringResource(R.string.meds_notifications_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = safetyUiState.medicationNotificationsEnabled,
                        onCheckedChange = onToggleMedicationNotifications,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = accents.greenPrimary
                        ),
                        modifier = Modifier.testTag("settings_med_notifications_switch")
                    )
                }

                // Emergency Location Controls (strictly green, black, white)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, accents.greenPrimary.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (safetyUiState.locationStatus == EmergencyLocationStatus.READY) {
                                        Icons.Rounded.LocationOn
                                    } else {
                                        Icons.Rounded.LocationOff
                                    },
                                    contentDescription = null,
                                    tint = accents.greenPrimary
                                )
                                Text(
                                    text = when (safetyUiState.locationStatus) {
                                        EmergencyLocationStatus.READY -> stringResource(R.string.location_status_ready)
                                        EmergencyLocationStatus.OFF -> stringResource(R.string.location_status_off)
                                        EmergencyLocationStatus.UNAVAILABLE -> stringResource(R.string.location_status_unavailable)
                                    },
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("settings_location_status")
                                )
                            }
                            if (safetyUiState.hasLocationPermission) {
                                Text(
                                    text = if (safetyUiState.hasFineLocationPermission) "Precise" else "Approximate",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = accents.greenPrimary
                                )
                            }
                        }

                        if (!safetyUiState.hasLocationPermission) {
                            Text(
                                text = stringResource(R.string.location_denied_affected_note),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = onEnableLocation,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("settings_enable_location_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accents.greenPrimary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(Icons.Rounded.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.location_enable_button))
                            }
                        }

                        if (safetyUiState.locationStatus == EmergencyLocationStatus.OFF) {
                            OutlinedButton(
                                onClick = {
                                    runCatching {
                                        context.startActivity(EmergencyLocationProvider.buildLocationSettingsIntent())
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("settings_open_location_settings_button"),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, accents.greenPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = null,
                                    tint = accents.greenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.location_open_settings),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onTestLocation,
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("settings_test_location_button"),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, accents.greenPrimary)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MyLocation,
                                    contentDescription = null,
                                    tint = accents.greenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.location_test_button),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            OutlinedButton(
                                onClick = { showClearLocationConfirm = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("settings_clear_location_button"),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.location_clear_button),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        when (val testState = safetyUiState.locationTestState) {
                            LocationTestUiState.Idle -> Unit
                            LocationTestUiState.Loading -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.testTag("settings_test_location_loading")
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = accents.greenPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = stringResource(R.string.location_resolving),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            is LocationTestUiState.Completed -> {
                                val res = testState.result
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, accents.greenPrimary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("settings_test_location_result")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (res.isAvailable) {
                                            val timeStr = SimpleDateFormat("h:mm:ss a", Locale.getDefault())
                                                .format(Date(res.timestampMs))
                                            val sourceLabel = if (res.source == AlertLocationSource.FRESH) {
                                                stringResource(R.string.location_source_fresh)
                                            } else {
                                                stringResource(R.string.location_source_last_known)
                                            }
                                            Text(
                                                text = "${res.formattedCoordinates()} • $sourceLabel",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = listOfNotNull(
                                                    res.formattedAccuracy()?.let { "Accuracy $it" },
                                                    "Time $timeStr"
                                                ).joinToString(" • "),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            OutlinedButton(
                                                onClick = {
                                                    runCatching {
                                                        context.startActivity(
                                                            EmergencyLocationProvider.buildOpenMapsIntent(res.mapsLink)
                                                        )
                                                    }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("settings_test_open_maps_button"),
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, accents.greenPrimary)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Map,
                                                    contentDescription = null,
                                                    tint = accents.greenPrimary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = stringResource(R.string.location_open_in_maps),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        } else {
                                            Text(
                                                text = stringResource(R.string.location_unavailable_text),
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = stringResource(R.string.location_privacy_note),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("settings_location_privacy_note")
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            // Export & Import Local Data
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.settings_backup_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onExportJson,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("export_json_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, accents.greenPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = null,
                            tint = accents.greenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.settings_export_json),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    OutlinedButton(
                        onClick = onExportCsv,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("export_csv_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, accents.greenPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = null,
                            tint = accents.greenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.settings_export_csv),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                OutlinedButton(
                    onClick = onImportFromFile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("import_backup_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.UploadFile,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.settings_import_file),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (!backupMessage.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = accents.greenTint,
                        border = BorderStroke(1.dp, accents.greenPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = backupMessage,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            // Clear Local Data Buttons
            OutlinedButton(
                onClick = { showClearConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("clear_history_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.settings_clear_history))
            }

            OutlinedButton(
                onClick = { showClearFoodConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("clear_food_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.settings_clear_food))
            }

            Button(
                onClick = { showClearAllConfirm = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("clear_all_data_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteForever,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.settings_clear_all_data))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.privacy_health_data_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(R.string.dialog_clear_title)) },
            text = { Text(stringResource(R.string.dialog_clear_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearHistory()
                        showClearConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("confirm_clear_history_button")
                ) {
                    Text(stringResource(R.string.dialog_confirm_clear))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearConfirm = false },
                    modifier = Modifier.testTag("cancel_clear_history_button")
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    if (showClearFoodConfirm) {
        AlertDialog(
            onDismissRequest = { showClearFoodConfirm = false },
            title = { Text(stringResource(R.string.dialog_clear_food_title)) },
            text = { Text(stringResource(R.string.dialog_clear_food_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearFoodData()
                        showClearFoodConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("confirm_clear_food_button")
                ) {
                    Text(stringResource(R.string.dialog_confirm_clear))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearFoodConfirm = false },
                    modifier = Modifier.testTag("cancel_clear_food_button")
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = { Text(stringResource(R.string.dialog_clear_all_title)) },
            text = { Text(stringResource(R.string.dialog_clear_all_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllAppData()
                        showClearAllConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("confirm_clear_all_button")
                ) {
                    Text(stringResource(R.string.dialog_confirm_clear))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearAllConfirm = false },
                    modifier = Modifier.testTag("cancel_clear_all_button")
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    if (showClearLocationConfirm) {
        AlertDialog(
            onDismissRequest = { showClearLocationConfirm = false },
            title = { Text(stringResource(R.string.location_clear_dialog_title)) },
            text = { Text(stringResource(R.string.location_clear_dialog_body)) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearLocationData()
                        showClearLocationConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accents.greenPrimary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("confirm_clear_location_button")
                ) {
                    Text(stringResource(R.string.dialog_confirm_clear))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearLocationConfirm = false },
                    modifier = Modifier.testTag("cancel_clear_location_button")
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}
