package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class WorkoutRepository(
    private val dao: WorkoutDao,
    private val settingsStore: SettingsStore? = null
) {
    private val mutex = Mutex()

    fun observeSetsForDate(date: String): Flow<List<WorkoutSetEntity>> = dao.observeSetsForDate(date)

    fun observeDaysForDates(dates: List<String>): Flow<List<WorkoutDayEntity>> =
        dao.observeDaysForDates(dates)

    fun observeWorkoutHistory(): Flow<List<WorkoutDayEntity>> = dao.observeWorkoutHistory()

    fun observeCustomExercises(): Flow<List<CustomExerciseEntity>> = dao.observeCustomExercises()

    fun observeAllPlannedExercises(): Flow<List<PlannedDayExerciseEntity>> =
        dao.observeAllPlannedExercises()

    suspend fun ensureDefaultPlanSeeded() = mutex.withLock {
        val alreadySeeded = settingsStore?.isWorkoutPlanSeeded() == true
        if (alreadySeeded) return@withLock

        if (dao.getAllPlannedExercises().isEmpty()) {
            val seedItems = mutableListOf<PlannedDayExerciseEntity>()
            ExerciseLibraryCatalog.defaultWeeklyScheduleMap.forEach { (dayIdx, ids) ->
                ids.forEachIndexed { order, exId ->
                    seedItems.add(
                        PlannedDayExerciseEntity(
                            dayOfWeekIndex = dayIdx,
                            exerciseId = exId,
                            sortOrder = order
                        )
                    )
                }
            }
            dao.insertPlannedExercises(seedItems)
        }
        settingsStore?.setWorkoutPlanSeeded(true)
    }

    suspend fun addExerciseToDay(dayOfWeekIndex: Int, exerciseId: String) = mutex.withLock {
        val safeDay = dayOfWeekIndex.coerceIn(0, 6)
        val existing = dao.getPlannedExercisesForDay(safeDay)
        if (existing.none { it.exerciseId == exerciseId }) {
            val nextOrder = (existing.maxOfOrNull { it.sortOrder } ?: -1) + 1
            dao.insertPlannedExercise(
                PlannedDayExerciseEntity(
                    dayOfWeekIndex = safeDay,
                    exerciseId = exerciseId,
                    sortOrder = nextOrder
                )
            )
            settingsStore?.setWorkoutPlanSeeded(true)
        }
    }

    suspend fun removePlannedExercise(plannedId: Long) = mutex.withLock {
        dao.deletePlannedExerciseById(plannedId)
    }

    suspend fun createCustomExercise(
        name: String,
        muscleGroup: MuscleGroup,
        sets: Int,
        repsLabel: String,
        notes: String
    ): LibraryExercise = mutex.withLock {
        val id = "custom_${UUID.randomUUID().toString().take(8)}"
        val safeSets = sets.coerceIn(1, 10)
        val safeReps = repsLabel.trim().ifEmpty { "10 reps" }
        val entity = CustomExerciseEntity(
            id = id,
            name = name.trim(),
            muscleGroup = muscleGroup.name,
            sets = safeSets,
            repsLabel = safeReps,
            notes = notes.trim()
        )
        dao.upsertCustomExercise(entity)
        entity.toLibraryExercise()
    }

    suspend fun deleteCustomExercise(id: String) = mutex.withLock {
        dao.removeExerciseFromAllDays(id)
        dao.deleteCustomExerciseById(id)
    }

    suspend fun toggleSet(
        date: String,
        dayOfWeekIndex: Int,
        exerciseId: String,
        setIndex: Int,
        completed: Boolean
    ) = mutex.withLock {
        dao.upsertSet(
            WorkoutSetEntity(
                date = date,
                exerciseId = exerciseId,
                setIndex = setIndex,
                completed = completed
            )
        )
        recomputeDayProgress(date, dayOfWeekIndex)
    }

    suspend fun toggleDayDone(date: String, dayOfWeekIndex: Int) = mutex.withLock {
        val totalSets = resolveTotalSetsForDay(dayOfWeekIndex)
        val existingDay = dao.getDay(date)
        val nextDone = !(existingDay?.isDone ?: false)
        val currentCompleted = dao.getSetsForDate(date).count { it.completed }
        dao.upsertDay(
            WorkoutDayEntity(
                date = date,
                dayOfWeekIndex = dayOfWeekIndex,
                isDone = nextDone,
                completedSets = currentCompleted,
                totalSets = totalSets,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private suspend fun resolveTotalSetsForDay(dayOfWeekIndex: Int): Int {
        val planned = dao.getPlannedExercisesForDay(dayOfWeekIndex)
        if (planned.isEmpty()) {
            return WorkoutCatalog.planForDay(dayOfWeekIndex).exercises.sumOf { it.sets }
        }
        val customById = dao.getAllCustomExercises().associateBy { it.id }
        return planned.sumOf { item ->
            ExerciseLibraryCatalog.findById(item.exerciseId)?.sets
                ?: customById[item.exerciseId]?.sets
                ?: 3
        }
    }

    private suspend fun recomputeDayProgress(date: String, dayOfWeekIndex: Int) {
        val totalSets = resolveTotalSetsForDay(dayOfWeekIndex)
        val completedSets = dao.getSetsForDate(date).count { it.completed }
        val existingDay = dao.getDay(date)
        val isDone = if (totalSets > 0 && completedSets >= totalSets) {
            true
        } else {
            existingDay?.isDone ?: false
        }
        dao.upsertDay(
            WorkoutDayEntity(
                date = date,
                dayOfWeekIndex = dayOfWeekIndex,
                isDone = isDone,
                completedSets = completedSets,
                totalSets = totalSets,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearAllWorkoutData(clearCustomAndPlan: Boolean = false) = mutex.withLock {
        dao.clearAllSets()
        dao.clearAllDays()
        if (clearCustomAndPlan) {
            dao.clearAllPlannedExercises()
            dao.clearAllCustomExercises()
            settingsStore?.setWorkoutPlanSeeded(false)
        }
    }
}

fun CustomExerciseEntity.toLibraryExercise(): LibraryExercise {
    val parsedGroup = runCatching { MuscleGroup.valueOf(muscleGroup) }.getOrDefault(MuscleGroup.FULL_BODY)
    val visual = when (parsedGroup) {
        MuscleGroup.CHEST -> ExerciseVisualType.CHEST_PRESS
        MuscleGroup.BACK -> ExerciseVisualType.BACK_ROW
        MuscleGroup.LEGS -> ExerciseVisualType.SQUATS
        MuscleGroup.CORE -> ExerciseVisualType.PLANK
        MuscleGroup.SHOULDERS -> ExerciseVisualType.SHOULDER_PRESS
        MuscleGroup.ARMS -> ExerciseVisualType.ARM_CURL
        MuscleGroup.FULL_BODY -> ExerciseVisualType.JUMPING_JACKS
        MuscleGroup.CARDIO -> ExerciseVisualType.CARDIO_RUN
    }
    val stepList = if (notes.isNotBlank()) {
        listOf(notes, "Complete $sets sets of $repsLabel at a steady pace.", "Rest briefly between sets and maintain smooth breathing.")
    } else {
        listOf(
            "Warm up lightly before beginning $name.",
            "Perform $sets sets of $repsLabel with controlled form.",
            "Stop right away if you feel sharp pain."
        )
    }
    return LibraryExercise(
        id = id,
        name = name,
        muscleGroup = parsedGroup,
        level = ExerciseLevel.BEGINNER,
        equipment = ExerciseEquipment.NONE,
        sets = sets,
        repsLabel = repsLabel,
        instructions = stepList,
        commonMistakes = listOf(
            "Rushing through reps without controlled posture.",
            "Holding your breath instead of breathing steadily."
        ),
        visualType = visual,
        isCustom = true,
        notes = notes
    )
}
