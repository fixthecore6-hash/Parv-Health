package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ReminderCategory {
    WORKOUT,
    WATER
}

@Entity(tableName = "sleep_entries")
data class SleepEntryEntity(
    @PrimaryKey val date: String,
    val hoursSlept: Float,
    val note: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val category: String, // ReminderCategory.name
    val title: String,
    val timeLabel: String,
    val scheduleLabel: String = "Daily",
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
