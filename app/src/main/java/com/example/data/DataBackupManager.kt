package com.example.data

import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

data class BackupImportSummary(
    val stepsImported: Int,
    val workoutDaysImported: Int,
    val customExercisesImported: Int,
    val foodEntriesImported: Int,
    val customMealsImported: Int,
    val sleepEntriesImported: Int,
    val remindersImported: Int
)

class DataBackupManager(
    private val pedometerDao: PedometerDao,
    private val workoutDao: WorkoutDao,
    private val foodDao: FoodDao,
    private val wellnessDao: WellnessDao,
    private val safetyHealthDao: SafetyHealthDao,
    private val settingsStore: SettingsStore
) {

    suspend fun exportToJson(): String {
        val root = JSONObject()
        root.put("format", "parv-health-backup")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        // Settings & Profile
        val snapshot = settingsStore.getSnapshot()
        val profile = settingsStore.userProfile.first()
        val settingsObj = JSONObject().apply {
            put("dailyGoal", snapshot.dailyGoal)
            put("strideLengthCm", snapshot.strideLengthCm)
            put("themeMode", snapshot.themeMode.name)
            put("activityLevel", settingsStore.activityLevel.first().name)
            settingsStore.calorieTargetOverride.first()?.let { put("calorieTargetOverride", it) }
            put("dietType", settingsStore.dietType.first().name)
            put("proteinGoalG", settingsStore.proteinGoalG.first())
            put("fallDetectionEnabled", settingsStore.fallDetectionEnabled.first())
            put("fallSensitivity", settingsStore.fallSensitivity.first().name)
            put("shakeToCallEnabled", settingsStore.shakeToCallEnabled.first())
            put("waterRemindersEnabled", settingsStore.waterRemindersEnabled.first())
            put("medicationNotificationsEnabled", settingsStore.medicationNotificationsEnabled.first())
            settingsStore.waterGoalOverrideMl.first()?.let { put("waterGoalOverrideMl", it) }
        }
        root.put("settings", settingsObj)

        val profileObj = JSONObject().apply {
            put("name", profile.name)
            profile.age?.let { put("age", it) }
            put("sex", profile.sex.name)
            profile.weightValue?.let { put("weightValue", it.toDouble()) }
            put("weightUnit", profile.weightUnit.name)
            profile.heightValue?.let { put("heightValue", it.toDouble()) }
            put("heightUnit", profile.heightUnit.name)
            put("emergencyContact", profile.emergencyContact)
        }
        root.put("profile", profileObj)

        // Daily steps
        val stepsArr = JSONArray()
        pedometerDao.observeAllDays().first().forEach { day ->
            stepsArr.put(JSONObject().apply {
                put("date", day.date)
                put("steps", day.steps)
                put("goal", day.goal)
                put("activeSeconds", day.activeSeconds)
                put("strideCm", day.strideCm)
                put("updatedAt", day.updatedAt)
            })
        }
        root.put("dailySteps", stepsArr)

        // Workout days & sets
        val workoutDaysArr = JSONArray()
        workoutDao.getAllDays().forEach { day ->
            workoutDaysArr.put(JSONObject().apply {
                put("date", day.date)
                put("dayOfWeekIndex", day.dayOfWeekIndex)
                put("isDone", day.isDone)
                put("completedSets", day.completedSets)
                put("totalSets", day.totalSets)
                put("updatedAt", day.updatedAt)
            })
        }
        root.put("workoutDays", workoutDaysArr)

        val workoutSetsArr = JSONArray()
        workoutDao.getAllSets().forEach { set ->
            workoutSetsArr.put(JSONObject().apply {
                put("date", set.date)
                put("exerciseId", set.exerciseId)
                put("setIndex", set.setIndex)
                put("completed", set.completed)
            })
        }
        root.put("workoutSets", workoutSetsArr)

        val plannedArr = JSONArray()
        workoutDao.getAllPlannedExercises().forEach { item ->
            plannedArr.put(JSONObject().apply {
                put("dayOfWeekIndex", item.dayOfWeekIndex)
                put("exerciseId", item.exerciseId)
                put("sortOrder", item.sortOrder)
                put("addedAt", item.addedAt)
            })
        }
        root.put("plannedDayExercises", plannedArr)

        val customExArr = JSONArray()
        workoutDao.getAllCustomExercises().forEach { ex ->
            customExArr.put(JSONObject().apply {
                put("id", ex.id)
                put("name", ex.name)
                put("muscleGroup", ex.muscleGroup)
                put("sets", ex.sets)
                put("repsLabel", ex.repsLabel)
                put("notes", ex.notes)
                put("createdAt", ex.createdAt)
            })
        }
        root.put("customExercises", customExArr)

        // Food entries & custom meals
        val foodArr = JSONArray()
        foodDao.getAllEntries().forEach { entry ->
            foodArr.put(JSONObject().apply {
                put("date", entry.date)
                put("mealType", entry.mealType)
                put("name", entry.name)
                put("amount", entry.amount.toDouble())
                put("amountUnit", entry.amountUnit)
                put("calories", entry.calories)
                put("proteinG", entry.proteinG.toDouble())
                put("carbsG", entry.carbsG.toDouble())
                put("fatG", entry.fatG.toDouble())
                put("createdAt", entry.createdAt)
            })
        }
        root.put("foodEntries", foodArr)

        val customMealsArr = JSONArray()
        foodDao.getAllCustomMeals().forEach { meal ->
            customMealsArr.put(JSONObject().apply {
                put("name", meal.name)
                put("mealType", meal.mealType)
                put("calories", meal.calories)
                put("proteinG", meal.proteinG.toDouble())
                put("carbsG", meal.carbsG.toDouble())
                put("fatG", meal.fatG.toDouble())
                put("ingredients", meal.ingredients)
                put("isVegetarian", meal.isVegetarian)
                put("createdAt", meal.createdAt)
            })
        }
        root.put("customMeals", customMealsArr)

        // Sleep entries
        val sleepArr = JSONArray()
        wellnessDao.getAllSleepEntries().forEach { sleep ->
            sleepArr.put(JSONObject().apply {
                put("date", sleep.date)
                put("hoursSlept", sleep.hoursSlept.toDouble())
                put("note", sleep.note)
                put("updatedAt", sleep.updatedAt)
            })
        }
        root.put("sleepEntries", sleepArr)

        // Reminders
        val remindersArr = JSONArray()
        wellnessDao.getAllReminders().forEach { reminder ->
            remindersArr.put(JSONObject().apply {
                put("category", reminder.category)
                put("title", reminder.title)
                put("timeLabel", reminder.timeLabel)
                put("scheduleLabel", reminder.scheduleLabel)
                put("enabled", reminder.enabled)
                put("createdAt", reminder.createdAt)
            })
        }
        root.put("reminders", remindersArr)

        // Doctors
        val doctorsArr = JSONArray()
        safetyHealthDao.getAllDoctors().forEach { d ->
            doctorsArr.put(JSONObject().apply {
                put("name", d.name)
                put("phoneNumber", d.phoneNumber)
                put("clinicOrHospital", d.clinicOrHospital)
                put("specialty", d.specialty)
                put("isPrimary", d.isPrimary)
                put("createdAt", d.createdAt)
            })
        }
        root.put("doctors", doctorsArr)

        // Fall & Emergency alerts
        val fallArr = JSONArray()
        safetyHealthDao.getAllFallAlerts().forEach { a ->
            fallArr.put(JSONObject().apply {
                put("timestampMs", a.timestampMs)
                put("dateIso", a.dateIso)
                put("sensitivity", a.sensitivity)
                put("peakG", a.peakG.toDouble())
                put("outcome", a.outcome)
                put("alertType", a.alertType)
                a.latitude?.let { put("latitude", it) }
                a.longitude?.let { put("longitude", it) }
                a.accuracyMeters?.let { put("accuracyMeters", it.toDouble()) }
                put("locationSource", a.locationSource)
                a.locationTimestampMs?.let { put("locationTimestampMs", it) }
                put("locationLink", a.locationLink)
            })
        }
        root.put("fallAlerts", fallArr)

        // Water entries
        val waterArr = JSONArray()
        safetyHealthDao.getAllWaterEntries().forEach { w ->
            waterArr.put(JSONObject().apply {
                put("dateIso", w.dateIso)
                put("amountMl", w.amountMl)
                put("timestampMs", w.timestampMs)
            })
        }
        root.put("waterEntries", waterArr)

        // Medications
        val medsArr = JSONArray()
        safetyHealthDao.getAllMedications().forEach { m ->
            medsArr.put(JSONObject().apply {
                put("id", m.id)
                put("name", m.name)
                put("dose", m.dose)
                put("timesPerDay", m.timesPerDay)
                put("startDateIso", m.startDateIso)
                put("endDateIso", m.endDateIso)
                put("daysOfWeekCsv", m.daysOfWeekCsv)
                put("createdAt", m.createdAt)
            })
        }
        root.put("medications", medsArr)

        // Medication dose logs
        val doseArr = JSONArray()
        safetyHealthDao.getAllDoseLogs().forEach { l ->
            doseArr.put(JSONObject().apply {
                put("medicationId", l.medicationId)
                put("dateIso", l.dateIso)
                put("slotIndex", l.slotIndex)
                put("scheduledTimeLabel", l.scheduledTimeLabel)
                put("scheduledTimestampMs", l.scheduledTimestampMs)
                l.snoozedUntilMs?.let { put("snoozedUntilMs", it) }
                put("status", l.status)
                l.actedAtMs?.let { put("actedAtMs", it) }
            })
        }
        root.put("medicationDoseLogs", doseArr)

        return root.toString(2)
    }

    suspend fun exportToCsv(): String {
        val sb = StringBuilder()
        sb.appendLine("section,col1,col2,col3,col4,col5,col6,col7,col8,col9,col10")

        val profile = settingsStore.userProfile.first()
        val snapshot = settingsStore.getSnapshot()
        sb.appendLine(
            csvRow(
                "SETTINGS",
                snapshot.dailyGoal.toString(),
                snapshot.strideLengthCm.toString(),
                snapshot.themeMode.name,
                settingsStore.activityLevel.first().name,
                (settingsStore.calorieTargetOverride.first() ?: "").toString(),
                settingsStore.dietType.first().name,
                settingsStore.proteinGoalG.first().toString()
            )
        )
        sb.appendLine(
            csvRow(
                "PROFILE",
                profile.name,
                (profile.age ?: "").toString(),
                profile.sex.name,
                (profile.weightValue ?: "").toString(),
                profile.weightUnit.name,
                (profile.heightValue ?: "").toString(),
                profile.heightUnit.name,
                profile.emergencyContact
            )
        )

        pedometerDao.observeAllDays().first().forEach { d ->
            sb.appendLine(
                csvRow(
                    "STEPS",
                    d.date,
                    d.steps.toString(),
                    d.goal.toString(),
                    d.activeSeconds.toString(),
                    d.strideCm.toString(),
                    d.updatedAt.toString()
                )
            )
        }

        workoutDao.getAllDays().forEach { d ->
            sb.appendLine(
                csvRow(
                    "WORKOUT_DAY",
                    d.date,
                    d.dayOfWeekIndex.toString(),
                    d.isDone.toString(),
                    d.completedSets.toString(),
                    d.totalSets.toString(),
                    d.updatedAt.toString()
                )
            )
        }

        workoutDao.getAllSets().forEach { s ->
            sb.appendLine(
                csvRow(
                    "WORKOUT_SET",
                    s.date,
                    s.exerciseId,
                    s.setIndex.toString(),
                    s.completed.toString()
                )
            )
        }

        workoutDao.getAllPlannedExercises().forEach { p ->
            sb.appendLine(
                csvRow(
                    "PLANNED_EXERCISE",
                    p.dayOfWeekIndex.toString(),
                    p.exerciseId,
                    p.sortOrder.toString(),
                    p.addedAt.toString()
                )
            )
        }

        workoutDao.getAllCustomExercises().forEach { e ->
            sb.appendLine(
                csvRow(
                    "CUSTOM_EXERCISE",
                    e.id,
                    e.name,
                    e.muscleGroup,
                    e.sets.toString(),
                    e.repsLabel,
                    e.notes,
                    e.createdAt.toString()
                )
            )
        }

        foodDao.getAllEntries().forEach { f ->
            sb.appendLine(
                csvRow(
                    "FOOD_ENTRY",
                    f.date,
                    f.mealType,
                    f.name,
                    f.amount.toString(),
                    f.amountUnit,
                    f.calories.toString(),
                    f.proteinG.toString(),
                    f.carbsG.toString(),
                    f.fatG.toString(),
                    f.createdAt.toString()
                )
            )
        }

        foodDao.getAllCustomMeals().forEach { m ->
            sb.appendLine(
                csvRow(
                    "CUSTOM_MEAL",
                    m.name,
                    m.mealType,
                    m.calories.toString(),
                    m.proteinG.toString(),
                    m.carbsG.toString(),
                    m.fatG.toString(),
                    m.ingredients,
                    m.isVegetarian.toString(),
                    m.createdAt.toString()
                )
            )
        }

        wellnessDao.getAllSleepEntries().forEach { s ->
            sb.appendLine(
                csvRow(
                    "SLEEP",
                    s.date,
                    s.hoursSlept.toString(),
                    s.note,
                    s.updatedAt.toString()
                )
            )
        }

        wellnessDao.getAllReminders().forEach { r ->
            sb.appendLine(
                csvRow(
                    "REMINDER",
                    r.category,
                    r.title,
                    r.timeLabel,
                    r.scheduleLabel,
                    r.enabled.toString(),
                    r.createdAt.toString()
                )
            )
        }

        safetyHealthDao.getAllDoctors().forEach { d ->
            sb.appendLine(
                csvRow(
                    "DOCTOR",
                    d.name,
                    d.phoneNumber,
                    d.clinicOrHospital,
                    d.specialty,
                    d.isPrimary.toString(),
                    d.createdAt.toString()
                )
            )
        }

        safetyHealthDao.getAllFallAlerts().forEach { a ->
            sb.appendLine(
                csvRow(
                    "FALL_ALERT",
                    a.timestampMs.toString(),
                    a.dateIso,
                    a.sensitivity,
                    a.peakG.toString(),
                    a.outcome,
                    a.alertType,
                    a.locationSource,
                    a.locationLink,
                    (a.accuracyMeters ?: "").toString(),
                    "${a.latitude ?: ""}|${a.longitude ?: ""}|${a.locationTimestampMs ?: ""}"
                )
            )
        }

        safetyHealthDao.getAllWaterEntries().forEach { w ->
            sb.appendLine(
                csvRow(
                    "WATER_ENTRY",
                    w.dateIso,
                    w.amountMl.toString(),
                    w.timestampMs.toString()
                )
            )
        }

        safetyHealthDao.getAllMedications().forEach { m ->
            sb.appendLine(
                csvRow(
                    "MEDICATION",
                    m.id.toString(),
                    m.name,
                    m.dose,
                    m.timesPerDay.toString(),
                    m.startDateIso,
                    m.endDateIso,
                    m.daysOfWeekCsv,
                    m.createdAt.toString()
                )
            )
        }

        return sb.toString()
    }

    suspend fun importFromText(rawText: String): BackupImportSummary {
        val trimmed = rawText.trim()
        require(trimmed.isNotEmpty()) { "Empty backup file" }
        return if (trimmed.startsWith("{")) {
            importJson(trimmed)
        } else {
            importCsv(trimmed)
        }
    }

    private suspend fun importJson(jsonText: String): BackupImportSummary {
        val root = JSONObject(jsonText)

        root.optJSONObject("settings")?.let { s ->
            if (s.has("dailyGoal")) settingsStore.setDailyGoal(s.getInt("dailyGoal"))
            if (s.has("strideLengthCm")) settingsStore.setStrideLengthCm(s.getInt("strideLengthCm"))
            if (s.has("themeMode")) {
                runCatching { ThemeMode.valueOf(s.getString("themeMode")) }
                    .getOrNull()?.let { settingsStore.setThemeMode(it) }
            }
            if (s.has("activityLevel")) {
                runCatching { ActivityLevel.valueOf(s.getString("activityLevel")) }
                    .getOrNull()?.let { settingsStore.setActivityLevel(it) }
            }
            if (s.has("calorieTargetOverride")) {
                settingsStore.setCalorieTargetOverride(s.getInt("calorieTargetOverride"))
            }
            if (s.has("dietType")) {
                runCatching { DietType.valueOf(s.getString("dietType")) }
                    .getOrNull()?.let { settingsStore.setDietType(it, updateDefaultProtein = false) }
            }
            if (s.has("proteinGoalG")) {
                settingsStore.setProteinGoalG(s.getInt("proteinGoalG"))
            }
            if (s.has("fallDetectionEnabled")) {
                settingsStore.setFallDetectionEnabled(s.getBoolean("fallDetectionEnabled"))
            }
            if (s.has("fallSensitivity")) {
                runCatching { FallSensitivity.valueOf(s.getString("fallSensitivity")) }
                    .getOrNull()?.let { settingsStore.setFallSensitivity(it) }
            }
            if (s.has("shakeToCallEnabled")) {
                settingsStore.setShakeToCallEnabled(s.getBoolean("shakeToCallEnabled"))
            }
            if (s.has("waterRemindersEnabled")) {
                settingsStore.setWaterRemindersEnabled(s.getBoolean("waterRemindersEnabled"))
            }
            if (s.has("medicationNotificationsEnabled")) {
                settingsStore.setMedicationNotificationsEnabled(s.getBoolean("medicationNotificationsEnabled"))
            }
            if (s.has("waterGoalOverrideMl")) {
                settingsStore.setWaterGoalOverrideMl(s.getInt("waterGoalOverrideMl"))
            }
        }

        root.optJSONObject("profile")?.let { p ->
            settingsStore.saveProfile(
                UserProfile(
                    name = p.optString("name", ""),
                    age = if (p.has("age") && !p.isNull("age")) p.optInt("age") else null,
                    sex = runCatching { ProfileSex.valueOf(p.optString("sex", "UNSPECIFIED")) }
                        .getOrDefault(ProfileSex.UNSPECIFIED),
                    weightValue = if (p.has("weightValue") && !p.isNull("weightValue")) p.optDouble("weightValue").toFloat() else null,
                    weightUnit = runCatching { WeightUnit.valueOf(p.optString("weightUnit", "KG")) }
                        .getOrDefault(WeightUnit.KG),
                    heightValue = if (p.has("heightValue") && !p.isNull("heightValue")) p.optDouble("heightValue").toFloat() else null,
                    heightUnit = runCatching { HeightUnit.valueOf(p.optString("heightUnit", "CM")) }
                        .getOrDefault(HeightUnit.CM),
                    emergencyContact = p.optString("emergencyContact", "")
                )
            )
        }

        var stepsCount = 0
        root.optJSONArray("dailySteps")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                pedometerDao.upsertDay(
                    DailyStepsEntity(
                        date = o.getString("date"),
                        steps = o.optInt("steps", 0),
                        goal = o.optInt("goal", SettingsStore.DEFAULT_GOAL),
                        activeSeconds = o.optLong("activeSeconds", 0L),
                        strideCm = o.optInt("strideCm", SettingsStore.DEFAULT_STRIDE_CM),
                        updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
                stepsCount++
            }
        }

        var workoutDaysCount = 0
        root.optJSONArray("workoutDays")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                workoutDao.upsertDay(
                    WorkoutDayEntity(
                        date = o.getString("date"),
                        dayOfWeekIndex = o.optInt("dayOfWeekIndex", 0),
                        isDone = o.optBoolean("isDone", false),
                        completedSets = o.optInt("completedSets", 0),
                        totalSets = o.optInt("totalSets", 0),
                        updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
                workoutDaysCount++
            }
        }

        root.optJSONArray("workoutSets")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                workoutDao.upsertSet(
                    WorkoutSetEntity(
                        date = o.getString("date"),
                        exerciseId = o.getString("exerciseId"),
                        setIndex = o.optInt("setIndex", 0),
                        completed = o.optBoolean("completed", false)
                    )
                )
            }
        }

        root.optJSONArray("plannedDayExercises")?.let { arr ->
            if (arr.length() > 0) {
                workoutDao.clearAllPlannedExercises()
                val list = mutableListOf<PlannedDayExerciseEntity>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        PlannedDayExerciseEntity(
                            dayOfWeekIndex = o.optInt("dayOfWeekIndex", 0),
                            exerciseId = o.getString("exerciseId"),
                            sortOrder = o.optInt("sortOrder", i),
                            addedAt = o.optLong("addedAt", System.currentTimeMillis())
                        )
                    )
                }
                workoutDao.insertPlannedExercises(list)
                settingsStore.setWorkoutPlanSeeded(true)
            }
        }

        var customExCount = 0
        root.optJSONArray("customExercises")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                workoutDao.upsertCustomExercise(
                    CustomExerciseEntity(
                        id = o.getString("id"),
                        name = o.getString("name"),
                        muscleGroup = o.optString("muscleGroup", MuscleGroup.FULL_BODY.name),
                        sets = o.optInt("sets", 3),
                        repsLabel = o.optString("repsLabel", "10 reps"),
                        notes = o.optString("notes", ""),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
                customExCount++
            }
        }

        var foodCount = 0
        root.optJSONArray("foodEntries")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                foodDao.insertEntry(
                    FoodEntryEntity(
                        date = o.getString("date"),
                        mealType = o.optString("mealType", MealType.BREAKFAST.name),
                        name = o.getString("name"),
                        amount = o.optDouble("amount", 1.0).toFloat(),
                        amountUnit = o.optString("amountUnit", FoodAmountUnit.SERVINGS.name),
                        calories = o.optInt("calories", 0),
                        proteinG = o.optDouble("proteinG", 0.0).toFloat(),
                        carbsG = o.optDouble("carbsG", 0.0).toFloat(),
                        fatG = o.optDouble("fatG", 0.0).toFloat(),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
                foodCount++
            }
        }

        var customMealsCount = 0
        root.optJSONArray("customMeals")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                foodDao.insertCustomMeal(
                    CustomMealEntity(
                        name = o.getString("name"),
                        mealType = o.optString("mealType", MealType.LUNCH.name),
                        calories = o.optInt("calories", 0),
                        proteinG = o.optDouble("proteinG", 0.0).toFloat(),
                        carbsG = o.optDouble("carbsG", 0.0).toFloat(),
                        fatG = o.optDouble("fatG", 0.0).toFloat(),
                        ingredients = o.optString("ingredients", ""),
                        isVegetarian = o.optBoolean("isVegetarian", true),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
                customMealsCount++
            }
        }

        var sleepCount = 0
        root.optJSONArray("sleepEntries")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                wellnessDao.upsertSleep(
                    SleepEntryEntity(
                        date = o.getString("date"),
                        hoursSlept = o.optDouble("hoursSlept", 7.0).toFloat(),
                        note = o.optString("note", ""),
                        updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
                sleepCount++
            }
        }

        var remindersCount = 0
        root.optJSONArray("reminders")?.let { arr ->
            if (arr.length() > 0) {
                val list = mutableListOf<ReminderEntity>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        ReminderEntity(
                            category = o.optString("category", ReminderCategory.WATER.name),
                            title = o.getString("title"),
                            timeLabel = o.optString("timeLabel", "09:00 AM"),
                            scheduleLabel = o.optString("scheduleLabel", "Daily"),
                            enabled = o.optBoolean("enabled", true),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                    remindersCount++
                }
                wellnessDao.insertReminders(list)
                settingsStore.setRemindersSeeded(true)
            }
        }

        root.optJSONArray("doctors")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                safetyHealthDao.upsertDoctor(
                    DoctorEntity(
                        name = o.getString("name"),
                        phoneNumber = o.getString("phoneNumber"),
                        clinicOrHospital = o.optString("clinicOrHospital", ""),
                        specialty = o.optString("specialty", ""),
                        isPrimary = o.optBoolean("isPrimary", false),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        root.optJSONArray("fallAlerts")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                safetyHealthDao.insertFallAlert(
                    FallAlertEntity(
                        timestampMs = o.optLong("timestampMs", System.currentTimeMillis()),
                        dateIso = o.optString("dateIso", DateUtils.todayIso()),
                        sensitivity = o.optString("sensitivity", FallSensitivity.MEDIUM.name),
                        peakG = o.optDouble("peakG", 2.5).toFloat(),
                        outcome = o.optString("outcome", FallAlertOutcome.CANCELLED.name),
                        alertType = o.optString("alertType", EmergencyAlertType.FALL.name),
                        latitude = if (o.has("latitude") && !o.isNull("latitude")) o.optDouble("latitude") else null,
                        longitude = if (o.has("longitude") && !o.isNull("longitude")) o.optDouble("longitude") else null,
                        accuracyMeters = if (o.has("accuracyMeters") && !o.isNull("accuracyMeters")) o.optDouble("accuracyMeters").toFloat() else null,
                        locationSource = o.optString("locationSource", AlertLocationSource.UNAVAILABLE.name),
                        locationTimestampMs = if (o.has("locationTimestampMs") && !o.isNull("locationTimestampMs")) o.optLong("locationTimestampMs") else null,
                        locationLink = o.optString("locationLink", "")
                    )
                )
            }
        }

        root.optJSONArray("waterEntries")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                safetyHealthDao.insertWaterEntry(
                    WaterEntryEntity(
                        dateIso = o.optString("dateIso", DateUtils.todayIso()),
                        amountMl = o.optInt("amountMl", 250),
                        timestampMs = o.optLong("timestampMs", System.currentTimeMillis())
                    )
                )
            }
        }

        val medIdMap = mutableMapOf<Long, Long>()
        root.optJSONArray("medications")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val oldId = o.optLong("id", 0L)
                val newId = safetyHealthDao.upsertMedication(
                    MedicationEntity(
                        name = o.getString("name"),
                        dose = o.optString("dose", ""),
                        timesPerDay = o.optInt("timesPerDay", 1),
                        startDateIso = o.optString("startDateIso", DateUtils.todayIso()),
                        endDateIso = o.optString("endDateIso", ""),
                        daysOfWeekCsv = o.optString("daysOfWeekCsv", "0,1,2,3,4,5,6"),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
                if (oldId > 0L) medIdMap[oldId] = newId
            }
        }

        root.optJSONArray("medicationDoseLogs")?.let { arr ->
            val logs = mutableListOf<MedicationDoseLogEntity>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val mappedMedId = medIdMap[o.optLong("medicationId", 0L)] ?: continue
                logs.add(
                    MedicationDoseLogEntity(
                        medicationId = mappedMedId,
                        dateIso = o.getString("dateIso"),
                        slotIndex = o.optInt("slotIndex", 0),
                        scheduledTimeLabel = o.optString("scheduledTimeLabel", "8:00 AM"),
                        scheduledTimestampMs = o.optLong("scheduledTimestampMs", System.currentTimeMillis()),
                        snoozedUntilMs = if (o.has("snoozedUntilMs") && !o.isNull("snoozedUntilMs")) o.optLong("snoozedUntilMs") else null,
                        status = o.optString("status", MedicationDoseStatus.PENDING.name),
                        actedAtMs = if (o.has("actedAtMs") && !o.isNull("actedAtMs")) o.optLong("actedAtMs") else null
                    )
                )
            }
            if (logs.isNotEmpty()) safetyHealthDao.insertDoseLogs(logs)
        }

        return BackupImportSummary(
            stepsImported = stepsCount,
            workoutDaysImported = workoutDaysCount,
            customExercisesImported = customExCount,
            foodEntriesImported = foodCount,
            customMealsImported = customMealsCount,
            sleepEntriesImported = sleepCount,
            remindersImported = remindersCount
        )
    }

    private suspend fun importCsv(csvText: String): BackupImportSummary {
        var stepsCount = 0
        var workoutDaysCount = 0
        var customExCount = 0
        var foodCount = 0
        var customMealsCount = 0
        var sleepCount = 0
        var remindersCount = 0
        val plannedList = mutableListOf<PlannedDayExerciseEntity>()

        csvText.lineSequence()
            .map { it.trimEnd() }
            .filter { it.isNotBlank() && !it.startsWith("section,") }
            .forEach { line ->
                val cols = parseCsvLine(line)
                when (cols.getOrNull(0)) {
                    "SETTINGS" -> {
                        cols.getOrNull(1)?.toIntOrNull()?.let { settingsStore.setDailyGoal(it) }
                        cols.getOrNull(2)?.toIntOrNull()?.let { settingsStore.setStrideLengthCm(it) }
                        cols.getOrNull(3)?.let { raw ->
                            runCatching { ThemeMode.valueOf(raw) }.getOrNull()?.let { settingsStore.setThemeMode(it) }
                        }
                        cols.getOrNull(4)?.let { raw ->
                            runCatching { ActivityLevel.valueOf(raw) }.getOrNull()?.let { settingsStore.setActivityLevel(it) }
                        }
                        cols.getOrNull(5)?.toIntOrNull()?.let { settingsStore.setCalorieTargetOverride(it) }
                        cols.getOrNull(6)?.let { raw ->
                            runCatching { DietType.valueOf(raw) }.getOrNull()?.let { settingsStore.setDietType(it, false) }
                        }
                        cols.getOrNull(7)?.toIntOrNull()?.let { settingsStore.setProteinGoalG(it) }
                    }
                    "PROFILE" -> {
                        settingsStore.saveProfile(
                            UserProfile(
                                name = cols.getOrNull(1) ?: "",
                                age = cols.getOrNull(2)?.toIntOrNull(),
                                sex = runCatching { ProfileSex.valueOf(cols.getOrNull(3) ?: "UNSPECIFIED") }
                                    .getOrDefault(ProfileSex.UNSPECIFIED),
                                weightValue = cols.getOrNull(4)?.toFloatOrNull(),
                                weightUnit = runCatching { WeightUnit.valueOf(cols.getOrNull(5) ?: "KG") }
                                    .getOrDefault(WeightUnit.KG),
                                heightValue = cols.getOrNull(6)?.toFloatOrNull(),
                                heightUnit = runCatching { HeightUnit.valueOf(cols.getOrNull(7) ?: "CM") }
                                    .getOrDefault(HeightUnit.CM),
                                emergencyContact = cols.getOrNull(8) ?: ""
                            )
                        )
                    }
                    "STEPS" -> {
                        val date = cols.getOrNull(1) ?: return@forEach
                        pedometerDao.upsertDay(
                            DailyStepsEntity(
                                date = date,
                                steps = cols.getOrNull(2)?.toIntOrNull() ?: 0,
                                goal = cols.getOrNull(3)?.toIntOrNull() ?: SettingsStore.DEFAULT_GOAL,
                                activeSeconds = cols.getOrNull(4)?.toLongOrNull() ?: 0L,
                                strideCm = cols.getOrNull(5)?.toIntOrNull() ?: SettingsStore.DEFAULT_STRIDE_CM,
                                updatedAt = cols.getOrNull(6)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        )
                        stepsCount++
                    }
                    "WORKOUT_DAY" -> {
                        val date = cols.getOrNull(1) ?: return@forEach
                        workoutDao.upsertDay(
                            WorkoutDayEntity(
                                date = date,
                                dayOfWeekIndex = cols.getOrNull(2)?.toIntOrNull() ?: 0,
                                isDone = cols.getOrNull(3)?.toBooleanStrictOrNull() ?: false,
                                completedSets = cols.getOrNull(4)?.toIntOrNull() ?: 0,
                                totalSets = cols.getOrNull(5)?.toIntOrNull() ?: 0,
                                updatedAt = cols.getOrNull(6)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        )
                        workoutDaysCount++
                    }
                    "WORKOUT_SET" -> {
                        val date = cols.getOrNull(1) ?: return@forEach
                        val exId = cols.getOrNull(2) ?: return@forEach
                        workoutDao.upsertSet(
                            WorkoutSetEntity(
                                date = date,
                                exerciseId = exId,
                                setIndex = cols.getOrNull(3)?.toIntOrNull() ?: 0,
                                completed = cols.getOrNull(4)?.toBooleanStrictOrNull() ?: false
                            )
                        )
                    }
                    "PLANNED_EXERCISE" -> {
                        val exId = cols.getOrNull(2) ?: return@forEach
                        plannedList.add(
                            PlannedDayExerciseEntity(
                                dayOfWeekIndex = cols.getOrNull(1)?.toIntOrNull() ?: 0,
                                exerciseId = exId,
                                sortOrder = cols.getOrNull(3)?.toIntOrNull() ?: 0,
                                addedAt = cols.getOrNull(4)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        )
                    }
                    "CUSTOM_EXERCISE" -> {
                        val id = cols.getOrNull(1) ?: return@forEach
                        val name = cols.getOrNull(2) ?: return@forEach
                        workoutDao.upsertCustomExercise(
                            CustomExerciseEntity(
                                id = id,
                                name = name,
                                muscleGroup = cols.getOrNull(3) ?: MuscleGroup.FULL_BODY.name,
                                sets = cols.getOrNull(4)?.toIntOrNull() ?: 3,
                                repsLabel = cols.getOrNull(5) ?: "10 reps",
                                notes = cols.getOrNull(6) ?: "",
                                createdAt = cols.getOrNull(7)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        )
                        customExCount++
                    }
                    "FOOD_ENTRY" -> {
                        val date = cols.getOrNull(1) ?: return@forEach
                        val name = cols.getOrNull(3) ?: return@forEach
                        foodDao.insertEntry(
                            FoodEntryEntity(
                                date = date,
                                mealType = cols.getOrNull(2) ?: MealType.BREAKFAST.name,
                                name = name,
                                amount = cols.getOrNull(4)?.toFloatOrNull() ?: 1f,
                                amountUnit = cols.getOrNull(5) ?: FoodAmountUnit.SERVINGS.name,
                                calories = cols.getOrNull(6)?.toIntOrNull() ?: 0,
                                proteinG = cols.getOrNull(7)?.toFloatOrNull() ?: 0f,
                                carbsG = cols.getOrNull(8)?.toFloatOrNull() ?: 0f,
                                fatG = cols.getOrNull(9)?.toFloatOrNull() ?: 0f,
                                createdAt = cols.getOrNull(10)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        )
                        foodCount++
                    }
                    "CUSTOM_MEAL" -> {
                        val name = cols.getOrNull(1) ?: return@forEach
                        foodDao.insertCustomMeal(
                            CustomMealEntity(
                                name = name,
                                mealType = cols.getOrNull(2) ?: MealType.LUNCH.name,
                                calories = cols.getOrNull(3)?.toIntOrNull() ?: 0,
                                proteinG = cols.getOrNull(4)?.toFloatOrNull() ?: 0f,
                                carbsG = cols.getOrNull(5)?.toFloatOrNull() ?: 0f,
                                fatG = cols.getOrNull(6)?.toFloatOrNull() ?: 0f,
                                ingredients = cols.getOrNull(7) ?: "",
                                isVegetarian = cols.getOrNull(8)?.toBooleanStrictOrNull() ?: true,
                                createdAt = cols.getOrNull(9)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        )
                        customMealsCount++
                    }
                    "SLEEP" -> {
                        val date = cols.getOrNull(1) ?: return@forEach
                        wellnessDao.upsertSleep(
                            SleepEntryEntity(
                                date = date,
                                hoursSlept = cols.getOrNull(2)?.toFloatOrNull() ?: 7f,
                                note = cols.getOrNull(3) ?: "",
                                updatedAt = cols.getOrNull(4)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        )
                        sleepCount++
                    }
                    "REMINDER" -> {
                        val title = cols.getOrNull(2) ?: return@forEach
                        wellnessDao.upsertReminder(
                            ReminderEntity(
                                category = cols.getOrNull(1) ?: ReminderCategory.WATER.name,
                                title = title,
                                timeLabel = cols.getOrNull(3) ?: "09:00 AM",
                                scheduleLabel = cols.getOrNull(4) ?: "Daily",
                                enabled = cols.getOrNull(5)?.toBooleanStrictOrNull() ?: true,
                                createdAt = cols.getOrNull(6)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        )
                        remindersCount++
                    }
                    "DOCTOR" -> {
                        val name = cols.getOrNull(1) ?: return@forEach
                        val phone = cols.getOrNull(2) ?: return@forEach
                        safetyHealthDao.upsertDoctor(
                            DoctorEntity(
                                name = name,
                                phoneNumber = phone,
                                clinicOrHospital = cols.getOrNull(3) ?: "",
                                specialty = cols.getOrNull(4) ?: "",
                                isPrimary = cols.getOrNull(5)?.toBooleanStrictOrNull() ?: false,
                                createdAt = cols.getOrNull(6)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        )
                    }
                    "FALL_ALERT" -> {
                        val latLonTime = cols.getOrNull(10)?.split("|").orEmpty()
                        safetyHealthDao.insertFallAlert(
                            FallAlertEntity(
                                timestampMs = cols.getOrNull(1)?.toLongOrNull() ?: System.currentTimeMillis(),
                                dateIso = cols.getOrNull(2) ?: DateUtils.todayIso(),
                                sensitivity = cols.getOrNull(3) ?: FallSensitivity.MEDIUM.name,
                                peakG = cols.getOrNull(4)?.toFloatOrNull() ?: 2.5f,
                                outcome = cols.getOrNull(5) ?: FallAlertOutcome.CANCELLED.name,
                                alertType = cols.getOrNull(6)?.takeIf { it.isNotBlank() } ?: EmergencyAlertType.FALL.name,
                                locationSource = cols.getOrNull(7)?.takeIf { it.isNotBlank() } ?: AlertLocationSource.UNAVAILABLE.name,
                                locationLink = cols.getOrNull(8) ?: "",
                                accuracyMeters = cols.getOrNull(9)?.toFloatOrNull(),
                                latitude = latLonTime.getOrNull(0)?.toDoubleOrNull(),
                                longitude = latLonTime.getOrNull(1)?.toDoubleOrNull(),
                                locationTimestampMs = latLonTime.getOrNull(2)?.toLongOrNull()
                            )
                        )
                    }
                    "WATER_ENTRY" -> {
                        val dateIso = cols.getOrNull(1) ?: return@forEach
                        safetyHealthDao.insertWaterEntry(
                            WaterEntryEntity(
                                dateIso = dateIso,
                                amountMl = cols.getOrNull(2)?.toIntOrNull() ?: 250,
                                timestampMs = cols.getOrNull(3)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        )
                    }
                    "MEDICATION" -> {
                        val name = cols.getOrNull(2) ?: return@forEach
                        safetyHealthDao.upsertMedication(
                            MedicationEntity(
                                name = name,
                                dose = cols.getOrNull(3) ?: "",
                                timesPerDay = cols.getOrNull(4)?.toIntOrNull() ?: 1,
                                startDateIso = cols.getOrNull(5) ?: DateUtils.todayIso(),
                                endDateIso = cols.getOrNull(6) ?: "",
                                daysOfWeekCsv = cols.getOrNull(7) ?: "0,1,2,3,4,5,6",
                                createdAt = cols.getOrNull(8)?.toLongOrNull() ?: System.currentTimeMillis()
                            )
                        )
                    }
                }
            }

        if (plannedList.isNotEmpty()) {
            workoutDao.clearAllPlannedExercises()
            workoutDao.insertPlannedExercises(plannedList)
            settingsStore.setWorkoutPlanSeeded(true)
        }
        if (remindersCount > 0) {
            settingsStore.setRemindersSeeded(true)
        }

        return BackupImportSummary(
            stepsImported = stepsCount,
            workoutDaysImported = workoutDaysCount,
            customExercisesImported = customExCount,
            foodEntriesImported = foodCount,
            customMealsImported = customMealsCount,
            sleepEntriesImported = sleepCount,
            remindersImported = remindersCount
        )
    }

    private fun csvRow(vararg fields: String): String {
        val padded = fields.toMutableList()
        while (padded.size < 11) padded.add("")
        return padded.take(11).joinToString(",") { escapeCsv(it) }
    }

    private fun escapeCsv(value: String): String {
        val clean = value.replace("\r", " ").replace("\n", " ")
        return if (clean.contains(',') || clean.contains('"')) {
            "\"" + clean.replace("\"", "\"\"") + "\""
        } else {
            clean
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                } else {
                    current.append(c)
                }
            } else {
                when (c) {
                    '"' -> inQuotes = true
                    ',' -> {
                        result.add(current.toString())
                        current.clear()
                    }
                    else -> current.append(c)
                }
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}
