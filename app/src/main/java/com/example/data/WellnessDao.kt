package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WellnessDao {

    @Query("SELECT * FROM sleep_entries WHERE date = :date LIMIT 1")
    fun observeSleepForDate(date: String): Flow<SleepEntryEntity?>

    @Query("SELECT * FROM sleep_entries WHERE date IN (:dates) ORDER BY date ASC")
    fun observeSleepForDates(dates: List<String>): Flow<List<SleepEntryEntity>>

    @Query("SELECT * FROM sleep_entries ORDER BY date DESC")
    suspend fun getAllSleepEntries(): List<SleepEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSleep(entry: SleepEntryEntity)

    @Query("DELETE FROM sleep_entries WHERE date = :date")
    suspend fun deleteSleepForDate(date: String)

    @Query("DELETE FROM sleep_entries")
    suspend fun clearAllSleep()

    @Query("SELECT * FROM reminders ORDER BY createdAt ASC")
    fun observeReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders ORDER BY createdAt ASC")
    suspend fun getAllReminders(): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReminder(reminder: ReminderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderEntity>)

    @Query("UPDATE reminders SET enabled = :enabled WHERE id = :id")
    suspend fun setReminderEnabled(id: Long, enabled: Boolean)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("DELETE FROM reminders")
    suspend fun clearAllReminders()
}
