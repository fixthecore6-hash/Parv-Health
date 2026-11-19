package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ActivityLevel
import com.example.data.CustomMealEntity
import com.example.data.DietType
import com.example.data.DoctorEntity
import com.example.data.ExerciseEquipment
import com.example.data.ExerciseLevel
import com.example.data.FallSensitivity
import com.example.data.FoodEntryEntity
import com.example.data.FoodSuggestion
import com.example.data.MedicationDoseLogEntity
import com.example.data.MedicationEntity
import com.example.data.MuscleGroup
import com.example.data.ReminderCategory
import com.example.data.ThemeMode
import com.example.data.UserProfile
import com.example.ui.FoodUiState
import com.example.ui.PedometerUiState
import com.example.ui.SafetyHealthUiState
import com.example.ui.WorkoutUiState
import com.example.ui.screens.NavTab.FOOD
import com.example.ui.screens.NavTab.HISTORY
import com.example.ui.screens.NavTab.PROFILE
import com.example.ui.screens.NavTab.TODAY
import com.example.ui.screens.NavTab.WORKOUT
import com.example.ui.theme.LocalAccentColors

enum class NavTab {
    TODAY,
    WORKOUT,
    FOOD,
    HISTORY,
    PROFILE
}

enum class SafetySubScreen {
    NONE,
    DOCTORS,
    MEDICATIONS
}

