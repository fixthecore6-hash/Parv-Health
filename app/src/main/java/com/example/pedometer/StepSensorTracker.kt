package com.example.pedometer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.data.StepRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class StepSensorTracker(
    private val context: Context,
    private val repository: StepRepository
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val stepDetectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private val accelerometerSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    val sensorMode: SensorMode = when {
        stepCounterSensor != null -> SensorMode.HARDWARE_COUNTER
        stepDetectorSensor != null -> SensorMode.HARDWARE_DETECTOR
        accelerometerSensor != null -> SensorMode.ACCELEROMETER_FALLBACK
        else -> SensorMode.UNAVAILABLE
    }

    // Accelerometer fallback state (only used when hardware step sensors are absent)
    private val gravity = FloatArray(3)
    private var gravityInitialized = false
    private var lastStepTimestampMs = 0L
    private var aboveThreshold = false

    fun hasRequiredPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    @Synchronized
    fun start(): Boolean {
        if (_isListening.value) return true
        val activeSensor = when (sensorMode) {
            SensorMode.HARDWARE_COUNTER -> {
                if (!hasRequiredPermission()) return false
                stepCounterSensor
            }
            SensorMode.HARDWARE_DETECTOR -> {
                if (!hasRequiredPermission()) return false
                stepDetectorSensor
            }
            SensorMode.ACCELEROMETER_FALLBACK -> accelerometerSensor
            SensorMode.UNAVAILABLE -> null
        } ?: return false

        val delay = if (sensorMode == SensorMode.ACCELEROMETER_FALLBACK) {
            SensorManager.SENSOR_DELAY_GAME
        } else {
            SensorManager.SENSOR_DELAY_UI
        }

        val registered = sensorManager.registerListener(this, activeSensor, delay, 0)
        _isListening.value = registered
        return registered
    }

    @Synchronized
    fun stop() {
        if (!_isListening.value) return
        sensorManager.unregisterListener(this)
        _isListening.value = false
        gravityInitialized = false
        aboveThreshold = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val totalSinceBoot = event.values.firstOrNull() ?: return
                scope.launch {
                    repository.recordHardwareCounterReading(totalSinceBoot)
                }
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                scope.launch {
                    repository.recordSingleStep()
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                processAccelerometerSample(event.values)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }

    private fun processAccelerometerSample(values: FloatArray) {
        if (values.size < 3) return
        val alpha = 0.85f
        if (!gravityInitialized) {
            gravity[0] = values[0]
            gravity[1] = values[1]
            gravity[2] = values[2]
            gravityInitialized = true
            return
        }

        gravity[0] = alpha * gravity[0] + (1f - alpha) * values[0]
        gravity[1] = alpha * gravity[1] + (1f - alpha) * values[1]
        gravity[2] = alpha * gravity[2] + (1f - alpha) * values[2]

        val lx = values[0] - gravity[0]
        val ly = values[1] - gravity[1]
        val lz = values[2] - gravity[2]
        val linearMagnitude = sqrt(lx * lx + ly * ly + lz * lz)

        val nowMs = System.currentTimeMillis()
        if (!aboveThreshold && linearMagnitude > ACCEL_STEP_THRESHOLD) {
            aboveThreshold = true
            if (nowMs - lastStepTimestampMs >= MIN_STEP_INTERVAL_MS) {
                lastStepTimestampMs = nowMs
                scope.launch {
                    repository.recordSingleStep()
                }
            }
        } else if (aboveThreshold && linearMagnitude < ACCEL_RESET_THRESHOLD) {
            aboveThreshold = false
        }
    }

    companion object {
        private const val ACCEL_STEP_THRESHOLD = 2.6f
        private const val ACCEL_RESET_THRESHOLD = 1.1f
        private const val MIN_STEP_INTERVAL_MS = 300L
    }
}
