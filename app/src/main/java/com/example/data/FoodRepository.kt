package com.example.data

import kotlinx.coroutines.flow.Flow

class FoodRepository(
    private val dao: FoodDao,
    private val settingsStore: SettingsStore
) {
    fun observeEntriesForDate(date: String): Flow<List<FoodEntryEntity>> = dao.observeEntriesForDate(date)

    fun observeEntriesForDates(dates: List<String>): Flow<List<FoodEntryEntity>> =
        dao.observeEntriesForDates(dates)

    val activityLevel: Flow<ActivityLevel> = settingsStore.activityLevel
    val calorieTargetOverride: Flow<Int?> = settingsStore.calorieTargetOverride
    val dietType: Flow<DietType> = settingsStore.dietType
    val proteinGoalG: Flow<Int> = settingsStore.proteinGoalG

    suspend fun saveEntry(entry: FoodEntryEntity) {
        if (entry.id == 0L) {
            dao.insertEntry(entry)
        } else {
            dao.updateEntry(entry)
        }
    }

    suspend fun deleteEntry(id: Long) {
        dao.deleteEntryById(id)
    }

    suspend fun setActivityLevel(level: ActivityLevel) {
        settingsStore.setActivityLevel(level)
    }

    suspend fun setCalorieTargetOverride(target: Int?) {
        settingsStore.setCalorieTargetOverride(target)
    }

    suspend fun setDietType(type: DietType, updateDefaultProtein: Boolean = true) {
        settingsStore.setDietType(type, updateDefaultProtein)
    }

    suspend fun setProteinGoalG(grams: Int) {
        settingsStore.setProteinGoalG(grams)
    }

    fun observeCustomMeals(): Flow<List<CustomMealEntity>> = dao.observeCustomMeals()

    suspend fun saveCustomMeal(meal: CustomMealEntity) {
        dao.insertCustomMeal(meal)
    }

    suspend fun deleteCustomMeal(id: Long) {
        dao.deleteCustomMealById(id)
    }

    suspend fun clearAllFoodData() {
        dao.clearAllEntries()
        dao.clearAllCustomMeals()
        settingsStore.resetFoodPreferences()
    }
}
