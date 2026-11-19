package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ParvHealthApplication
import com.example.data.DailyStepsEntity
import com.example.data.DataBackupManager
import com.example.data.DateUtils
import com.example.data.FoodRepository
import com.example.data.ReminderCategory
import com.example.data.ReminderEntity
import com.example.data.SettingsStore
import com.example.data.SleepEntryEntity
import com.example.data.StepRepository
import com.example.data.ThemeMode
import com.example.data.UserProfile
import com.example.data.WellnessRepository
import com.example.data.WorkoutDayEntity
import com.example.data.WorkoutRepository
import com.example.pedometer.PedometerForegroundService
import com.example.pedometer.SensorMode
import com.example.pedometer.StepSensorTracker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChartMetricPoint(
    val dateIso: String,
    val value: Float
)

data class PedometerUiState(
    val today: DailyStepsEntity = DailyStepsEntity(date = DateUtils.todayIso()),
    val history: List<DailyStepsEntity> = emptyList(),
    val weeklyDays: List<DailyStepsEntity> = emptyList(),
    val monthlyStepPoints: List<ChartMetricPoint> = emptyList(),
    val weeklyWorkoutPoints: List<ChartMetricPoint> = emptyList(),
    val monthlyWorkoutPoints: List<ChartMetricPoint> = emptyList(),
    val weeklyCaloriePoints: List<ChartMetricPoint> = emptyList(),
    val monthlyCaloriePoints: List<ChartMetricPoint> = emptyList(),
    val weeklySleepPoints: List<ChartMetricPoint> = emptyList(),
    val monthlySleepPoints: List<ChartMetricPoint> = emptyList(),
    val workoutHistory: List<WorkoutDayEntity> = emptyList(),
    val todaySleep: SleepEntryEntity? = null,
    val reminders: List<ReminderEntity> = emptyList(),
    val dailyGoal: Int = SettingsStore.DEFAULT_GOAL,
    val strideLengthCm: Int = SettingsStore.DEFAULT_STRIDE_CM,
    val isTrackingEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val userProfile: UserProfile = UserProfile(),
    val isSensorListening: Boolean = false,
    val hasActivityPermission: Boolean = false,
    val sensorMode: SensorMode = SensorMode.HARDWARE_COUNTER,
    val weeklyAverageSteps: Int = 0,
    val totalStoredSteps: Long = 0L,
    val bestDaySteps: Int = 0,
    val currentStreak: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class PedometerViewModel(
    application: Application,
    private val repository: StepRepository,
    private val workoutRepository: WorkoutRepository,
    private val foodRepository: FoodRepository,
    private val wellnessRepository: WellnessRepository,
    private val dataBackupManager: DataBackupManager,
    private val settingsStore: SettingsStore,
    private val sensorTracker: StepSensorTracker
) : AndroidViewModel(application) {

    private val currentDateIso = MutableStateFlow(DateUtils.todayIso())
    private val lastThirtyDates = DateUtils.lastDaysIso(30)
    private val lastSevenDates = DateUtils.lastSevenDaysIso()
    private val permissionGrantedState = MutableStateFlow(sensorTracker.hasRequiredPermission())

    private val todayFlow = currentDateIso.flatMapLatest { date ->
        repository.observeToday(date)
    }

    private val todaySleepFlow = currentDateIso.flatMapLatest { date ->
        wellnessRepository.observeSleepForDate(date)
    }

    private data class SettingsTuple(
        val goal: Int,
        val strideCm: Int,
        val trackingEnabled: Boolean,
        val themeMode: ThemeMode,
        val userProfile: UserProfile
    )

    private val settingsFlow = combine(
        repository.dailyGoal,
        repository.strideLengthCm,
        repository.isTrackingEnabled,
        repository.themeMode,
        repository.userProfile
    ) { goal, strideCm, trackingEnabled, themeMode, userProfile ->
        SettingsTuple(goal, strideCm, trackingEnabled, themeMode, userProfile)
    }

    private val trackingStatusFlow = combine(
        sensorTracker.isListening,
        permissionGrantedState,
        workoutRepository.observeWorkoutHistory()
    ) { isListening, hasPermission, workoutHistory ->
        Triple(isListening, hasPermission, workoutHistory)
    }

    private val wellnessChartsFlow = combine(
        todaySleepFlow,
        wellnessRepository.observeSleepForDates(lastThirtyDates),
        wellnessRepository.observeReminders(),
        foodRepository.observeEntriesForDates(lastThirtyDates),
        workoutRepository.observeDaysForDates(lastThirtyDates)
    ) { todaySleep, monthlySleep, reminders, monthlyFood, monthlyWorkouts ->
        WellnessBundle(todaySleep, monthlySleep, reminders, monthlyFood, monthlyWorkouts)
    }

    private data class WellnessBundle(
        val todaySleep: SleepEntryEntity?,
        val monthlySleep: List<SleepEntryEntity>,
        val reminders: List<ReminderEntity>,
        val monthlyFood: List<com.example.data.FoodEntryEntity>,
        val monthlyWorkouts: List<WorkoutDayEntity>
    )

    val uiState: StateFlow<PedometerUiState> = combine(
        todayFlow,
        repository.observeAllDays(),
        settingsFlow,
        trackingStatusFlow,
        wellnessChartsFlow
    ) { today, allDays, settings, status, wellness ->
        val (isListening, hasPermission, workoutHistory) = status
        val resolvedToday = today.copy(goal = settings.goal, strideCm = settings.strideCm)

        val dayByDate = allDays.associateBy { it.date }
        val weeklyDays = lastSevenDates.map { date ->
            val stored = dayByDate[date]
            if (date == resolvedToday.date) {
                resolvedToday
            } else {
                stored ?: DailyStepsEntity(
                    date = date,
                    steps = 0,
                    goal = settings.goal,
                    activeSeconds = 0L,
                    strideCm = settings.strideCm
                )
            }
        }

        val monthlyStepPoints = lastThirtyDates.map { date ->
            val steps = if (date == resolvedToday.date) resolvedToday.steps else (dayByDate[date]?.steps ?: 0)
            ChartMetricPoint(dateIso = date, value = steps.toFloat())
        }

        val workoutSetsByDate = wellness.monthlyWorkouts.associate { it.date to it.completedSets.toFloat() }
        val weeklyWorkoutPoints = lastSevenDates.map { date ->
            ChartMetricPoint(dateIso = date, value = workoutSetsByDate[date] ?: 0f)
        }
        val monthlyWorkoutPoints = lastThirtyDates.map { date ->
            ChartMetricPoint(dateIso = date, value = workoutSetsByDate[date] ?: 0f)
        }

        val calsByDate = wellness.monthlyFood.groupBy { it.date }.mapValues { (_, list) ->
            list.sumOf { it.calories }.toFloat()
        }
        val weeklyCaloriePoints = lastSevenDates.map { date ->
            ChartMetricPoint(dateIso = date, value = calsByDate[date] ?: 0f)
        }
        val monthlyCaloriePoints = lastThirtyDates.map { date ->
            ChartMetricPoint(dateIso = date, value = calsByDate[date] ?: 0f)
        }

        val sleepByDate = wellness.monthlySleep.associate { it.date to it.hoursSlept }
        val weeklySleepPoints = lastSevenDates.map { date ->
            ChartMetricPoint(dateIso = date, value = sleepByDate[date] ?: 0f)
        }
        val monthlySleepPoints = lastThirtyDates.map { date ->
            ChartMetricPoint(dateIso = date, value = sleepByDate[date] ?: 0f)
        }

        val weeklyAvg = if (weeklyDays.isNotEmpty()) {
            weeklyDays.sumOf { it.steps } / weeklyDays.size
        } else {
            0
        }

        val totalSteps = allDays.sumOf { it.steps.toLong() }
        val bestDay = allDays.maxOfOrNull { it.steps } ?: 0
        val streak = calculateStreak(dayByDate, settings.goal)

        PedometerUiState(
            today = resolvedToday,
            history = allDays,
            weeklyDays = weeklyDays,
            monthlyStepPoints = monthlyStepPoints,
            weeklyWorkoutPoints = weeklyWorkoutPoints,
            monthlyWorkoutPoints = monthlyWorkoutPoints,
            weeklyCaloriePoints = weeklyCaloriePoints,
            monthlyCaloriePoints = monthlyCaloriePoints,
            weeklySleepPoints = weeklySleepPoints,
            monthlySleepPoints = monthlySleepPoints,
            workoutHistory = workoutHistory,
            todaySleep = wellness.todaySleep,
            reminders = wellness.reminders,
            dailyGoal = settings.goal,
            strideLengthCm = settings.strideCm,
            isTrackingEnabled = settings.trackingEnabled,
            themeMode = settings.themeMode,
            userProfile = settings.userProfile,
            isSensorListening = isListening,
            hasActivityPermission = hasPermission,
            sensorMode = sensorTracker.sensorMode,
            weeklyAverageSteps = weeklyAvg,
            totalStoredSteps = totalSteps,
            bestDaySteps = bestDay,
            currentStreak = streak
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PedometerUiState(
            hasActivityPermission = sensorTracker.hasRequiredPermission(),
            sensorMode = sensorTracker.sensorMode
        )
    )

    init {
        viewModelScope.launch {
            repository.ensureTodayInitialized()
            wellnessRepository.ensureDefaultRemindersSeeded()
            syncTrackingState()
        }
    }

    fun refreshOnResume() {
        val todayIso = DateUtils.todayIso()
        if (currentDateIso.value != todayIso) {
            currentDateIso.value = todayIso
        }
        permissionGrantedState.value = sensorTracker.hasRequiredPermission()
        viewModelScope.launch {
            repository.ensureTodayInitialized()
            syncTrackingState()
        }
    }

    fun onPermissionResult(granted: Boolean) {
        permissionGrantedState.value = granted || sensorTracker.hasRequiredPermission()
        viewModelScope.launch {
            syncTrackingState()
        }
    }

    fun toggleTracking() {
        viewModelScope.launch {
            val nextEnabled = !uiState.value.isTrackingEnabled
            repository.setTrackingEnabled(nextEnabled)
            if (nextEnabled) {
                startPedometerIfPermitted()
            } else {
                sensorTracker.stop()
                PedometerForegroundService.stop(getApplication())
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun toggleBrightDark(currentlyDark: Boolean) {
        setThemeMode(if (currentlyDark) ThemeMode.BRIGHT else ThemeMode.DARK)
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
        }
    }

    fun clearProfile() {
        viewModelScope.launch {
            repository.clearProfile()
        }
    }

    fun updateGoal(goal: Int) {
        viewModelScope.launch {
            repository.updateDailyGoal(goal)
        }
    }

    fun updateStrideLength(cm: Int) {
        viewModelScope.launch {
            repository.updateStrideLengthCm(cm)
        }
    }

    fun saveSleep(hoursSlept: Float, note: String) {
        viewModelScope.launch {
            wellnessRepository.saveSleep(currentDateIso.value, hoursSlept, note)
        }
    }

    fun deleteTodaySleep() {
        viewModelScope.launch {
            wellnessRepository.deleteSleepForDate(currentDateIso.value)
        }
    }

    fun addReminder(
        category: ReminderCategory,
        title: String,
        timeLabel: String,
        scheduleLabel: String
    ) {
        viewModelScope.launch {
            wellnessRepository.addReminder(category, title, timeLabel, scheduleLabel)
        }
    }

    fun toggleReminder(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            wellnessRepository.toggleReminder(id, enabled)
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            wellnessRepository.deleteReminder(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearLocalHistory()
            workoutRepository.clearAllWorkoutData(clearCustomAndPlan = false)
        }
    }

    fun clearAllFoodData() {
        viewModelScope.launch {
            foodRepository.clearAllFoodData()
        }
    }

    fun clearAllAppData() {
        viewModelScope.launch {
            repository.clearLocalHistory()
            workoutRepository.clearAllWorkoutData(clearCustomAndPlan = true)
            foodRepository.clearAllFoodData()
            wellnessRepository.clearAllWellnessData()
            (getApplication<Application>() as? ParvHealthApplication)?.let { app ->
                app.safetyHealthRepository.clearAllSafetyHealthData()
                app.safetyMotionMonitor.updateConfig(false, com.example.data.FallSensitivity.MEDIUM, false)
            }
            com.example.safety.SafetyReminderScheduler.cancelAll(getApplication())
            settingsStore.clearAllSettings()
            workoutRepository.ensureDefaultPlanSeeded()
        }
    }

    suspend fun exportJsonBackup(): String = dataBackupManager.exportToJson()

    suspend fun exportCsvBackup(): String = dataBackupManager.exportToCsv()

    suspend fun importBackup(rawText: String) {
        dataBackupManager.importFromText(rawText)
    }

    private suspend fun syncTrackingState() {
        val snapshot = settingsStore.getSnapshot()
        if (snapshot.isTrackingEnabled) {
            startPedometerIfPermitted()
        } else {
            sensorTracker.stop()
            PedometerForegroundService.stop(getApplication())
        }
    }

    private fun startPedometerIfPermitted() {
        val hasPerm = sensorTracker.hasRequiredPermission()
        permissionGrantedState.value = hasPerm
        if (hasPerm || sensorTracker.sensorMode == SensorMode.ACCELEROMETER_FALLBACK) {
            sensorTracker.start()
            if (hasPerm) {
                PedometerForegroundService.start(getApplication())
            }
        }
    }

    private fun calculateStreak(dayByDate: Map<String, DailyStepsEntity>, fallbackGoal: Int): Int {
        var streak = 0
        val today = DateUtils.todayIso()
        val todayEntry = dayByDate[today]
        val startOffset = if (todayEntry != null && todayEntry.goalReached) 0 else 1
        var offset = startOffset
        while (offset < 365) {
            val date = DateUtils.daysAgoIso(offset)
            val entry = dayByDate[date] ?: break
            val target = if (entry.goal > 0) entry.goal else fallbackGoal
            if (entry.steps >= target) {
                streak++
                offset++
            } else {
                break
            }
        }
        return streak
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ParvHealthApplication
                PedometerViewModel(
                    application = app,
                    repository = app.repository,
                    workoutRepository = app.workoutRepository,
                    foodRepository = app.foodRepository,
                    wellnessRepository = app.wellnessRepository,
                    dataBackupManager = app.dataBackupManager,
                    settingsStore = app.settingsStore,
                    sensorTracker = app.sensorTracker
                )
            }
        }
    }
}
