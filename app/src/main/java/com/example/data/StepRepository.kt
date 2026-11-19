package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.roundToLong

class StepRepository(
    private val dao: PedometerDao,
    private val settingsStore: SettingsStore
) {
    private val mutex = Mutex()

    fun observeToday(todayIso: String = DateUtils.todayIso()): Flow<DailyStepsEntity> {
        return dao.observeDay(todayIso).map { entity ->
            entity ?: DailyStepsEntity(
                date = todayIso,
                steps = 0,
                goal = SettingsStore.DEFAULT_GOAL,
                activeSeconds = 0L,
                strideCm = SettingsStore.DEFAULT_STRIDE_CM
            )
        }
    }

    fun observeAllDays(): Flow<List<DailyStepsEntity>> = dao.observeAllDays()

    val dailyGoal: Flow<Int> = settingsStore.dailyGoal
    val strideLengthCm: Flow<Int> = settingsStore.strideLengthCm
    val isTrackingEnabled: Flow<Boolean> = settingsStore.isTrackingEnabled
    val themeMode: Flow<ThemeMode> = settingsStore.themeMode
    val userProfile: Flow<UserProfile> = settingsStore.userProfile

    suspend fun setThemeMode(mode: ThemeMode) {
        settingsStore.setThemeMode(mode)
    }

    suspend fun saveProfile(profile: UserProfile) {
        settingsStore.saveProfile(profile)
    }

    suspend fun clearProfile() {
        settingsStore.clearProfile()
    }

    suspend fun ensureTodayInitialized() = mutex.withLock {
        val today = DateUtils.todayIso()
        val settings = settingsStore.getSnapshot()
        val existing = dao.getDay(today)
        if (existing == null) {
            dao.upsertDay(
                DailyStepsEntity(
                    date = today,
                    steps = 0,
                    goal = settings.dailyGoal,
                    activeSeconds = 0L,
                    strideCm = settings.strideLengthCm
                )
            )
        } else if (existing.goal != settings.dailyGoal || existing.strideCm != settings.strideLengthCm) {
            dao.upsertDay(
                existing.copy(
                    goal = settings.dailyGoal,
                    strideCm = settings.strideLengthCm,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * Records a reading from Sensor.TYPE_STEP_COUNTER (total steps since last device reboot).
     * Computes an exact delta against the stored SensorAnchorEntity and handles device reboots
     * and midnight date rollovers without double counting.
     */
    suspend fun recordHardwareCounterReading(totalSinceBoot: Float) = mutex.withLock {
        if (totalSinceBoot < 0f) return@withLock

        val now = System.currentTimeMillis()
        val today = DateUtils.todayIso()
        val settings = settingsStore.getSnapshot()
        val anchor = dao.getAnchor()

        val deltaSteps: Int = when {
            anchor == null || anchor.lastHardwareCounter < 0f -> {
                // First ever baseline anchor: start counting from this point forward
                0
            }
            totalSinceBoot < anchor.lastHardwareCounter -> {
                // Device rebooted since last reading; totalSinceBoot represents new steps
                totalSinceBoot.toInt()
            }
            else -> {
                (totalSinceBoot - anchor.lastHardwareCounter).toInt()
            }
        }

        val currentDay = dao.getDay(today) ?: DailyStepsEntity(
            date = today,
            steps = 0,
            goal = settings.dailyGoal,
            activeSeconds = 0L,
            strideCm = settings.strideLengthCm,
            updatedAt = now
        )

        if (deltaSteps > 0) {
            // Estimate ~0.55s of active walking time per step (~109 steps/min average pace)
            val addedActiveSeconds = (deltaSteps * 0.55).roundToLong().coerceAtLeast(1L)
            dao.upsertDay(
                currentDay.copy(
                    steps = currentDay.steps + deltaSteps,
                    goal = settings.dailyGoal,
                    strideCm = settings.strideLengthCm,
                    activeSeconds = currentDay.activeSeconds + addedActiveSeconds,
                    updatedAt = now
                )
            )
        } else if (dao.getDay(today) == null) {
            dao.upsertDay(currentDay)
        }

        dao.upsertAnchor(
            SensorAnchorEntity(
                id = 1,
                lastHardwareCounter = totalSinceBoot,
                lastCounterDate = today,
                updatedAt = now
            )
        )
    }

    /**
     * Records individual step events when using TYPE_STEP_DETECTOR or Accelerometer fallback.
     */
    suspend fun recordSingleStep() = mutex.withLock {
        val now = System.currentTimeMillis()
        val today = DateUtils.todayIso()
        val settings = settingsStore.getSnapshot()
        val currentDay = dao.getDay(today) ?: DailyStepsEntity(
            date = today,
            steps = 0,
            goal = settings.dailyGoal,
            activeSeconds = 0L,
            strideCm = settings.strideLengthCm,
            updatedAt = now
        )
        dao.upsertDay(
            currentDay.copy(
                steps = currentDay.steps + 1,
                goal = settings.dailyGoal,
                strideCm = settings.strideLengthCm,
                activeSeconds = currentDay.activeSeconds + 1L,
                updatedAt = now
            )
        )
    }

    suspend fun updateDailyGoal(goal: Int) = mutex.withLock {
        settingsStore.setDailyGoal(goal)
        val today = DateUtils.todayIso()
        val currentDay = dao.getDay(today)
        if (currentDay != null) {
            dao.upsertDay(
                currentDay.copy(
                    goal = goal.coerceIn(1000, 50000),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun updateStrideLengthCm(cm: Int) = mutex.withLock {
        settingsStore.setStrideLengthCm(cm)
        val today = DateUtils.todayIso()
        val currentDay = dao.getDay(today)
        if (currentDay != null) {
            dao.upsertDay(
                currentDay.copy(
                    strideCm = cm.coerceIn(45, 120),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun setTrackingEnabled(enabled: Boolean) {
        settingsStore.setTrackingEnabled(enabled)
    }

    suspend fun clearLocalHistory() = mutex.withLock {
        dao.clearAllDays()
        dao.clearAnchor()
        val today = DateUtils.todayIso()
        val settings = settingsStore.getSnapshot()
        dao.upsertDay(
            DailyStepsEntity(
                date = today,
                steps = 0,
                goal = settings.dailyGoal,
                activeSeconds = 0L,
                strideCm = settings.strideLengthCm
            )
        )
    }
}
