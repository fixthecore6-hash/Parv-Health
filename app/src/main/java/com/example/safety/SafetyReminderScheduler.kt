package com.example.safety

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.data.MedicationDoseLogEntity
import com.example.data.MedicationDoseStatus
import java.util.Calendar

object SafetyReminderScheduler {

    const val CHANNEL_WATER = "parv_water_reminders"
    const val CHANNEL_MEDICATIONS = "parv_medication_reminders"

    const val ACTION_WATER_REMINDER = "com.example.safety.ACTION_WATER_REMINDER"
    const val ACTION_MEDICATION_CHECK = "com.example.safety.ACTION_MEDICATION_CHECK"
    const val ACTION_MED_TAKEN = "com.example.safety.ACTION_MED_TAKEN"
    const val ACTION_MED_SNOOZE = "com.example.safety.ACTION_MED_SNOOZE"
    const val ACTION_MED_SKIP = "com.example.safety.ACTION_MED_SKIP"

    const val EXTRA_DOSE_LOG_ID = "extra_dose_log_id"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

    private const val WATER_ALARM_REQUEST_CODE = 4101
    private const val MED_ALARM_REQUEST_CODE = 4201
    private val WATER_HOURS = listOf(8, 10, 12, 14, 16, 18, 20)

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            val waterChannel = NotificationChannel(
                CHANNEL_WATER,
                "Water Intake Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminds you every 2 hours between 8:00 AM and 9:00 PM when your water goal is not yet reached."
            }
            val medChannel = NotificationChannel(
                CHANNEL_MEDICATIONS,
                "Medication Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when a scheduled medication dose is due."
            }
            manager.createNotificationChannel(waterChannel)
            manager.createNotificationChannel(medChannel)
        }
    }

    fun scheduleNextWaterReminder(context: Context, enabled: Boolean, nowMs: Long = System.currentTimeMillis()) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, SafetyReminderReceiver::class.java).apply {
            action = ACTION_WATER_REMINDER
        }
        val pi = PendingIntent.getBroadcast(
            context,
            WATER_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!enabled) {
            alarmManager.cancel(pi)
            return
        }

        ensureChannels(context)
        val nextTriggerMs = computeNextWaterReminderMillis(nowMs)
        scheduleInexactAlarm(alarmManager, nextTriggerMs, pi)
    }

    fun scheduleNextMedicationCheck(
        context: Context,
        enabled: Boolean,
        pendingOrSnoozedLogs: List<MedicationDoseLogEntity>,
        nowMs: Long = System.currentTimeMillis()
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, SafetyReminderReceiver::class.java).apply {
            action = ACTION_MEDICATION_CHECK
        }
        val pi = PendingIntent.getBroadcast(
            context,
            MED_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (!enabled) {
            alarmManager.cancel(pi)
            return
        }

        ensureChannels(context)

        val upcomingTriggerMs = pendingOrSnoozedLogs.mapNotNull { log ->
            val status = runCatching { MedicationDoseStatus.valueOf(log.status) }.getOrNull()
            when (status) {
                MedicationDoseStatus.PENDING -> log.scheduledTimestampMs
                MedicationDoseStatus.SNOOZED -> log.snoozedUntilMs ?: log.scheduledTimestampMs
                else -> null
            }
        }.filter { it > nowMs }.minOrNull()

        // Also ensure periodic fallback check every 20 min if there are pending doses
        val fallbackMs = nowMs + 20L * 60L * 1000L
        val targetMs = if (upcomingTriggerMs != null) {
            minOf(upcomingTriggerMs, fallbackMs)
        } else {
            nowMs + 60L * 60L * 1000L
        }
        scheduleInexactAlarm(alarmManager, targetMs, pi)
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        listOf(
            WATER_ALARM_REQUEST_CODE to ACTION_WATER_REMINDER,
            MED_ALARM_REQUEST_CODE to ACTION_MEDICATION_CHECK
        ).forEach { (code, action) ->
            val pi = PendingIntent.getBroadcast(
                context,
                code,
                Intent(context, SafetyReminderReceiver::class.java).apply { this.action = action },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pi)
        }
        val nm = context.getSystemService(NotificationManager::class.java)
        nm?.cancelAll()
    }

    fun computeNextWaterReminderMillis(nowMs: Long): Long {
        val nowCal = Calendar.getInstance().apply { timeInMillis = nowMs }
        for (hour in WATER_HOURS) {
            val slot = (nowCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (slot.timeInMillis > nowMs + 60_000L) {
                return slot.timeInMillis
            }
        }
        return (nowCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, WATER_HOURS.first())
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun scheduleInexactAlarm(alarmManager: AlarmManager, triggerAtMs: Long, pi: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pi)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMs, pi)
        }
    }
}
