package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ExerciseEquipment
import com.example.data.ExerciseLevel
import com.example.data.LibraryExercise
import com.example.data.MuscleGroup
import com.example.ui.PlannedDayExerciseItem
import com.example.ui.WeekDaySummary
import com.example.ui.WorkoutUiState
import com.example.ui.components.ExerciseIllustration
import com.example.ui.theme.LocalAccentColors
import com.example.ui.theme.LocalIsDarkTheme

private enum class WorkoutSubTab {
    PLANNER,
    LIBRARY
}

@Composable
fun WorkoutScreen(
    uiState: WorkoutUiState,
    onSelectDay: (Int) -> Unit,
    onToggleSet: (String, Int, Boolean) -> Unit,
    onToggleDayDone: () -> Unit,
    onUpdateSearchQuery: (String) -> Unit,
    onSelectMuscle: (MuscleGroup?) -> Unit,
    onSelectLevel: (ExerciseLevel?) -> Unit,
    onSelectEquipment: (ExerciseEquipment?) -> Unit,
    onAddExerciseToDay: (Int, String) -> Unit,
    onRemovePlannedExercise: (Long) -> Unit,
    onCreateCustomExercise: (String, MuscleGroup, Int, String, String) -> Unit,
    onDeleteCustomExercise: (String) -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current

    var subTab by rememberSaveable { mutableStateOf(WorkoutSubTab.PLANNER) }
    var selectedExercise by remember { mutableStateOf<LibraryExercise?>(null) }
    var exerciseToAdd by remember { mutableStateOf<LibraryExercise?>(null) }
    var showCreateCustomSheet by remember { mutableStateOf(false) }
    var addedBanner by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(addedBanner) {
        if (addedBanner != null) {
            kotlinx.coroutines.delay(2500)
            addedBanner = null
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.workout_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.workout_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = { onToggleTheme(isDark) },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("workout_theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                        contentDescription = stringResource(R.string.toggle_theme_desc),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Segmented Switcher: My Planner | Exercise Library
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                WorkoutSubTab.entries.forEach { tab ->
                    val selected = subTab == tab
                    val label = if (tab == WorkoutSubTab.PLANNER) {
                        stringResource(R.string.workout_tab_planner)
                    } else {
                        stringResource(R.string.workout_tab_library)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) accents.greenPrimary else MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                            .clickable { subTab = tab }
                            .testTag("workout_subtab_${tab.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(visible = addedBanner != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(accents.greenTint)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
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
                        text = addedBanner ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            when (subTab) {
                WorkoutSubTab.PLANNER -> {
                    PlannerTabContent(
                        uiState = uiState,
                        onSelectDay = onSelectDay,
                        onToggleSet = onToggleSet,
                        onToggleDayDone = onToggleDayDone,
                        onOpenDetail = { selectedExercise = it },
                        onRemovePlannedExercise = onRemovePlannedExercise,
                        onBrowseLibrary = { subTab = WorkoutSubTab.LIBRARY },
                        modifier = Modifier.weight(1f)
                    )
                }
                WorkoutSubTab.LIBRARY -> {
                    ExerciseLibraryList(
                        uiState = uiState,
                        onUpdateSearchQuery = onUpdateSearchQuery,
                        onSelectMuscle = onSelectMuscle,
                        onSelectLevel = onSelectLevel,
                        onSelectEquipment = onSelectEquipment,
                        onOpenDetail = { selectedExercise = it },
                        onRequestAddToDay = { exerciseToAdd = it },
                        onOpenCreateCustom = { showCreateCustomSheet = true },
                        onDeleteCustom = onDeleteCustomExercise,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    selectedExercise?.let { exercise ->
        LibraryExerciseDetailSheet(
            exercise = exercise,
            onAddToDay = {
                exerciseToAdd = exercise
                selectedExercise = null
            },
            onDismiss = { selectedExercise = null }
        )
    }

    exerciseToAdd?.let { exercise ->
        val dayLabels = uiState.weekSummaries
        ChooseDayDialog(
            exercise = exercise,
            weekSummaries = dayLabels,
            defaultDayIndex = uiState.selectedDayIndex,
            onConfirmDay = { chosenDayIndex ->
                onAddExerciseToDay(chosenDayIndex, exercise.id)
                val dayLabel = dayLabels.getOrNull(chosenDayIndex)?.shortLabel ?: "Day"
                addedBanner = "Added ${exercise.name} to $dayLabel"
                exerciseToAdd = null
            },
            onDismiss = { exerciseToAdd = null }
        )
    }

    if (showCreateCustomSheet) {
        CreateCustomExerciseSheet(
            onSave = { name, group, sets, reps, notes ->
                onCreateCustomExercise(name, group, sets, reps, notes)
                addedBanner = "Created \"$name\" and added to ${uiState.dayShortLabel}"
                showCreateCustomSheet = false
            },
            onDismiss = { showCreateCustomSheet = false }
        )
    }
}

@Composable
private fun PlannerTabContent(
    uiState: WorkoutUiState,
    onSelectDay: (Int) -> Unit,
    onToggleSet: (String, Int, Boolean) -> Unit,
    onToggleDayDone: () -> Unit,
    onOpenDetail: (LibraryExercise) -> Unit,
    onRemovePlannedExercise: (Long) -> Unit,
    onBrowseLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Mon-Sun Weekly Planner Strip
        item {
            WeeklyPlannerStrip(
                weekSummaries = uiState.weekSummaries,
                selectedDayIndex = uiState.selectedDayIndex,
                onSelectDay = onSelectDay
            )
        }

        // Day Progress & Mark Day Done Card
        item {
            val progressFraction = if (uiState.totalSetsCount > 0) {
                (uiState.completedSetsCount.toFloat() / uiState.totalSetsCount.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${uiState.dayShortLabel} • ${uiState.dayTitle}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(
                                    R.string.workout_sets_progress,
                                    uiState.completedSetsCount,
                                    uiState.totalSetsCount
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (uiState.isSelectedDayDone) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = stringResource(R.string.workout_day_done),
                                tint = accents.greenPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // Green gradient progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        if (progressFraction > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progressFraction)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(50))
                                    .background(accents.horizontalGradient)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onToggleDayDone,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("workout_mark_done_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = if (uiState.isSelectedDayDone) {
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            } else {
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(
                                    if (uiState.isSelectedDayDone) {
                                        R.string.workout_day_done
                                    } else {
                                        R.string.workout_mark_done
                                    }
                                ),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        OutlinedButton(
                            onClick = onBrowseLibrary,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("workout_browse_library_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FitnessCenter,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Library")
                        }
                    }
                }
            }
        }

        if (uiState.plannedExercises.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.workout_empty_day),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onBrowseLibrary,
                            modifier = Modifier.heightIn(min = 48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(stringResource(R.string.workout_browse_library))
                        }
                    }
                }
            }
        } else {
            items(
                items = uiState.plannedExercises,
                key = { "${uiState.selectedDayIndex}_${it.plannedId}_${it.exercise.id}" }
            ) { item ->
                PlannedExerciseCard(
                    item = item,
                    completedSetKeys = uiState.completedSetKeys,
                    onToggleSet = { setIdx, checked ->
                        onToggleSet(item.exercise.id, setIdx, checked)
                    },
                    onOpenDetail = { onOpenDetail(item.exercise) },
                    onRemove = { onRemovePlannedExercise(item.plannedId) }
                )
            }
        }

        // Safety Note Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Rounded.HealthAndSafety,
                        contentDescription = null,
                        tint = accents.greenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.workout_safety_note),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("workout_safety_note")
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
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
                    text = stringResource(R.string.data_stays_on_device),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WeeklyPlannerStrip(
    weekSummaries: List<WeekDaySummary>,
    selectedDayIndex: Int,
    onSelectDay: (Int) -> Unit
) {
    val accents = LocalAccentColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        weekSummaries.forEach { day ->
            val isSelected = day.dayIndex == selectedDayIndex
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelectDay(day.dayIndex) }
                    .testTag("workout_day_tab_${day.dayIndex}"),
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) {
                    accents.greenPrimary
                } else {
                    MaterialTheme.colorScheme.surface
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) accents.greenPrimary else MaterialTheme.colorScheme.outline
                )
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = day.shortLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (day.isDone) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = stringResource(R.string.workout_day_done),
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else accents.greenPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    } else if (day.isToday) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.onPrimary else accents.greenPrimary
                                )
                        )
                    } else {
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PlannedExerciseCard(
    item: PlannedDayExerciseItem,
    completedSetKeys: Set<Pair<String, Int>>,
    onToggleSet: (Int, Boolean) -> Unit,
    onOpenDetail: () -> Unit,
    onRemove: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current
    val exercise = item.exercise

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("workout_exercise_card_${exercise.id}"),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenDetail)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExerciseIllustration(
                    visualType = exercise.visualType,
                    modifier = Modifier.size(56.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${exercise.sets} sets • ${exercise.repsLabel} • ${exercise.muscleGroup.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.workout_tap_for_guide),
                        style = MaterialTheme.typography.labelSmall,
                        color = accents.greenPrimary
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = exercise.name,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("remove_planned_${exercise.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.workout_remove_from_day),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))

            // Render set checkboxes in rows of up to 3 for clean wrapping
            val setRows = (0 until exercise.sets).chunked(3)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                setRows.forEach { rowSets ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowSets.forEach { setIdx ->
                            val isChecked = (exercise.id to setIdx) in completedSetKeys
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onToggleSet(setIdx, !isChecked) }
                                    .testTag("workout_set_${exercise.id}_$setIdx"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isChecked) {
                                    accents.greenTint
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked -> onToggleSet(setIdx, checked) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = accents.greenPrimary,
                                            checkmarkColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Set ${setIdx + 1}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
