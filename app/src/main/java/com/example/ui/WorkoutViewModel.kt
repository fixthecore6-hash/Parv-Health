package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.ParvHealthApplication
import com.example.data.DateUtils
import com.example.data.DayWorkoutPlan
import com.example.data.ExerciseEquipment
import com.example.data.ExerciseLevel
import com.example.data.ExerciseLibraryCatalog
import com.example.data.LibraryExercise
import com.example.data.MuscleGroup
import com.example.data.WorkoutCatalog
import com.example.data.WorkoutDayEntity
import com.example.data.WorkoutRepository
import com.example.data.toLibraryExercise
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WeekDaySummary(
    val dayIndex: Int,
    val shortLabel: String,
    val dateIso: String,
    val isToday: Boolean,
    val isDone: Boolean,
    val completedSets: Int,
    val totalSets: Int
)

data class PlannedDayExerciseItem(
    val plannedId: Long,
    val exercise: LibraryExercise
)

data class WorkoutUiState(
    val selectedDayIndex: Int = DateUtils.todayWeekDayIndex(),
    val selectedDateIso: String = DateUtils.todayIso(),
    val weekSummaries: List<WeekDaySummary> = emptyList(),
    val dayTitle: String = WorkoutCatalog.planForDay(DateUtils.todayWeekDayIndex()).title,
    val dayShortLabel: String = WorkoutCatalog.planForDay(DateUtils.todayWeekDayIndex()).shortLabel,
    val plannedExercises: List<PlannedDayExerciseItem> = emptyList(),
    val completedSetKeys: Set<Pair<String, Int>> = emptySet(),
    val isSelectedDayDone: Boolean = false,
    val completedSetsCount: Int = 0,
    val totalSetsCount: Int = 0,
    val allLibraryExercises: List<LibraryExercise> = ExerciseLibraryCatalog.builtInExercises,
    val filteredLibraryExercises: List<LibraryExercise> = ExerciseLibraryCatalog.builtInExercises,
    val searchQuery: String = "",
    val selectedMuscleFilter: MuscleGroup? = null,
    val selectedLevelFilter: ExerciseLevel? = null,
    val selectedEquipmentFilter: ExerciseEquipment? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModel(
    application: Application,
    private val repository: WorkoutRepository
) : AndroidViewModel(application) {

    private val weekDates = DateUtils.currentWeekDaysIso()
    private val todayIndex = DateUtils.todayWeekDayIndex()
    private val selectedDayIndexFlow = MutableStateFlow(todayIndex)

    private val searchQueryFlow = MutableStateFlow("")
    private val muscleFilterFlow = MutableStateFlow<MuscleGroup?>(null)
    private val levelFilterFlow = MutableStateFlow<ExerciseLevel?>(null)
    private val equipmentFilterFlow = MutableStateFlow<ExerciseEquipment?>(null)

    private data class FilterTuple(
        val query: String,
        val muscle: MuscleGroup?,
        val level: ExerciseLevel?,
        val equipment: ExerciseEquipment?
    )

    private val filterFlow = combine(
        searchQueryFlow,
        muscleFilterFlow,
        levelFilterFlow,
        equipmentFilterFlow
    ) { query, muscle, level, equipment ->
        FilterTuple(query, muscle, level, equipment)
    }

    private val selectedSetsFlow = selectedDayIndexFlow.flatMapLatest { index ->
        val dateIso = weekDates.getOrElse(index) { DateUtils.todayIso() }
        repository.observeSetsForDate(dateIso)
    }

    private val plannerDataFlow = combine(
        repository.observeDaysForDates(weekDates),
        repository.observeAllPlannedExercises(),
        repository.observeCustomExercises()
    ) { savedDays, planned, customList ->
        Triple(savedDays, planned, customList)
    }

    val uiState: StateFlow<WorkoutUiState> = combine(
        selectedDayIndexFlow,
        plannerDataFlow,
        selectedSetsFlow,
        filterFlow
    ) { dayIndex, plannerData, sets, filters ->
        val (savedDays, allPlanned, customEntities) = plannerData
        val customExercises = customEntities.map { it.toLibraryExercise() }
        val allLibrary = customExercises + ExerciseLibraryCatalog.builtInExercises
        val exerciseById = allLibrary.associateBy { it.id }

        val plannedByDay = allPlanned.groupBy { it.dayOfWeekIndex }
        val dayByDate: Map<String, WorkoutDayEntity> = savedDays.associateBy { it.date }

        val summaries = weekDates.mapIndexed { idx, dateIso ->
            val fallbackPlan = WorkoutCatalog.planForDay(idx)
            val saved = dayByDate[dateIso]
            val dayExercises = plannedByDay[idx].orEmpty().mapNotNull { exerciseById[it.exerciseId] }
            val totalSets = dayExercises.sumOf { it.sets }
            WeekDaySummary(
                dayIndex = idx,
                shortLabel = fallbackPlan.shortLabel,
                dateIso = dateIso,
                isToday = idx == todayIndex,
                isDone = saved?.isDone == true,
                completedSets = saved?.completedSets ?: 0,
                totalSets = totalSets
            )
        }

        val currentFallback = WorkoutCatalog.planForDay(dayIndex)
        val currentDateIso = weekDates.getOrElse(dayIndex) { DateUtils.todayIso() }
        val currentPlannedItems = plannedByDay[dayIndex].orEmpty().mapNotNull { entity ->
            exerciseById[entity.exerciseId]?.let { ex ->
                PlannedDayExerciseItem(plannedId = entity.id, exercise = ex)
            }
        }

        val completedSetKeys = sets.filter { it.completed }.map { it.exerciseId to it.setIndex }.toSet()
        val totalSets = currentPlannedItems.sumOf { it.exercise.sets }
        val completedCount = currentPlannedItems.sumOf { item ->
            (0 until item.exercise.sets).count { setIdx -> (item.exercise.id to setIdx) in completedSetKeys }
        }
        val savedDay = dayByDate[currentDateIso]
        val isDone = savedDay?.isDone ?: false

        val cleanQuery = filters.query.trim().lowercase()
        val filtered = allLibrary.filter { ex ->
            val matchQuery = cleanQuery.isEmpty() || ex.name.lowercase().contains(cleanQuery)
            val matchMuscle = filters.muscle == null || ex.muscleGroup == filters.muscle
            val matchLevel = filters.level == null || ex.level == filters.level
            val matchEquip = filters.equipment == null || ex.equipment == filters.equipment
            matchQuery && matchMuscle && matchLevel && matchEquip
        }

        WorkoutUiState(
            selectedDayIndex = dayIndex,
            selectedDateIso = currentDateIso,
            weekSummaries = summaries,
            dayTitle = currentFallback.title,
            dayShortLabel = currentFallback.shortLabel,
            plannedExercises = currentPlannedItems,
            completedSetKeys = completedSetKeys,
            isSelectedDayDone = isDone,
            completedSetsCount = completedCount,
            totalSetsCount = totalSets,
            allLibraryExercises = allLibrary,
            filteredLibraryExercises = filtered,
            searchQuery = filters.query,
            selectedMuscleFilter = filters.muscle,
            selectedLevelFilter = filters.level,
            selectedEquipmentFilter = filters.equipment
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkoutUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureDefaultPlanSeeded()
        }
    }

    fun selectDay(dayIndex: Int) {
        selectedDayIndexFlow.value = dayIndex.coerceIn(0, 6)
    }

    fun toggleSet(exerciseId: String, setIndex: Int, completed: Boolean) {
        val dayIndex = selectedDayIndexFlow.value
        val dateIso = weekDates.getOrElse(dayIndex) { DateUtils.todayIso() }
        viewModelScope.launch {
            repository.toggleSet(dateIso, dayIndex, exerciseId, setIndex, completed)
        }
    }

    fun toggleSelectedDayDone() {
        val dayIndex = selectedDayIndexFlow.value
        val dateIso = weekDates.getOrElse(dayIndex) { DateUtils.todayIso() }
        viewModelScope.launch {
            repository.toggleDayDone(dateIso, dayIndex)
        }
    }

    fun updateSearchQuery(query: String) {
        searchQueryFlow.value = query
    }

    fun selectMuscleFilter(group: MuscleGroup?) {
        muscleFilterFlow.value = group
    }

    fun selectLevelFilter(level: ExerciseLevel?) {
        levelFilterFlow.value = level
    }

    fun selectEquipmentFilter(equipment: ExerciseEquipment?) {
        equipmentFilterFlow.value = equipment
    }

    fun addExerciseToDay(dayIndex: Int, exerciseId: String) {
        viewModelScope.launch {
            repository.addExerciseToDay(dayIndex, exerciseId)
        }
    }

    fun removePlannedExercise(plannedId: Long) {
        viewModelScope.launch {
            repository.removePlannedExercise(plannedId)
        }
    }

    fun createCustomExercise(
        name: String,
        muscleGroup: MuscleGroup,
        sets: Int,
        repsLabel: String,
        notes: String,
        addToSelectedDay: Boolean = true
    ) {
        viewModelScope.launch {
            val created = repository.createCustomExercise(name, muscleGroup, sets, repsLabel, notes)
            if (addToSelectedDay) {
                repository.addExerciseToDay(selectedDayIndexFlow.value, created.id)
            }
        }
    }

    fun deleteCustomExercise(exerciseId: String) {
        viewModelScope.launch {
            repository.deleteCustomExercise(exerciseId)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ParvHealthApplication
                WorkoutViewModel(
                    application = app,
                    repository = app.workoutRepository
                )
            }
        }
    }
}
