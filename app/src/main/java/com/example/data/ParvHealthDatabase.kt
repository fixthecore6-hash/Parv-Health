package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        DailyStepsEntity::class,
        SensorAnchorEntity::class,
        WorkoutSetEntity::class,
        WorkoutDayEntity::class,
        CustomExerciseEntity::class,
        PlannedDayExerciseEntity::class,
        FoodEntryEntity::class,
        CustomMealEntity::class,
        SleepEntryEntity::class,
        ReminderEntity::class,
        DoctorEntity::class,
        FallAlertEntity::class,
        WaterEntryEntity::class,
        MedicationEntity::class,
        MedicationDoseLogEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class ParvHealthDatabase : RoomDatabase() {
    abstract fun pedometerDao(): PedometerDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun foodDao(): FoodDao
    abstract fun wellnessDao(): WellnessDao
    abstract fun safetyHealthDao(): SafetyHealthDao

    companion object {
        @Volatile
        private var instance: ParvHealthDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `workout_sets` (
                        `date` TEXT NOT NULL,
                        `exerciseId` TEXT NOT NULL,
                        `setIndex` INTEGER NOT NULL,
                        `completed` INTEGER NOT NULL,
                        PRIMARY KEY(`date`, `exerciseId`, `setIndex`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `workout_days` (
                        `date` TEXT NOT NULL,
                        `dayOfWeekIndex` INTEGER NOT NULL,
                        `isDone` INTEGER NOT NULL,
                        `completedSets` INTEGER NOT NULL,
                        `totalSets` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`date`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `food_entries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `date` TEXT NOT NULL,
                        `mealType` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `amountUnit` TEXT NOT NULL,
                        `calories` INTEGER NOT NULL,
                        `proteinG` REAL NOT NULL,
                        `carbsG` REAL NOT NULL,
                        `fatG` REAL NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `custom_exercises` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `muscleGroup` TEXT NOT NULL,
                        `sets` INTEGER NOT NULL,
                        `repsLabel` TEXT NOT NULL,
                        `notes` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `planned_day_exercises` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `dayOfWeekIndex` INTEGER NOT NULL,
                        `exerciseId` TEXT NOT NULL,
                        `sortOrder` INTEGER NOT NULL,
                        `addedAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `custom_meals` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `mealType` TEXT NOT NULL,
                        `calories` INTEGER NOT NULL,
                        `proteinG` REAL NOT NULL,
                        `carbsG` REAL NOT NULL,
                        `fatG` REAL NOT NULL,
                        `ingredients` TEXT NOT NULL,
                        `isVegetarian` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sleep_entries` (
                        `date` TEXT NOT NULL,
                        `hoursSlept` REAL NOT NULL,
                        `note` TEXT NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`date`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `reminders` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `category` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `timeLabel` TEXT NOT NULL,
                        `scheduleLabel` TEXT NOT NULL,
                        `enabled` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `doctors` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `phoneNumber` TEXT NOT NULL,
                        `clinicOrHospital` TEXT NOT NULL,
                        `specialty` TEXT NOT NULL,
                        `isPrimary` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `fall_alerts` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `timestampMs` INTEGER NOT NULL,
                        `dateIso` TEXT NOT NULL,
                        `sensitivity` TEXT NOT NULL,
                        `peakG` REAL NOT NULL,
                        `outcome` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `water_entries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `dateIso` TEXT NOT NULL,
                        `amountMl` INTEGER NOT NULL,
                        `timestampMs` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `medications` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `dose` TEXT NOT NULL,
                        `timesPerDay` INTEGER NOT NULL,
                        `startDateIso` TEXT NOT NULL,
                        `endDateIso` TEXT NOT NULL,
                        `daysOfWeekCsv` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `medication_dose_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `medicationId` INTEGER NOT NULL,
                        `dateIso` TEXT NOT NULL,
                        `slotIndex` INTEGER NOT NULL,
                        `scheduledTimeLabel` TEXT NOT NULL,
                        `scheduledTimestampMs` INTEGER NOT NULL,
                        `snoozedUntilMs` INTEGER,
                        `status` TEXT NOT NULL,
                        `actedAtMs` INTEGER
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `fall_alerts` ADD COLUMN `alertType` TEXT NOT NULL DEFAULT 'FALL'")
                db.execSQL("ALTER TABLE `fall_alerts` ADD COLUMN `latitude` REAL")
                db.execSQL("ALTER TABLE `fall_alerts` ADD COLUMN `longitude` REAL")
                db.execSQL("ALTER TABLE `fall_alerts` ADD COLUMN `accuracyMeters` REAL")
                db.execSQL("ALTER TABLE `fall_alerts` ADD COLUMN `locationSource` TEXT NOT NULL DEFAULT 'UNAVAILABLE'")
                db.execSQL("ALTER TABLE `fall_alerts` ADD COLUMN `locationTimestampMs` INTEGER")
                db.execSQL("ALTER TABLE `fall_alerts` ADD COLUMN `locationLink` TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): ParvHealthDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ParvHealthDatabase::class.java,
                    "parv_health.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
