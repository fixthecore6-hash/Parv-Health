package com.example.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ParvHealthApplication
import com.example.data.AlertLocationSource
import com.example.data.DateUtils
import com.example.data.DoctorEntity
import com.example.data.EmergencyAlertType
import com.example.data.FallAlertEntity
import com.example.data.FallAlertOutcome
import com.example.data.FallSensitivity
import com.example.data.MedicationDoseLogEntity
import com.example.data.MedicationDoseStatus
import com.example.data.MedicationEntity
import com.example.data.MedicationWeeklyStats
import com.example.data.SafetyHealthRepository
import com.example.data.SettingsStore
import com.example.data.UserProfile
import com.example.data.WaterEntryEntity
import com.example.safety.EmergencyLocationProvider
import com.example.safety.EmergencyLocationStatus
import com.example.safety.LocationResolutionResult
import com.example.safety.MotionTriggerEvent
import com.example.safety.SafetyMotionMonitor
import com.example.safety.SafetyReminderScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MedicationWithStats(
    val medication: MedicationEntity,
    val stats: MedicationWeeklyStats,
    val todayLogs: List<MedicationDoseLogEntity>
)

data class DueMedicationDoseItem(
    val log: MedicationDoseLogEntity,
    val medication: MedicationEntity,
    val status: MedicationDoseStatus
)

data class WaterDaySummary(
    val dateIso: String,
    val totalMl: Int
)

data class ActiveFallAlertState(
    val alertId: Long,
    val alertType: EmergencyAlertType = EmergencyAlertType.FALL,
    val peakG: Float,
    val sensitivity: FallSensitivity,
    val alertTimestampMs: Long,
    val totalSeconds: Int = 30,
    val secondsRemaining: Int,
    val timedOutNoResponse: Boolean,
    val isResolvingLocation: Boolean = false,
    val locationResult: LocationResolutionResult? = null
)

data class ActiveShakeAlertState(
    val secondsRemaining: Int
)

sealed interface LocationTestUiState {
    data object Idle : LocationTestUiState
    data object Loading : LocationTestUiState
    data class Completed(val result: LocationResolutionResult) : LocationTestUiState
}

data class SafetyHealthUiState(
    val doctors: List<DoctorEntity> = emptyList(),
    val primaryDoctor: DoctorEntity? = null,
    val fallAlerts: List<FallAlertEntity> = emptyList(),
    val todayWaterEntries: List<WaterEntryEntity> = emptyList(),
    val todayWaterMl: Int = 0,
    val suggestedWaterGoalMl: Int = SafetyHealthRepository.DEFAULT_WATER_GOAL_ML,
    val waterGoalOverrideMl: Int? = null,
    val effectiveWaterGoalMl: Int = SafetyHealthRepository.DEFAULT_WATER_GOAL_ML,
    val weeklyWaterDays: List<WaterDaySummary> = emptyList(),
    val medicationsWithStats: List<MedicationWithStats> = emptyList(),
    val dueMedicationDoses: List<DueMedicationDoseItem> = emptyList(),
    val fallDetectionEnabled: Boolean = false,
    val fallSensitivity: FallSensitivity = FallSensitivity.MEDIUM,
    val shakeToCallEnabled: Boolean = false,
    val waterRemindersEnabled: Boolean = false,
    val medicationNotificationsEnabled: Boolean = false,
    val hasAccelerometer: Boolean = true,
    val userProfile: UserProfile = UserProfile(),
    val activeFallAlert: ActiveFallAlertState? = null,
    val activeShakeAlert: ActiveShakeAlertState? = null,
    val locationStatus: EmergencyLocationStatus = EmergencyLocationStatus.UNAVAILABLE,
    val hasLocationPermission: Boolean = false,
    val hasFineLocationPermission: Boolean = false,
    val locationRationaleShown: Boolean = false,
    val locationTestState: LocationTestUiState = LocationTestUiState.Idle
)

