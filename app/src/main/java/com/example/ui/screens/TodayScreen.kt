package com.example.ui.screens

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DailyStepsEntity
import com.example.data.DateUtils
import com.example.data.MedicationDoseLogEntity
import com.example.data.ReminderCategory
import com.example.data.ReminderEntity
import com.example.data.SleepEntryEntity
import com.example.pedometer.SensorMode
import com.example.ui.PedometerUiState
import com.example.ui.SafetyHealthUiState
import com.example.ui.components.BmiMeterCard
import com.example.ui.components.EmergencyCard
import com.example.ui.components.StepProgressRing
import com.example.ui.components.WaterIntakeCard
import com.example.ui.theme.LocalAccentColors
import com.example.ui.theme.LocalIsDarkTheme
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun TodayScreen(
    uiState: PedometerUiState,
    safetyUiState: SafetyHealthUiState,
    onToggleTracking: () -> Unit,
    onRequestPermission: () -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenDoctors: () -> Unit,
    onOpenMedications: () -> Unit,
    onAddWater: (Int) -> Unit,
    onUndoWater: () -> Unit,
    onSetWaterGoalOverride: (Int?) -> Unit,
    onMarkDoseTaken: (MedicationDoseLogEntity) -> Unit,
    onSnoozeDose: (MedicationDoseLogEntity) -> Unit,
    onSkipDose: (MedicationDoseLogEntity) -> Unit,
    onSaveSleep: (Float, String) -> Unit,
    onDeleteTodaySleep: () -> Unit,
    onAddReminder: (ReminderCategory, String, String, String) -> Unit,
    onToggleReminder: (Long, Boolean) -> Unit,
    onDeleteReminder: (Long) -> Unit,
    onTriggerSos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current
    val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
        !uiState.hasActivityPermission &&
        (uiState.sensorMode == SensorMode.HARDWARE_COUNTER || uiState.sensorMode == SensorMode.HARDWARE_DETECTOR)

    val sensorLabel = when (uiState.sensorMode) {
        SensorMode.HARDWARE_COUNTER -> stringResource(R.string.sensor_hardware_counter)
        SensorMode.HARDWARE_DETECTOR -> stringResource(R.string.sensor_hardware_detector)
        SensorMode.ACCELEROMETER_FALLBACK -> stringResource(R.string.sensor_accelerometer)
        SensorMode.UNAVAILABLE -> stringResource(R.string.sensor_unavailable)
    }

    val greetingTitle = if (uiState.userProfile.name.isNotBlank()) {
        stringResource(R.string.greeting_named, uiState.userProfile.name.trim())
    } else {
        stringResource(R.string.greeting_anonymous)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Top Greeting Header with Small Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenProfile)
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.parv_logo),
                        contentDescription = stringResource(R.string.logo_content_desc),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Column {
                        Text(
                            text = greetingTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.testTag("header_greeting_text")
                        )
                        Text(
                            text = stringResource(R.string.subtitle_today),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.testTag("header_today_subtitle")
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    StatusPill(
                        isActive = uiState.isTrackingEnabled && !needsPermission,
                        needsPermission = needsPermission
                    )
                    IconButton(
                        onClick = { onToggleTheme(isDark) },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                            contentDescription = stringResource(R.string.toggle_theme_desc),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings_content_desc),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // Runtime Permission Prompt Card (Android 10+ Activity Recognition)
            AnimatedVisibility(visible = needsPermission) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = accents.greenTint
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Sensors,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.permission_card_title),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = stringResource(R.string.permission_card_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onRequestPermission,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("allow_activity_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(stringResource(R.string.action_allow_tracking))
                        }
                    }
                }
            }

            // Section 1: Daily Activity
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.section_daily_activity),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shadowElevation = if (isDark) 2.dp else 5.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        StepProgressRing(
                            steps = uiState.today.steps,
                            goal = uiState.dailyGoal
                        )

                        Button(
                            onClick = {
                                if (!uiState.isTrackingEnabled && needsPermission) {
                                    onRequestPermission()
                                } else {
                                    onToggleTracking()
                                }
                            },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("toggle_tracking_button"),
                            shape = RoundedCornerShape(50),
                            colors = if (uiState.isTrackingEnabled) {
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        ) {
                            Icon(
                                imageVector = if (uiState.isTrackingEnabled) {
                                    Icons.Rounded.Pause
                                } else {
                                    Icons.Rounded.PlayArrow
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(
                                    if (uiState.isTrackingEnabled) {
                                        R.string.action_pause_tracking
                                    } else {
                                        R.string.action_start_tracking
                                    }
                                ),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                // Compact 3-Stat Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val formattedKm = String.format(Locale.getDefault(), "%.2f", uiState.today.distanceKm)
                    MetricCard(
                        icon = Icons.Rounded.Route,
                        label = stringResource(R.string.metric_distance),
                        value = stringResource(R.string.unit_km, formattedKm),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        icon = Icons.Rounded.LocalFireDepartment,
                        label = stringResource(R.string.metric_calories),
                        value = stringResource(R.string.unit_kcal, uiState.today.caloriesKcal),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        icon = Icons.Rounded.Timer,
                        label = stringResource(R.string.metric_active_time),
                        value = stringResource(R.string.unit_min, uiState.today.activeMinutes),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Section 2: Water Intake
            WaterIntakeCard(
                todayWaterMl = safetyUiState.todayWaterMl,
                effectiveGoalMl = safetyUiState.effectiveWaterGoalMl,
                suggestedGoalMl = safetyUiState.suggestedWaterGoalMl,
                hasEntriesToday = safetyUiState.todayWaterEntries.isNotEmpty(),
                onAddWater = onAddWater,
                onUndoLastWater = onUndoWater,
                onSetGoalOverride = onSetWaterGoalOverride
            )

            // Section 3: Care, Doctors & Medications Hub
            CareAndMedicationsSection(
                safetyUiState = safetyUiState,
                onOpenDoctors = onOpenDoctors,
                onOpenMedications = onOpenMedications,
                onMarkDoseTaken = onMarkDoseTaken,
                onSnoozeDose = onSnoozeDose,
                onSkipDose = onSkipDose
            )

            // Section 4: BMI Meter
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.section_body_metrics),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                BmiMeterCard(
                    userProfile = uiState.userProfile,
                    onAddProfileClick = onOpenProfile
                )
            }

            // Section 5: Weekly Overview
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.section_weekly_overview),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                WeeklySparkPreviewCard(
                    weeklyDays = uiState.weeklyDays,
                    goal = uiState.dailyGoal,
                    onClick = onOpenHistory
                )
            }

            // Section 6: Sleep Log
            SleepLogCard(
                todaySleep = uiState.todaySleep,
                onSaveSleep = onSaveSleep,
                onDeleteSleep = onDeleteTodaySleep
            )

            // Section 7: On-screen Reminders (Workout & Water)
            RemindersCard(
                reminders = uiState.reminders,
                onAddReminder = onAddReminder,
                onToggleReminder = onToggleReminder,
                onDeleteReminder = onDeleteReminder
            )

            // Section 8: Emergency Card (with optional Primary Doctor Dialer Button & Location Status)
            EmergencyCard(
                emergencyNumber = uiState.userProfile.emergencyContact,
                primaryDoctor = safetyUiState.primaryDoctor,
                locationStatus = safetyUiState.locationStatus,
                hasLocationPermission = safetyUiState.hasLocationPermission,
                onTriggerSos = onTriggerSos
            )

            // Local Storage Transparency Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.local_storage_badge, sensorLabel),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = stringResource(R.string.data_stays_on_device),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPill(
    isActive: Boolean,
    needsPermission: Boolean
) {
    val accents = LocalAccentColors.current
    val bgColor = when {
        needsPermission -> accents.greenTint
        isActive -> accents.greenTint
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val dotColor = when {
        needsPermission -> accents.greenPrimary
        isActive -> accents.greenPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = when {
        needsPermission -> stringResource(R.string.status_permission_needed)
        isActive -> stringResource(R.string.status_tracking_active)
        else -> stringResource(R.string.status_tracking_paused)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
private fun MetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = if (isDark) 2.dp else 3.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accents.greenTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WeeklySparkPreviewCard(
    weeklyDays: List<DailyStepsEntity>,
    goal: Int,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .testTag("weekly_preview_card"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = if (isDark) 2.dp else 4.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.weekly_preview_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.weekly_preview_action),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            val maxVal = maxOf(goal, weeklyDays.maxOfOrNull { it.steps } ?: goal, 1000)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyDays.forEach { day ->
                    val ratio = (day.steps.toFloat() / maxVal.toFloat()).coerceIn(0f, 1f)
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.55f)
                                .height(34.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            if (day.steps > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((34f * ratio).coerceAtLeast(4f).dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(accents.verticalGradient)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = DateUtils.singleLetterWeekday(day.date),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SleepLogCard(
    todaySleep: SleepEntryEntity?,
    onSaveSleep: (Float, String) -> Unit,
    onDeleteSleep: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current

    var hours by remember(todaySleep?.hoursSlept) {
        mutableFloatStateOf(todaySleep?.hoursSlept ?: 7.5f)
    }
    var note by remember(todaySleep?.note) {
        mutableStateOf(todaySleep?.note ?: "")
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today_sleep_card"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = if (isDark) 2.dp else 4.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accents.greenTint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bedtime,
                            contentDescription = null,
                            tint = accents.greenPrimary
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.sleep_card_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (todaySleep != null) {
                                "Saved today: ${String.format(Locale.getDefault(), "%.1f", todaySleep.hoursSlept)} hrs"
                            } else {
                                "Log last night's sleep"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(accents.greenTint)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.sleep_hours_label,
                                String.format(Locale.getDefault(), "%.1f", hours)
                            ),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (todaySleep != null) {
                        IconButton(
                            onClick = onDeleteSleep,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("delete_sleep_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Delete sleep log",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { hours = ((hours - 0.5f).coerceIn(0f, 14f) * 2f).roundToInt() / 2f },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Remove,
                        contentDescription = "Decrease sleep",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Slider(
                    value = hours,
                    onValueChange = { raw ->
                        hours = ((raw * 2f).roundToInt() / 2f).coerceIn(0f, 14f)
                    },
                    valueRange = 0f..14f,
                    colors = SliderDefaults.colors(
                        thumbColor = accents.greenPrimary,
                        activeTrackColor = accents.greenPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sleep_slider")
                )
                IconButton(
                    onClick = { hours = ((hours + 0.5f).coerceIn(0f, 14f) * 2f).roundToInt() / 2f },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Increase sleep",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text(stringResource(R.string.sleep_note_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sleep_note_input")
            )

            Button(
                onClick = { onSaveSleep(hours, note.trim()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_sleep_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(stringResource(R.string.sleep_save_button))
            }
        }
    }
}

@Composable
private fun RemindersCard(
    reminders: List<ReminderEntity>,
    onAddReminder: (ReminderCategory, String, String, String) -> Unit,
    onToggleReminder: (Long, Boolean) -> Unit,
    onDeleteReminder: (Long) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current
    var showAddDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today_reminders_card"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = if (isDark) 2.dp else 4.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accents.greenTint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = accents.greenPrimary
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.reminders_card_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.reminders_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                OutlinedButton(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, accents.greenPrimary),
                    modifier = Modifier.testTag("add_reminder_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = accents.greenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (reminders.isEmpty()) {
                Text(
                    text = "No reminders saved. Tap Add to create an on-screen workout or water reminder.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                reminders.forEach { reminder ->
                    val category = runCatching { ReminderCategory.valueOf(reminder.category) }
                        .getOrDefault(ReminderCategory.WATER)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("reminder_row_${reminder.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (category == ReminderCategory.WORKOUT) {
                                Icons.Rounded.FitnessCenter
                            } else {
                                Icons.Rounded.WaterDrop
                            },
                            contentDescription = null,
                            tint = accents.greenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = reminder.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${reminder.timeLabel} • ${reminder.scheduleLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = reminder.enabled,
                            onCheckedChange = { checked -> onToggleReminder(reminder.id, checked) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = accents.greenPrimary
                            ),
                            modifier = Modifier.testTag("reminder_switch_${reminder.id}")
                        )
                        IconButton(
                            onClick = { onDeleteReminder(reminder.id) },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = "Delete reminder",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var category by remember { mutableStateOf(ReminderCategory.WATER) }
        var title by remember { mutableStateOf("") }
        var timeLabel by remember { mutableStateOf("09:00 AM") }
        var scheduleLabel by remember { mutableStateOf("Daily") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text(stringResource(R.string.reminders_add_button)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = category == ReminderCategory.WATER,
                            onClick = { category = ReminderCategory.WATER },
                            label = { Text(stringResource(R.string.reminders_type_water)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accents.greenTint,
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        FilterChip(
                            selected = category == ReminderCategory.WORKOUT,
                            onClick = { category = ReminderCategory.WORKOUT },
                            label = { Text(stringResource(R.string.reminders_type_workout)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accents.greenTint,
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(stringResource(R.string.reminders_title_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = timeLabel,
                        onValueChange = { timeLabel = it },
                        label = { Text(stringResource(R.string.reminders_time_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = scheduleLabel,
                        onValueChange = { scheduleLabel = it },
                        label = { Text(stringResource(R.string.reminders_schedule_hint)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanTitle = title.trim().ifEmpty {
                            if (category == ReminderCategory.WORKOUT) "Workout session" else "Drink a glass of water"
                        }
                        onAddReminder(
                            category,
                            cleanTitle,
                            timeLabel.trim().ifEmpty { "09:00 AM" },
                            scheduleLabel.trim().ifEmpty { "Daily" }
                        )
                        showAddDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(stringResource(R.string.reminders_add_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

@Composable
private fun CareAndMedicationsSection(
    safetyUiState: SafetyHealthUiState,
    onOpenDoctors: () -> Unit,
    onOpenMedications: () -> Unit,
    onMarkDoseTaken: (MedicationDoseLogEntity) -> Unit,
    onSnoozeDose: (MedicationDoseLogEntity) -> Unit,
    onSkipDose: (MedicationDoseLogEntity) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Care & Medications",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.large)
                    .clickable(onClick = onOpenDoctors)
                    .testTag("today_doctors_card"),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = if (isDark) 2.dp else 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accents.greenTint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MedicalServices,
                            contentDescription = null,
                            tint = accents.greenPrimary
                        )
                    }
                    Text(
                        text = stringResource(R.string.doctors_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = safetyUiState.primaryDoctor?.let { "Primary: ${it.name}" }
                            ?: "${safetyUiState.doctors.size} saved",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.large)
                    .clickable(onClick = onOpenMedications)
                    .testTag("today_medications_card"),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = if (isDark) 2.dp else 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accents.greenTint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Medication,
                            contentDescription = null,
                            tint = accents.greenPrimary
                        )
                    }
                    Text(
                        text = stringResource(R.string.meds_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (safetyUiState.dueMedicationDoses.isNotEmpty()) {
                            "${safetyUiState.dueMedicationDoses.size} due today"
                        } else {
                            "${safetyUiState.medicationsWithStats.size} medicines"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (safetyUiState.dueMedicationDoses.isNotEmpty()) {
                            accents.greenPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Inline Due Medication Action Strip
        safetyUiState.dueMedicationDoses.take(2).forEach { due ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("today_due_med_${due.log.id}"),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, accents.greenPrimary)
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${due.medication.name} (${due.medication.dose})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Dose due • ${due.log.scheduledTimeLabel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = onOpenMedications) {
                            Text("All")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onMarkDoseTaken(due.log) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.med_action_taken), maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { onSnoozeDose(due.log) },
                            modifier = Modifier
                                .weight(1.2f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Rounded.Snooze, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.med_action_snooze), maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = { onSkipDose(due.log) },
                            modifier = Modifier
                                .weight(0.9f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.med_action_skip), maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

