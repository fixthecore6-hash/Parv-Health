package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.roundToInt

@Entity(tableName = "daily_steps")
data class DailyStepsEntity(
    @PrimaryKey val date: String,
    val steps: Int = 0,
    val goal: Int = 8000,
    val activeSeconds: Long = 0L,
    val strideCm: Int = 76,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val distanceKm: Float
        get() = (steps * (strideCm / 100f)) / 1000f

    val caloriesKcal: Int
        get() = (steps * 0.04f).roundToInt()

    val activeMinutes: Int
        get() = (activeSeconds / 60L).toInt()

    val progressFraction: Float
        get() = if (goal > 0) (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f

    val goalReached: Boolean
        get() = goal > 0 && steps >= goal
}
