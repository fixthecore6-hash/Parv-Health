package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PedometerDao {

    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    fun observeDay(date: String): Flow<DailyStepsEntity?>

    @Query("SELECT * FROM daily_steps ORDER BY date DESC")
    fun observeAllDays(): Flow<List<DailyStepsEntity>>

    @Query("SELECT * FROM daily_steps WHERE date = :date LIMIT 1")
    suspend fun getDay(date: String): DailyStepsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDay(day: DailyStepsEntity)

    @Query("SELECT * FROM sensor_anchor WHERE id = 1 LIMIT 1")
    suspend fun getAnchor(): SensorAnchorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAnchor(anchor: SensorAnchorEntity)

    @Query("DELETE FROM daily_steps")
    suspend fun clearAllDays()

    @Query("DELETE FROM sensor_anchor")
    suspend fun clearAnchor()
}
