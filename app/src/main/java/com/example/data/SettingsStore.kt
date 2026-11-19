package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

enum class ThemeMode {
    SYSTEM,
    BRIGHT,
    DARK
}

enum class WeightUnit {
    KG,
    LB
}

enum class HeightUnit {
    CM,
    FT
}

enum class ProfileSex {
    UNSPECIFIED,
    FEMALE,
    MALE
}

enum class BmiCategory {
    UNDERWEIGHT,
    HEALTHY,
    OVERWEIGHT,
    OBESE
}

enum class ActivityLevel(val multiplier: Float, val label: String) {
    SEDENTARY(1.2f, "Sedentary"),
    LIGHT(1.375f, "Light"),
    MODERATE(1.55f, "Moderate"),
    ACTIVE(1.725f, "Active")
}

enum class DietType(val label: String, val defaultProteinG: Int) {
    BALANCED("Balanced", 75),
    HIGH_PROTEIN("High Protein", 120),
    VEGETARIAN("Vegetarian", 70),
    CUSTOM("Custom", 80)
}

data class UserProfile(
    val name: String = "",
    val age: Int? = null,
    val sex: ProfileSex = ProfileSex.UNSPECIFIED,
    val weightValue: Float? = null,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val heightValue: Float? = null,
    val heightUnit: HeightUnit = HeightUnit.CM,
    val emergencyContact: String = ""
) {
    val weightKg: Float?
        get() = weightValue?.takeIf { it > 0f }?.let { value ->
            when (weightUnit) {
                WeightUnit.KG -> value
                WeightUnit.LB -> value * 0.45359237f
            }
        }

    val heightCm: Float?
        get() = heightValue?.takeIf { it > 0f }?.let { value ->
            when (heightUnit) {
                HeightUnit.CM -> value
                HeightUnit.FT -> value * 30.48f
            }
        }

    val heightMeters: Float?
        get() = heightCm?.let { it / 100f }

    val hasCalorieFormulaData: Boolean
        get() = (age != null && age > 0) && (weightKg != null) && (heightCm != null)

    val bmi: Float?
        get() {
            val kg = weightKg ?: return null
            val m = heightMeters ?: return null
            if (kg <= 0f || m <= 0f) return null
            return kg / (m * m)
        }

    val bmiCategory: BmiCategory?
        get() = bmi?.let { value ->
            when {
                value < 18.5f -> BmiCategory.UNDERWEIGHT
                value < 25.0f -> BmiCategory.HEALTHY
                value < 30.0f -> BmiCategory.OVERWEIGHT
                else -> BmiCategory.OBESE
            }
        }
}

private val Context.parvHealthDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "parv_health_prefs"
)

class SettingsStore(private val context: Context) {

    private object Keys {
        val DAILY_GOAL = intPreferencesKey("daily_goal")
        val STRIDE_LENGTH_CM = intPreferencesKey("stride_length_cm")
        val TRACKING_ENABLED = booleanPreferencesKey("tracking_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")

        val PROFILE_NAME = stringPreferencesKey("profile_name")
        val PROFILE_AGE = intPreferencesKey("profile_age")
        val PROFILE_SEX = stringPreferencesKey("profile_sex")
        val PROFILE_WEIGHT_VALUE = floatPreferencesKey("profile_weight_value")
        val PROFILE_WEIGHT_UNIT = stringPreferencesKey("profile_weight_unit")
        val PROFILE_HEIGHT_VALUE = floatPreferencesKey("profile_height_value")
        val PROFILE_HEIGHT_UNIT = stringPreferencesKey("profile_height_unit")
        val PROFILE_EMERGENCY_CONTACT = stringPreferencesKey("profile_emergency_contact")

        val FOOD_ACTIVITY_LEVEL = stringPreferencesKey("food_activity_level")
        val FOOD_CALORIE_OVERRIDE = intPreferencesKey("food_calorie_override")
        val FOOD_DIET_TYPE = stringPreferencesKey("food_diet_type")
        val FOOD_PROTEIN_GOAL_G = intPreferencesKey("food_protein_goal_g")

        val WORKOUT_PLAN_SEEDED = booleanPreferencesKey("workout_plan_seeded")
        val REMINDERS_SEEDED = booleanPreferencesKey("reminders_seeded")

