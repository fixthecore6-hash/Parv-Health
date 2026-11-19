package com.example.safety

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.example.data.FallSensitivity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

sealed interface MotionTriggerEvent {
    data class FallDetected(
        val peakG: Float,
        val sensitivity: FallSensitivity,
        val timestampMs: Long
    ) : MotionTriggerEvent

    data class ShakeDetected(
        val timestampMs: Long
    ) : MotionTriggerEvent
}

/**
 * Pure state machine for fall detection:
 * Flags a fall when total acceleration spikes >= sensitivity.spikeThresholdG
 * and then remains almost still (~1.0g +/- 0.38g) for 10 seconds.
 */
class FallDetectorEngine(
    private var sensitivity: FallSensitivity = FallSensitivity.MEDIUM
) {
    private var monitoringPostSpike = false
    private var spikeTimestampMs = 0L
    private var lastMovementTimestampMs = 0L
    private var peakG = 0f
    private var cooldownUntilMs = 0L

    fun setSensitivity(sensitivity: FallSensitivity) {
        this.sensitivity = sensitivity
    }

    fun getSensitivity(): FallSensitivity = sensitivity

    fun reset() {
        monitoringPostSpike = false
        spikeTimestampMs = 0L
        lastMovementTimestampMs = 0L
        peakG = 0f
    }

    /**
     * Feeds total acceleration magnitude in g units (Earth gravity at rest ~= 1.0f).
     * Returns the peak g-force if a fall + 10s stillness has been confirmed, or null otherwise.
     */
    fun onSample(totalG: Float, timestampMs: Long): Float? {
        if (timestampMs < cooldownUntilMs) return null

        if (!monitoringPostSpike) {
            if (totalG >= sensitivity.spikeThresholdG) {
                monitoringPostSpike = true
                spikeTimestampMs = timestampMs
                lastMovementTimestampMs = timestampMs
                peakG = totalG
            }
            return null
        }

        val elapsedFromSpike = timestampMs - spikeTimestampMs
        if (elapsedFromSpike < 0L) {
            reset()
            return null
        }

        val deviationFromRest = abs(totalG - 1.0f)
        if (elapsedFromSpike <= SETTLE_WINDOW_MS) {
            // Allow brief impact bounce while capturing peak g
            peakG = max(peakG, totalG)
            if (deviationFromRest > STILLNESS_TOLERANCE_G) {
                lastMovementTimestampMs = timestampMs
            }
            return null
        }

        if (deviationFromRest > STILLNESS_TOLERANCE_G) {
            // User is actively moving after the spike, so it was not an immobile fall
            reset()
            return null
        }

        val stillDuration = timestampMs - lastMovementTimestampMs
        if (stillDuration >= STILLNESS_DURATION_MS) {
            val capturedPeak = peakG
            reset()
            cooldownUntilMs = timestampMs + POST_TRIGGER_COOLDOWN_MS
            return capturedPeak
        }

        return null
    }

    companion object {
        const val SETTLE_WINDOW_MS = 600L
        const val STILLNESS_DURATION_MS = 10_000L
        const val STILLNESS_TOLERANCE_G = 0.38f
        const val POST_TRIGGER_COOLDOWN_MS = 5_000L
    }
}

/**
 * Pure state machine for Shake to Call:
 * Triggers when total acceleration crosses 2.0g at least 3 times within 2 seconds.
 */
class ShakeDetectorEngine {
    private val shakeTimestamps = ArrayDeque<Long>()
    private var lastShakeTimestampMs = Long.MIN_VALUE
    private var cooldownUntilMs = 0L

    fun reset() {
        shakeTimestamps.clear()
        lastShakeTimestampMs = Long.MIN_VALUE
    }

    fun onSample(totalG: Float, timestampMs: Long): Boolean {
        if (timestampMs < cooldownUntilMs) return false
        if (totalG < SHAKE_THRESHOLD_G) return false

        if (lastShakeTimestampMs != Long.MIN_VALUE &&
            timestampMs - lastShakeTimestampMs < MIN_SHAKE_GAP_MS
        ) {
            return false
        }

        lastShakeTimestampMs = timestampMs
        shakeTimestamps.addLast(timestampMs)

        while (shakeTimestamps.isNotEmpty() &&
            timestampMs - shakeTimestamps.first() > SHAKE_WINDOW_MS
        ) {
            shakeTimestamps.removeFirst()
        }

        if (shakeTimestamps.size >= REQUIRED_SHAKES) {
            reset()
            cooldownUntilMs = timestampMs + POST_SHAKE_COOLDOWN_MS
            return true
        }
        return false
    }

    companion object {
        const val SHAKE_THRESHOLD_G = 2.0f
        const val SHAKE_WINDOW_MS = 2_000L
        const val MIN_SHAKE_GAP_MS = 200L
        const val REQUIRED_SHAKES = 3
        const val POST_SHAKE_COOLDOWN_MS = 4_000L
    }
}

class SafetyMotionMonitor(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val fallEngine = FallDetectorEngine(FallSensitivity.MEDIUM)
    private val shakeEngine = ShakeDetectorEngine()

    private val _events = MutableSharedFlow<MotionTriggerEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<MotionTriggerEvent> = _events.asSharedFlow()

    private var fallEnabled = false
    private var shakeEnabled = false
    private var isListening = false
    private var alertOverlayActive = false

    fun hasAccelerometer(): Boolean = accelerometer != null

    @Synchronized
    fun updateConfig(
        fallDetectionEnabled: Boolean,
        sensitivity: FallSensitivity,
        shakeToCallEnabled: Boolean
    ) {
        val prevFall = fallEnabled
        val prevShake = shakeEnabled
        fallEnabled = fallDetectionEnabled
        shakeEnabled = shakeToCallEnabled
        fallEngine.setSensitivity(sensitivity)

        if (!fallEnabled && prevFall) {
            fallEngine.reset()
        }
        if (!shakeEnabled && prevShake) {
            shakeEngine.reset()
        }

        val shouldListen = (fallEnabled || shakeEnabled) && accelerometer != null
        if (shouldListen && !isListening) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
            isListening = true
        } else if (!shouldListen && isListening) {
            sensorManager.unregisterListener(this)
            isListening = false
            fallEngine.reset()
            shakeEngine.reset()
        }
    }

    @Synchronized
    fun setAlertOverlayActive(active: Boolean) {
        alertOverlayActive = active
        if (active) {
            fallEngine.reset()
            shakeEngine.reset()
        }
    }

    @Synchronized
    fun stop() {
        if (isListening) {
            sensorManager.unregisterListener(this)
            isListening = false
        }
        fallEngine.reset()
        shakeEngine.reset()
    }

    @Synchronized
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        if (alertOverlayActive) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val totalMs2 = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val gForce = totalMs2 / SensorManager.GRAVITY_EARTH
        val nowMs = System.currentTimeMillis()

        if (fallEnabled) {
            val peak = fallEngine.onSample(gForce, nowMs)
            if (peak != null) {
                _events.tryEmit(
                    MotionTriggerEvent.FallDetected(
                        peakG = peak,
                        sensitivity = fallEngine.getSensitivity(),
                        timestampMs = nowMs
                    )
                )
            }
        }

        if (shakeEnabled) {
            if (shakeEngine.onSample(gForce, nowMs)) {
                _events.tryEmit(
                    MotionTriggerEvent.ShakeDetected(timestampMs = nowMs)
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
