package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bluetooth_speakers")
data class BluetoothSpeakerEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val macAddress: String,
    val zone: String, // "KELAS", "LAPANGAN", "KORIDOR", "GURU", "LAB"
    val isConnected: Boolean,
    val volume: Int = 80,
    val latencyMs: Int = 15,
    val isMultiAudioActive: Boolean = true
)