        val FALL_DETECTION_ENABLED = booleanPreferencesKey("fall_detection_enabled")
        val FALL_SENSITIVITY = stringPreferencesKey("fall_sensitivity")
        val SHAKE_TO_CALL_ENABLED = booleanPreferencesKey("shake_to_call_enabled")
        val WATER_REMINDERS_ENABLED = booleanPreferencesKey("water_reminders_enabled")
        val MEDICATION_NOTIFICATIONS_ENABLED = booleanPreferencesKey("medication_notifications_enabled")
        val WATER_GOAL_OVERRIDE_ML = intPreferencesKey("water_goal_override_ml")
        val LOCATION_RATIONALE_SHOWN = booleanPreferencesKey("location_rationale_shown")
    }

    val dailyGoal: Flow<Int> = context.parvHealthDataStore.data.map { prefs ->
        prefs[Keys.DAILY_GOAL] ?: DEFAULT_GOAL
    }

    val strideLengthCm: Flow<Int> = context.parvHealthDataStore.data.map { prefs ->
        prefs[Keys.STRIDE_LENGTH_CM] ?: DEFAULT_STRIDE_CM
    }

    val isTrackingEnabled: Flow<Boolean> = context.parvHealthDataStore.data.map { prefs ->
        prefs[Keys.TRACKING_ENABLED] ?: true
    }

