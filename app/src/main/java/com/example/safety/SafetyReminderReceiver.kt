package com.example.safety

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.ParvHealthApplication
import com.example.R
import com.example.data.DateUtils
import com.example.data.MedicationDoseStatus
import com.example.data.SafetyHealthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class SafetyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val app = context.applicationContext as? ParvHealthApplication ?: return
        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                SafetyReminderScheduler.ensureChannels(context)
                when (action) {
                    SafetyReminderScheduler.ACTION_WATER_REMINDER -> handleWaterReminder(context, app)
                    SafetyReminderScheduler.ACTION_MEDICATION_CHECK -> handleMedicationCheck(context, app)
                    SafetyReminderScheduler.ACTION_MED_TAKEN -> handleDoseAction(context, app, intent, MedicationDoseStatus.TAKEN)
                    SafetyReminderScheduler.ACTION_MED_SNOOZE -> handleDoseAction(context, app, intent, MedicationDoseStatus.SNOOZED)
                    SafetyReminderScheduler.ACTION_MED_SKIP -> handleDoseAction(context, app, intent, MedicationDoseStatus.SKIPPED)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleWaterReminder(context: Context, app: ParvHealthApplication) {
        val enabled = app.settingsStore.waterRemindersEnabled.first()
        if (!enabled) {
            SafetyReminderScheduler.scheduleNextWaterReminder(context, enabled = false)
            return
        }

        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val withinWindow = hour in 8..20 // 8:00 AM to 9:00 PM

        if (withinWindow && SafetyReminderScheduler.hasNotificationPermission(context)) {
            val todayIso = DateUtils.todayIso()
            val profile = app.settingsStore.userProfile.first()
            val overrideGoal = app.settingsStore.waterGoalOverrideMl.first()
            val effectiveGoal = overrideGoal ?: SafetyHealthRepository.suggestedWaterGoalMl(profile)
            val entries = app.safetyHealthRepository.observeWaterEntriesForDate(todayIso).first()
            val totalToday = entries.sumOf { it.amountMl }

            if (totalToday < effectiveGoal) {
                val remaining = effectiveGoal - totalToday
                val openIntent = PendingIntent.getActivity(
                    context,
                    WATER_NOTIFICATION_ID,
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val notification = NotificationCompat.Builder(context, SafetyReminderScheduler.CHANNEL_WATER)
                    .setSmallIcon(R.drawable.ic_notification_step)
                    .setContentTitle(context.getString(R.string.water_notif_title))
                    .setContentText(context.getString(R.string.water_notif_body, remaining))
                    .setAutoCancel(true)
                    .setContentIntent(openIntent)
                    .build()

                val nm = context.getSystemService(NotificationManager::class.java)
                nm?.notify(WATER_NOTIFICATION_ID, notification)
            }
        }

        SafetyReminderScheduler.scheduleNextWaterReminder(context, enabled = true)
    }

    private suspend fun handleMedicationCheck(context: Context, app: ParvHealthApplication) {
        val nowMs = System.currentTimeMillis()
        val todayIso = DateUtils.todayIso()
        app.safetyHealthRepository.syncTodayDoseScheduleAndMissed(todayIso, nowMs)

        val enabled = app.settingsStore.medicationNotificationsEnabled.first()
        val todayLogs = app.safetyHealthRepository.observeDoseLogsForDates(listOf(todayIso)).first()
        if (!enabled) {
            SafetyReminderScheduler.scheduleNextMedicationCheck(context, enabled = false, emptyList())
            return
        }

        if (SafetyReminderScheduler.hasNotificationPermission(context)) {
            val medsById = app.safetyHealthRepository.observeMedications().first().associateBy { it.id }
            val nm = context.getSystemService(NotificationManager::class.java)
            todayLogs.forEach { log ->
                val status = runCatching { MedicationDoseStatus.valueOf(log.status) }.getOrNull()
                val dueAt = when (status) {
                    MedicationDoseStatus.PENDING -> log.scheduledTimestampMs
                    MedicationDoseStatus.SNOOZED -> log.snoozedUntilMs ?: log.scheduledTimestampMs
                    else -> Long.MAX_VALUE
                }
                if (nowMs >= dueAt && nowMs - dueAt < SafetyHealthRepository.MISSED_GRACE_WINDOW_MS) {
                    val med = medsById[log.medicationId] ?: return@forEach
                    val notifId = MED_NOTIFICATION_BASE + (log.id % 1000).toInt()
                    val openIntent = PendingIntent.getActivity(
                        context,
                        notifId,
                        Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        },
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val takenPi = actionPendingIntent(context, SafetyReminderScheduler.ACTION_MED_TAKEN, log.id, notifId, 1)
                    val snoozePi = actionPendingIntent(context, SafetyReminderScheduler.ACTION_MED_SNOOZE, log.id, notifId, 2)
                    val skipPi = actionPendingIntent(context, SafetyReminderScheduler.ACTION_MED_SKIP, log.id, notifId, 3)

                    val bodyText = if (med.dose.isNotBlank()) {
                        "${med.dose} • ${log.scheduledTimeLabel}"
                    } else {
                        log.scheduledTimeLabel
                    }

                    val notification = NotificationCompat.Builder(context, SafetyReminderScheduler.CHANNEL_MEDICATIONS)
                        .setSmallIcon(R.drawable.ic_notification_step)
                        .setContentTitle(med.name)
                        .setContentText(bodyText)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(openIntent)
                        .addAction(0, context.getString(R.string.med_action_taken), takenPi)
                        .addAction(0, context.getString(R.string.med_action_snooze), snoozePi)
                        .addAction(0, context.getString(R.string.med_action_skip), skipPi)
                        .build()

                    nm?.notify(notifId, notification)
                }
            }
        }

        val activeLogs = todayLogs.filter {
            it.status == MedicationDoseStatus.PENDING.name || it.status == MedicationDoseStatus.SNOOZED.name
        }
        SafetyReminderScheduler.scheduleNextMedicationCheck(context, enabled = true, pendingOrSnoozedLogs = activeLogs)
    }

    private suspend fun handleDoseAction(
        context: Context,
        app: ParvHealthApplication,
        intent: Intent,
        targetAction: MedicationDoseStatus
    ) {
        val logId = intent.getLongExtra(SafetyReminderScheduler.EXTRA_DOSE_LOG_ID, -1L)
        val notifId = intent.getIntExtra(SafetyReminderScheduler.EXTRA_NOTIFICATION_ID, -1)
        if (notifId > 0) {
            val nm = context.getSystemService(NotificationManager::class.java)
            nm?.cancel(notifId)
        }
        if (logId <= 0L) return

        val todayIso = DateUtils.todayIso()
        val logs = app.safetyHealthRepository.observeDoseLogsForDates(listOf(todayIso)).first()
        val target = logs.firstOrNull { it.id == logId } ?: return
        when (targetAction) {
            MedicationDoseStatus.TAKEN -> app.safetyHealthRepository.markDoseTaken(target)
            MedicationDoseStatus.SNOOZED -> app.safetyHealthRepository.snoozeDoseTenMinutes(target)
            MedicationDoseStatus.SKIPPED -> app.safetyHealthRepository.skipDose(target)
            else -> Unit
        }
        val enabled = app.settingsStore.medicationNotificationsEnabled.first()
        val refreshed = app.safetyHealthRepository.observeDoseLogsForDates(listOf(todayIso)).first().filter {
            it.status == MedicationDoseStatus.PENDING.name || it.status == MedicationDoseStatus.SNOOZED.name
        }
        SafetyReminderScheduler.scheduleNextMedicationCheck(context, enabled, refreshed)
    }

    private fun actionPendingIntent(
        context: Context,
        action: String,
        doseLogId: Long,
        notifId: Int,
        salt: Int
    ): PendingIntent {
        val intent = Intent(context, SafetyReminderReceiver::class.java).apply {
            this.action = action
            putExtra(SafetyReminderScheduler.EXTRA_DOSE_LOG_ID, doseLogId)
            putExtra(SafetyReminderScheduler.EXTRA_NOTIFICATION_ID, notifId)
        }
        return PendingIntent.getBroadcast(
            context,
            (doseLogId.toInt() * 10) + salt,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val WATER_NOTIFICATION_ID = 5001
        private const val MED_NOTIFICATION_BASE = 5100
    }
}
