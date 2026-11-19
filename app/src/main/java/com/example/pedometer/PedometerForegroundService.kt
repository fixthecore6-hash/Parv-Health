package com.example.pedometer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.ParvHealthApplication
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.text.NumberFormat

class PedometerForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var observerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = application as ParvHealthApplication
        if (intent?.action == ACTION_PAUSE) {
            serviceScope.launch {
                app.repository.setTrackingEnabled(false)
                app.sensorTracker.stop()
                stopSelf()
            }
            return START_NOT_STICKY
        }

        if (!app.sensorTracker.hasRequiredPermission()) {
            stopSelf()
            return START_NOT_STICKY
        }

        val initialNotification = buildNotification(steps = 0, goal = 8000)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    initialNotification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
                )
            } else {
                startForeground(NOTIFICATION_ID, initialNotification)
            }
        } catch (_: SecurityException) {
            stopSelf()
            return START_NOT_STICKY
        }

        app.sensorTracker.start()
        serviceScope.launch {
            kotlinx.coroutines.flow.combine(
                app.settingsStore.fallDetectionEnabled,
                app.settingsStore.fallSensitivity,
                app.settingsStore.shakeToCallEnabled
            ) { fallEnabled, sensitivity, shakeEnabled ->
                Triple(fallEnabled, sensitivity, shakeEnabled)
            }.collect { (fallEnabled, sensitivity, shakeEnabled) ->
                app.safetyMotionMonitor.updateConfig(fallEnabled, sensitivity, shakeEnabled)
            }
        }
        startObservingSteps(app)
        return START_STICKY
    }

    private fun startObservingSteps(app: ParvHealthApplication) {
        observerJob?.cancel()
        observerJob = serviceScope.launch {
            app.repository.observeToday()
                .distinctUntilChanged()
                .collect { today ->
                    val manager = getSystemService(NotificationManager::class.java)
                    manager?.notify(
                        NOTIFICATION_ID,
                        buildNotification(today.steps, today.goal)
                    )
                }
        }
    }

    private fun buildNotification(steps: Int, goal: Int): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, PedometerForegroundService::class.java).apply {
                action = ACTION_PAUSE
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formatter = NumberFormat.getIntegerInstance()
        val contentText = getString(
            R.string.notification_steps_text,
            formatter.format(steps),
            formatter.format(goal)
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_step)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(contentText)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setContentIntent(openIntent)
            .addAction(0, getString(R.string.notification_action_pause), pauseIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        observerJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "pedometer_tracking"
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_PAUSE = "com.example.pedometer.ACTION_PAUSE"

        fun start(context: Context) {
            val intent = Intent(context, PedometerForegroundService::class.java)
            runCatching {
                ContextCompat.startForegroundService(context, intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, PedometerForegroundService::class.java)
            context.stopService(intent)
        }
    }
}
