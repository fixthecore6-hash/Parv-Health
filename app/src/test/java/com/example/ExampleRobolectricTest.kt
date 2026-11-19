package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.ActivityLevel
import com.example.data.CustomMealEntity
import com.example.data.DataBackupManager
import com.example.data.DateUtils
import com.example.data.DietType
import com.example.data.DoctorEntity
import com.example.data.ExerciseEquipment
import com.example.data.ExerciseLevel
import com.example.data.ExerciseLibraryCatalog
import com.example.data.FallAlertOutcome
import com.example.data.FallSensitivity
import com.example.data.FoodAmountUnit
import com.example.data.FoodCatalog
import com.example.data.FoodEntryEntity
import com.example.data.FoodRepository
import com.example.data.FoodSuggestionCatalog
import com.example.data.HeightUnit
import com.example.data.MealType
import com.example.data.MedicationDoseStatus
import com.example.data.MedicationEntity
import com.example.data.MuscleGroup
import com.example.data.ParvHealthDatabase
import com.example.data.ProfileSex
import com.example.data.ReminderCategory
import com.example.data.SafetyHealthRepository
import com.example.data.SettingsStore
import com.example.data.StepRepository
import com.example.data.UserProfile
import com.example.data.WeightUnit
import com.example.data.WellnessRepository
import com.example.data.WorkoutCatalog
import com.example.data.WorkoutRepository
import com.example.safety.FallDetectorEngine
import com.example.safety.SafetyReminderScheduler
import com.example.safety.ShakeDetectorEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: ParvHealthDatabase
    private lateinit var settingsStore: SettingsStore
    private lateinit var repository: StepRepository
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var foodRepository: FoodRepository
    private lateinit var wellnessRepository: WellnessRepository
    private lateinit var safetyHealthRepository: SafetyHealthRepository
    private lateinit var dataBackupManager: DataBackupManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, ParvHealthDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        settingsStore = SettingsStore(context)
        repository = StepRepository(db.pedometerDao(), settingsStore)
        workoutRepository = WorkoutRepository(db.workoutDao(), settingsStore)
        foodRepository = FoodRepository(db.foodDao(), settingsStore)
        wellnessRepository = WellnessRepository(db.wellnessDao(), settingsStore)
        safetyHealthRepository = SafetyHealthRepository(db.safetyHealthDao(), settingsStore)
        dataBackupManager = DataBackupManager(
            pedometerDao = db.pedometerDao(),
            workoutDao = db.workoutDao(),
            foodDao = db.foodDao(),
            wellnessDao = db.wellnessDao(),
            safetyHealthDao = db.safetyHealthDao(),
            settingsStore = settingsStore
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `read app name from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("parv-health", appName)
    }

    @Test
    fun `hardware step counter tracks deltas and handles reboot`() = runTest {
        val today = DateUtils.todayIso()
        repository.ensureTodayInitialized()

        repository.recordHardwareCounterReading(1000f)
        assertEquals(0, repository.observeToday(today).first().steps)

        repository.recordHardwareCounterReading(1250f)
        assertEquals(250, repository.observeToday(today).first().steps)

        repository.recordHardwareCounterReading(45f)
        assertEquals(295, repository.observeToday(today).first().steps)

        repository.recordSingleStep()
        assertEquals(296, repository.observeToday(today).first().steps)

        repository.clearLocalHistory()
        assertEquals(0, repository.observeToday(today).first().steps)
    }

    @Test
    fun `theme mode preference persists locally`() = runTest {
        assertEquals(com.example.data.ThemeMode.SYSTEM, repository.themeMode.first())
        repository.setThemeMode(com.example.data.ThemeMode.BRIGHT)
        assertEquals(com.example.data.ThemeMode.BRIGHT, repository.themeMode.first())
        repository.setThemeMode(com.example.data.ThemeMode.DARK)
        assertEquals(com.example.data.ThemeMode.DARK, repository.themeMode.first())
    }

    @Test
    fun `profile persists locally and computes BMI accurately across units`() = runTest {
        val metricProfile = UserProfile(
            name = "Parv",
            age = 26,
            weightValue = 70f,
            weightUnit = WeightUnit.KG,
            heightValue = 175f,
            heightUnit = HeightUnit.CM,
            emergencyContact = "+15550199"
        )
        repository.saveProfile(metricProfile)
        val loaded = repository.userProfile.first()
        assertEquals("Parv", loaded.name)
        assertEquals(26, loaded.age)
        assertEquals("+15550199", loaded.emergencyContact)
        assertEquals(22.86f, loaded.bmi!!, 0.1f)
        assertEquals(com.example.data.BmiCategory.HEALTHY, loaded.bmiCategory)

        val imperialProfile = UserProfile(
            name = "Parv",
            age = 26,
            weightValue = 180f,
            weightUnit = WeightUnit.LB,
            heightValue = 5.8f,
            heightUnit = HeightUnit.FT
        )
        assertEquals(26.1f, imperialProfile.bmi!!, 0.3f)
        assertEquals(com.example.data.BmiCategory.OVERWEIGHT, imperialProfile.bmiCategory)

        repository.clearProfile()
        val cleared = repository.userProfile.first()
        assertEquals("", cleared.name)
        assertEquals("", cleared.emergencyContact)
        assertEquals(null, cleared.bmi)
    }

    @Test
    fun `exercise library contains at least 60 original exercises with full coverage`() {
        val exercises = ExerciseLibraryCatalog.builtInExercises
        assertTrue(exercises.size >= 60)

        MuscleGroup.entries.forEach { group ->
            assertTrue("Missing muscle group $group", exercises.any { it.muscleGroup == group })
        }
        ExerciseLevel.entries.forEach { level ->
            assertTrue("Missing level $level", exercises.any { it.level == level })
        }
        ExerciseEquipment.entries.forEach { equipment ->
            assertTrue("Missing equipment $equipment", exercises.any { it.equipment == equipment })
        }
        exercises.forEach { ex ->
            assertTrue("${ex.id} must have 3-5 steps", ex.instructions.size in 3..5)
            assertTrue("${ex.id} must have 2-3 mistakes", ex.commonMistakes.size in 2..3)
        }
    }

    @Test
    fun `workout sets day completion custom exercises and planner work locally`() = runTest {
        val today = DateUtils.todayIso()
        workoutRepository.ensureDefaultPlanSeeded()
        val mondayPlanned = workoutRepository.observeAllPlannedExercises().first().filter { it.dayOfWeekIndex == 0 }
        assertTrue(mondayPlanned.isNotEmpty())

        val firstExerciseId = mondayPlanned.first().exerciseId
        workoutRepository.toggleSet(today, 0, firstExerciseId, 0, true)
        val sets = workoutRepository.observeSetsForDate(today).first()
        assertEquals(1, sets.count { it.completed })

        val historyAfterOneSet = workoutRepository.observeWorkoutHistory().first()
        assertEquals(1, historyAfterOneSet.size)
        assertEquals(1, historyAfterOneSet.first().completedSets)
        assertFalse(historyAfterOneSet.first().isDone)

        workoutRepository.toggleDayDone(today, 0)
        assertTrue(workoutRepository.observeWorkoutHistory().first().first().isDone)

        // Create custom exercise and add to Tuesday (day 1)
        val custom = workoutRepository.createCustomExercise(
            name = "Tempo Goblet Squat",
            muscleGroup = MuscleGroup.LEGS,
            sets = 4,
            repsLabel = "10 reps",
            notes = "Pause 2 seconds at bottom"
        )
        workoutRepository.addExerciseToDay(1, custom.id)
        val customList = workoutRepository.observeCustomExercises().first()
        assertEquals(1, customList.size)
        assertEquals("Tempo Goblet Squat", customList.first().name)
        assertTrue(workoutRepository.observeAllPlannedExercises().first().any {
            it.dayOfWeekIndex == 1 && it.exerciseId == custom.id
        })

        // Delete custom exercise removes both custom exercise & planned day link
        workoutRepository.deleteCustomExercise(custom.id)
        assertTrue(workoutRepository.observeCustomExercises().first().isEmpty())
        assertFalse(workoutRepository.observeAllPlannedExercises().first().any { it.exerciseId == custom.id })

        // Clearing workout history preserves planned schedule unless clearCustomAndPlan = true
        workoutRepository.clearAllWorkoutData(clearCustomAndPlan = false)
        assertTrue(workoutRepository.observeSetsForDate(today).first().isEmpty())
        assertTrue(workoutRepository.observeAllPlannedExercises().first().isNotEmpty())
    }

    @Test
    fun `food entries calorie target suggestions and custom meals work accurately`() = runTest {
        assertNull(
            FoodCatalog.calculateMifflinStJeorTarget(
                UserProfile(name = "Parv"),
                ActivityLevel.MODERATE
            )
        )

        val completeProfile = UserProfile(
            age = 28,
            sex = ProfileSex.MALE,
            weightValue = 70f,
            weightUnit = WeightUnit.KG,
            heightValue = 175f,
            heightUnit = HeightUnit.CM
        )
        val suggested = FoodCatalog.calculateMifflinStJeorTarget(completeProfile, ActivityLevel.MODERATE)
        assertNotNull(suggested)
        assertEquals(2571, suggested!!)

        // Food suggestions cover veg & non-veg and filter accurately
        val vegSuggestions = FoodSuggestionCatalog.filterSuggestions(
            dietType = DietType.VEGETARIAN,
            mealType = null,
            remainingCalories = 400,
            onlyFitRemaining = true
        )
        assertTrue(vegSuggestions.isNotEmpty())
        assertTrue(vegSuggestions.all { it.isVegetarian && it.calories <= 400 })

        val highProteinSuggestions = FoodSuggestionCatalog.filterSuggestions(
            dietType = DietType.HIGH_PROTEIN,
            mealType = MealType.DINNER,
            remainingCalories = null,
            onlyFitRemaining = false
        )
        assertTrue(highProteinSuggestions.isNotEmpty())
        assertTrue(highProteinSuggestions.all {
            it.mealType == MealType.DINNER && (it.isHighProtein || it.proteinG >= 15f)
        })

        // Save, edit, custom meal, and clear food entries
        val today = DateUtils.todayIso()
        foodRepository.saveEntry(
            FoodEntryEntity(
                date = today,
                mealType = MealType.BREAKFAST.name,
                name = "Oats",
                amount = 60f,
                amountUnit = FoodAmountUnit.GRAMS.name,
                calories = 233,
                proteinG = 10.1f,
                carbsG = 39.8f,
                fatG = 4.1f
            )
        )
        val savedEntries = foodRepository.observeEntriesForDate(today).first()
        assertEquals(1, savedEntries.size)
        foodRepository.saveEntry(savedEntries.first().copy(calories = 250))
        assertEquals(250, foodRepository.observeEntriesForDate(today).first().first().calories)

        foodRepository.saveCustomMeal(
            CustomMealEntity(
                name = "Chickpea Protein Salad",
                mealType = MealType.LUNCH.name,
                calories = 410,
                proteinG = 22f,
                carbsG = 48f,
                fatG = 12f,
                ingredients = "Chickpeas, cucumber, olive oil",
                isVegetarian = true
            )
        )
        assertEquals(1, foodRepository.observeCustomMeals().first().size)

        foodRepository.setDietType(DietType.HIGH_PROTEIN)
        assertEquals(DietType.HIGH_PROTEIN, foodRepository.dietType.first())

        foodRepository.clearAllFoodData()
        assertTrue(foodRepository.observeEntriesForDate(today).first().isEmpty())
        assertTrue(foodRepository.observeCustomMeals().first().isEmpty())
        assertEquals(DietType.BALANCED, foodRepository.dietType.first())
    }

    @Test
    fun `sleep reminders and json csv export import round trip work locally`() = runTest {
        val today = DateUtils.todayIso()
        repository.ensureTodayInitialized()
        repository.recordSingleStep()
        repository.saveProfile(
            UserProfile(
                name = "Parv",
                age = 27,
                sex = ProfileSex.MALE,
                weightValue = 72f,
                heightValue = 176f
            )
        )
        workoutRepository.createCustomExercise("Kettlebell Carry", MuscleGroup.CORE, 3, "40 m", "Keep ribs down")
        foodRepository.saveCustomMeal(
            CustomMealEntity(
                name = "Paneer Bowl",
                mealType = MealType.DINNER.name,
                calories = 480,
                proteinG = 28f,
                carbsG = 42f,
                fatG = 18f,
                ingredients = "Paneer, spinach, rice",
                isVegetarian = true
            )
        )
        wellnessRepository.saveSleep(today, 7.5f, "Slept soundly")
        wellnessRepository.addReminder(ReminderCategory.WATER, "Morning Water", "08:00 AM", "Daily")

        val jsonBackup = dataBackupManager.exportToJson()
        val csvBackup = dataBackupManager.exportToCsv()
        assertTrue(jsonBackup.contains("Kettlebell Carry"))
        assertTrue(jsonBackup.contains("Paneer Bowl"))
        assertTrue(csvBackup.contains("SLEEP"))
        assertTrue(csvBackup.contains("REMINDER"))

        // Wipe everything, then restore from JSON
        repository.clearLocalHistory()
        repository.clearProfile()
        workoutRepository.clearAllWorkoutData(clearCustomAndPlan = true)
        foodRepository.clearAllFoodData()
        wellnessRepository.clearAllWellnessData()
        assertTrue(workoutRepository.observeCustomExercises().first().isEmpty())
        assertNull(wellnessRepository.observeSleepForDate(today).first())

        val jsonSummary = dataBackupManager.importFromText(jsonBackup)
        assertEquals(1, jsonSummary.customExercisesImported)
        assertEquals(1, jsonSummary.customMealsImported)
        assertEquals(1, jsonSummary.sleepEntriesImported)
        assertEquals(1, jsonSummary.remindersImported)
        assertEquals("Parv", repository.userProfile.first().name)
        assertEquals(7.5f, wellnessRepository.observeSleepForDate(today).first()!!.hoursSlept, 0.01f)

        // Wipe again and restore from CSV
        wellnessRepository.clearAllWellnessData()
        foodRepository.clearAllFoodData()
        val csvSummary = dataBackupManager.importFromText(csvBackup)
        assertEquals(1, csvSummary.sleepEntriesImported)
        assertEquals(1, csvSummary.customMealsImported)
        assertEquals(1, csvSummary.remindersImported)
        assertEquals("Slept soundly", wellnessRepository.observeSleepForDate(today).first()!!.note)
    }

    @Test
    fun `fall detector requires spike above sensitivity threshold and 10s stillness`() {
        val mediumEngine = FallDetectorEngine(FallSensitivity.MEDIUM)
        // Below 2.5g should not arm
        assertNull(mediumEngine.onSample(2.2f, 0L))
        assertNull(mediumEngine.onSample(1.0f, 11_000L))

        // Spike at 2.8g then movement at 3s cancels
        assertNull(mediumEngine.onSample(2.8f, 20_000L))
        assertNull(mediumEngine.onSample(1.0f, 22_000L))
        assertNull(mediumEngine.onSample(1.9f, 23_000L))
        assertNull(mediumEngine.onSample(1.0f, 35_000L))

        // Spike at 2.7g then 10s stillness triggers
        assertNull(mediumEngine.onSample(2.7f, 40_000L))
        assertNull(mediumEngine.onSample(1.02f, 45_000L))
        val triggered = mediumEngine.onSample(0.98f, 50_000L)
        assertNotNull(triggered)
        assertEquals(2.7f, triggered!!, 0.01f)

        // Low sensitivity ignores 2.7g, triggers on 3.4g
        val lowEngine = FallDetectorEngine(FallSensitivity.LOW)
        assertNull(lowEngine.onSample(2.7f, 0L))
        assertNull(lowEngine.onSample(1.0f, 12_000L))
        assertNull(lowEngine.onSample(3.4f, 20_000L))
        assertNotNull(lowEngine.onSample(1.0f, 30_000L))
    }

    @Test
    fun `shake detector triggers only on 3 firm shakes within 2 seconds`() {
        val engine = ShakeDetectorEngine()
        // Shakes too far apart (>2s window) should not trigger
        assertFalse(engine.onSample(2.3f, 0L))
        assertFalse(engine.onSample(2.3f, 1_500L))
        assertFalse(engine.onSample(2.3f, 2_500L)) // 0ms pruned cleanly

        // 3 shakes within 2s trigger
        val fresh = ShakeDetectorEngine()
        assertFalse(fresh.onSample(2.2f, 1_000L))
        assertFalse(fresh.onSample(2.4f, 1_450L))
        assertTrue(fresh.onSample(2.1f, 2_100L))
    }

    @Test
    fun `doctors water medications and fall alerts work with auto-missed and defaults`() = runTest {
        // Off by default checks
        assertFalse(settingsStore.fallDetectionEnabled.first())
        assertFalse(settingsStore.shakeToCallEnabled.first())
        assertFalse(settingsStore.waterRemindersEnabled.first())

        // Doctors + Primary toggle
        safetyHealthRepository.saveDoctor(
            DoctorEntity(name = "Dr. Rao", phoneNumber = "555-0101", clinicOrHospital = "City Care", isPrimary = true)
        )
        safetyHealthRepository.saveDoctor(
            DoctorEntity(name = "Dr. Lee", phoneNumber = "555-0102", specialty = "Cardiology", isPrimary = true)
        )
        val doctors = safetyHealthRepository.observeDoctors().first()
        assertEquals(2, doctors.size)
        assertEquals(1, doctors.count { it.isPrimary })
        assertEquals("Dr. Lee", doctors.first { it.isPrimary }.name)

        // Water suggested 35ml * kg, quick-add, undo, delete
        val profile = UserProfile(weightValue = 70f, weightUnit = WeightUnit.KG)
        assertEquals(2450, SafetyHealthRepository.suggestedWaterGoalMl(profile))
        assertEquals(2000, SafetyHealthRepository.suggestedWaterGoalMl(UserProfile()))

        val today = DateUtils.todayIso()
        safetyHealthRepository.addWaterIntake(250, today)
        safetyHealthRepository.addWaterIntake(500, today)
        assertEquals(750, safetyHealthRepository.observeWaterEntriesForDate(today).first().sumOf { it.amountMl })
        assertTrue(safetyHealthRepository.undoLastWaterEntry(today))
        assertEquals(250, safetyHealthRepository.observeWaterEntriesForDate(today).first().sumOf { it.amountMl })

        // Water scheduler stays strictly between 8:00 AM and 9:00 PM
        val ninePmCal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 21)
            set(java.util.Calendar.MINUTE, 15)
        }
        val nextWaterMs = SafetyReminderScheduler.computeNextWaterReminderMillis(ninePmCal.timeInMillis)
        val nextCal = java.util.Calendar.getInstance().apply { timeInMillis = nextWaterMs }
        assertEquals(8, nextCal.get(java.util.Calendar.HOUR_OF_DAY))

        // Medications: add, snooze 10m, taken, skip, 60-min auto-Missed & weekly percentage
        val yesterday = DateUtils.daysAgoIso(1)
        safetyHealthRepository.saveMedication(
            MedicationEntity(
                name = "Vitamin D",
                dose = "1000 IU",
                timesPerDay = 2,
                startDateIso = yesterday,
                createdAt = System.currentTimeMillis() - 86_400_000L
            ),
            todayIso = today
        )
        val logs = safetyHealthRepository.observeDoseLogsForDates(listOf(today)).first()
        assertEquals(2, logs.size)

        val firstSlot = logs.first()
        safetyHealthRepository.snoozeDoseTenMinutes(firstSlot, nowMs = firstSlot.scheduledTimestampMs + 5_000L)
        val snoozed = safetyHealthRepository.observeDoseLogsForDates(listOf(today)).first().first { it.id == firstSlot.id }
        assertEquals(MedicationDoseStatus.SNOOZED.name, snoozed.status)
        assertNotNull(snoozed.snoozedUntilMs)

        // 60 minutes after snooze expires -> auto-Missed
        safetyHealthRepository.evaluateExpiredDoses(
            nowMs = snoozed.snoozedUntilMs!! + SafetyHealthRepository.MISSED_GRACE_WINDOW_MS + 1_000L
        )
        val secondSlot = logs[1]
        safetyHealthRepository.markDoseTaken(secondSlot)

        val evaluatedLogs = safetyHealthRepository.observeDoseLogsForDates(listOf(today)).first()
        val stats = SafetyHealthRepository.computeWeeklyStats(evaluatedLogs)
        assertEquals(1, stats.takenCount)
        assertEquals(1, stats.missedCount)
        assertEquals(50, stats.weeklyPercentage)

        // Fall alert logging
        val alertId = safetyHealthRepository.logFallAlert(FallSensitivity.MEDIUM, 2.9f, FallAlertOutcome.NO_RESPONSE)
        safetyHealthRepository.updateFallAlertOutcome(alertId, FallAlertOutcome.CANCELLED)
        assertEquals(FallAlertOutcome.CANCELLED.name, safetyHealthRepository.observeFallAlerts().first().first().outcome)
    }

    @Test
    fun `emergency location resolver follows fresh 10s then 30m last-known then unavailable`() = runTest {
        val now = 1_750_000_000_000L

        // 1. Fresh fix succeeds within timeout
        val freshResult = com.example.safety.EmergencyLocationProvider.resolveLocationWithFallback(
            hasPermission = true,
            isLocationEnabled = true,
            nowMs = now,
            timeoutMs = 250L,
            fetchFresh = {
                com.example.safety.LocationFix(
                    latitude = 28.6139,
                    longitude = 77.2090,
                    accuracyMeters = 11.4f,
                    timeMs = now
                )
            },
            getLastKnown = { null }
        )
        assertEquals(com.example.data.AlertLocationSource.FRESH, freshResult.source)
        assertEquals("https://maps.google.com/?q=28.613900,77.209000", freshResult.mapsLink)
        assertEquals("±11 m", freshResult.formattedAccuracy())

        // 2. Fresh times out -> falls back to last known < 30 minutes old
        val lastKnownValid = com.example.safety.EmergencyLocationProvider.resolveLocationWithFallback(
            hasPermission = true,
            isLocationEnabled = true,
            nowMs = now,
            timeoutMs = 30L,
            fetchFresh = {
                kotlinx.coroutines.delay(100L)
                null
            },
            getLastKnown = {
                com.example.safety.LocationFix(
                    latitude = 19.0760,
                    longitude = 72.8777,
                    accuracyMeters = 24.8f,
                    timeMs = now - (15L * 60L * 1000L)
                )
            }
        )
        assertEquals(com.example.data.AlertLocationSource.LAST_KNOWN, lastKnownValid.source)
        assertEquals("https://maps.google.com/?q=19.076000,72.877700", lastKnownValid.mapsLink)
        assertEquals("±25 m", lastKnownValid.formattedAccuracy())

        // 3. Fresh fails and last known is >= 30 minutes old -> Unavailable + SMS says "Location unavailable."
        val staleLastKnown = com.example.safety.EmergencyLocationProvider.resolveLocationWithFallback(
            hasPermission = true,
            isLocationEnabled = true,
            nowMs = now,
            timeoutMs = 20L,
            fetchFresh = { null },
            getLastKnown = {
                com.example.safety.LocationFix(
                    latitude = 19.0760,
                    longitude = 72.8777,
                    accuracyMeters = 30f,
                    timeMs = now - (31L * 60L * 1000L)
                )
            }
        )
        assertEquals(com.example.data.AlertLocationSource.UNAVAILABLE, staleLastKnown.source)
        assertFalse(staleLastKnown.isAvailable)
        val unavailableSms = com.example.safety.EmergencyLocationProvider.buildEmergencySmsBody(
            alertLabel = "SOS",
            timestampMs = now,
            location = staleLastKnown
        )
        assertTrue(unavailableSms.contains("Location unavailable."))

        // 4. SMS body with valid location contains exact Maps link and accuracy
        val freshSms = com.example.safety.EmergencyLocationProvider.buildEmergencySmsBody(
            alertLabel = "Fall",
            timestampMs = now,
            location = freshResult
        )
        assertTrue(freshSms.contains("https://maps.google.com/?q=28.613900,77.209000"))
        assertTrue(freshSms.contains("±11 m"))

        // 5. Location embedded in alert entry persists and clears cleanly without removing alert entry
        val sosAlertId = safetyHealthRepository.logFallAlert(
            sensitivity = FallSensitivity.MEDIUM,
            peakG = 0f,
            outcome = FallAlertOutcome.NO_RESPONSE,
            alertType = com.example.data.EmergencyAlertType.SOS,
            timestampMs = now
        )
        safetyHealthRepository.updateFallAlertLocation(
            id = sosAlertId,
            latitude = 28.6139,
            longitude = 77.2090,
            accuracyMeters = 11.4f,
            locationSource = com.example.data.AlertLocationSource.FRESH,
            locationTimestampMs = now,
            locationLink = "https://maps.google.com/?q=28.613900,77.209000"
        )
        val savedAlert = safetyHealthRepository.observeFallAlerts().first().first { it.id == sosAlertId }
        assertEquals(com.example.data.EmergencyAlertType.SOS.name, savedAlert.alertType)
        assertEquals("https://maps.google.com/?q=28.613900,77.209000", savedAlert.locationLink)
        assertEquals(com.example.data.AlertLocationSource.FRESH.name, savedAlert.locationSource)

        safetyHealthRepository.clearLocationData()
        val clearedLocAlert = safetyHealthRepository.observeFallAlerts().first().first { it.id == sosAlertId }
        assertEquals("", clearedLocAlert.locationLink)
        assertNull(clearedLocAlert.latitude)
        assertEquals(com.example.data.AlertLocationSource.UNAVAILABLE.name, clearedLocAlert.locationSource)
    }

    @Test
    fun `location strings and manifest permissions match policy requirements`() {
        assertEquals(
            "Parv Health uses your location only when an emergency alert is sent, so your emergency contact can find you. It is never tracked in the background.",
            context.getString(R.string.location_permission_explanation)
        )
        assertEquals(
            "Location is used only when you trigger an alert. Your location is never stored in the background or shared with anyone except the contacts you choose.",
            context.getString(R.string.location_privacy_note)
        )
    }
}
