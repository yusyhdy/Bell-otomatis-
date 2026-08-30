package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String,
    val source: String,
    val message: String,
    val level: String = "INFO", // "INFO", "SUCCESS", "WARNING", "ERROR"
    val metadata: String = ""
)
