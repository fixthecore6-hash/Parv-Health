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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.DailyStepsEntity
import com.example.data.DateUtils
import com.example.data.FallAlertEntity
import com.example.data.FallAlertOutcome
import com.example.data.WaterEntryEntity
import com.example.data.WorkoutCatalog
import com.example.data.WorkoutDayEntity
import com.example.ui.ChartMetricPoint
import com.example.ui.PedometerUiState
import com.example.ui.SafetyHealthUiState
import com.example.ui.components.WeeklyStepChart
import com.example.ui.theme.LocalAccentColors
import com.example.ui.theme.LocalIsDarkTheme
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class ChartPeriod {
    WEEKLY,
    MONTHLY
}

@Composable
fun HistoryScreen(
    uiState: PedometerUiState,
    safetyUiState: SafetyHealthUiState,
    onDeleteWaterEntry: (Long) -> Unit,
    onDeleteFallAlert: (Long) -> Unit,
    onToggleTheme: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current
    val numberFormatter = NumberFormat.getIntegerInstance()
    val hasAnyLoggedSteps = uiState.history.any { it.steps > 0 }
    var chartPeriod by rememberSaveable { mutableStateOf(ChartPeriod.WEEKLY) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.history_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { onToggleTheme(isDark) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("history_theme_toggle_button")
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
                                .testTag("history_settings_button")
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

            // 2x2 Summary Stats Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryStatCard(
                            label = stringResource(R.string.history_weekly_avg),
                            value = numberFormatter.format(uiState.weeklyAverageSteps),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStatCard(
                            label = stringResource(R.string.history_best_day),
                            value = numberFormatter.format(uiState.bestDaySteps),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryStatCard(
                            label = stringResource(R.string.history_streak),
                            value = "${uiState.currentStreak}d",
                            modifier = Modifier.weight(1f)
                        )
                        SummaryStatCard(
                            label = stringResource(R.string.history_total_stored),
                            value = numberFormatter.format(uiState.totalStoredSteps),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Progress Charts Header + Weekly/Monthly Toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.history_charts_section),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(modifier = Modifier.padding(3.dp)) {
                            listOf(
                                ChartPeriod.WEEKLY to stringResource(R.string.history_period_weekly),
                                ChartPeriod.MONTHLY to stringResource(R.string.history_period_monthly)
                            ).forEach { (period, label) ->
                                val selected = chartPeriod == period
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(if (selected) accents.greenPrimary else MaterialTheme.colorScheme.surface)
                                        .clickable { chartPeriod = period }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("chart_period_${period.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Steps Chart Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shadowElevation = if (isDark) 2.dp else 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (chartPeriod == ChartPeriod.WEEKLY) {
                                stringResource(R.string.history_chart_title)
                            } else {
                                "${stringResource(R.string.history_chart_steps)} • 30 Days"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (chartPeriod == ChartPeriod.WEEKLY) {
                            WeeklyStepChart(
                                days = uiState.weeklyDays,
                                goal = uiState.dailyGoal
                            )
                        } else {
                            MetricTrendBarChart(
                                points = uiState.monthlyStepPoints,
                                minScaleMax = uiState.dailyGoal.toFloat(),
                                unitLabel = "steps",
                                isDecimal = false
                            )
                        }
                    }
                }
            }

            // Workouts (sets) Chart Card
            item {
                MetricChartCard(
                    title = stringResource(R.string.history_chart_workouts),
                    points = if (chartPeriod == ChartPeriod.WEEKLY) {
                        uiState.weeklyWorkoutPoints
                    } else {
                        uiState.monthlyWorkoutPoints
                    },
                    minScaleMax = 10f,
                    unitLabel = "sets",
                    isDecimal = false
                )
            }

            // Calories Chart Card
            item {
                MetricChartCard(
                    title = stringResource(R.string.history_chart_calories),
                    points = if (chartPeriod == ChartPeriod.WEEKLY) {
                        uiState.weeklyCaloriePoints
                    } else {
                        uiState.monthlyCaloriePoints
                    },
                    minScaleMax = 2000f,
                    unitLabel = "kcal",
                    isDecimal = false
                )
            }

            // Sleep Chart Card
            item {
                MetricChartCard(
                    title = stringResource(R.string.history_chart_sleep),
                    points = if (chartPeriod == ChartPeriod.WEEKLY) {
                        uiState.weeklySleepPoints
                    } else {
                        uiState.monthlySleepPoints
                    },
                    minScaleMax = 8f,
                    unitLabel = "hrs",
                    isDecimal = true
                )
            }

            // 7-Day Water Intake Chart Card
            item {
                MetricChartCard(
                    title = stringResource(R.string.history_water_chart_title),
                    points = safetyUiState.weeklyWaterDays.map {
                        ChartMetricPoint(dateIso = it.dateIso, value = it.totalMl.toFloat())
                    },
                    minScaleMax = safetyUiState.effectiveWaterGoalMl.toFloat(),
                    unitLabel = "ml",
                    isDecimal = false
                )
            }

            // Today's Water Entries (with Delete)
            item {
                Text(
                    text = stringResource(R.string.history_water_entries_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (safetyUiState.todayWaterEntries.isEmpty()) {
                item {
                    SimpleHistoryEmptyCard(
                        icon = Icons.Rounded.WaterDrop,
                        text = stringResource(R.string.history_water_empty)
                    )
                }
            } else {
                items(
                    items = safetyUiState.todayWaterEntries,
                    key = { "water_${it.id}" }
                ) { entry ->
                    WaterHistoryEntryCard(
                        entry = entry,
                        onDelete = { onDeleteWaterEntry(entry.id) }
                    )
                }
            }

            // Emergency & Fall Alerts History Section
            item {
                Text(
                    text = stringResource(R.string.emergency_alerts_history_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (safetyUiState.fallAlerts.isEmpty()) {
                item {
                    SimpleHistoryEmptyCard(
                        icon = Icons.Rounded.Sensors,
                        text = stringResource(R.string.fall_history_empty)
                    )
                }
            } else {
                items(
                    items = safetyUiState.fallAlerts,
                    key = { "fall_${it.id}" }
                ) { alert ->
                    FallAlertHistoryCard(
                        alert = alert,
                        onDelete = { onDeleteFallAlert(alert.id) }
                    )
                }
            }

            // Completed Workouts Section
            item {
                Text(
                    text = stringResource(R.string.history_workouts_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (uiState.workoutHistory.isEmpty()) {
                item {
                    EmptyWorkoutHistoryCard()
                }
            } else {
                items(
                    items = uiState.workoutHistory,
                    key = { "workout_${it.date}" }
                ) { workoutDay ->
                    WorkoutHistoryCard(day = workoutDay)
                }
            }

            // Stored Daily Logs Header
            item {
                Text(
                    text = stringResource(R.string.history_daily_logs_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (!hasAnyLoggedSteps && uiState.history.isEmpty()) {
                item {
                    EmptyHistoryCard()
                }
            } else {
                items(
                    items = uiState.history,
                    key = { it.date }
                ) { day ->
                    DailyHistoryCard(day = day)
                }
                if (!hasAnyLoggedSteps) {
                    item {
                        EmptyHistoryCard()
                    }
                }
            }

            // Device Privacy Footer
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
                        text = stringResource(R.string.data_stays_on_device),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = if (isDark) 2.dp else 3.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DailyHistoryCard(day: DailyStepsEntity) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current
    val formatter = NumberFormat.getIntegerInstance()
    val km = String.format(Locale.getDefault(), "%.2f", day.distanceKm)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_day_card_${day.date}"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = if (isDark) 2.dp else 3.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = DateUtils.formatDisplayDate(
                            day.date,
                            stringResource(R.string.label_today),
                            stringResource(R.string.label_yesterday)
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (day.goalReached) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = stringResource(R.string.goal_reached_badge),
                            tint = accents.greenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = "${formatter.format(day.steps)} steps",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Green gradient progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                if (day.progressFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(day.progressFraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(accents.horizontalGradient)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$km km • ${day.caloriesKcal} kcal • ${day.activeMinutes} min",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Goal ${formatter.format(day.goal)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryCard() {
    val accents = LocalAccentColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accents.greenTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.DirectionsWalk,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = stringResource(R.string.history_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.history_empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WorkoutHistoryCard(day: WorkoutDayEntity) {
    val isDark = LocalIsDarkTheme.current
    val accents = LocalAccentColors.current
    val plan = WorkoutCatalog.planForDay(day.dayOfWeekIndex)
    val fraction = if (day.totalSets > 0) {
        (day.completedSets.toFloat() / day.totalSets.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_workout_card_${day.date}"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = if (isDark) 2.dp else 3.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = DateUtils.formatDisplayDate(
                            day.date,
                            stringResource(R.string.label_today),
                            stringResource(R.string.label_yesterday)
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (day.isDone) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = stringResource(R.string.workout_day_done),
                            tint = accents.greenPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.workout_sets_progress, day.completedSets, day.totalSets),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                if (fraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(accents.horizontalGradient)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = plan.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (day.isDone) {
                    Text(
                        text = stringResource(R.string.workout_day_done),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = accents.greenPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyWorkoutHistoryCard() {
    val accents = LocalAccentColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accents.greenTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = stringResource(R.string.history_workouts_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricChartCard(
    title: String,
    points: List<ChartMetricPoint>,
    minScaleMax: Float,
    unitLabel: String,
    isDecimal: Boolean
) {
    val isDark = LocalIsDarkTheme.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = if (isDark) 2.dp else 4.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            MetricTrendBarChart(
                points = points,
                minScaleMax = minScaleMax,
                unitLabel = unitLabel,
                isDecimal = isDecimal
            )
        }
    }
}

@Composable
private fun MetricTrendBarChart(
    points: List<ChartMetricPoint>,
    minScaleMax: Float,
    unitLabel: String,
    isDecimal: Boolean
) {
    if (points.isEmpty()) return

    var selectedIndex by remember(points.size) { mutableIntStateOf(points.lastIndex) }
    val safeIndex = selectedIndex.coerceIn(0, points.lastIndex)
    val selectedPoint = points[safeIndex]
    val maxVal = maxOf(minScaleMax, points.maxOfOrNull { it.value } ?: minScaleMax)
    val accents = LocalAccentColors.current
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val formatter = NumberFormat.getIntegerInstance()

    val formattedValue = if (isDecimal) {
        "${String.format(Locale.getDefault(), "%.1f", selectedPoint.value)} $unitLabel"
    } else {
        "${formatter.format(selectedPoint.value.toInt())} $unitLabel"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = DateUtils.formatDisplayDate(
                    selectedPoint.dateIso,
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
                    text = formattedValue,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val spacing = if (points.size > 10) 2.dp else 10.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp),
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.Bottom
        ) {
            points.forEachIndexed { index, pt ->
                val targetRatio = (pt.value / maxVal).coerceIn(0f, 1f)
                val animatedRatio by animateFloatAsState(
                    targetValue = targetRatio,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    ),
                    label = "metric_bar_${unitLabel}_$index"
                )
                val isSelected = index == safeIndex
                val brush = Brush.verticalGradient(
                    colors = if (isSelected) {
                        listOf(accents.gradientEnd, accents.gradientStart)
                    } else {
                        listOf(
                            accents.gradientEnd.copy(alpha = 0.72f),
                            accents.gradientStart.copy(alpha = 0.72f)
                        )
                    }
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedIndex = index },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth(if (points.size > 10) 0.85f else 0.62f)
                            .fillMaxHeight()
                    ) {
                        val corner = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        drawRoundRect(
                            color = trackColor.copy(alpha = if (isSelected) 0.85f else 0.45f),
                            topLeft = Offset.Zero,
                            size = size,
                            cornerRadius = corner
                        )
                        val minBarPx = if (pt.value > 0f) 4.dp.toPx() else 0f
                        val barHeight = (size.height * animatedRatio).coerceAtLeast(minBarPx)
                        if (barHeight > 0f) {
                            drawRoundRect(
                                brush = brush,
                                topLeft = Offset(0f, size.height - barHeight),
                                size = Size(size.width, barHeight),
                                cornerRadius = corner
                            )
                        }
                    }
                }
            }
        }

        if (points.size <= 10) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                points.forEachIndexed { index, pt ->
                    val isSelected = index == safeIndex
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = DateUtils.shortWeekday(pt.dateIso),
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
        } else {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = DateUtils.shortWeekday(points.first().dateIso) + " " + points.first().dateIso.takeLast(5),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.label_today),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SimpleHistoryEmptyCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    val accents = LocalAccentColors.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accents.greenTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accents.greenPrimary
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WaterHistoryEntryCard(
    entry: WaterEntryEntity,
    onDelete: () -> Unit
) {
    val accents = LocalAccentColors.current
    val timeLabel = remember(entry.timestampMs) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(entry.timestampMs))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_water_entry_${entry.id}"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.WaterDrop,
                    contentDescription = null,
                    tint = accents.greenPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "${entry.amountMl} ml",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("delete_water_entry_${entry.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete water entry",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FallAlertHistoryCard(
    alert: FallAlertEntity,
    onDelete: () -> Unit
) {
    val accents = LocalAccentColors.current
    val timeLabel = remember(alert.timestampMs) {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(alert.timestampMs))
    }
    val outcomeEnum = runCatching { FallAlertOutcome.valueOf(alert.outcome) }
        .getOrDefault(FallAlertOutcome.CANCELLED)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("history_fall_alert_${alert.id}"),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = timeLabel,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(accents.greenTint)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = outcomeEnum.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Text(
                    text = "Peak ${String.format(Locale.getDefault(), "%.1f", alert.peakG)}g • Sensitivity ${alert.sensitivity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("delete_fall_alert_${alert.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = "Delete fall alert",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

