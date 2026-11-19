package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    @Query("SELECT * FROM food_entries WHERE date = :date ORDER BY createdAt ASC")
    fun observeEntriesForDate(date: String): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries WHERE date IN (:dates) ORDER BY date ASC, createdAt ASC")
    fun observeEntriesForDates(dates: List<String>): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries ORDER BY date DESC, createdAt ASC")
    suspend fun getAllEntries(): List<FoodEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: FoodEntryEntity): Long

    @Update
    suspend fun updateEntry(entry: FoodEntryEntity)

    @Query("DELETE FROM food_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("DELETE FROM food_entries")
    suspend fun clearAllEntries()

    @Query("SELECT * FROM custom_meals ORDER BY createdAt DESC")
    fun observeCustomMeals(): Flow<List<CustomMealEntity>>

    @Query("SELECT * FROM custom_meals ORDER BY createdAt DESC")
    suspend fun getAllCustomMeals(): List<CustomMealEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomMeal(meal: CustomMealEntity): Long

    @Query("DELETE FROM custom_meals WHERE id = :id")
    suspend fun deleteCustomMealById(id: Long)

    @Query("DELETE FROM custom_meals")
    suspend fun clearAllCustomMeals()
}
