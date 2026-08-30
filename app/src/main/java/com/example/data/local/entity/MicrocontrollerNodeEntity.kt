package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "microcontroller_nodes")
data class MicrocontrollerNodeEntity(
    @PrimaryKey
    val id: String,
    val nodeName: String,
    val ipAddress: String,
    val macAddress: String,
    val firmwareVersion: String,
    val chipModel: String,
    val relayPin: Int,
    val amplifierState: Boolean,
    val lastNtpSyncMillis: Long,
    val ntpServer: String,
    val ntpDriftMs: Long,
    val isOnline: Boolean,
    val mqttConnected: Boolean,
    val rssi: Int,
    val uptimeSeconds: Long
)
