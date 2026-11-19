package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DateUtils
import com.example.safety.SafetyReminderScheduler
import com.example.ui.FoodViewModel
import com.example.ui.PedometerViewModel
import com.example.ui.SafetyHealthViewModel
import com.example.ui.SafetyNavigationEvent
import com.example.ui.WorkoutViewModel
import com.example.ui.screens.MainScreen
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.ParvHealthTheme
import com.example.ui.theme.PureWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class PendingNotificationFeature {
    WATER,
    MEDICATIONS
}

private enum class PendingLocationAction {
    ENABLE_FALL,
    ENABLE_SHAKE,
    TRIGGER_SOS,
    SETTINGS_ENABLE_ONLY
}

class MainActivity : ComponentActivity() {

    private val viewModel: PedometerViewModel by viewModels { PedometerViewModel.Factory }
    private val workoutViewModel: WorkoutViewModel by viewModels { WorkoutViewModel.Factory }
    private val foodViewModel: FoodViewModel by viewModels { FoodViewModel.Factory }
    private val safetyViewModel: SafetyHealthViewModel by viewModels { SafetyHealthViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val workoutUiState by workoutViewModel.uiState.collectAsStateWithLifecycle()
            val foodUiState by foodViewModel.uiState.collectAsStateWithLifecycle()
            val safetyUiState by safetyViewModel.uiState.collectAsStateWithLifecycle()
            val coroutineScope = rememberCoroutineScope()
            var backupMessage by remember { mutableStateOf<String?>(null) }

            var pendingNotifFeature by remember { mutableStateOf<PendingNotificationFeature?>(null) }
            var showActivityReasonDialog by remember { mutableStateOf(false) }
            var showMissingEmergencyDialog by remember { mutableStateOf(false) }
            var pendingLocationRationaleAction by remember { mutableStateOf<PendingLocationAction?>(null) }
            var activeLocationLauncherAction by remember { mutableStateOf<PendingLocationAction?>(null) }
            var deniedContinuationAction by remember { mutableStateOf<PendingLocationAction?>(null) }
            var showLocationDeniedInfoDialog by remember { mutableStateOf(false) }

            ParvHealthTheme(themeMode = uiState.themeMode) {
                val lifecycleOwner = LocalLifecycleOwner.current

                val exportJsonLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("application/json")
                ) { uri ->
                    if (uri != null) {
                        coroutineScope.launch {
                            val ok = runCatching {
                                val json = viewModel.exportJsonBackup()
                                withContext(Dispatchers.IO) {
                                    contentResolver.openOutputStream(uri)?.use { out ->
                                        out.write(json.toByteArray(Charsets.UTF_8))
                                    } ?: error("Cannot open output stream")
                                }
                            }.isSuccess
                            backupMessage = getString(
                                if (ok) R.string.settings_export_success else R.string.settings_backup_error
                            )
                        }
                    }
                }

                val exportCsvLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.CreateDocument("text/csv")
                ) { uri ->
                    if (uri != null) {
                        coroutineScope.launch {
                            val ok = runCatching {
                                val csv = viewModel.exportCsvBackup()
                                withContext(Dispatchers.IO) {
                                    contentResolver.openOutputStream(uri)?.use { out ->
                                        out.write(csv.toByteArray(Charsets.UTF_8))
                                    } ?: error("Cannot open output stream")
                                }
                            }.isSuccess
                            backupMessage = getString(
                                if (ok) R.string.settings_export_success else R.string.settings_backup_error
                            )
                        }
                    }
                }

