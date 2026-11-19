package com.example

import android.app.Application
import com.example.data.DataBackupManager
import com.example.data.FoodRepository
import com.example.data.ParvHealthDatabase
import com.example.data.SafetyHealthRepository
import com.example.data.SettingsStore
import com.example.data.StepRepository
import com.example.data.WellnessRepository
import com.example.data.WorkoutRepository
import com.example.pedometer.StepSensorTracker
import com.example.safety.EmergencyLocationProvider
import com.example.safety.SafetyMotionMonitor

class ParvHealthApplication : Application() {

    val database: ParvHealthDatabase by lazy {
        ParvHealthDatabase.getInstance(this)
    }

    val settingsStore: SettingsStore by lazy {
        SettingsStore(this)
    }

    val repository: StepRepository by lazy {
        StepRepository(
            dao = database.pedometerDao(),
            settingsStore = settingsStore
        )
    }

    val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepository(
            dao = database.workoutDao(),
            settingsStore = settingsStore
        )
    }

    val foodRepository: FoodRepository by lazy {
        FoodRepository(
            dao = database.foodDao(),
            settingsStore = settingsStore
        )
    }

    val wellnessRepository: WellnessRepository by lazy {
        WellnessRepository(
            dao = database.wellnessDao(),
            settingsStore = settingsStore
        )
    }

    val safetyHealthRepository: SafetyHealthRepository by lazy {
        SafetyHealthRepository(
            dao = database.safetyHealthDao(),
            settingsStore = settingsStore
        )
    }

    val dataBackupManager: DataBackupManager by lazy {
        DataBackupManager(
            pedometerDao = database.pedometerDao(),
            workoutDao = database.workoutDao(),
            foodDao = database.foodDao(),
            wellnessDao = database.wellnessDao(),
            safetyHealthDao = database.safetyHealthDao(),
            settingsStore = settingsStore
        )
    }

    val sensorTracker: StepSensorTracker by lazy {
        StepSensorTracker(
            context = this,
            repository = repository
        )
    }

    val safetyMotionMonitor: SafetyMotionMonitor by lazy {
        SafetyMotionMonitor(context = this)
    }

    val emergencyLocationProvider: EmergencyLocationProvider by lazy {
        EmergencyLocationProvider(context = this)
    }
}
