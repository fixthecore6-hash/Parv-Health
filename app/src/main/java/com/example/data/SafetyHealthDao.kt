package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SafetyHealthDao {

    // ---- Doctors ----
    @Query("SELECT * FROM doctors ORDER BY isPrimary DESC, createdAt ASC")
    fun observeDoctors(): Flow<List<DoctorEntity>>

    @Query("SELECT * FROM doctors ORDER BY isPrimary DESC, createdAt ASC")
    suspend fun getAllDoctors(): List<DoctorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDoctor(doctor: DoctorEntity): Long

    @Query("UPDATE doctors SET isPrimary = 0")
    suspend fun clearPrimaryDoctorFlag()

    @Query("UPDATE doctors SET isPrimary = CASE WHEN id = :id THEN 1 ELSE 0 END")
    suspend fun setPrimaryDoctor(id: Long)

    @Query("DELETE FROM doctors WHERE id = :id")
    suspend fun deleteDoctorById(id: Long)

    @Query("DELETE FROM doctors")
    suspend fun clearAllDoctors()

    // ---- Fall Alerts ----
    @Query("SELECT * FROM fall_alerts ORDER BY timestampMs DESC")
    fun observeFallAlerts(): Flow<List<FallAlertEntity>>

    @Query("SELECT * FROM fall_alerts ORDER BY timestampMs DESC")
    suspend fun getAllFallAlerts(): List<FallAlertEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFallAlert(alert: FallAlertEntity): Long

    @Query("UPDATE fall_alerts SET outcome = :outcome WHERE id = :id")
    suspend fun updateFallAlertOutcome(id: Long, outcome: String)

    @Query(
        "UPDATE fall_alerts SET latitude = :latitude, longitude = :longitude, accuracyMeters = :accuracyMeters, locationSource = :locationSource, locationTimestampMs = :locationTimestampMs, locationLink = :locationLink WHERE id = :id"
    )
    suspend fun updateFallAlertLocation(
        id: Long,
        latitude: Double?,
        longitude: Double?,
        accuracyMeters: Float?,
        locationSource: String,
        locationTimestampMs: Long?,
        locationLink: String
    )

    @Query(
        "UPDATE fall_alerts SET latitude = NULL, longitude = NULL, accuracyMeters = NULL, locationSource = 'UNAVAILABLE', locationTimestampMs = NULL, locationLink = ''"
    )
    suspend fun clearAllAlertLocations()

    @Query("DELETE FROM fall_alerts WHERE id = :id")
    suspend fun deleteFallAlertById(id: Long)

    @Query("DELETE FROM fall_alerts")
    suspend fun clearAllFallAlerts()

    // ---- Water Intake ----
    @Query("SELECT * FROM water_entries WHERE dateIso = :dateIso ORDER BY timestampMs DESC")
    fun observeWaterEntriesForDate(dateIso: String): Flow<List<WaterEntryEntity>>

    @Query("SELECT * FROM water_entries WHERE dateIso IN (:dates) ORDER BY timestampMs DESC")
    fun observeWaterEntriesForDates(dates: List<String>): Flow<List<WaterEntryEntity>>

    @Query("SELECT * FROM water_entries ORDER BY timestampMs DESC")
    suspend fun getAllWaterEntries(): List<WaterEntryEntity>

    @Query("SELECT * FROM water_entries WHERE dateIso = :dateIso ORDER BY timestampMs DESC LIMIT 1")
    suspend fun getLatestWaterEntryForDate(dateIso: String): WaterEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterEntry(entry: WaterEntryEntity): Long

    @Query("DELETE FROM water_entries WHERE id = :id")
    suspend fun deleteWaterEntryById(id: Long)

    @Query("DELETE FROM water_entries")
    suspend fun clearAllWaterEntries()

    // ---- Medications & Dose Logs ----
    @Query("SELECT * FROM medications ORDER BY createdAt DESC")
    fun observeMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medications ORDER BY createdAt DESC")
    suspend fun getAllMedications(): List<MedicationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMedication(medication: MedicationEntity): Long

    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedicationById(id: Long)

    @Query("DELETE FROM medication_dose_logs WHERE medicationId = :medicationId")
    suspend fun deleteDoseLogsForMedication(medicationId: Long)

    @Query("SELECT * FROM medication_dose_logs WHERE dateIso IN (:dates) ORDER BY scheduledTimestampMs ASC")
    fun observeDoseLogsForDates(dates: List<String>): Flow<List<MedicationDoseLogEntity>>

    @Query("SELECT * FROM medication_dose_logs ORDER BY scheduledTimestampMs ASC")
    suspend fun getAllDoseLogs(): List<MedicationDoseLogEntity>

    @Query("SELECT * FROM medication_dose_logs WHERE dateIso = :dateIso")
    suspend fun getDoseLogsForDate(dateIso: String): List<MedicationDoseLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDoseLog(log: MedicationDoseLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoseLogs(logs: List<MedicationDoseLogEntity>)

    @Query("DELETE FROM medications")
    suspend fun clearAllMedications()

    @Query("DELETE FROM medication_dose_logs")
    suspend fun clearAllDoseLogs()
}
