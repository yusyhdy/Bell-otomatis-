package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bell_schedules")
data class BellScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val dayOfWeek: Int, // 1=Senin .. 7=Minggu
    val time: String, // "07:00"
    val title: String,
    val toneType: String,
    val audioTitle: String,
    val ampPreTriggerSeconds: Int = 5,
    val ampPostDelaySeconds: Int = 10,
    val targetZones: String = "ALL", // "ALL", "KELAS", "LAPANGAN", "KORIDOR", "GURU"
    val presetMode: String = "REGULER",
    val isEnabled: Boolean = true,
    val orderIndex: Int = 0
)
