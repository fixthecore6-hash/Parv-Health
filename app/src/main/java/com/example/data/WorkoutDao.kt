package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM workout_sets WHERE date = :date")
    fun observeSetsForDate(date: String): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM workout_days WHERE date IN (:dates)")
    fun observeDaysForDates(dates: List<String>): Flow<List<WorkoutDayEntity>>

    @Query("SELECT * FROM workout_days WHERE isDone = 1 OR completedSets > 0 ORDER BY date DESC")
    fun observeWorkoutHistory(): Flow<List<WorkoutDayEntity>>

    @Query("SELECT * FROM workout_days ORDER BY date DESC")
    suspend fun getAllDays(): List<WorkoutDayEntity>

    @Query("SELECT * FROM workout_sets ORDER BY date DESC, exerciseId ASC, setIndex ASC")
    suspend fun getAllSets(): List<WorkoutSetEntity>

    @Query("SELECT * FROM workout_days WHERE date = :date LIMIT 1")
    suspend fun getDay(date: String): WorkoutDayEntity?

    @Query("SELECT * FROM workout_sets WHERE date = :date")
    suspend fun getSetsForDate(date: String): List<WorkoutSetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSet(setEntity: WorkoutSetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDay(dayEntity: WorkoutDayEntity)

    @Query("DELETE FROM workout_sets")
    suspend fun clearAllSets()

    @Query("DELETE FROM workout_days")
    suspend fun clearAllDays()

    // Custom exercises
    @Query("SELECT * FROM custom_exercises ORDER BY createdAt DESC")
    fun observeCustomExercises(): Flow<List<CustomExerciseEntity>>

    @Query("SELECT * FROM custom_exercises ORDER BY createdAt DESC")
    suspend fun getAllCustomExercises(): List<CustomExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomExercise(exercise: CustomExerciseEntity)

    @Query("DELETE FROM custom_exercises WHERE id = :id")
    suspend fun deleteCustomExerciseById(id: String)

    @Query("DELETE FROM planned_day_exercises WHERE exerciseId = :exerciseId")
    suspend fun removeExerciseFromAllDays(exerciseId: String)

    @Query("DELETE FROM custom_exercises")
    suspend fun clearAllCustomExercises()

    // Planned day exercises
    @Query("SELECT * FROM planned_day_exercises ORDER BY dayOfWeekIndex ASC, sortOrder ASC, addedAt ASC")
    fun observeAllPlannedExercises(): Flow<List<PlannedDayExerciseEntity>>

    @Query("SELECT * FROM planned_day_exercises WHERE dayOfWeekIndex = :dayIndex ORDER BY sortOrder ASC, addedAt ASC")
    suspend fun getPlannedExercisesForDay(dayIndex: Int): List<PlannedDayExerciseEntity>

    @Query("SELECT * FROM planned_day_exercises ORDER BY dayOfWeekIndex ASC, sortOrder ASC, addedAt ASC")
    suspend fun getAllPlannedExercises(): List<PlannedDayExerciseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedExercise(item: PlannedDayExerciseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedExercises(items: List<PlannedDayExerciseEntity>)

    @Query("DELETE FROM planned_day_exercises WHERE id = :id")
    suspend fun deletePlannedExerciseById(id: Long)

    @Query("DELETE FROM planned_day_exercises")
    suspend fun clearAllPlannedExercises()
}
