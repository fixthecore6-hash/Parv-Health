package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor_anchor")
data class SensorAnchorEntity(
    @PrimaryKey val id: Int = 1,
    val lastHardwareCounter: Float = -1f,
    val lastCounterDate: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