    val themeMode: Flow<ThemeMode> = context.parvHealthDataStore.data.map { prefs ->
        val raw = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.SYSTEM)
    }

    val userProfile: Flow<UserProfile> = context.parvHealthDataStore.data.map { prefs ->
        val weightUnitRaw = prefs[Keys.PROFILE_WEIGHT_UNIT] ?: WeightUnit.KG.name
        val heightUnitRaw = prefs[Keys.PROFILE_HEIGHT_UNIT] ?: HeightUnit.CM.name
        val sexRaw = prefs[Keys.PROFILE_SEX] ?: ProfileSex.UNSPECIFIED.name
        UserProfile(
            name = prefs[Keys.PROFILE_NAME] ?: "",
            age = prefs[Keys.PROFILE_AGE],
            sex = runCatching { ProfileSex.valueOf(sexRaw) }.getOrDefault(ProfileSex.UNSPECIFIED),
            weightValue = prefs[Keys.PROFILE_WEIGHT_VALUE],
            weightUnit = runCatching { WeightUnit.valueOf(weightUnitRaw) }.getOrDefault(WeightUnit.KG),
            heightValue = prefs[Keys.PROFILE_HEIGHT_VALUE],
            heightUnit = runCatching { HeightUnit.valueOf(heightUnitRaw) }.getOrDefault(HeightUnit.CM),
            emergencyContact = prefs[Keys.PROFILE_EMERGENCY_CONTACT] ?: ""
        )
    }

    val activityLevel: Flow<ActivityLevel> = context.parvHealthDataStore.data.map { prefs ->
        val raw = prefs[Keys.FOOD_ACTIVITY_LEVEL] ?: ActivityLevel.MODERATE.name
        runCatching { ActivityLevel.valueOf(raw) }.getOrDefault(ActivityLevel.MODERATE)
    }

    val calorieTargetOverride: Flow<Int?> = context.parvHealthDataStore.data.map { prefs ->
        prefs[Keys.FOOD_CALORIE_OVERRIDE]
    }

    val dietType: Flow<DietType> = context.parvHealthDataStore.data.map { prefs ->
        val raw = prefs[Keys.FOOD_DIET_TYPE] ?: DietType.BALANCED.name
        runCatching { DietType.valueOf(raw) }.getOrDefault(DietType.BALANCED)
    }

    val proteinGoalG: Flow<Int> = context.parvHealthDataStore.data.map { prefs ->
        prefs[Keys.FOOD_PROTEIN_GOAL_G] ?: DietType.BALANCED.defaultProteinG
    }

    val fallDetectionEnabled: Flow<Boolean> = context.parvHealthDataStore.data.map { prefs ->
        prefs[Keys.FALL_DETECTION_ENABLED] ?: false
    }

    val fallSensitivity: Flow<FallSensitivity> = context.parvHealthDataStore.data.map { prefs ->
        val raw = prefs[Keys.FALL_SENSITIVITY] ?: FallSensitivity.MEDIUM.name
        runCatching { FallSensitivity.valueOf(raw) }.getOrDefault(FallSensitivity.MEDIUM)
    }

    val shakeToCallEnabled: Flow<Boolean> = context.parvHealthDataStore.data.map { prefs ->
        prefs[Keys.SHAKE_TO_CALL_ENABLED] ?: false
    }

    val waterRemindersEnabled: Flow<Boolean> = context.parvHealthDataStore.data.map { prefs ->
        prefs[Keys.WATER_REMINDERS_ENABLED] ?: false
    }

    val medicationNotificationsEnabled: Flow<Boolean> = context.parvHealthDataStore.data.map { prefs ->
        prefs[Keys.MEDICATION_NOTIFICATIONS_ENABLED] ?: false
    }

    val waterGoalOverrideMl: Flow<Int?> = context.parvHealthDataStore.data.map { prefs ->
        prefs[Keys.WATER_GOAL_OVERRIDE_ML]
    }

    val locationRationaleShown: Flow<Boolean> = context.parvHealthDataStore.data.map { prefs ->
        prefs[Keys.LOCATION_RATIONALE_SHOWN] ?: false
    }

    suspend fun getSnapshot(): SettingsSnapshot {
        val prefs = context.parvHealthDataStore.data.first()
        val rawTheme = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        return SettingsSnapshot(
            dailyGoal = prefs[Keys.DAILY_GOAL] ?: DEFAULT_GOAL,
            strideLengthCm = prefs[Keys.STRIDE_LENGTH_CM] ?: DEFAULT_STRIDE_CM,
            isTrackingEnabled = prefs[Keys.TRACKING_ENABLED] ?: true,
            themeMode = runCatching { ThemeMode.valueOf(rawTheme) }.getOrDefault(ThemeMode.SYSTEM)
        )
    }

    suspend fun setDailyGoal(goal: Int) {
        context.parvHealthDataStore.edit { prefs ->
            prefs[Keys.DAILY_GOAL] = goal.coerceIn(1000, 50000)
        }
    }

    suspend fun setStrideLengthCm(cm: Int) {
        context.parvHealthDataStore.edit { prefs ->
            prefs[Keys.STRIDE_LENGTH_CM] = cm.coerceIn(45, 120)
        }
    }

    suspend fun setTrackingEnabled(enabled: Boolean) {
        context.parvHealthDataStore.edit { prefs ->
            prefs[Keys.TRACKING_ENABLED] = enabled
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.parvHealthDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun saveProfile(profile: UserProfile) {
        context.parvHealthDataStore.edit { prefs ->
            val trimmedName = profile.name.trim()
            if (trimmedName.isEmpty()) {
                prefs.remove(Keys.PROFILE_NAME)
            } else {
                prefs[Keys.PROFILE_NAME] = trimmedName
            }

            if (profile.age != null && profile.age > 0) {
                prefs[Keys.PROFILE_AGE] = profile.age
            } else {
                prefs.remove(Keys.PROFILE_AGE)
            }

            prefs[Keys.PROFILE_SEX] = profile.sex.name

            if (profile.weightValue != null && profile.weightValue > 0f) {
                prefs[Keys.PROFILE_WEIGHT_VALUE] = profile.weightValue
            } else {
                prefs.remove(Keys.PROFILE_WEIGHT_VALUE)
            }
            prefs[Keys.PROFILE_WEIGHT_UNIT] = profile.weightUnit.name

            if (profile.heightValue != null && profile.heightValue > 0f) {
                prefs[Keys.PROFILE_HEIGHT_VALUE] = profile.heightValue
            } else {
                prefs.remove(Keys.PROFILE_HEIGHT_VALUE)
            }
            prefs[Keys.PROFILE_HEIGHT_UNIT] = profile.heightUnit.name

            val trimmedPhone = profile.emergencyContact.trim()
            if (trimmedPhone.isEmpty()) {
                prefs.remove(Keys.PROFILE_EMERGENCY_CONTACT)
            } else {
                prefs[Keys.PROFILE_EMERGENCY_CONTACT] = trimmedPhone
            }
        }
    }

    suspend fun clearProfile() {
        context.parvHealthDataStore.edit { prefs ->
            prefs.remove(Keys.PROFILE_NAME)
            prefs.remove(Keys.PROFILE_AGE)
            prefs.remove(Keys.PROFILE_SEX)
            prefs.remove(Keys.PROFILE_WEIGHT_VALUE)
            prefs.remove(Keys.PROFILE_WEIGHT_UNIT)
            prefs.remove(Keys.PROFILE_HEIGHT_VALUE)
            prefs.remove(Keys.PROFILE_HEIGHT_UNIT)
            prefs.remove(Keys.PROFILE_EMERGENCY_CONTACT)
        }
    }

    suspend fun setActivityLevel(level: ActivityLevel) {
        context.parvHealthDataStore.edit { prefs ->
            prefs[Keys.FOOD_ACTIVITY_LEVEL] = level.name
        }
    }

    suspend fun setCalorieTargetOverride(target: Int?) {
        context.parvHealthDataStore.edit { prefs ->
            if (target == null || target <= 0) {
                prefs.remove(Keys.FOOD_CALORIE_OVERRIDE)
            } else {
                prefs[Keys.FOOD_CALORIE_OVERRIDE] = target.coerceIn(800, 8000)
            }
        }
    }

    suspend fun setDietType(type: DietType, updateDefaultProtein: Boolean = false) {
        context.parvHealthDataStore.edit { prefs ->
            prefs[Keys.FOOD_DIET_TYPE] = type.name
            if (updateDefaultProtein && type != DietType.CUSTOM) {
                prefs[Keys.FOOD_PROTEIN_GOAL_G] = type.defaultProteinG
            }
        }
    }

    suspend fun setProteinGoalG(grams: Int) {
        context.parvHealthDataStore.edit { prefs ->
            prefs[Keys.FOOD_PROTEIN_GOAL_G] = grams.coerceIn(20, 400)
        }
    }

    suspend fun resetFoodPreferences() {
        context.parvHealthDataStore.edit { prefs ->
            prefs.remove(Keys.FOOD_ACTIVITY_LEVEL)
            prefs.remove(Keys.FOOD_CALORIE_OVERRIDE)
            prefs.remove(Keys.FOOD_DIET_TYPE)
            prefs.remove(Keys.FOOD_PROTEIN_GOAL_G)
        }
    }

    suspend fun isWorkoutPlanSeeded(): Boolean {
        return context.parvHealthDataStore.data.first()[Keys.WORKOUT_PLAN_SEEDED] == true
    }

    suspend fun setWorkoutPlanSeeded(seeded: Boolean) {
        context.parvHealthDataStore.edit { prefs ->
            prefs[Keys.WORKOUT_PLAN_SEEDED] = seeded
        }
    }

    suspend fun isRemindersSeeded(): Boolean {
        return context.parvHealthDataStore.data.first()[Keys.REMINDERS_SEEDED] == true
    }

    suspend fun setRemindersSeeded(seeded: Boolean) {
        context.parvHealthDataStore.edit { prefs ->
            prefs[Keys.REMINDERS_SEEDED] = seeded
        }
    }

    suspend fun setFallDetectionEnabled(enabled: Boolean) {
        context.parvHealthDataStore.edit { prefs ->
            prefs[Keys.FALL_DETECTION_ENABLED] = enabled
        }
    }

    suspend fun setFallSensitivity(sensitivity: FallSensitivity) {
        context.parvHealthDataStore.edit { prefs ->
            prefs[Keys.FALL_SENSITIVITY] = sensitivity.name
        }
    }

    suspend fun setShakeToCallEnabled(enabled: Boolean) {
        context.parvHealthDataStore.edit { prefs ->
            prefs[Keys.SHAKE_TO_CALL_ENABLED] = enabled
        }
    }

    suspend fun setWaterRemindersEnabled(enabled: Boolean) {
        context.parvHealthDataStore.edit { prefs ->
            prefs[Keys.WATER_REMINDERS_ENABLED] = enabled
        }
    }

    suspend fun setMedicationNotificationsEnabled(enabled: Boolean) {
        context.parvHealthDataStore.edit { prefs ->
            prefs[Keys.MEDICATION_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setWaterGoalOverrideMl(goalMl: Int?) {
        context.parvHealthDataStore.edit { prefs ->
            if (goalMl == null || goalMl <= 0) {
                prefs.remove(Keys.WATER_GOAL_OVERRIDE_ML)
            } else {
                prefs[Keys.WATER_GOAL_OVERRIDE_ML] = goalMl.coerceIn(500, 8000)
            }
        }
    }

    suspend fun setLocationRationaleShown(shown: Boolean) {
        context.parvHealthDataStore.edit { prefs ->
            prefs[Keys.LOCATION_RATIONALE_SHOWN] = shown
        }
    }

    suspend fun clearAllSettings() {
        context.parvHealthDataStore.edit { prefs ->
            val currentTheme = prefs[Keys.THEME_MODE]
            prefs.clear()
            if (currentTheme != null) {
                prefs[Keys.THEME_MODE] = currentTheme
            }
        }
    }

    data class SettingsSnapshot(
        val dailyGoal: Int,
        val strideLengthCm: Int,
        val isTrackingEnabled: Boolean,
        val themeMode: ThemeMode
    )

    companion object {
        const val DEFAULT_GOAL = 8000
        const val DEFAULT_STRIDE_CM = 76
    }
}
