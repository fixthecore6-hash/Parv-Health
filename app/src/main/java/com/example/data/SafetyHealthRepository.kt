package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Calendar
import kotlin.math.roundToInt

data class MedicationWeeklyStats(
    val takenCount: Int,
    val missedCount: Int,
    val skippedCount: Int,
    val weeklyPercentage: Int
)

class SafetyHealthRepository(
    private val dao: SafetyHealthDao,
    private val settingsStore: SettingsStore
) {
    private val doseSyncMutex = Mutex()

    // ---- Doctors ----
    fun observeDoctors(): Flow<List<DoctorEntity>> = dao.observeDoctors()

    suspend fun saveDoctor(doctor: DoctorEntity) {
        val clean = doctor.copy(
            name = doctor.name.trim(),
            phoneNumber = doctor.phoneNumber.trim(),
            clinicOrHospital = doctor.clinicOrHospital.trim(),
            specialty = doctor.specialty.trim()
        )
        if (clean.isPrimary) {
            dao.clearPrimaryDoctorFlag()
        }
        val id = dao.upsertDoctor(clean)
        // If this is the very first doctor saved, auto-mark as primary unless user unchecked
        val all = dao.getAllDoctors()
        if (all.size == 1 && !all.first().isPrimary && clean.isPrimary) {
            dao.setPrimaryDoctor(id)
        }
    }

    suspend fun setPrimaryDoctor(id: Long) {
        dao.setPrimaryDoctor(id)
    }

    suspend fun deleteDoctor(id: Long) {
        dao.deleteDoctorById(id)
    }

    // ---- Emergency & Fall Alerts ----
    fun observeFallAlerts(): Flow<List<FallAlertEntity>> = dao.observeFallAlerts()

    suspend fun logFallAlert(
        sensitivity: FallSensitivity,
        peakG: Float,
        outcome: FallAlertOutcome,
        alertType: EmergencyAlertType = EmergencyAlertType.FALL,
        timestampMs: Long = System.currentTimeMillis(),
        dateIso: String = DateUtils.todayIso()
    ): Long {
        return dao.insertFallAlert(
            FallAlertEntity(
                timestampMs = timestampMs,
                dateIso = dateIso,
                sensitivity = sensitivity.name,
                peakG = peakG,
                outcome = outcome.name,
                alertType = alertType.name
            )
        )
    }

    suspend fun updateFallAlertOutcome(id: Long, outcome: FallAlertOutcome) {
        dao.updateFallAlertOutcome(id, outcome.name)
    }

    suspend fun updateFallAlertLocation(
        id: Long,
        latitude: Double?,
        longitude: Double?,
        accuracyMeters: Float?,
        locationSource: AlertLocationSource,
        locationTimestampMs: Long?,
        locationLink: String
    ) {
        dao.updateFallAlertLocation(
            id = id,
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = accuracyMeters,
            locationSource = locationSource.name,
            locationTimestampMs = locationTimestampMs,
            locationLink = locationLink
        )
    }

    suspend fun clearLocationData() {
        dao.clearAllAlertLocations()
    }

    suspend fun deleteFallAlert(id: Long) {
        dao.deleteFallAlertById(id)
    }

    // ---- Water Intake ----
    fun observeWaterEntriesForDate(dateIso: String): Flow<List<WaterEntryEntity>> =
        dao.observeWaterEntriesForDate(dateIso)

    fun observeWaterEntriesForDates(dates: List<String>): Flow<List<WaterEntryEntity>> =
        dao.observeWaterEntriesForDates(dates)

    suspend fun addWaterIntake(amountMl: Int, dateIso: String = DateUtils.todayIso()) {
        if (amountMl <= 0) return
        dao.insertWaterEntry(
            WaterEntryEntity(
                dateIso = dateIso,
                amountMl = amountMl.coerceIn(1, 5000),
                timestampMs = System.currentTimeMillis()
            )
        )
    }

    suspend fun undoLastWaterEntry(dateIso: String = DateUtils.todayIso()): Boolean {
        val latest = dao.getLatestWaterEntryForDate(dateIso) ?: return false
        dao.deleteWaterEntryById(latest.id)
        return true
    }

    suspend fun deleteWaterEntry(id: Long) {
        dao.deleteWaterEntryById(id)
    }

    // ---- Medications & Dose Scheduling / Evaluation ----
    fun observeMedications(): Flow<List<MedicationEntity>> = dao.observeMedications()

    fun observeDoseLogsForDates(dates: List<String>): Flow<List<MedicationDoseLogEntity>> =
        dao.observeDoseLogsForDates(dates)

    suspend fun saveMedication(medication: MedicationEntity, todayIso: String = DateUtils.todayIso()) {
        val clean = medication.copy(
            name = medication.name.trim(),
            dose = medication.dose.trim(),
            timesPerDay = medication.timesPerDay.coerceIn(1, 6),
            startDateIso = medication.startDateIso.ifBlank { todayIso },
            endDateIso = medication.endDateIso.trim()
        )
        dao.upsertMedication(clean)
        syncTodayDoseScheduleAndMissed(todayIso)
    }

    suspend fun deleteMedication(id: Long) {
        dao.deleteDoseLogsForMedication(id)
        dao.deleteMedicationById(id)
    }

    suspend fun syncTodayDoseScheduleAndMissed(
        todayIso: String = DateUtils.todayIso(),
        nowMs: Long = System.currentTimeMillis()
    ) = doseSyncMutex.withLock {
        val dayOfWeek = dayOfWeekIndexForIso(todayIso)
        val medications = dao.getAllMedications()
        val existingTodayLogs = dao.getDoseLogsForDate(todayIso)
        val existingKeys = existingTodayLogs.map { it.medicationId to it.slotIndex }.toSet()

        val newLogs = mutableListOf<MedicationDoseLogEntity>()
        medications.forEach { med ->
            if (med.isActiveOn(todayIso, dayOfWeek)) {
                val hours = defaultDoseHoursForCount(med.timesPerDay)
                val medCreatedDayIso = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    .format(java.util.Date(med.createdAt))
                val createdToday = medCreatedDayIso == todayIso
                val hasUpcomingSlotToday = hours.any { h ->
                    timestampForDateAndHour(todayIso, h, 0) + MISSED_GRACE_WINDOW_MS > med.createdAt
                }

                hours.forEachIndexed { slotIndex, hourOfDay ->
                    if ((med.id to slotIndex) !in existingKeys) {
                        val baseScheduledMs = timestampForDateAndHour(todayIso, hourOfDay, 0)
                        val isPreCreationExpired = createdToday && (baseScheduledMs + MISSED_GRACE_WINDOW_MS <= med.createdAt)
                        if (isPreCreationExpired && hasUpcomingSlotToday) {
                            return@forEachIndexed
                        }
                        val effectiveScheduledMs = if (isPreCreationExpired) med.createdAt else baseScheduledMs
                        val label = if (isPreCreationExpired) {
                            formatMillisLabel(med.createdAt)
                        } else {
                            formatHourLabel(hourOfDay, 0)
                        }
                        newLogs.add(
                            MedicationDoseLogEntity(
                                medicationId = med.id,
                                dateIso = todayIso,
                                slotIndex = slotIndex,
                                scheduledTimeLabel = label,
                                scheduledTimestampMs = effectiveScheduledMs,
                                snoozedUntilMs = null,
                                status = MedicationDoseStatus.PENDING.name,
                                actedAtMs = null
                            )
                        )
                    }
                }
            }
        }
        if (newLogs.isNotEmpty()) {
            dao.insertDoseLogs(newLogs)
        }

        evaluateExpiredDoses(nowMs)
    }

    suspend fun evaluateExpiredDoses(nowMs: Long = System.currentTimeMillis()) {
        val allLogs = dao.getAllDoseLogs()
        allLogs.forEach { log ->
            val currentStatus = runCatching { MedicationDoseStatus.valueOf(log.status) }
                .getOrDefault(MedicationDoseStatus.PENDING)
            if (currentStatus == MedicationDoseStatus.PENDING || currentStatus == MedicationDoseStatus.SNOOZED) {
                val referenceMs = log.snoozedUntilMs ?: log.scheduledTimestampMs
                if (nowMs - referenceMs >= MISSED_GRACE_WINDOW_MS) {
                    dao.upsertDoseLog(
                        log.copy(
                            status = MedicationDoseStatus.MISSED.name,
                            actedAtMs = referenceMs + MISSED_GRACE_WINDOW_MS
                        )
                    )
                }
            }
        }
    }

    suspend fun markDoseTaken(log: MedicationDoseLogEntity, nowMs: Long = System.currentTimeMillis()) {
        dao.upsertDoseLog(
            log.copy(
                status = MedicationDoseStatus.TAKEN.name,
                snoozedUntilMs = null,
                actedAtMs = nowMs
            )
        )
    }

    suspend fun snoozeDoseTenMinutes(log: MedicationDoseLogEntity, nowMs: Long = System.currentTimeMillis()) {
        dao.upsertDoseLog(
            log.copy(
                status = MedicationDoseStatus.SNOOZED.name,
                snoozedUntilMs = nowMs + SNOOZE_DURATION_MS,
                actedAtMs = nowMs
            )
        )
    }

    suspend fun skipDose(log: MedicationDoseLogEntity, nowMs: Long = System.currentTimeMillis()) {
        dao.upsertDoseLog(
            log.copy(
                status = MedicationDoseStatus.SKIPPED.name,
                snoozedUntilMs = null,
                actedAtMs = nowMs
            )
        )
    }

    // ---- Settings Shortcuts ----
    suspend fun setFallDetectionEnabled(enabled: Boolean) =
        settingsStore.setFallDetectionEnabled(enabled)

    suspend fun setFallSensitivity(sensitivity: FallSensitivity) =
        settingsStore.setFallSensitivity(sensitivity)

    suspend fun setShakeToCallEnabled(enabled: Boolean) =
        settingsStore.setShakeToCallEnabled(enabled)

    suspend fun setWaterRemindersEnabled(enabled: Boolean) =
        settingsStore.setWaterRemindersEnabled(enabled)

    suspend fun setMedicationNotificationsEnabled(enabled: Boolean) =
        settingsStore.setMedicationNotificationsEnabled(enabled)

    suspend fun setWaterGoalOverrideMl(goalMl: Int?) =
        settingsStore.setWaterGoalOverrideMl(goalMl)

    suspend fun clearAllSafetyHealthData() {
        dao.clearAllDoctors()
        dao.clearAllFallAlerts()
        dao.clearAllWaterEntries()
        dao.clearAllDoseLogs()
        dao.clearAllMedications()
    }

    companion object {
        const val DEFAULT_WATER_GOAL_ML = 2000
        const val MISSED_GRACE_WINDOW_MS = 60L * 60L * 1000L // 60 minutes
        const val SNOOZE_DURATION_MS = 10L * 60L * 1000L // 10 minutes

        fun suggestedWaterGoalMl(profile: UserProfile): Int {
            val weightKg = profile.weightKg
            return if (weightKg != null && weightKg > 0f) {
                (weightKg * 35f).roundToInt().coerceIn(800, 6000)
            } else {
                DEFAULT_WATER_GOAL_ML
            }
        }

        fun defaultDoseHoursForCount(timesPerDay: Int): List<Int> {
            return when (timesPerDay.coerceIn(1, 6)) {
                1 -> listOf(8)
                2 -> listOf(8, 20)
                3 -> listOf(8, 14, 20)
                4 -> listOf(8, 12, 16, 20)
                5 -> listOf(7, 11, 14, 18, 21)
                else -> listOf(7, 10, 13, 16, 19, 22)
            }
        }

        fun formatHourLabel(hour24: Int, minute: Int): String {
            val amPm = if (hour24 < 12) "AM" else "PM"
            val hour12 = when {
                hour24 == 0 -> 12
                hour24 > 12 -> hour24 - 12
                else -> hour24
            }
            return String.format("%d:%02d %s", hour12, minute, amPm)
        }

        fun formatMillisLabel(timeMs: Long): String {
            val cal = Calendar.getInstance().apply { timeInMillis = timeMs }
            return formatHourLabel(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        }

        fun timestampForDateAndHour(dateIso: String, hour24: Int, minute: Int): Long {
            val parts = dateIso.split("-")
            if (parts.size != 3) return System.currentTimeMillis()
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, parts[0].toIntOrNull() ?: 2026)
                set(Calendar.MONTH, ((parts[1].toIntOrNull() ?: 1) - 1).coerceIn(0, 11))
                set(Calendar.DAY_OF_MONTH, parts[2].toIntOrNull() ?: 1)
                set(Calendar.HOUR_OF_DAY, hour24.coerceIn(0, 23))
                set(Calendar.MINUTE, minute.coerceIn(0, 59))
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }

        fun dayOfWeekIndexForIso(dateIso: String): Int {
            val parts = dateIso.split("-")
            if (parts.size != 3) return DateUtils.todayWeekDayIndex()
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, parts[0].toIntOrNull() ?: 2026)
                set(Calendar.MONTH, ((parts[1].toIntOrNull() ?: 1) - 1).coerceIn(0, 11))
                set(Calendar.DAY_OF_MONTH, parts[2].toIntOrNull() ?: 1)
            }
            return when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                else -> 6
            }
        }

        fun computeWeeklyStats(logs: List<MedicationDoseLogEntity>): MedicationWeeklyStats {
            var taken = 0
            var missed = 0
            var skipped = 0
            logs.forEach { log ->
                when (runCatching { MedicationDoseStatus.valueOf(log.status) }.getOrNull()) {
                    MedicationDoseStatus.TAKEN -> taken++
                    MedicationDoseStatus.MISSED -> missed++
                    MedicationDoseStatus.SKIPPED -> skipped++
                    else -> Unit
                }
            }
            val denominator = taken + missed + skipped
            val pct = if (denominator > 0) ((taken * 100f) / denominator).roundToInt() else 0
            return MedicationWeeklyStats(
                takenCount = taken,
                missedCount = missed,
                skippedCount = skipped,
                weeklyPercentage = pct
            )
        }
    }
}
