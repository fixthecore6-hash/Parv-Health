package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sets",
    primaryKeys = ["date", "exerciseId", "setIndex"]
)
data class WorkoutSetEntity(
    val date: String,
    val exerciseId: String,
    val setIndex: Int,
    val completed: Boolean
)

@Entity(
    tableName = "workout_days",
    primaryKeys = ["date"]
)
data class WorkoutDayEntity(
    val date: String,
    val dayOfWeekIndex: Int, // 0 = Monday .. 6 = Sunday
    val isDone: Boolean = false,
    val completedSets: Int = 0,
    val totalSets: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_exercises")
data class CustomExerciseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val muscleGroup: String,
    val sets: Int,
    val repsLabel: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "planned_day_exercises")
data class PlannedDayExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val dayOfWeekIndex: Int, // 0 = Monday .. 6 = Sunday
    val exerciseId: String,
    val sortOrder: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)
