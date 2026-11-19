package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class WellnessRepository(
    private val dao: WellnessDao,
    private val settingsStore: SettingsStore
) {
    private val mutex = Mutex()

    fun observeSleepForDate(date: String): Flow<SleepEntryEntity?> = dao.observeSleepForDate(date)

    fun observeSleepForDates(dates: List<String>): Flow<List<SleepEntryEntity>> =
        dao.observeSleepForDates(dates)

    fun observeReminders(): Flow<List<ReminderEntity>> = dao.observeReminders()

    suspend fun ensureDefaultRemindersSeeded() = mutex.withLock {
        if (settingsStore.isRemindersSeeded()) return@withLock
        if (dao.getAllReminders().isEmpty()) {
            dao.insertReminders(
                listOf(
                    ReminderEntity(
                        category = ReminderCategory.WORKOUT.name,
                        title = "Daily movement & strength session",
                        timeLabel = "06:00 PM",
                        scheduleLabel = "Mon–Sat",
                        enabled = true
                    ),
                    ReminderEntity(
                        category = ReminderCategory.WATER.name,
                        title = "Drink a glass of water",
                        timeLabel = "10:00 AM",
                        scheduleLabel = "Every 2 hrs",
                        enabled = true
                    ),
                    ReminderEntity(
                        category = ReminderCategory.WATER.name,
                        title = "Afternoon hydration check",
                        timeLabel = "03:00 PM",
                        scheduleLabel = "Daily",
                        enabled = true
                    )
                )
            )
        }
        settingsStore.setRemindersSeeded(true)
    }

    suspend fun saveSleep(date: String, hoursSlept: Float, note: String) {
        dao.upsertSleep(
            SleepEntryEntity(
                date = date,
                hoursSlept = hoursSlept.coerceIn(0f, 24f),
                note = note.trim(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteSleepForDate(date: String) {
        dao.deleteSleepForDate(date)
    }

    suspend fun addReminder(
        category: ReminderCategory,
        title: String,
        timeLabel: String,
        scheduleLabel: String
    ) {
        dao.upsertReminder(
            ReminderEntity(
                category = category.name,
                title = title.trim(),
                timeLabel = timeLabel.trim().ifEmpty { "09:00 AM" },
                scheduleLabel = scheduleLabel.trim().ifEmpty { "Daily" },
                enabled = true,
                createdAt = System.currentTimeMillis()
            )
        )
        settingsStore.setRemindersSeeded(true)
    }

    suspend fun toggleReminder(id: Long, enabled: Boolean) {
        dao.setReminderEnabled(id, enabled)
    }

    suspend fun deleteReminder(id: Long) {
        dao.deleteReminderById(id)
    }

    suspend fun clearAllWellnessData() {
        dao.clearAllSleep()
        dao.clearAllReminders()
        settingsStore.setRemindersSeeded(false)
    }
}