                val importBackupLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri ->
                    if (uri != null) {
                        coroutineScope.launch {
                            val ok = runCatching {
                                val content = withContext(Dispatchers.IO) {
                                    contentResolver.openInputStream(uri)?.use { input ->
                                        input.bufferedReader(Charsets.UTF_8).readText()
                                    } ?: error("Cannot open input stream")
                                }
                                viewModel.importBackup(content)
                            }.isSuccess
                            backupMessage = getString(
                                if (ok) R.string.settings_import_success else R.string.settings_backup_error
                            )
                        }
                    }
                }

                // Request Physical Activity ONLY when user turns on step tracking
                val activityPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted ->
                    viewModel.onPermissionResult(granted)
                }

                // Request POST_NOTIFICATIONS ONLY when user turns on Water or Medication reminders
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted ->
                    val target = pendingNotifFeature
                    pendingNotifFeature = null
                    if (granted && target != null) {
                        when (target) {
                            PendingNotificationFeature.WATER -> safetyViewModel.setWaterRemindersEnabled(true)
                            PendingNotificationFeature.MEDICATIONS -> safetyViewModel.setMedicationNotificationsEnabled(true)
                        }
                    }
                }

                val performPostLocationAction: (PendingLocationAction) -> Unit = { action ->
                    when (action) {
                        PendingLocationAction.ENABLE_FALL -> safetyViewModel.setFallDetectionEnabled(true)
                        PendingLocationAction.ENABLE_SHAKE -> safetyViewModel.setShakeToCallEnabled(true)
                        PendingLocationAction.TRIGGER_SOS -> safetyViewModel.startSosAlertCountdown()
                        PendingLocationAction.SETTINGS_ENABLE_ONLY -> Unit
                    }
                }

                // Foreground Location permission launcher (supports precise or user-chosen approximate fallback)
                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    safetyViewModel.refreshLocationStatus()
                    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    val action = activeLocationLauncherAction
                    activeLocationLauncherAction = null

                    if (granted) {
                        if (action != null) performPostLocationAction(action)
                    } else {
                        deniedContinuationAction = action
                        showLocationDeniedInfoDialog = true
                    }
                }

                val requestActivityPermissionWithReason = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        showActivityReasonDialog = true
                    } else {
                        viewModel.onPermissionResult(true)
                    }
                }

                val handleToggleFallDetection: (Boolean) -> Unit = { enable ->
                    if (!enable) {
                        safetyViewModel.setFallDetectionEnabled(false)
                    } else if (safetyUiState.hasLocationPermission || safetyUiState.locationRationaleShown) {
                        safetyViewModel.setFallDetectionEnabled(true)
                    } else {
                        pendingLocationRationaleAction = PendingLocationAction.ENABLE_FALL
                    }
                }

                val handleToggleShakeToCall: (Boolean) -> Unit = { enable ->
                    if (!enable) {
                        safetyViewModel.setShakeToCallEnabled(false)
                    } else if (safetyUiState.hasLocationPermission || safetyUiState.locationRationaleShown) {
                        safetyViewModel.setShakeToCallEnabled(true)
                    } else {
                        pendingLocationRationaleAction = PendingLocationAction.ENABLE_SHAKE
                    }
                }

                val handleTriggerSos: () -> Unit = {
                    if (safetyUiState.hasLocationPermission || safetyUiState.locationRationaleShown) {
                        safetyViewModel.startSosAlertCountdown()
                    } else {
                        pendingLocationRationaleAction = PendingLocationAction.TRIGGER_SOS
                    }
                }

                val handleEnableLocationFromSettings: () -> Unit = {
                    pendingLocationRationaleAction = PendingLocationAction.SETTINGS_ENABLE_ONLY
                }

                val handleToggleWaterReminders: (Boolean) -> Unit = { enable ->
                    if (!enable) {
                        safetyViewModel.setWaterRemindersEnabled(false)
                    } else if (SafetyReminderScheduler.hasNotificationPermission(this)) {
                        safetyViewModel.setWaterRemindersEnabled(true)
                    } else {
                        pendingNotifFeature = PendingNotificationFeature.WATER
                    }
                }

                val handleToggleMedicationNotifications: (Boolean) -> Unit = { enable ->
                    if (!enable) {
                        safetyViewModel.setMedicationNotificationsEnabled(false)
                    } else if (SafetyReminderScheduler.hasNotificationPermission(this)) {
                        safetyViewModel.setMedicationNotificationsEnabled(true)
                    } else {
                        pendingNotifFeature = PendingNotificationFeature.MEDICATIONS
                    }
                }

                // Handle one-shot emergency dialer events (from Shake-to-Call countdown completion)
                LaunchedEffect(safetyViewModel) {
                    safetyViewModel.events.collect { event ->
                        when (event) {
                            is SafetyNavigationEvent.OpenEmergencyDialer -> {
                                if (event.phoneNumber.isNotBlank()) {
                                    runCatching {
                                        startActivity(SafetyHealthViewModel.buildDialIntent(event.phoneNumber))
                                    }
                                } else {
                                    showMissingEmergencyDialog = true
                                }
                            }
                        }
                    }
                }

                // Refresh today's date, location status & sensor state whenever returning to foreground
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            viewModel.refreshOnResume()
                            safetyViewModel.refreshOnResume()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                MainScreen(
                    uiState = uiState,
                    workoutUiState = workoutUiState,
                    foodUiState = foodUiState,
                    safetyUiState = safetyUiState,
                    backupMessage = backupMessage,
                    onToggleTracking = viewModel::toggleTracking,
                    onRequestPermission = requestActivityPermissionWithReason,
                    onToggleTheme = viewModel::toggleBrightDark,
                    onSetThemeMode = viewModel::setThemeMode,
                    onSaveProfile = viewModel::saveProfile,
                    onClearProfile = viewModel::clearProfile,
                    onUpdateGoal = viewModel::updateGoal,
                    onUpdateStrideLength = viewModel::updateStrideLength,
                    onExportJson = {
                        exportJsonLauncher.launch("parv-health-backup-${DateUtils.todayIso()}.json")
                    },
                    onExportCsv = {
                        exportCsvLauncher.launch("parv-health-backup-${DateUtils.todayIso()}.csv")
                    },
                    onImportFromFile = {
                        importBackupLauncher.launch(
                            arrayOf("application/json", "text/csv", "text/comma-separated-values", "text/plain", "*/*")
                        )
                    },
                    onClearHistory = viewModel::clearHistory,
                    onClearFoodData = viewModel::clearAllFoodData,
                    onClearAllAppData = {
                        viewModel.clearAllAppData()
                        backupMessage = null
                    },
                    onSaveSleep = viewModel::saveSleep,
                    onDeleteTodaySleep = viewModel::deleteTodaySleep,
                    onAddReminder = viewModel::addReminder,
                    onToggleReminder = viewModel::toggleReminder,
                    onDeleteReminder = viewModel::deleteReminder,
                    onSelectWorkoutDay = workoutViewModel::selectDay,
                    onToggleWorkoutSet = workoutViewModel::toggleSet,
                    onToggleWorkoutDayDone = workoutViewModel::toggleSelectedDayDone,
                    onUpdateWorkoutSearch = workoutViewModel::updateSearchQuery,
                    onSelectWorkoutMuscle = workoutViewModel::selectMuscleFilter,
                    onSelectWorkoutLevel = workoutViewModel::selectLevelFilter,
                    onSelectWorkoutEquipment = workoutViewModel::selectEquipmentFilter,
                    onAddExerciseToDay = workoutViewModel::addExerciseToDay,
                    onRemovePlannedExercise = workoutViewModel::removePlannedExercise,
                    onCreateCustomExercise = { name, muscle, sets, reps, notes ->
                        workoutViewModel.createCustomExercise(name, muscle, sets, reps, notes)
                    },
                    onDeleteCustomExercise = workoutViewModel::deleteCustomExercise,
                    onSelectFoodDate = foodViewModel::selectDate,
                    onSaveFoodEntry = foodViewModel::saveEntry,
                    onDeleteFoodEntry = foodViewModel::deleteEntry,
                    onLogSuggestedMeal = { foodViewModel.logSuggestedMeal(it) },
                    onSaveCustomMeal = foodViewModel::saveCustomMeal,
                    onLogCustomMeal = { foodViewModel.logCustomMeal(it) },
                    onDeleteCustomMeal = foodViewModel::deleteCustomMeal,
                    onSetActivityLevel = foodViewModel::setActivityLevel,
                    onSetCalorieOverride = foodViewModel::setCalorieOverride,
                    onSetDietType = foodViewModel::setDietType,
                    onSetProteinGoal = foodViewModel::setProteinGoalG,
                    onSaveDoctor = safetyViewModel::saveDoctor,
                    onSetPrimaryDoctor = safetyViewModel::setPrimaryDoctor,
                    onDeleteDoctor = safetyViewModel::deleteDoctor,
                    onAddWater = safetyViewModel::addWaterIntake,
                    onUndoWater = safetyViewModel::undoLastWaterEntry,
                    onDeleteWaterEntry = safetyViewModel::deleteWaterEntry,
                    onSetWaterGoalOverride = safetyViewModel::setWaterGoalOverride,
                    onSaveMedication = safetyViewModel::saveMedication,
                    onDeleteMedication = safetyViewModel::deleteMedication,
                    onMarkDoseTaken = safetyViewModel::markDoseTaken,
                    onSnoozeDose = safetyViewModel::snoozeDoseTenMinutes,
                    onSkipDose = safetyViewModel::skipDose,
                    onSetFallDetectionEnabled = handleToggleFallDetection,
                    onSetFallSensitivity = safetyViewModel::setFallSensitivity,
                    onSetShakeToCallEnabled = handleToggleShakeToCall,
                    onToggleWaterReminders = handleToggleWaterReminders,
                    onToggleMedicationNotifications = handleToggleMedicationNotifications,
                    onFallImOk = safetyViewModel::respondFallImOk,
                    onFallFalseAlarm = safetyViewModel::respondFallFalseAlarm,
                    onFallDismissTimeout = safetyViewModel::dismissFallTimeoutOverlay,
                    onDeleteFallAlert = safetyViewModel::deleteFallAlert,
                    onCancelShakeCountdown = safetyViewModel::cancelShakeCountdown,
                    onTriggerSos = handleTriggerSos,
                    onEnableLocation = handleEnableLocationFromSettings,
                    onTestLocation = safetyViewModel::testLocation,
                    onClearLocationData = safetyViewModel::clearLocationData
                )

                // Plain-language explanation dialog BEFORE requesting Foreground Location permission
                pendingLocationRationaleAction?.let { action ->
                    AlertDialog(
                        onDismissRequest = {
                            safetyViewModel.markLocationRationaleShown()
                            pendingLocationRationaleAction = null
                            if (action != PendingLocationAction.SETTINGS_ENABLE_ONLY) {
                                deniedContinuationAction = action
                                showLocationDeniedInfoDialog = true
                            }
                        },
                        title = { Text(stringResource(R.string.location_permission_title)) },
                        text = { Text(stringResource(R.string.location_permission_explanation)) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    safetyViewModel.markLocationRationaleShown()
                                    pendingLocationRationaleAction = null
                                    activeLocationLauncherAction = action
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GreenPrimary,
                                    contentColor = PureWhite
                                ),
                                modifier = Modifier.testTag("location_rationale_continue_button")
                            ) {
                                Text(stringResource(R.string.permission_continue))
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    safetyViewModel.markLocationRationaleShown()
                                    pendingLocationRationaleAction = null
                                    if (action != PendingLocationAction.SETTINGS_ENABLE_ONLY) {
                                        deniedContinuationAction = action
                                        showLocationDeniedInfoDialog = true
                                    }
                                },
                                modifier = Modifier.testTag("location_rationale_skip_button")
                            ) {
                                Text(stringResource(R.string.location_continue_without))
                            }
                        }
                    )
                }

                if (showLocationDeniedInfoDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showLocationDeniedInfoDialog = false
                            val action = deniedContinuationAction
                            deniedContinuationAction = null
                            if (action != null) performPostLocationAction(action)
                        },
                        title = { Text(stringResource(R.string.location_status_unavailable)) },
                        text = { Text(stringResource(R.string.location_denied_affected_note)) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showLocationDeniedInfoDialog = false
                                    val action = deniedContinuationAction
                                    deniedContinuationAction = null
                                    if (action != null) performPostLocationAction(action)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GreenPrimary,
                                    contentColor = PureWhite
                                ),
                                modifier = Modifier.testTag("location_denied_continue_button")
                            ) {
                                Text(stringResource(R.string.location_continue_without))
                            }
                        }
                    )
                }

                // Plain-language rationale dialog BEFORE requesting Notification permission
                pendingNotifFeature?.let { feature ->
                    val reasonText = when (feature) {
                        PendingNotificationFeature.WATER -> stringResource(R.string.permission_notif_reason_water)
                        PendingNotificationFeature.MEDICATIONS -> stringResource(R.string.permission_notif_reason_meds)
                    }
                    AlertDialog(
                        onDismissRequest = { pendingNotifFeature = null },
                        title = { Text(stringResource(R.string.permission_notif_reason_title)) },
                        text = { Text(reasonText) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        when (feature) {
                                            PendingNotificationFeature.WATER -> safetyViewModel.setWaterRemindersEnabled(true)
                                            PendingNotificationFeature.MEDICATIONS -> safetyViewModel.setMedicationNotificationsEnabled(true)
                                        }
                                        pendingNotifFeature = null
                                    }
                                }
                            ) {
                                Text(stringResource(R.string.permission_continue))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { pendingNotifFeature = null }) {
                                Text(stringResource(R.string.dialog_cancel))
                            }
                        }
                    )
                }

                // Plain-language rationale dialog BEFORE requesting Activity Recognition permission
                if (showActivityReasonDialog) {
                    AlertDialog(
                        onDismissRequest = { showActivityReasonDialog = false },
                        title = { Text(stringResource(R.string.permission_activity_reason_title)) },
                        text = { Text(stringResource(R.string.permission_card_body)) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showActivityReasonDialog = false
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                        activityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                                    }
                                }
                            ) {
                                Text(stringResource(R.string.permission_continue))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showActivityReasonDialog = false }) {
                                Text(stringResource(R.string.dialog_cancel))
                            }
                        }
                    )
                }

                if (showMissingEmergencyDialog) {
                    AlertDialog(
                        onDismissRequest = { showMissingEmergencyDialog = false },
                        title = { Text(stringResource(R.string.shake_alert_title)) },
                        text = { Text(stringResource(R.string.emergency_no_number_message)) },
                        confirmButton = {
                            Button(onClick = { showMissingEmergencyDialog = false }) {
                                Text(stringResource(R.string.dialog_done))
                            }
                        }
                    )
                }
            }
        }
    }
}
