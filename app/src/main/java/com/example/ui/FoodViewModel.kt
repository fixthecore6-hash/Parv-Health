package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ParvHealthApplication
import com.example.data.ActivityLevel
import com.example.data.CustomMealEntity
import com.example.data.DateUtils
import com.example.data.DietType
import com.example.data.FoodAmountUnit
import com.example.data.FoodCatalog
import com.example.data.FoodEntryEntity
import com.example.data.FoodRepository
import com.example.data.FoodSuggestion
import com.example.data.MealType
import com.example.data.SettingsStore
import com.example.data.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DailyCalorieBar(
    val dateIso: String,
    val calories: Int
)

data class FoodUiState(
    val selectedDateIso: String = DateUtils.todayIso(),
    val entriesForSelectedDate: List<FoodEntryEntity> = emptyList(),
    val customMeals: List<CustomMealEntity> = emptyList(),
    val totalCalories: Int = 0,
    val totalProteinG: Float = 0f,
    val totalCarbsG: Float = 0f,
    val totalFatG: Float = 0f,
    val weeklyCalories: List<DailyCalorieBar> = emptyList(),
    val userProfile: UserProfile = UserProfile(),
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val suggestedCalorieTarget: Int? = null,
    val calorieTargetOverride: Int? = null,
    val effectiveCalorieTarget: Int? = null,
    val dietType: DietType = DietType.BALANCED,
    val proteinGoalG: Int = DietType.BALANCED.defaultProteinG
)

@OptIn(ExperimentalCoroutinesApi::class)
class FoodViewModel(
    application: Application,
    private val foodRepository: FoodRepository,
    settingsStore: SettingsStore
) : AndroidViewModel(application) {

    private val lastSevenDates = DateUtils.lastSevenDaysIso()
    private val selectedDateFlow = MutableStateFlow(DateUtils.todayIso())

    private val selectedDateEntriesFlow = selectedDateFlow.flatMapLatest { date ->
        foodRepository.observeEntriesForDate(date)
    }

    private data class NutritionPrefs(
        val profile: UserProfile,
        val activityLevel: ActivityLevel,
        val calorieOverride: Int?,
        val dietType: DietType,
        val proteinGoalG: Int
    )

    private val prefsFlow = combine(
        settingsStore.userProfile,
        foodRepository.activityLevel,
        foodRepository.calorieTargetOverride,
        foodRepository.dietType,
        foodRepository.proteinGoalG
    ) { profile, activity, override, diet, proteinGoal ->
        NutritionPrefs(profile, activity, override, diet, proteinGoal)
    }

    val uiState: StateFlow<FoodUiState> = combine(
        selectedDateFlow,
        selectedDateEntriesFlow,
        foodRepository.observeEntriesForDates(lastSevenDates),
        foodRepository.observeCustomMeals(),
        prefsFlow
    ) { selectedDate, entries, weekEntries, customMeals, prefs ->
        val totalCals = entries.sumOf { it.calories }
        val totalP = entries.sumOf { it.proteinG.toDouble() }.toFloat()
        val totalC = entries.sumOf { it.carbsG.toDouble() }.toFloat()
        val totalF = entries.sumOf { it.fatG.toDouble() }.toFloat()

        val calsByDate = weekEntries.groupBy { it.date }.mapValues { (_, list) ->
            list.sumOf { it.calories }
        }
        val weeklyBars = lastSevenDates.map { date ->
            DailyCalorieBar(dateIso = date, calories = calsByDate[date] ?: 0)
        }

        val suggested = FoodCatalog.calculateMifflinStJeorTarget(prefs.profile, prefs.activityLevel)
        val effective = prefs.calorieOverride ?: suggested

        FoodUiState(
            selectedDateIso = selectedDate,
            entriesForSelectedDate = entries,
            customMeals = customMeals,
            totalCalories = totalCals,
            totalProteinG = totalP,
            totalCarbsG = totalC,
            totalFatG = totalF,
            weeklyCalories = weeklyBars,
            userProfile = prefs.profile,
            activityLevel = prefs.activityLevel,
            suggestedCalorieTarget = suggested,
            calorieTargetOverride = prefs.calorieOverride,
            effectiveCalorieTarget = effective,
            dietType = prefs.dietType,
            proteinGoalG = prefs.proteinGoalG
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FoodUiState()
    )

    fun selectDate(dateIso: String) {
        selectedDateFlow.value = dateIso
    }

    fun saveEntry(entry: FoodEntryEntity) {
        viewModelScope.launch {
            foodRepository.saveEntry(entry)
        }
    }

    fun logSuggestedMeal(suggestion: FoodSuggestion, dateIso: String = selectedDateFlow.value) {
        viewModelScope.launch {
            foodRepository.saveEntry(
                FoodEntryEntity(
                    date = dateIso,
                    mealType = suggestion.mealType.name,
                    name = suggestion.name,
                    amount = 1f,
                    amountUnit = FoodAmountUnit.SERVINGS.name,
                    calories = suggestion.calories,
                    proteinG = suggestion.proteinG,
                    carbsG = suggestion.carbsG,
                    fatG = suggestion.fatG
                )
            )
        }
    }

    fun saveCustomMeal(meal: CustomMealEntity) {
        viewModelScope.launch {
            foodRepository.saveCustomMeal(meal)
        }
    }

    fun logCustomMeal(meal: CustomMealEntity, dateIso: String = selectedDateFlow.value) {
        viewModelScope.launch {
            foodRepository.saveEntry(
                FoodEntryEntity(
                    date = dateIso,
                    mealType = meal.mealType,
                    name = meal.name,
                    amount = 1f,
                    amountUnit = FoodAmountUnit.SERVINGS.name,
                    calories = meal.calories,
                    proteinG = meal.proteinG,
                    carbsG = meal.carbsG,
                    fatG = meal.fatG
                )
            )
        }
    }

    fun deleteCustomMeal(id: Long) {
        viewModelScope.launch {
            foodRepository.deleteCustomMeal(id)
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            foodRepository.deleteEntry(id)
        }
    }

    fun setActivityLevel(level: ActivityLevel) {
        viewModelScope.launch {
            foodRepository.setActivityLevel(level)
        }
    }

    fun setCalorieOverride(target: Int?) {
        viewModelScope.launch {
            foodRepository.setCalorieTargetOverride(target)
        }
    }

    fun setDietType(type: DietType) {
        viewModelScope.launch {
            foodRepository.setDietType(type, updateDefaultProtein = type != DietType.CUSTOM)
        }
    }

    fun setProteinGoalG(grams: Int) {
        viewModelScope.launch {
            foodRepository.setProteinGoalG(grams)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ParvHealthApplication
                FoodViewModel(
                    application = app,
                    foodRepository = app.foodRepository,
                    settingsStore = app.settingsStore
                )
            }
        }
    }
}
