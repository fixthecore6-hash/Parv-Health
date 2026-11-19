package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FallSensitivity(val label: String, val spikeThresholdG: Float) {
    LOW("Low", 3.2f),
    MEDIUM("Medium", 2.5f),
    HIGH("High", 2.0f)
}

enum class FallAlertOutcome(val label: String) {
    FALSE_ALARM("False alarm"),
    CANCELLED("Cancelled"),
    NO_RESPONSE("No response")
}

enum class EmergencyAlertType(val label: String) {
    FALL("Fall"),
    SHAKE("Shake"),
    SOS("SOS")
}

enum class AlertLocationSource(val label: String) {
    FRESH("Fresh"),
    LAST_KNOWN("Last known"),
    UNAVAILABLE("Location unavailable")
}

enum class MedicationDoseStatus(val label: String) {
    PENDING("Pending"),
    TAKEN("Taken"),
    SNOOZED("Snoozed"),
    SKIPPED("Skipped"),
    MISSED("Missed")
}

@Entity(tableName = "doctors")
data class DoctorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val phoneNumber: String,
    val clinicOrHospital: String = "",
    val specialty: String = "",
    val isPrimary: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "fall_alerts")
data class FallAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestampMs: Long,
    val dateIso: String,
    val sensitivity: String, // FallSensitivity.name
    val peakG: Float,
    val outcome: String, // FallAlertOutcome.name
    val alertType: String = EmergencyAlertType.FALL.name,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracyMeters: Float? = null,
    val locationSource: String = AlertLocationSource.UNAVAILABLE.name,
    val locationTimestampMs: Long? = null,
    val locationLink: String = ""
)

@Entity(tableName = "water_entries")
data class WaterEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val dateIso: String,
    val amountMl: Int,
    val timestampMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "medications")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val dose: String,
    val timesPerDay: Int = 1,
    val startDateIso: String,
    val endDateIso: String = "",
    val daysOfWeekCsv: String = "0,1,2,3,4,5,6",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun scheduledDays(): Set<Int> {
        return daysOfWeekCsv.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 0..6 }
            .toSet()
    }

    fun isActiveOn(dateIso: String, dayOfWeekIndex: Int): Boolean {
        if (dateIso < startDateIso) return false
        if (endDateIso.isNotBlank() && dateIso > endDateIso) return false
        val days = scheduledDays()
        return days.isEmpty() || dayOfWeekIndex in days
    }
}

@Entity(tableName = "medication_dose_logs")
data class MedicationDoseLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val medicationId: Long,
    val dateIso: String,
    val slotIndex: Int,
    val scheduledTimeLabel: String,
    val scheduledTimestampMs: Long,
    val snoozedUntilMs: Long? = null,
    val status: String = MedicationDoseStatus.PENDING.name,
    val actedAtMs: Long? = null
)
