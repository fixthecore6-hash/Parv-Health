package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ActivityLevel
import com.example.data.CommonFoodItem
import com.example.data.CustomMealEntity
import com.example.data.DateUtils
import com.example.data.DietType
import com.example.data.FoodAmountUnit
import com.example.data.FoodCatalog
import com.example.data.FoodEntryEntity
import com.example.data.FoodSuggestion
import com.example.data.MealType
import com.example.ui.DailyCalorieBar
import com.example.ui.FoodUiState
import com.example.ui.theme.LocalAccentColors
import com.example.ui.theme.LocalIsDarkTheme
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FoodScreen(
    uiState: FoodUiState,
    onSelectDate: (String) -> Unit,
    onSaveEntry: (FoodEntryEntity) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onLogSuggestedMeal: (FoodSuggestion) -> Unit,
    onSaveCustomMeal: (CustomMealEntity) -> Unit,
    onLogCustomMeal: (CustomMealEntity) -> Unit,
    onDeleteCustomMeal: (Long) -> Unit,
    onSetActivityLevel: (ActivityLevel) -> Unit,
    onSetCalorieOverride: (Int?) -> Unit,
    onSetDietType: (DietType) -> Unit,
    onSetProteinGoal: (Int) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current
    val context = LocalContext.current
    val todayIso = DateUtils.todayIso()
    val numberFormatter = NumberFormat.getIntegerInstance()

    var activeEditorMeal by remember { mutableStateOf<MealType?>(null) }
    var editingEntry by remember { mutableStateOf<FoodEntryEntity?>(null) }
    var loggedFeedback by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(loggedFeedback) {
        if (loggedFeedback != null) {
            kotlinx.coroutines.delay(2500)
            loggedFeedback = null
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.food_title),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = DateUtils.formatDisplayDate(
                                uiState.selectedDateIso,
                                stringResource(R.string.label_today),
                                stringResource(R.string.label_yesterday)
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.selectedDateIso != todayIso) {
                            TextButton(
                                onClick = { onSelectDate(todayIso) },
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Text(stringResource(R.string.label_today))
                            }
                        }
                        IconButton(
                            onClick = { onToggleTheme(isDark) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("food_theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                                contentDescription = stringResource(R.string.toggle_theme_desc),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("food_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = stringResource(R.string.settings_content_desc),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            // Daily Totals Summary Card
            item {
                DailyTotalsSummaryCard(
                    uiState = uiState,
                    numberFormatter = numberFormatter
                )
            }

            // Calorie Target Card (Mifflin-St Jeor or Prompt)
            item {
                CalorieGoalCard(
                    uiState = uiState,
                    onSetActivityLevel = onSetActivityLevel,
                    onSetCalorieOverride = onSetCalorieOverride,
                    onOpenProfile = onOpenProfile
                )
            }

            // Diet Preferences Card
            item {
                DietPreferencesCard(
                    dietType = uiState.dietType,
                    proteinGoalG = uiState.proteinGoalG,
                    consumedProteinG = uiState.totalProteinG,
                    onSetDietType = onSetDietType,
                    onSetProteinGoal = onSetProteinGoal
                )
            }

            // 7-Day Calorie Bar Chart
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shadowElevation = if (isDark) 2.dp else 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.food_weekly_calories_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        WeeklyCalorieChart(
                            bars = uiState.weeklyCalories,
                            selectedDateIso = uiState.selectedDateIso,
                            goalCalories = uiState.effectiveCalorieTarget,
                            onSelectDate = onSelectDate
                        )
                    }
                }
            }

            // Meal Sections: Breakfast, Lunch, Dinner, Snacks
            MealType.entries.forEach { meal ->
                item(key = "meal_${meal.name}") {
                    val mealEntries = uiState.entriesForSelectedDate.filter {
                        it.mealType == meal.name
                    }
                    MealSectionCard(
                        mealType = meal,
                        entries = mealEntries,
                        onAddFood = {
                            editingEntry = null
                            activeEditorMeal = meal
                        },
                        onEditEntry = { entry ->
                            editingEntry = entry
                            activeEditorMeal = meal
                        },
                        onDeleteEntry = onDeleteEntry
                    )
                }
            }

            // Logged Meal Feedback Banner
            if (loggedFeedback != null) {
                item(key = "logged_feedback_banner") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("food_logged_feedback_banner"),
                        shape = RoundedCornerShape(14.dp),
                        color = accents.greenTint,
                        border = BorderStroke(1.dp, accents.greenPrimary)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = accents.greenPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = loggedFeedback.orEmpty(),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Food Suggestions & Custom Meals Section
            item(key = "food_suggestions_section") {
                FoodSuggestionsSection(
                    uiState = uiState,
                    onLogSuggestion = { suggestion ->
                        onLogSuggestedMeal(suggestion)
                        val mealName = when (suggestion.mealType) {
                            MealType.BREAKFAST -> context.getString(R.string.food_meal_breakfast)
                            MealType.LUNCH -> context.getString(R.string.food_meal_lunch)
                            MealType.DINNER -> context.getString(R.string.food_meal_dinner)
                            MealType.SNACKS -> context.getString(R.string.food_meal_snacks)
                        }
                        loggedFeedback = context.getString(
                            R.string.food_meal_logged_feedback,
                            suggestion.name,
                            mealName
                        )
                    },
                    onSaveCustomMeal = onSaveCustomMeal,
                    onLogCustomMeal = { meal ->
                        onLogCustomMeal(meal)
                        val mealEnum = runCatching { MealType.valueOf(meal.mealType) }
                            .getOrDefault(MealType.LUNCH)
                        val mealName = when (mealEnum) {
                            MealType.BREAKFAST -> context.getString(R.string.food_meal_breakfast)
                            MealType.LUNCH -> context.getString(R.string.food_meal_lunch)
                            MealType.DINNER -> context.getString(R.string.food_meal_dinner)
                            MealType.SNACKS -> context.getString(R.string.food_meal_snacks)
                        }
                        loggedFeedback = context.getString(
                            R.string.food_meal_logged_feedback,
                            meal.name,
                            mealName
                        )
                    },
                    onDeleteCustomMeal = onDeleteCustomMeal,
                    onOpenProfile = onOpenProfile
                )
            }

            // Privacy Note
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.food_privacy_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    activeEditorMeal?.let { meal ->
        FoodEntryEditorSheet(
            dateIso = uiState.selectedDateIso,
            mealType = meal,
            existingEntry = editingEntry,
            onSave = { saved ->
                onSaveEntry(saved)
                activeEditorMeal = null
                editingEntry = null
            },
            onDismiss = {
                activeEditorMeal = null
                editingEntry = null
            }
        )
    }
}

@Composable
private fun DailyTotalsSummaryCard(
    uiState: FoodUiState,
    numberFormatter: NumberFormat
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current
    val targetCals = uiState.effectiveCalorieTarget
    val remainingText = if (targetCals != null) {
        val diff = targetCals - uiState.totalCalories
        if (diff >= 0) {
            "${numberFormatter.format(diff)} kcal remaining"
        } else {
            "${numberFormatter.format(abs(diff))} kcal above suggested target"
        }
    } else {
        null
    }

    val calProgress = if (targetCals != null && targetCals > 0) {
        (uiState.totalCalories.toFloat() / targetCals.toFloat()).coerceIn(0f, 1f)
    } else {
        if (uiState.totalCalories > 0) 0.25f else 0f
    }

    val proteinProgress = (uiState.totalProteinG / uiState.proteinGoalG.coerceAtLeast(1)).coerceIn(0f, 1f)
    val carbsTarget = ((targetCals ?: 2000) * 0.45f / 4f).coerceAtLeast(50f)
    val fatTarget = ((targetCals ?: 2000) * 0.30f / 9f).coerceAtLeast(30f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("food_totals_card"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = if (isDark) 2.dp else 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.food_daily_totals),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (remainingText != null) {
                        Text(
                            text = remainingText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = if (targetCals != null) {
                        "${numberFormatter.format(uiState.totalCalories)} / ${numberFormatter.format(targetCals)} kcal"
                    } else {
                        "${numberFormatter.format(uiState.totalCalories)} kcal"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = accents.greenPrimary
                )
            }

            GreenGradientProgressBar(fraction = calProgress)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MacroProgressColumn(
                    label = stringResource(R.string.food_protein_label),
                    valueText = "${uiState.totalProteinG.roundToInt()} / ${uiState.proteinGoalG} g",
                    fraction = proteinProgress,
                    modifier = Modifier.weight(1f)
                )
                MacroProgressColumn(
                    label = stringResource(R.string.food_carbs_label),
                    valueText = "${uiState.totalCarbsG.roundToInt()} g",
                    fraction = (uiState.totalCarbsG / carbsTarget).coerceIn(0f, 1f),
                    modifier = Modifier.weight(1f)
                )
                MacroProgressColumn(
                    label = stringResource(R.string.food_fat_label),
                    valueText = "${uiState.totalFatG.roundToInt()} g",
                    fraction = (uiState.totalFatG / fatTarget).coerceIn(0f, 1f),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MacroProgressColumn(
    label: String,
    valueText: String,
    fraction: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valueText,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        GreenGradientProgressBar(fraction = fraction, heightDp = 6)
    }
}

@Composable
private fun GreenGradientProgressBar(
    fraction: Float,
    heightDp: Int = 8
) {
    val accents = LocalAccentColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        if (fraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(accents.horizontalGradient)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CalorieGoalCard(
    uiState: FoodUiState,
    onSetActivityLevel: (ActivityLevel) -> Unit,
    onSetCalorieOverride: (Int?) -> Unit,
    onOpenProfile: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current
    val formatter = NumberFormat.getIntegerInstance()
    var overrideText by remember(uiState.calorieTargetOverride) {
        mutableStateOf(uiState.calorieTargetOverride?.toString() ?: "")
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = if (isDark) 2.dp else 3.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.food_suggested_target_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.food_guidance_note),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (uiState.effectiveCalorieTarget != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(accents.greenTint)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${formatter.format(uiState.effectiveCalorieTarget)} kcal",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (!uiState.userProfile.hasCalorieFormulaData) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.food_profile_prompt_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.food_profile_prompt_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onOpenProfile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("food_add_profile_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.bmi_add_profile_cta))
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.food_activity_level),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ActivityLevel.entries.forEach { level ->
                        FilterChip(
                            selected = uiState.activityLevel == level,
                            onClick = { onSetActivityLevel(level) },
                            label = { Text(level.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accents.greenTint,
                                selectedLabelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.testTag("activity_level_${level.name.lowercase()}")
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = overrideText,
                    onValueChange = { raw ->
                        if (raw.length <= 4 && raw.all { it.isDigit() }) {
                            overrideText = raw
                            val parsed = raw.toIntOrNull()
                            if (raw.isEmpty()) {
                                onSetCalorieOverride(null)
                            } else if (parsed != null && parsed >= 800) {
                                onSetCalorieOverride(parsed)
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.food_override_target)) },
                    placeholder = {
                        Text(uiState.suggestedCalorieTarget?.toString() ?: "e.g. 2000")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("food_calorie_override_input")
                )
                if (uiState.calorieTargetOverride != null) {
                    OutlinedButton(
                        onClick = {
                            overrideText = ""
                            onSetCalorieOverride(null)
                        },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(stringResource(R.string.food_use_suggested))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DietPreferencesCard(
    dietType: DietType,
    proteinGoalG: Int,
    consumedProteinG: Float,
    onSetDietType: (DietType) -> Unit,
    onSetProteinGoal: (Int) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current
    var proteinInput by remember(proteinGoalG) { mutableStateOf(proteinGoalG.toString()) }

    val consumedRounded = consumedProteinG.roundToInt()
    val diff = consumedRounded - proteinGoalG
    val proteinStatusMessage = when {
        abs(diff) <= 5 -> "On target for your protein goal"
        diff < 0 -> "Under protein goal — ${abs(diff)} g remaining"
        else -> "Over protein goal by $diff g"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = if (isDark) 2.dp else 3.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.food_diet_preferences),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DietType.entries.forEach { type ->
                    FilterChip(
                        selected = dietType == type,
                        onClick = { onSetDietType(type) },
                        label = { Text(type.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accents.greenTint,
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("diet_type_${type.name.lowercase()}")
                    )
                }
            }

            OutlinedTextField(
                value = proteinInput,
                onValueChange = { raw ->
                    if (raw.length <= 3 && raw.all { it.isDigit() }) {
                        proteinInput = raw
                        val parsed = raw.toIntOrNull()
                        if (parsed != null && parsed >= 20) {
                            onSetProteinGoal(parsed)
                        }
                    }
                },
                label = { Text(stringResource(R.string.food_protein_goal_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("protein_goal_input")
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(accents.greenTint)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = proteinStatusMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("protein_status_message")
                )
            }
        }
    }
}

@Composable
private fun WeeklyCalorieChart(
    bars: List<DailyCalorieBar>,
    selectedDateIso: String,
    goalCalories: Int?,
    onSelectDate: (String) -> Unit
) {
    if (bars.isEmpty()) return

    val accents = LocalAccentColors.current
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val goalLineColor = accents.goalLine.copy(alpha = 0.70f)
    val effectiveGoal = goalCalories ?: 2000
    val maxCalories = maxOf(
        effectiveGoal,
        bars.maxOfOrNull { it.calories } ?: effectiveGoal,
        1200
    )
    val selectedBar = bars.firstOrNull { it.dateIso == selectedDateIso } ?: bars.last()
    val formatter = NumberFormat.getIntegerInstance()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = DateUtils.formatDisplayDate(
                    selectedBar.dateIso,
                    stringResource(R.string.label_today),
                    stringResource(R.string.label_yesterday)
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(accents.greenTint)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${formatter.format(selectedBar.calories)} kcal",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(148.dp)
        ) {
            if (goalCalories != null) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val goalRatio = (goalCalories.toFloat() / maxCalories.toFloat()).coerceIn(0.05f, 1f)
                    val goalY = size.height * (1f - goalRatio)
                    drawLine(
                        color = goalLineColor,
                        start = Offset(0f, goalY),
                        end = Offset(size.width, goalY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                bars.forEachIndexed { index, bar ->
                    val targetRatio = (bar.calories.toFloat() / maxCalories.toFloat()).coerceIn(0f, 1f)
                    val animatedRatio by animateFloatAsState(
                        targetValue = targetRatio,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "cal_bar_$index"
                    )
                    val isSelected = bar.dateIso == selectedDateIso
                    val barBrush = if (isSelected) {
                        accents.verticalGradient
                    } else {
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(
                                accents.gradientEnd.copy(alpha = 0.7f),
                                accents.gradientStart.copy(alpha = 0.7f)
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .testTag("calorie_chart_bar_$index")
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onSelectDate(bar.dateIso) },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth(0.62f)
                                .fillMaxHeight()
                        ) {
                            val cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                            drawRoundRect(
                                color = trackColor.copy(alpha = if (isSelected) 0.85f else 0.45f),
                                topLeft = Offset.Zero,
                                size = size,
                                cornerRadius = cornerRadius
                            )
                            val minBarPx = if (bar.calories > 0) 6.dp.toPx() else 0f
                            val barHeight = (size.height * animatedRatio).coerceAtLeast(minBarPx)
                            if (barHeight > 0f) {
                                drawRoundRect(
                                    brush = barBrush,
                                    topLeft = Offset(0f, size.height - barHeight),
                                    size = Size(size.width, barHeight),
                                    cornerRadius = cornerRadius
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            bars.forEach { bar ->
                val isSelected = bar.dateIso == selectedDateIso
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = DateUtils.shortWeekday(bar.dateIso),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MealSectionCard(
    mealType: MealType,
    entries: List<FoodEntryEntity>,
    onAddFood: () -> Unit,
    onEditEntry: (FoodEntryEntity) -> Unit,
    onDeleteEntry: (Long) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val mealTitle = when (mealType) {
        MealType.BREAKFAST -> stringResource(R.string.food_meal_breakfast)
        MealType.LUNCH -> stringResource(R.string.food_meal_lunch)
        MealType.DINNER -> stringResource(R.string.food_meal_dinner)
        MealType.SNACKS -> stringResource(R.string.food_meal_snacks)
    }
    val mealCalories = entries.sumOf { it.calories }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meal_card_${mealType.name.lowercase()}"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = if (isDark) 2.dp else 3.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = mealTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$mealCalories kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = onAddFood,
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .testTag("add_food_${mealType.name.lowercase()}"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.food_add_food))
                }
            }

            if (entries.isEmpty()) {
                Text(
                    text = stringResource(R.string.food_empty_meal),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                entries.forEach { entry ->
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
                    val unitLabel = if (entry.amountUnit == FoodAmountUnit.SERVINGS.name) {
                        stringResource(R.string.food_unit_servings)
                    } else {
                        stringResource(R.string.food_unit_grams)
                    }
                    val amountFormatted = if (entry.amount % 1f == 0f) {
                        entry.amount.toInt().toString()
                    } else {
                        String.format(Locale.US, "%.1f", entry.amount)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${entry.name} ($amountFormatted $unitLabel)",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${entry.calories} kcal • P ${entry.proteinG.roundToInt()}g • C ${entry.carbsG.roundToInt()}g • F ${entry.fatG.roundToInt()}g",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row {
                            IconButton(
                                onClick = { onEditEntry(entry) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("edit_food_${entry.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = stringResource(R.string.food_edit_food),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = { onDeleteEntry(entry.id) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("delete_food_${entry.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = stringResource(R.string.dialog_confirm_clear),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun FoodEntryEditorSheet(
    dateIso: String,
    mealType: MealType,
    existingEntry: FoodEntryEntity?,
    onSave: (FoodEntryEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val accents = LocalAccentColors.current
    var name by remember(existingEntry) { mutableStateOf(existingEntry?.name ?: "") }
    var amountText by remember(existingEntry) {
        mutableStateOf(
            existingEntry?.amount?.let {
                if (it % 1f == 0f) it.toInt().toString() else String.format(Locale.US, "%.1f", it)
            } ?: "100"
        )
    }
    var amountUnit by remember(existingEntry) {
        mutableStateOf(
            existingEntry?.amountUnit?.let {
                runCatching { FoodAmountUnit.valueOf(it) }.getOrDefault(FoodAmountUnit.GRAMS)
            } ?: FoodAmountUnit.GRAMS
        )
    }
    var caloriesText by remember(existingEntry) {
        mutableStateOf(existingEntry?.calories?.toString() ?: "")
    }
    var proteinText by remember(existingEntry) {
        mutableStateOf(existingEntry?.proteinG?.roundToInt()?.toString() ?: "")
    }
    var carbsText by remember(existingEntry) {
        mutableStateOf(existingEntry?.carbsG?.roundToInt()?.toString() ?: "")
    }
    var fatText by remember(existingEntry) {
        mutableStateOf(existingEntry?.fatG?.roundToInt()?.toString() ?: "")
    }
    var matchedTemplate by remember { mutableStateOf<CommonFoodItem?>(null) }

    fun applyCatalogValues(item: CommonFoodItem, amount: Float, unit: FoodAmountUnit) {
        val grams = if (unit == FoodAmountUnit.GRAMS) amount else amount * item.defaultServingGrams
        val factor = grams / 100f
        caloriesText = (item.caloriesPer100g * factor).roundToInt().toString()
        proteinText = String.format(Locale.US, "%.1f", item.proteinPer100g * factor)
        carbsText = String.format(Locale.US, "%.1f", item.carbsPer100g * factor)
        fatText = String.format(Locale.US, "%.1f", item.fatPer100g * factor)
    }

    val suggestions = remember(name) { FoodCatalog.findMatches(name) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        if (existingEntry == null) R.string.food_add_food else R.string.food_edit_food
                    ),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { typed ->
                    name = typed
                    val exact = FoodCatalog.findExactMatch(typed)
                    if (exact != null) {
                        matchedTemplate = exact
                        val amt = amountText.toFloatOrNull() ?: 100f
                        applyCatalogValues(exact, amt, amountUnit)
                    } else {
                        matchedTemplate = null
                    }
                },
                label = { Text(stringResource(R.string.food_name_label)) },
                placeholder = { Text(stringResource(R.string.food_name_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("food_editor_name_input")
            )

            // Built-in Common Food Suggestions
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                suggestions.take(6).forEach { item ->
                    FilterChip(
                        selected = matchedTemplate?.name == item.name,
                        onClick = {
                            name = item.name
                            matchedTemplate = item
                            val amt = if (amountUnit == FoodAmountUnit.SERVINGS) {
                                1f
                            } else {
                                amountText.toFloatOrNull() ?: 100f
                            }
                            if (amountUnit == FoodAmountUnit.SERVINGS && amountText == "100") {
                                amountText = "1"
                            }
                            applyCatalogValues(item, amt, amountUnit)
                        },
                        label = { Text(item.name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accents.greenTint,
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            // Amount + grams / servings toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { raw ->
                        if (raw.length <= 6) {
                            amountText = raw
                            val amt = raw.toFloatOrNull()
                            val tpl = matchedTemplate
                            if (amt != null && amt > 0f && tpl != null) {
                                applyCatalogValues(tpl, amt, amountUnit)
                            }
                        }
                    },
                    label = { Text(stringResource(R.string.food_amount_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("food_editor_amount_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = amountUnit == FoodAmountUnit.GRAMS,
                        onClick = {
                            amountUnit = FoodAmountUnit.GRAMS
                            if (amountText == "1" && matchedTemplate != null) {
                                amountText = "100"
                            }
                            val amt = amountText.toFloatOrNull() ?: 100f
                            matchedTemplate?.let { applyCatalogValues(it, amt, FoodAmountUnit.GRAMS) }
                        },
                        label = { Text(stringResource(R.string.food_unit_grams)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accents.greenTint,
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    FilterChip(
                        selected = amountUnit == FoodAmountUnit.SERVINGS,
                        onClick = {
                            amountUnit = FoodAmountUnit.SERVINGS
                            if (amountText == "100") {
                                amountText = "1"
                            }
                            val amt = amountText.toFloatOrNull() ?: 1f
                            matchedTemplate?.let { applyCatalogValues(it, amt, FoodAmountUnit.SERVINGS) }
                        },
                        label = { Text(stringResource(R.string.food_unit_servings)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accents.greenTint,
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            // Editable Macros & Calories
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = caloriesText,
                    onValueChange = { if (it.length <= 5) caloriesText = it },
                    label = { Text(stringResource(R.string.food_calories_label)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("food_editor_calories_input")
                )
                OutlinedTextField(
                    value = proteinText,
                    onValueChange = { if (it.length <= 5) proteinText = it },
                    label = { Text("${stringResource(R.string.food_protein_label)} (g)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("food_editor_protein_input")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = carbsText,
                    onValueChange = { if (it.length <= 5) carbsText = it },
                    label = { Text("${stringResource(R.string.food_carbs_label)} (g)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("food_editor_carbs_input")
                )
                OutlinedTextField(
                    value = fatText,
                    onValueChange = { if (it.length <= 5) fatText = it },
                    label = { Text("${stringResource(R.string.food_fat_label)} (g)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("food_editor_fat_input")
                )
            }

            Button(
                onClick = {
                    val cleanName = name.trim()
                    if (cleanName.isNotEmpty()) {
                        val entry = FoodEntryEntity(
                            id = existingEntry?.id ?: 0L,
                            date = existingEntry?.date ?: dateIso,
                            mealType = existingEntry?.mealType ?: mealType.name,
                            name = cleanName,
                            amount = (amountText.toFloatOrNull() ?: 100f).coerceAtLeast(0.1f),
                            amountUnit = amountUnit.name,
                            calories = (caloriesText.toIntOrNull() ?: 0).coerceAtLeast(0),
                            proteinG = (proteinText.toFloatOrNull() ?: 0f).coerceAtLeast(0f),
                            carbsG = (carbsText.toFloatOrNull() ?: 0f).coerceAtLeast(0f),
                            fatG = (fatText.toFloatOrNull() ?: 0f).coerceAtLeast(0f),
                            createdAt = existingEntry?.createdAt ?: System.currentTimeMillis()
                        )
                        onSave(entry)
                    }
                },
                enabled = name.trim().isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
                    .testTag("food_editor_save_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(stringResource(R.string.food_save_entry))
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