sealed interface SafetyNavigationEvent {
    data class OpenEmergencyDialer(val phoneNumber: String) : SafetyNavigationEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
class SafetyHealthViewModel(
    application: Application,
    private val repository: SafetyHealthRepository,
    private val settingsStore: SettingsStore,
    private val motionMonitor: SafetyMotionMonitor,
    private val locationProvider: EmergencyLocationProvider
) : AndroidViewModel(application) {

    private val currentDateIso = MutableStateFlow(DateUtils.todayIso())
    private val lastSevenDates = DateUtils.lastSevenDaysIso()

    private val activeFallAlert = MutableStateFlow<ActiveFallAlertState?>(null)
    private val activeShakeAlert = MutableStateFlow<ActiveShakeAlertState?>(null)
    private val currentMinuteTick = MutableStateFlow(System.currentTimeMillis())

    private val locationStatusState = MutableStateFlow(locationProvider.getLocationStatus())
    private val hasLocationPermissionState = MutableStateFlow(locationProvider.hasAnyLocationPermission())
    private val hasFineLocationPermissionState = MutableStateFlow(locationProvider.hasFineLocationPermission())
    private val locationTestState = MutableStateFlow<LocationTestUiState>(LocationTestUiState.Idle)

    private val _events = MutableSharedFlow<SafetyNavigationEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<SafetyNavigationEvent> = _events.asSharedFlow()

    private var fallCountdownJob: Job? = null
    private var shakeCountdownJob: Job? = null
    private var locationTestJob: Job? = null

    private val todayWaterFlow = currentDateIso.flatMapLatest { date ->
        repository.observeWaterEntriesForDate(date)
    }

    private val weeklyWaterFlow = repository.observeWaterEntriesForDates(lastSevenDates)
    private val weeklyDoseLogsFlow = repository.observeDoseLogsForDates(lastSevenDates)

    private data class SafetyPrefs(
        val fallEnabled: Boolean,
        val fallSensitivity: FallSensitivity,
        val shakeEnabled: Boolean,
        val waterRemindersEnabled: Boolean,
        val medNotificationsEnabled: Boolean,
        val waterGoalOverrideMl: Int?,
        val profile: UserProfile,
        val locationRationaleShown: Boolean
    )

    private val prefsFlow = combine(
        settingsStore.fallDetectionEnabled,
        settingsStore.fallSensitivity,
        settingsStore.shakeToCallEnabled,
        settingsStore.waterRemindersEnabled,
        settingsStore.medicationNotificationsEnabled,
        settingsStore.waterGoalOverrideMl,
        settingsStore.userProfile,
        settingsStore.locationRationaleShown
    ) { values ->
        SafetyPrefs(
            fallEnabled = values[0] as Boolean,
            fallSensitivity = values[1] as FallSensitivity,
            shakeEnabled = values[2] as Boolean,
            waterRemindersEnabled = values[3] as Boolean,
            medNotificationsEnabled = values[4] as Boolean,
            waterGoalOverrideMl = values[5] as Int?,
            profile = values[6] as UserProfile,
            locationRationaleShown = values[7] as Boolean
        )
    }

    private data class CoreRecords(
        val doctors: List<DoctorEntity>,
        val fallAlerts: List<FallAlertEntity>,
        val todayWater: List<WaterEntryEntity>,
        val weeklyWater: List<WaterEntryEntity>,
        val medications: List<MedicationEntity>,
        val weeklyDoseLogs: List<MedicationDoseLogEntity>
    )

    private val recordsFlow = combine(
        repository.observeDoctors(),
        repository.observeFallAlerts(),
        todayWaterFlow,
        weeklyWaterFlow,
        repository.observeMedications(),
        weeklyDoseLogsFlow
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        CoreRecords(
            doctors = values[0] as List<DoctorEntity>,
            fallAlerts = values[1] as List<FallAlertEntity>,
            todayWater = values[2] as List<WaterEntryEntity>,
            weeklyWater = values[3] as List<WaterEntryEntity>,
            medications = values[4] as List<MedicationEntity>,
            weeklyDoseLogs = values[5] as List<MedicationDoseLogEntity>
        )
    }

    private data class LocationUiSnapshot(
        val status: EmergencyLocationStatus,
        val hasAnyPermission: Boolean,
        val hasFinePermission: Boolean,
        val testState: LocationTestUiState
    )

    private val locationSnapshotFlow = combine(
        locationStatusState,
        hasLocationPermissionState,
        hasFineLocationPermissionState,
        locationTestState
    ) { status, hasAny, hasFine, testState ->
        LocationUiSnapshot(
            status = status,
            hasAnyPermission = hasAny,
            hasFinePermission = hasFine,
            testState = testState
        )
    }

    val uiState: StateFlow<SafetyHealthUiState> = combine(
        recordsFlow,
        prefsFlow,
        activeFallAlert,
        activeShakeAlert,
        currentMinuteTick,
        locationSnapshotFlow
    ) { values ->
        val records = values[0] as CoreRecords
        val prefs = values[1] as SafetyPrefs
        val fallOverlay = values[2] as ActiveFallAlertState?
        val shakeOverlay = values[3] as ActiveShakeAlertState?
        val nowMs = values[4] as Long
        val locSnap = values[5] as LocationUiSnapshot

        val todayIso = currentDateIso.value
        val primaryDoctor = records.doctors.firstOrNull { it.isPrimary }

        val todayWaterTotal = records.todayWater.sumOf { it.amountMl }
        val suggestedGoal = SafetyHealthRepository.suggestedWaterGoalMl(prefs.profile)
        val effectiveGoal = prefs.waterGoalOverrideMl ?: suggestedGoal

        val waterByDate = records.weeklyWater.groupBy { it.dateIso }.mapValues { (_, list) ->
            list.sumOf { it.amountMl }
        }
        val weeklyWaterDays = lastSevenDates.map { date ->
            WaterDaySummary(dateIso = date, totalMl = waterByDate[date] ?: 0)
        }

        val medsById = records.medications.associateBy { it.id }
        val logsByMedId = records.weeklyDoseLogs.groupBy { it.medicationId }
        val medsWithStats = records.medications.map { med ->
            val medLogs = logsByMedId[med.id].orEmpty()
            val stats = SafetyHealthRepository.computeWeeklyStats(medLogs)
            val todayMedLogs = medLogs.filter { it.dateIso == todayIso }.sortedBy { it.slotIndex }
            MedicationWithStats(
                medication = med,
                stats = stats,
                todayLogs = todayMedLogs
            )
        }

        val dueDoses = records.weeklyDoseLogs
            .filter { it.dateIso == todayIso }
            .mapNotNull { log ->
                val med = medsById[log.medicationId] ?: return@mapNotNull null
                val status = runCatching { MedicationDoseStatus.valueOf(log.status) }
                    .getOrDefault(MedicationDoseStatus.PENDING)
                val referenceMs = log.snoozedUntilMs ?: log.scheduledTimestampMs
                val isActionable = (status == MedicationDoseStatus.PENDING && nowMs >= referenceMs) ||
                    status == MedicationDoseStatus.SNOOZED
                if (isActionable) {
                    DueMedicationDoseItem(log = log, medication = med, status = status)
                } else {
                    null
                }
            }
            .sortedBy { it.log.snoozedUntilMs ?: it.log.scheduledTimestampMs }

        SafetyHealthUiState(
            doctors = records.doctors,
            primaryDoctor = primaryDoctor,
            fallAlerts = records.fallAlerts,
            todayWaterEntries = records.todayWater,
            todayWaterMl = todayWaterTotal,
            suggestedWaterGoalMl = suggestedGoal,
            waterGoalOverrideMl = prefs.waterGoalOverrideMl,
            effectiveWaterGoalMl = effectiveGoal,
            weeklyWaterDays = weeklyWaterDays,
            medicationsWithStats = medsWithStats,
            dueMedicationDoses = dueDoses,
            fallDetectionEnabled = prefs.fallEnabled,
            fallSensitivity = prefs.fallSensitivity,
            shakeToCallEnabled = prefs.shakeEnabled,
            waterRemindersEnabled = prefs.waterRemindersEnabled,
            medicationNotificationsEnabled = prefs.medNotificationsEnabled,
            hasAccelerometer = motionMonitor.hasAccelerometer(),
            userProfile = prefs.profile,
            activeFallAlert = fallOverlay,
            activeShakeAlert = shakeOverlay,
            locationStatus = locSnap.status,
            hasLocationPermission = locSnap.hasAnyPermission,
            hasFineLocationPermission = locSnap.hasFinePermission,
            locationRationaleShown = prefs.locationRationaleShown,
            locationTestState = locSnap.testState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SafetyHealthUiState(
            hasAccelerometer = motionMonitor.hasAccelerometer(),
            locationStatus = locationProvider.getLocationStatus(),
            hasLocationPermission = locationProvider.hasAnyLocationPermission(),
            hasFineLocationPermission = locationProvider.hasFineLocationPermission()
        )
    )

    init {
        viewModelScope.launch {
            repository.syncTodayDoseScheduleAndMissed(currentDateIso.value)
        }

        // Sync motion monitor whenever toggles or sensitivity change
        viewModelScope.launch {
            combine(
                settingsStore.fallDetectionEnabled,
                settingsStore.fallSensitivity,
                settingsStore.shakeToCallEnabled
            ) { fallEnabled, sensitivity, shakeEnabled ->
                Triple(fallEnabled, sensitivity, shakeEnabled)
            }.collect { (fallEnabled, sensitivity, shakeEnabled) ->
                motionMonitor.updateConfig(
                    fallDetectionEnabled = fallEnabled,
                    sensitivity = sensitivity,
                    shakeToCallEnabled = shakeEnabled
                )
            }
        }

        // Listen for Fall & Shake triggers
        viewModelScope.launch {
            motionMonitor.events.collect { event ->
                when (event) {
                    is MotionTriggerEvent.FallDetected -> startFallAlertCountdown(event)
                    is MotionTriggerEvent.ShakeDetected -> startShakeCountdown()
                }
            }
        }

        // Periodic 30-second dose expiration evaluator & midnight rollover guard
        viewModelScope.launch {
            while (isActive) {
                delay(30_000L)
                val today = DateUtils.todayIso()
                if (currentDateIso.value != today) {
                    currentDateIso.value = today
                }
                val now = System.currentTimeMillis()
                currentMinuteTick.value = now
                repository.syncTodayDoseScheduleAndMissed(today, now)
            }
        }
    }

    fun refreshOnResume() {
        val today = DateUtils.todayIso()
        if (currentDateIso.value != today) {
            currentDateIso.value = today
        }
        val now = System.currentTimeMillis()
        currentMinuteTick.value = now
        refreshLocationStatus()
        viewModelScope.launch {
            repository.syncTodayDoseScheduleAndMissed(today, now)
            val s = uiState.value
            motionMonitor.updateConfig(s.fallDetectionEnabled, s.fallSensitivity, s.shakeToCallEnabled)
        }
    }

    fun refreshLocationStatus() {
        locationStatusState.value = locationProvider.getLocationStatus()
        hasLocationPermissionState.value = locationProvider.hasAnyLocationPermission()
        hasFineLocationPermissionState.value = locationProvider.hasFineLocationPermission()
    }

    fun markLocationRationaleShown() {
        viewModelScope.launch {
            settingsStore.setLocationRationaleShown(true)
        }
    }

    fun testLocation() {
        locationTestJob?.cancel()
        locationTestState.value = LocationTestUiState.Loading
        locationTestJob = viewModelScope.launch {
            refreshLocationStatus()
            val result = locationProvider.resolveEmergencyLocation()
            locationTestState.value = LocationTestUiState.Completed(result)
        }
    }

    fun clearLocationData() {
        locationTestJob?.cancel()
        locationTestState.value = LocationTestUiState.Idle
        viewModelScope.launch {
            repository.clearLocationData()
        }
    }

    // ---- Doctors ----
    fun saveDoctor(doctor: DoctorEntity) {
        viewModelScope.launch {
            repository.saveDoctor(doctor)
        }
    }

    fun setPrimaryDoctor(id: Long) {
        viewModelScope.launch {
            repository.setPrimaryDoctor(id)
        }
    }

    fun deleteDoctor(id: Long) {
        viewModelScope.launch {
            repository.deleteDoctor(id)
        }
    }

    // ---- Water Intake ----
    fun addWaterIntake(amountMl: Int) {
        viewModelScope.launch {
            repository.addWaterIntake(amountMl, currentDateIso.value)
        }
    }

    fun undoLastWaterEntry() {
        viewModelScope.launch {
            repository.undoLastWaterEntry(currentDateIso.value)
        }
    }

    fun deleteWaterEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteWaterEntry(id)
        }
    }

