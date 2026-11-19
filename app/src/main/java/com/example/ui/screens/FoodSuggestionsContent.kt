package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.RestaurantMenu
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CustomMealEntity
import com.example.data.FoodSuggestion
import com.example.data.FoodSuggestionCatalog
import com.example.data.MealType
import com.example.ui.FoodUiState
import com.example.ui.theme.LocalAccentColors
import com.example.ui.theme.LocalIsDarkTheme
import kotlin.math.roundToInt

@Composable
fun FoodSuggestionsSection(
    uiState: FoodUiState,
    onLogSuggestion: (FoodSuggestion) -> Unit,
    onSaveCustomMeal: (CustomMealEntity) -> Unit,
    onLogCustomMeal: (CustomMealEntity) -> Unit,
    onDeleteCustomMeal: (Long) -> Unit,
    onOpenProfile: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current

    var selectedMealFilter by remember { mutableStateOf<MealType?>(null) }
    var fitRemainingOnly by remember { mutableStateOf(false) }
    var showCreateCustomMeal by remember { mutableStateOf(false) }

    val targetCalories = uiState.effectiveCalorieTarget
    val remainingCalories = targetCalories?.let { it - uiState.totalCalories }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.food_suggestions_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.food_suggestions_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(
                onClick = { showCreateCustomMeal = true },
                modifier = Modifier
                    .heightIn(min = 44.dp)
                    .testTag("create_custom_meal_button"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, accents.greenPrimary)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = accents.greenPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Custom",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Custom Meals created by user (always available)
        if (uiState.customMeals.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shadowElevation = if (isDark) 2.dp else 3.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.food_custom_meals_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    uiState.customMeals.forEach { meal ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_meal_card_${meal.id}"),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = meal.name,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${meal.calories} kcal • P ${meal.proteinG.roundToInt()}g • C ${meal.carbsG.roundToInt()}g • F ${meal.fatG.roundToInt()}g",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (meal.ingredients.isNotBlank()) {
                                            Text(
                                                text = meal.ingredients,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { onDeleteCustomMeal(meal.id) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Button(
                                    onClick = { onLogCustomMeal(meal) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text(stringResource(R.string.food_log_this_meal))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Profile Completeness Gate for Calorie-Based Suggestions
        if (!uiState.userProfile.hasCalorieFormulaData) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("suggestions_profile_prompt"),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.food_suggestions_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.food_suggestions_profile_prompt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onOpenProfile,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("suggestions_open_profile_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
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
            }
        } else {
            // Filter Controls: Meal Type + Fit Remaining Calories
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedMealFilter == null,
                    onClick = { selectedMealFilter = null },
                    label = { Text(stringResource(R.string.library_filter_all)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accents.greenTint,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                MealType.entries.forEach { meal ->
                    val mealTitle = when (meal) {
                        MealType.BREAKFAST -> stringResource(R.string.food_meal_breakfast)
                        MealType.LUNCH -> stringResource(R.string.food_meal_lunch)
                        MealType.DINNER -> stringResource(R.string.food_meal_dinner)
                        MealType.SNACKS -> stringResource(R.string.food_meal_snacks)
                    }
                    FilterChip(
                        selected = selectedMealFilter == meal,
                        onClick = {
                            selectedMealFilter = if (selectedMealFilter == meal) null else meal
                        },
                        label = { Text(mealTitle) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accents.greenTint,
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("sug_filter_${meal.name.lowercase()}")
                    )
                }
                FilterChip(
                    selected = fitRemainingOnly,
                    onClick = { fitRemainingOnly = !fitRemainingOnly },
                    label = {
                        val rem = remainingCalories ?: 0
                        Text("${stringResource(R.string.food_suggestions_fit_remaining)} (${rem.coerceAtLeast(0)} kcal)")
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accents.greenTint,
                        selectedLabelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.testTag("sug_filter_fit_remaining")
                )
            }

            val suggestions = remember(
                uiState.dietType,
                selectedMealFilter,
                remainingCalories,
                fitRemainingOnly
            ) {
                FoodSuggestionCatalog.filterSuggestions(
                    dietType = uiState.dietType,
                    mealType = selectedMealFilter,
                    remainingCalories = remainingCalories,
                    onlyFitRemaining = fitRemainingOnly
                )
            }

            if (suggestions.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(
                        text = "No suggested meals fit the remaining calorie filter right now. Turn off \"Fit remaining calories\" or switch meal filters to browse all ${uiState.dietType.label.lowercase()} ideas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                suggestions.forEach { suggestion ->
                    SuggestionCard(
                        suggestion = suggestion,
                        remainingCalories = remainingCalories,
                        onLogMeal = { onLogSuggestion(suggestion) }
                    )
                }
            }
        }
    }

    if (showCreateCustomMeal) {
        CreateCustomMealSheet(
            onSave = { meal ->
                onSaveCustomMeal(meal)
                showCreateCustomMeal = false
            },
            onDismiss = { showCreateCustomMeal = false }
        )
    }
}

@Composable
private fun SuggestionCard(
    suggestion: FoodSuggestion,
    remainingCalories: Int?,
    onLogMeal: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current
    val mealLabel = when (suggestion.mealType) {
        MealType.BREAKFAST -> stringResource(R.string.food_meal_breakfast)
        MealType.LUNCH -> stringResource(R.string.food_meal_lunch)
        MealType.DINNER -> stringResource(R.string.food_meal_dinner)
        MealType.SNACKS -> stringResource(R.string.food_meal_snacks)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("suggestion_card_${suggestion.id}"),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = if (isDark) 2.dp else 3.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(accents.greenTint)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = mealLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (suggestion.isVegetarian) "Veg" else "Non-Veg",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "~${suggestion.calories} kcal • ${suggestion.proteinG.roundToInt()}g protein",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = accents.greenPrimary
                )
            }

            Text(
                text = suggestion.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = suggestion.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Ingredients: ${suggestion.ingredients.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (remainingCalories != null) {
                val fitsText = if (suggestion.calories <= remainingCalories) {
                    "Suggested • Fits remaining ${remainingCalories.coerceAtLeast(0)} kcal • For general guidance only"
                } else {
                    "Suggested • For general guidance only"
                }
                Text(
                    text = fitsText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onLogMeal,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 46.dp)
                    .testTag("log_suggestion_${suggestion.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.RestaurantMenu,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.food_log_this_meal))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CreateCustomMealSheet(
    onSave: (CustomMealEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val accents = LocalAccentColors.current
    var name by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf(MealType.LUNCH) }
    var caloriesText by remember { mutableStateOf("") }
    var proteinText by remember { mutableStateOf("") }
    var carbsText by remember { mutableStateOf("") }
    var fatText by remember { mutableStateOf("") }
    var ingredients by remember { mutableStateOf("") }
    var isVeg by remember { mutableStateOf(true) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.food_create_custom_meal),
                    style = MaterialTheme.typography.headlineSmall,
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
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.food_name_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_meal_name_input")
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MealType.entries.forEach { type ->
                    val label = when (type) {
                        MealType.BREAKFAST -> stringResource(R.string.food_meal_breakfast)
                        MealType.LUNCH -> stringResource(R.string.food_meal_lunch)
                        MealType.DINNER -> stringResource(R.string.food_meal_dinner)
                        MealType.SNACKS -> stringResource(R.string.food_meal_snacks)
                    }
                    FilterChip(
                        selected = mealType == type,
                        onClick = { mealType = type },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accents.greenTint,
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = caloriesText,
                    onValueChange = { s -> if (s.all { it.isDigit() }) caloriesText = s },
                    label = { Text("Calories") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = proteinText,
                    onValueChange = { proteinText = it },
                    label = { Text("Protein (g)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = carbsText,
                    onValueChange = { carbsText = it },
                    label = { Text("Carbs (g)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = fatText,
                    onValueChange = { fatText = it },
                    label = { Text("Fat (g)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = ingredients,
                onValueChange = { ingredients = it },
                label = { Text(stringResource(R.string.food_custom_meal_ingredients)) },
                minLines = 2,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Vegetarian option", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = isVeg, onCheckedChange = { isVeg = it })
            }

            Button(
                onClick = {
                    val trimmed = name.trim()
                    if (trimmed.isNotEmpty()) {
                        onSave(
                            CustomMealEntity(
                                name = trimmed,
                                mealType = mealType.name,
                                calories = caloriesText.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                                proteinG = proteinText.toFloatOrNull()?.coerceAtLeast(0f) ?: 0f,
                                carbsG = carbsText.toFloatOrNull()?.coerceAtLeast(0f) ?: 0f,
                                fatG = fatText.toFloatOrNull()?.coerceAtLeast(0f) ?: 0f,
                                ingredients = ingredients.trim(),
                                isVegetarian = isVeg
                            )
                        )
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
                    .testTag("custom_meal_save_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(stringResource(R.string.food_custom_meal_save))
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