@Composable
fun MainScreen(
    uiState: PedometerUiState,
    workoutUiState: WorkoutUiState,
    foodUiState: FoodUiState,
    safetyUiState: SafetyHealthUiState,
    backupMessage: String?,
    onToggleTracking: () -> Unit,
    onRequestPermission: () -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSaveProfile: (UserProfile) -> Unit,
    onClearProfile: () -> Unit,
    onUpdateGoal: (Int) -> Unit,
    onUpdateStrideLength: (Int) -> Unit,
    onExportJson: () -> Unit,
    onExportCsv: () -> Unit,
    onImportFromFile: () -> Unit,
    onClearHistory: () -> Unit,
    onClearFoodData: () -> Unit,
    onClearAllAppData: () -> Unit,
    onSaveSleep: (Float, String) -> Unit,
    onDeleteTodaySleep: () -> Unit,
    onAddReminder: (ReminderCategory, String, String, String) -> Unit,
    onToggleReminder: (Long, Boolean) -> Unit,
    onDeleteReminder: (Long) -> Unit,
    onSelectWorkoutDay: (Int) -> Unit,
    onToggleWorkoutSet: (String, Int, Boolean) -> Unit,
    onToggleWorkoutDayDone: () -> Unit,
    onUpdateWorkoutSearch: (String) -> Unit,
    onSelectWorkoutMuscle: (MuscleGroup?) -> Unit,
    onSelectWorkoutLevel: (ExerciseLevel?) -> Unit,
    onSelectWorkoutEquipment: (ExerciseEquipment?) -> Unit,
    onAddExerciseToDay: (Int, String) -> Unit,
    onRemovePlannedExercise: (Long) -> Unit,
    onCreateCustomExercise: (String, MuscleGroup, Int, String, String) -> Unit,
    onDeleteCustomExercise: (String) -> Unit,
    onSelectFoodDate: (String) -> Unit,
    onSaveFoodEntry: (FoodEntryEntity) -> Unit,
    onDeleteFoodEntry: (Long) -> Unit,
    onLogSuggestedMeal: (FoodSuggestion) -> Unit,
    onSaveCustomMeal: (CustomMealEntity) -> Unit,
    onLogCustomMeal: (CustomMealEntity) -> Unit,
    onDeleteCustomMeal: (Long) -> Unit,
    onSetActivityLevel: (ActivityLevel) -> Unit,
    onSetCalorieOverride: (Int?) -> Unit,
    onSetDietType: (DietType) -> Unit,
    onSetProteinGoal: (Int) -> Unit,
    onSaveDoctor: (DoctorEntity) -> Unit,
    onSetPrimaryDoctor: (Long) -> Unit,
    onDeleteDoctor: (Long) -> Unit,
    onAddWater: (Int) -> Unit,
    onUndoWater: () -> Unit,
    onDeleteWaterEntry: (Long) -> Unit,
    onSetWaterGoalOverride: (Int?) -> Unit,
    onSaveMedication: (MedicationEntity) -> Unit,
    onDeleteMedication: (Long) -> Unit,
    onMarkDoseTaken: (MedicationDoseLogEntity) -> Unit,
    onSnoozeDose: (MedicationDoseLogEntity) -> Unit,
    onSkipDose: (MedicationDoseLogEntity) -> Unit,
    onSetFallDetectionEnabled: (Boolean) -> Unit,
    onSetFallSensitivity: (FallSensitivity) -> Unit,
    onSetShakeToCallEnabled: (Boolean) -> Unit,
    onToggleWaterReminders: (Boolean) -> Unit,
    onToggleMedicationNotifications: (Boolean) -> Unit,
    onFallImOk: () -> Unit,
    onFallFalseAlarm: () -> Unit,
    onFallDismissTimeout: () -> Unit,
    onDeleteFallAlert: (Long) -> Unit,
    onCancelShakeCountdown: () -> Unit,
    onTriggerSos: () -> Unit,
    onEnableLocation: () -> Unit,
    onTestLocation: () -> Unit,
    onClearLocationData: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(TODAY) }
    var activeSubScreen by rememberSaveable { mutableStateOf(SafetySubScreen.NONE) }
    var showSettingsSheet by rememberSaveable { mutableStateOf(false) }

    val accents = LocalAccentColors.current

    BackHandler(enabled = activeSubScreen != SafetySubScreen.NONE || selectedTab != TODAY) {
        if (activeSubScreen != SafetySubScreen.NONE) {
            activeSubScreen = SafetySubScreen.NONE
        } else {
            selectedTab = TODAY
        }
    }

    val selectMainTab: (NavTab) -> Unit = { tab ->
        activeSubScreen = SafetySubScreen.NONE
        selectedTab = tab
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        val navBarColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onSurface,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            indicatorColor = accents.greenTint,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val navRailColors = NavigationRailItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onSurface,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            indicatorColor = accents.greenTint,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == TODAY,
                            onClick = { selectMainTab(TODAY) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == TODAY) {
                                        Icons.Rounded.DirectionsWalk
                                    } else {
                                        Icons.Outlined.DirectionsWalk
                                    },
                                    contentDescription = stringResource(R.string.nav_today)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(R.string.nav_today),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = navBarColors,
                            modifier = Modifier.testTag("nav_today_tab")
                        )
                        NavigationBarItem(
                            selected = selectedTab == WORKOUT,
                            onClick = { selectMainTab(WORKOUT) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == WORKOUT) {
                                        Icons.Rounded.FitnessCenter
                                    } else {
                                        Icons.Outlined.FitnessCenter
                                    },
                                    contentDescription = stringResource(R.string.nav_workout)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(R.string.nav_workout),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = navBarColors,
                            modifier = Modifier.testTag("nav_workout_tab")
                        )
                        NavigationBarItem(
                            selected = selectedTab == FOOD,
                            onClick = { selectMainTab(FOOD) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == FOOD) {
                                        Icons.Rounded.Restaurant
                                    } else {
                                        Icons.Outlined.Restaurant
                                    },
                                    contentDescription = stringResource(R.string.nav_food)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(R.string.nav_food),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = navBarColors,
                            modifier = Modifier.testTag("nav_food_tab")
                        )
                        NavigationBarItem(
                            selected = selectedTab == HISTORY,
                            onClick = { selectMainTab(HISTORY) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == HISTORY) {
                                        Icons.Rounded.BarChart
                                    } else {
                                        Icons.Outlined.BarChart
                                    },
                                    contentDescription = stringResource(R.string.nav_history)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(R.string.nav_history),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = navBarColors,
                            modifier = Modifier.testTag("nav_history_tab")
                        )
                        NavigationBarItem(
                            selected = selectedTab == PROFILE,
                            onClick = { selectMainTab(PROFILE) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == PROFILE) {
                                        Icons.Rounded.Person
                                    } else {
                                        Icons.Outlined.Person
                                    },
                                    contentDescription = stringResource(R.string.nav_profile)
                                )
                            },
                            label = {
                                Text(
                                    text = stringResource(R.string.nav_profile),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = navBarColors,
                            modifier = Modifier.testTag("nav_profile_tab")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Spacer(modifier = Modifier.weight(0.1f))
                        NavigationRailItem(
                            selected = selectedTab == TODAY,
                            onClick = { selectMainTab(TODAY) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == TODAY) {
                                        Icons.Rounded.DirectionsWalk
                                    } else {
                                        Icons.Outlined.DirectionsWalk
                                    },
                                    contentDescription = stringResource(R.string.nav_today)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_today)) },
                            colors = navRailColors,
                            modifier = Modifier.testTag("nav_today_rail")
                        )
                        NavigationRailItem(
                            selected = selectedTab == WORKOUT,
                            onClick = { selectMainTab(WORKOUT) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == WORKOUT) {
                                        Icons.Rounded.FitnessCenter
                                    } else {
                                        Icons.Outlined.FitnessCenter
                                    },
                                    contentDescription = stringResource(R.string.nav_workout)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_workout)) },
                            colors = navRailColors,
                            modifier = Modifier.testTag("nav_workout_rail")
                        )
                        NavigationRailItem(
                            selected = selectedTab == FOOD,
                            onClick = { selectMainTab(FOOD) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == FOOD) {
                                        Icons.Rounded.Restaurant
                                    } else {
                                        Icons.Outlined.Restaurant
                                    },
                                    contentDescription = stringResource(R.string.nav_food)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_food)) },
                            colors = navRailColors,
                            modifier = Modifier.testTag("nav_food_rail")
                        )
                        NavigationRailItem(
                            selected = selectedTab == HISTORY,
                            onClick = { selectMainTab(HISTORY) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == HISTORY) {
                                        Icons.Rounded.BarChart
                                    } else {
                                        Icons.Outlined.BarChart
                                    },
                                    contentDescription = stringResource(R.string.nav_history)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_history)) },
                            colors = navRailColors,
                            modifier = Modifier.testTag("nav_history_rail")
                        )
                        NavigationRailItem(
                            selected = selectedTab == PROFILE,
                            onClick = { selectMainTab(PROFILE) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == PROFILE) {
                                        Icons.Rounded.Person
                                    } else {
                                        Icons.Outlined.Person
                                    },
                                    contentDescription = stringResource(R.string.nav_profile)
                                )
                            },
                            label = { Text(stringResource(R.string.nav_profile)) },
                            colors = navRailColors,
                            modifier = Modifier.testTag("nav_profile_rail")
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                when {
                    activeSubScreen == SafetySubScreen.DOCTORS -> DoctorsScreen(
                        doctors = safetyUiState.doctors,
                        onSaveDoctor = onSaveDoctor,
                        onSetPrimary = onSetPrimaryDoctor,
                        onDeleteDoctor = onDeleteDoctor,
                        onToggleTheme = onToggleTheme,
                        onBack = { activeSubScreen = SafetySubScreen.NONE },
                        modifier = Modifier.weight(1f)
                    )
                    activeSubScreen == SafetySubScreen.MEDICATIONS -> MedicationsScreen(
                        medicationsWithStats = safetyUiState.medicationsWithStats,
                        dueDoses = safetyUiState.dueMedicationDoses,
                        medicationNotificationsEnabled = safetyUiState.medicationNotificationsEnabled,
                        onToggleNotifications = onToggleMedicationNotifications,
                        onSaveMedication = onSaveMedication,
                        onDeleteMedication = onDeleteMedication,
                        onMarkTaken = onMarkDoseTaken,
                        onSnoozeTenMinutes = onSnoozeDose,
                        onSkipDose = onSkipDose,
                        onToggleTheme = onToggleTheme,
                        onBack = { activeSubScreen = SafetySubScreen.NONE },
                        modifier = Modifier.weight(1f)
                    )
                    selectedTab == TODAY -> TodayScreen(
                        uiState = uiState,
                        safetyUiState = safetyUiState,
                        onToggleTracking = onToggleTracking,
                        onRequestPermission = onRequestPermission,
                        onToggleTheme = onToggleTheme,
                        onOpenSettings = { showSettingsSheet = true },
                        onOpenHistory = { selectMainTab(HISTORY) },
                        onOpenProfile = { selectMainTab(PROFILE) },
                        onOpenDoctors = { activeSubScreen = SafetySubScreen.DOCTORS },
                        onOpenMedications = { activeSubScreen = SafetySubScreen.MEDICATIONS },
                        onAddWater = onAddWater,
                        onUndoWater = onUndoWater,
                        onSetWaterGoalOverride = onSetWaterGoalOverride,
                        onMarkDoseTaken = onMarkDoseTaken,
                        onSnoozeDose = onSnoozeDose,
                        onSkipDose = onSkipDose,
                        onSaveSleep = onSaveSleep,
                        onDeleteTodaySleep = onDeleteTodaySleep,
                        onAddReminder = onAddReminder,
                        onToggleReminder = onToggleReminder,
                        onDeleteReminder = onDeleteReminder,
                        onTriggerSos = onTriggerSos,
                        modifier = Modifier.weight(1f)
                    )
                    selectedTab == WORKOUT -> WorkoutScreen(
                        uiState = workoutUiState,
                        onSelectDay = onSelectWorkoutDay,
                        onToggleSet = onToggleWorkoutSet,
                        onToggleDayDone = onToggleWorkoutDayDone,
                        onUpdateSearchQuery = onUpdateWorkoutSearch,
                        onSelectMuscle = onSelectWorkoutMuscle,
                        onSelectLevel = onSelectWorkoutLevel,
                        onSelectEquipment = onSelectWorkoutEquipment,
                        onAddExerciseToDay = onAddExerciseToDay,
                        onRemovePlannedExercise = onRemovePlannedExercise,
                        onCreateCustomExercise = onCreateCustomExercise,
                        onDeleteCustomExercise = onDeleteCustomExercise,
                        onToggleTheme = onToggleTheme,
                        modifier = Modifier.weight(1f)
                    )
                    selectedTab == FOOD -> FoodScreen(
                        uiState = foodUiState,
                        onSelectDate = onSelectFoodDate,
                        onSaveEntry = onSaveFoodEntry,
                        onDeleteEntry = onDeleteFoodEntry,
                        onLogSuggestedMeal = onLogSuggestedMeal,
                        onSaveCustomMeal = onSaveCustomMeal,
                        onLogCustomMeal = onLogCustomMeal,
                        onDeleteCustomMeal = onDeleteCustomMeal,
                        onSetActivityLevel = onSetActivityLevel,
                        onSetCalorieOverride = onSetCalorieOverride,
                        onSetDietType = onSetDietType,
                        onSetProteinGoal = onSetProteinGoal,
                        onOpenProfile = { selectMainTab(PROFILE) },
                        onOpenSettings = { showSettingsSheet = true },
                        onToggleTheme = onToggleTheme,
                        modifier = Modifier.weight(1f)
                    )
                    selectedTab == HISTORY -> HistoryScreen(
                        uiState = uiState,
                        safetyUiState = safetyUiState,
                        onDeleteWaterEntry = onDeleteWaterEntry,
                        onDeleteFallAlert = onDeleteFallAlert,
                        onToggleTheme = onToggleTheme,
                        onOpenSettings = { showSettingsSheet = true },
                        modifier = Modifier.weight(1f)
                    )
                    selectedTab == PROFILE -> ProfileScreen(
                        userProfile = uiState.userProfile,
                        onSaveProfile = onSaveProfile,
                        onClearProfile = onClearProfile,
                        onToggleTheme = onToggleTheme,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Full-Screen Fall Detection & Shake Countdown Overlays (strictly green, black, white)
        safetyUiState.activeFallAlert?.let { fallAlert ->
            FallAlertOverlay(
                alertState = fallAlert,
                emergencyNumber = uiState.userProfile.emergencyContact,
                primaryDoctor = safetyUiState.primaryDoctor,
                onImOk = onFallImOk,
                onFalseAlarm = onFallFalseAlarm,
                onDismissTimeout = onFallDismissTimeout
            )
        }

        safetyUiState.activeShakeAlert?.let { shakeAlert ->
            ShakeCountdownOverlay(
                alertState = shakeAlert,
                emergencyNumber = uiState.userProfile.emergencyContact,
                onCancel = onCancelShakeCountdown
            )
        }
    }

    if (showSettingsSheet) {
        SettingsSheet(
            dailyGoal = uiState.dailyGoal,
            strideLengthCm = uiState.strideLengthCm,
            themeMode = uiState.themeMode,
            safetyUiState = safetyUiState,
            backupMessage = backupMessage,
            onUpdateGoal = onUpdateGoal,
            onUpdateStrideLength = onUpdateStrideLength,
            onSetThemeMode = onSetThemeMode,
            onSetFallDetectionEnabled = onSetFallDetectionEnabled,
            onSetFallSensitivity = onSetFallSensitivity,
            onSetShakeToCallEnabled = onSetShakeToCallEnabled,
            onToggleWaterReminders = onToggleWaterReminders,
            onToggleMedicationNotifications = onToggleMedicationNotifications,
            onEnableLocation = onEnableLocation,
            onTestLocation = onTestLocation,
            onClearLocationData = onClearLocationData,
            onExportJson = onExportJson,
            onExportCsv = onExportCsv,
            onImportFromFile = onImportFromFile,
            onClearHistory = onClearHistory,
            onClearFoodData = onClearFoodData,
            onClearAllAppData = onClearAllAppData,
            onDismiss = { showSettingsSheet = false }
        )
    }
}