    fun setWaterGoalOverride(goalMl: Int?) {
        viewModelScope.launch {
            repository.setWaterGoalOverrideMl(goalMl)
        }
    }

    // ---- Medications ----
    fun saveMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            repository.saveMedication(medication, currentDateIso.value)
            currentMinuteTick.value = System.currentTimeMillis()
            rescheduleMedicationAlarm()
        }
    }

    fun deleteMedication(id: Long) {
        viewModelScope.launch {
            repository.deleteMedication(id)
            rescheduleMedicationAlarm()
        }
    }

    fun markDoseTaken(log: MedicationDoseLogEntity) {
        viewModelScope.launch {
            repository.markDoseTaken(log)
            currentMinuteTick.value = System.currentTimeMillis()
            rescheduleMedicationAlarm()
        }
    }

    fun snoozeDoseTenMinutes(log: MedicationDoseLogEntity) {
        viewModelScope.launch {
            repository.snoozeDoseTenMinutes(log)
            currentMinuteTick.value = System.currentTimeMillis()
            rescheduleMedicationAlarm()
        }
    }

    fun skipDose(log: MedicationDoseLogEntity) {
        viewModelScope.launch {
            repository.skipDose(log)
            currentMinuteTick.value = System.currentTimeMillis()
            rescheduleMedicationAlarm()
        }
    }

    // ---- Settings Toggles ----
    fun setFallDetectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setFallDetectionEnabled(enabled)
        }
    }

    fun setFallSensitivity(sensitivity: FallSensitivity) {
        viewModelScope.launch {
            repository.setFallSensitivity(sensitivity)
        }
    }

    fun setShakeToCallEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setShakeToCallEnabled(enabled)
        }
    }

    fun setWaterRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setWaterRemindersEnabled(enabled)
            SafetyReminderScheduler.scheduleNextWaterReminder(getApplication(), enabled)
        }
    }

    fun setMedicationNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setMedicationNotificationsEnabled(enabled)
            rescheduleMedicationAlarm(enabledOverride = enabled)
        }
    }

    // ---- Fall, SOS & Shake Alert Overlays + Post-Countdown Location ----
    private fun startFallAlertCountdown(event: MotionTriggerEvent.FallDetected) {
        if (activeFallAlert.value != null || activeShakeAlert.value != null) return
        fallCountdownJob?.cancel()
        motionMonitor.setAlertOverlayActive(true)

        fallCountdownJob = viewModelScope.launch {
            val alertId = repository.logFallAlert(
                sensitivity = event.sensitivity,
                peakG = event.peakG,
                outcome = FallAlertOutcome.NO_RESPONSE,
                alertType = EmergencyAlertType.FALL,
                timestampMs = event.timestampMs,
                dateIso = currentDateIso.value
            )
            var remaining = 30
            while (remaining > 0) {
                activeFallAlert.value = ActiveFallAlertState(
                    alertId = alertId,
                    alertType = EmergencyAlertType.FALL,
                    peakG = event.peakG,
                    sensitivity = event.sensitivity,
                    alertTimestampMs = event.timestampMs,
                    totalSeconds = 30,
                    secondsRemaining = remaining,
                    timedOutNoResponse = false
                )
                delay(1_000L)
                remaining--
            }
            resolvePostCountdownLocation(
                alertId = alertId,
                alertType = EmergencyAlertType.FALL,
                peakG = event.peakG,
                sensitivity = event.sensitivity,
                alertTimestampMs = event.timestampMs,
                totalSeconds = 30
            )
        }
    }

    fun startSosAlertCountdown() {
        if (activeFallAlert.value != null || activeShakeAlert.value != null) return
        fallCountdownJob?.cancel()
        motionMonitor.setAlertOverlayActive(true)
        val nowMs = System.currentTimeMillis()
        val sensitivity = uiState.value.fallSensitivity

        fallCountdownJob = viewModelScope.launch {
            val alertId = repository.logFallAlert(
                sensitivity = sensitivity,
                peakG = 0f,
                outcome = FallAlertOutcome.NO_RESPONSE,
                alertType = EmergencyAlertType.SOS,
                timestampMs = nowMs,
                dateIso = currentDateIso.value
            )
            var remaining = 5
            while (remaining > 0) {
                activeFallAlert.value = ActiveFallAlertState(
                    alertId = alertId,
                    alertType = EmergencyAlertType.SOS,
                    peakG = 0f,
                    sensitivity = sensitivity,
                    alertTimestampMs = nowMs,
                    totalSeconds = 5,
                    secondsRemaining = remaining,
                    timedOutNoResponse = false
                )
                delay(1_000L)
                remaining--
            }
            resolvePostCountdownLocation(
                alertId = alertId,
                alertType = EmergencyAlertType.SOS,
                peakG = 0f,
                sensitivity = sensitivity,
                alertTimestampMs = nowMs,
                totalSeconds = 5
            )
        }
    }

    private suspend fun resolvePostCountdownLocation(
        alertId: Long,
        alertType: EmergencyAlertType,
        peakG: Float,
        sensitivity: FallSensitivity,
        alertTimestampMs: Long,
        totalSeconds: Int
    ) {
        activeFallAlert.value = ActiveFallAlertState(
            alertId = alertId,
            alertType = alertType,
            peakG = peakG,
            sensitivity = sensitivity,
            alertTimestampMs = alertTimestampMs,
            totalSeconds = totalSeconds,
            secondsRemaining = 0,
            timedOutNoResponse = true,
            isResolvingLocation = true,
            locationResult = null
        )

        val resolved = locationProvider.resolveEmergencyLocation()
        repository.updateFallAlertLocation(
            id = alertId,
            latitude = resolved.latitude,
            longitude = resolved.longitude,
            accuracyMeters = resolved.accuracyMeters,
            locationSource = resolved.source,
            locationTimestampMs = if (resolved.isAvailable) resolved.timestampMs else null,
            locationLink = resolved.mapsLink
        )

        val current = activeFallAlert.value
        if (current != null && current.alertId == alertId && current.timedOutNoResponse) {
            activeFallAlert.value = current.copy(
                isResolvingLocation = false,
                locationResult = resolved
            )
        }
    }

    fun respondFallImOk() {
        val current = activeFallAlert.value ?: return
        fallCountdownJob?.cancel()
        viewModelScope.launch {
            repository.updateFallAlertOutcome(current.alertId, FallAlertOutcome.CANCELLED)
            activeFallAlert.value = null
            motionMonitor.setAlertOverlayActive(false)
        }
    }

    fun respondFallFalseAlarm() {
        val current = activeFallAlert.value ?: return
        fallCountdownJob?.cancel()
        viewModelScope.launch {
            repository.updateFallAlertOutcome(current.alertId, FallAlertOutcome.FALSE_ALARM)
            activeFallAlert.value = null
            motionMonitor.setAlertOverlayActive(false)
        }
    }

    fun dismissFallTimeoutOverlay() {
        fallCountdownJob?.cancel()
        activeFallAlert.value = null
        motionMonitor.setAlertOverlayActive(false)
    }

    fun updateFallAlertHistoryOutcome(id: Long, outcome: FallAlertOutcome) {
        viewModelScope.launch {
            repository.updateFallAlertOutcome(id, outcome)
        }
    }

    fun deleteFallAlert(id: Long) {
        viewModelScope.launch {
            repository.deleteFallAlert(id)
        }
    }

    private fun startShakeCountdown() {
        if (activeShakeAlert.value != null || activeFallAlert.value != null) return
        shakeCountdownJob?.cancel()
        motionMonitor.setAlertOverlayActive(true)
        val nowMs = System.currentTimeMillis()
        val sensitivity = uiState.value.fallSensitivity

        shakeCountdownJob = viewModelScope.launch {
            var remaining = 5
            while (remaining > 0) {
                activeShakeAlert.value = ActiveShakeAlertState(secondsRemaining = remaining)
                delay(1_000L)
                remaining--
            }
            activeShakeAlert.value = null
            val alertId = repository.logFallAlert(
                sensitivity = sensitivity,
                peakG = 2.2f,
                outcome = FallAlertOutcome.NO_RESPONSE,
                alertType = EmergencyAlertType.SHAKE,
                timestampMs = nowMs,
                dateIso = currentDateIso.value
            )
            val phone = uiState.value.userProfile.emergencyContact.trim()
            if (phone.isNotBlank()) {
                _events.tryEmit(SafetyNavigationEvent.OpenEmergencyDialer(phone))
            }
            resolvePostCountdownLocation(
                alertId = alertId,
                alertType = EmergencyAlertType.SHAKE,
                peakG = 2.2f,
                sensitivity = sensitivity,
                alertTimestampMs = nowMs,
                totalSeconds = 5
            )
        }
    }

    fun cancelShakeCountdown() {
        shakeCountdownJob?.cancel()
        activeShakeAlert.value = null
        motionMonitor.setAlertOverlayActive(false)
    }

    private fun rescheduleMedicationAlarm(enabledOverride: Boolean? = null) {
        val state = uiState.value
        val enabled = enabledOverride ?: state.medicationNotificationsEnabled
        val pendingLogs = state.medicationsWithStats.flatMap { it.todayLogs }.filter {
            it.status == MedicationDoseStatus.PENDING.name || it.status == MedicationDoseStatus.SNOOZED.name
        }
        SafetyReminderScheduler.scheduleNextMedicationCheck(
            context = getApplication(),
            enabled = enabled,
            pendingOrSnoozedLogs = pendingLogs
        )
    }

    companion object {
        fun buildDialIntent(phoneNumber: String): Intent {
            return Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${Uri.encode(phoneNumber.trim())}")
            }
        }

        fun buildEmergencySmsIntent(phoneNumber: String, messageBody: String): Intent {
            val uri = if (phoneNumber.isNotBlank()) {
                Uri.parse("smsto:${Uri.encode(phoneNumber.trim())}")
            } else {
                Uri.parse("smsto:")
            }
            return Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", messageBody)
            }
        }

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ParvHealthApplication
                SafetyHealthViewModel(
                    application = app,
                    repository = app.safetyHealthRepository,
                    settingsStore = app.settingsStore,
                    motionMonitor = app.safetyMotionMonitor,
                    locationProvider = app.emergencyLocationProvider
                )
            }
        }
    }
}
