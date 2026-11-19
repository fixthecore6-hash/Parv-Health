package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MealType(val displayOrder: Int) {
    BREAKFAST(0),
    LUNCH(1),
    DINNER(2),
    SNACKS(3)
}

enum class FoodAmountUnit {
    GRAMS,
    SERVINGS
}

@Entity(tableName = "food_entries")
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val date: String,
    val mealType: String,
    val name: String,
    val amount: Float,
    val amountUnit: String,
    val calories: Int,
    val proteinG: Float,
    val carbsG: Float,
    val fatG: Float,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_meals")
data class CustomMealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val mealType: String,
    val calories: Int,
    val proteinG: Float,
    val carbsG: Float,
    val fatG: Float,
    val ingredients: String = "",
    val isVegetarian: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
