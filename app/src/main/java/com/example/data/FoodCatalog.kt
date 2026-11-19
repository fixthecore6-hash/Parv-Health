package com.example.data

import kotlin.math.roundToInt

data class CommonFoodItem(
    val name: String,
    val caloriesPer100g: Int,
    val proteinPer100g: Float,
    val carbsPer100g: Float,
    val fatPer100g: Float,
    val defaultServingGrams: Float
)

object FoodCatalog {
    val commonFoods: List<CommonFoodItem> = listOf(
        CommonFoodItem("Rice (cooked)", 130, 2.7f, 28.0f, 0.3f, 150f),
        CommonFoodItem("Eggs", 155, 13.0f, 1.1f, 11.0f, 50f),
        CommonFoodItem("Chicken breast", 165, 31.0f, 0.0f, 3.6f, 120f),
        CommonFoodItem("Bread (whole wheat)", 247, 13.0f, 41.0f, 3.4f, 35f),
        CommonFoodItem("Banana", 89, 1.1f, 22.8f, 0.3f, 118f),
        CommonFoodItem("Milk", 61, 3.2f, 4.8f, 3.3f, 240f),
        CommonFoodItem("Oats", 389, 16.9f, 66.3f, 6.9f, 40f),
        CommonFoodItem("Lentils (cooked)", 116, 9.0f, 20.1f, 0.4f, 150f),
        CommonFoodItem("Greek yogurt", 59, 10.0f, 3.6f, 0.4f, 150f),
        CommonFoodItem("Apple", 52, 0.3f, 13.8f, 0.2f, 180f),
        CommonFoodItem("Paneer", 265, 18.3f, 1.2f, 20.8f, 100f)
    )

    fun findMatches(query: String): List<CommonFoodItem> {
        val clean = query.trim().lowercase()
        if (clean.isEmpty()) return commonFoods.take(7)
        return commonFoods.filter { it.name.lowercase().contains(clean) }
    }

    fun findExactMatch(name: String): CommonFoodItem? {
        val clean = name.trim().lowercase()
        if (clean.isEmpty()) return null
        return commonFoods.firstOrNull { item ->
            val itemLower = item.name.lowercase()
            itemLower == clean || itemLower.substringBefore(" (") == clean
        }
    }

    /**
     * Computes suggested daily calorie target using the Mifflin-St Jeor equation
     * multiplied by the selected activity factor. Returns null if profile data is incomplete.
     */
    fun calculateMifflinStJeorTarget(
        profile: UserProfile,
        activityLevel: ActivityLevel
    ): Int? {
        val age = profile.age?.takeIf { it > 0 } ?: return null
        val weightKg = profile.weightKg?.takeIf { it > 0f } ?: return null
        val heightCm = profile.heightCm?.takeIf { it > 0f } ?: return null

        val sexConstant = when (profile.sex) {
            ProfileSex.MALE -> 5f
            ProfileSex.FEMALE -> -161f
            ProfileSex.UNSPECIFIED -> -78f
        }

        val bmr = (10f * weightKg) + (6.25f * heightCm) - (5f * age) + sexConstant
        val tdee = bmr * activityLevel.multiplier
        return tdee.roundToInt().coerceAtLeast(800)
    }
}
