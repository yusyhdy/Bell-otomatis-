package com.example.data.repository

import com.example.data.local.dao.*
import com.example.data.local.entity.*
import com.example.data.mqtt.MqttManager
import com.example.data.ntp.NtpStatus
import com.example.data.ntp.NtpSyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class BellSystemRepository(
    private val bellScheduleDao: BellScheduleDao,
    private val microcontrollerNodeDao: MicrocontrollerNodeDao,
    private val activityLogDao: ActivityLogDao,
    private val bluetoothSpeakerDao: BluetoothSpeakerDao,
    private val presetModeDao: PresetModeDao
) {
    val allSchedules: Flow<List<BellScheduleEntity>> = bellScheduleDao.getAllSchedules()
    val allNodes: Flow<List<MicrocontrollerNodeEntity>> = microcontrollerNodeDao.getAllNodes()
    val recentLogs: Flow<List<ActivityLogEntity>> = activityLogDao.getRecentLogs(100)
    val allSpeakers: Flow<List<BluetoothSpeakerEntity>> = bluetoothSpeakerDao.getAllSpeakers()
    val allPresets: Flow<List<PresetModeEntity>> = presetModeDao.getAllPresets()
    val activePreset: Flow<PresetModeEntity?> = presetModeDao.getActivePreset()

    suspend fun initializeDefaultDataIfEmpty() {
        val existingPresets = allPresets.first()
        if (existingPresets.isEmpty()) {
            seedInitialPresets()
        }

        val existingNodes = allNodes.first()
        if (existingNodes.isEmpty()) {
            seedInitialNodes()
        }

        val existingSpeakers = allSpeakers.first()
        if (existingSpeakers.isEmpty()) {
            seedInitialBluetoothSpeakers()
        }

        val existingSchedules = allSchedules.first()
        if (existingSchedules.isEmpty()) {
            seedInitialSchedules()
        }

        val existingLogs = recentLogs.first()
        if (existingLogs.isEmpty()) {
            seedInitialLogs()
        }
    }

    private suspend fun seedInitialPresets() {
        val presets = listOf(
            PresetModeEntity("REGULER", "Jadwal Reguler KBM", "Jadwal standar 8 jam pelajaran (Senin - Jumat)", isActive = true),
            PresetModeEntity("UJIAN", "Jadwal Ujian (PTS/PAS)", "Durasi per sesi 90 menit dengan jeda istirahat 30 menit", isActive = false),
            PresetModeEntity("UPACARA", "Jadwal Hari Senin (Upacara)", "Dimulai pukul 06:45 dengan upacara bendera dan apel", isActive = false),
            PresetModeEntity("RAMADHAN", "Jadwal Khusus Ramadhan", "Pemadatan jam pelajaran 35 menit per sesi, pulang lebih awal", isActive = false)
        )
        presetModeDao.insertAll(presets)
    }

    private suspend fun seedInitialNodes() {
        val nodes = listOf(
            MicrocontrollerNodeEntity(
                id = "ESP32-HUB-01",
                nodeName = "ESP32 Gateway Audio & Relay",
                ipAddress = "192.168.1.120",
                macAddress = "24:6F:28:B4:7A:1C",
                firmwareVersion = "v2.4.1-NTP-MQTT",
                chipModel = "ESP32-WROOM-32D Dual Core 240MHz",
                relayPin = 23,
                amplifierState = false,
                lastNtpSyncMillis = System.currentTimeMillis() - 60_000,
                ntpServer = "id.pool.ntp.org",
                ntpDriftMs = 3,
                isOnline = true,
                mqttConnected = true,
                rssi = -52,
                uptimeSeconds = 345600
            ),
            MicrocontrollerNodeEntity(
                id = "ESP32-HUB-02",
                nodeName = "ESP32 Sub-Node Lapangan Utama",
                ipAddress = "192.168.1.125",
                macAddress = "24:6F:28:C8:11:4F",
                firmwareVersion = "v2.4.0-RELAY-BT",
                chipModel = "ESP32-S3 AI-IoT 512KB SRAM",
                relayPin = 19,
                amplifierState = false,
                lastNtpSyncMillis = System.currentTimeMillis() - 180_000,
                ntpServer = "id.pool.ntp.org",
                ntpDriftMs = 5,
                isOnline = true,
                mqttConnected = true,
                rssi = -61,
                uptimeSeconds = 172800
            )
        )
        microcontrollerNodeDao.insertAll(nodes)
    }

    private suspend fun seedInitialBluetoothSpeakers() {
        val speakers = listOf(
            BluetoothSpeakerEntity("BT-SPK-01", "Soundbar Ruang Kelas A (Lt 1)", "FC:58:FA:31:09:A1", "KELAS", isConnected = true, volume = 85, latencyMs = 12, isMultiAudioActive = true),
            BluetoothSpeakerEntity("BT-SPK-02", "Soundbar Ruang Kelas B (Lt 2)", "FC:58:FA:31:09:B2", "KELAS", isConnected = true, volume = 85, latencyMs = 14, isMultiAudioActive = true),
            BluetoothSpeakerEntity("BT-SPK-03", "Horn Speaker Lapangan Utama", "A4:C1:38:D2:77:E9", "LAPANGAN", isConnected = true, volume = 100, latencyMs = 18, isMultiAudioActive = true),
            BluetoothSpeakerEntity("BT-SPK-04", "Ceiling Speaker Koridor & Lab", "BC:DD:C2:55:10:33", "KORIDOR", isConnected = true, volume = 75, latencyMs = 15, isMultiAudioActive = true),
            BluetoothSpeakerEntity("BT-SPK-05", "Monitor Speaker Ruang Guru & TU", "48:E7:29:84:66:F1", "GURU", isConnected = true, volume = 65, latencyMs = 10, isMultiAudioActive = true)
        )
        bluetoothSpeakerDao.insertAll(speakers)
    }

    private suspend fun seedInitialSchedules() {
        val sampleSchedules = mutableListOf<BellScheduleEntity>()
        
        // Senin sampai Jumat standard schedule
        for (day in 1..5) {
            val dayName = when (day) {
                1 -> "Senin"
                2 -> "Selasa"
                3 -> "Rabu"
                4 -> "Kamis"
                else -> "Jumat"
            }

            // 07:00 Masuk
            sampleSchedules.add(
                BellScheduleEntity(
                    dayOfWeek = day,
                    time = if (day == 1) "06:45" else "07:00",
                    title = if (day == 1) "Upacara Bendera & Masuk" else "Bel Masuk & Pembiasaan Pagi",
                    toneType = if (day == 1) "NATIONAL_ANTHEM" else "WESTMINSTER_CHIME",
                    audioTitle = if (day == 1) "Indonesia Raya & Mars" else "Westminster Chime 4-Nada",
                    ampPreTriggerSeconds = 8,
                    ampPostDelaySeconds = 12,
                    targetZones = "ALL",
                    presetMode = "REGULER",
                    isEnabled = true,
                    orderIndex = 1
                )
            )

            // 07:45 Jam Ke-1
            sampleSchedules.add(
                BellScheduleEntity(
                    dayOfWeek = day,
                    time = "07:45",
                    title = "Pergantian Jam Pelajaran Ke-1",
                    toneType = "THREE_TONE_MELODY",
                    audioTitle = "Chime 3-Nada Melodi C-E-G",
                    ampPreTriggerSeconds = 5,
                    ampPostDelaySeconds = 8,
                    targetZones = "KELAS",
                    presetMode = "REGULER",
                    isEnabled = true,
                    orderIndex = 2
                )
            )

            // 08:30 Jam Ke-2
            sampleSchedules.add(
                BellScheduleEntity(
                    dayOfWeek = day,
                    time = "08:30",
                    title = "Pergantian Jam Pelajaran Ke-2",
                    toneType = "THREE_TONE_MELODY",
                    audioTitle = "Chime 3-Nada Melodi C-E-G",
                    ampPreTriggerSeconds = 5,
                    ampPostDelaySeconds = 8,
                    targetZones = "KELAS",
                    presetMode = "REGULER",
                    isEnabled = true,
                    orderIndex = 3
                )
            )

            // 09:15 Jam Ke-3
            sampleSchedules.add(
                BellScheduleEntity(
                    dayOfWeek = day,
                    time = "09:15",
                    title = "Pergantian Jam Pelajaran Ke-3",
                    toneType = "THREE_TONE_MELODY",
                    audioTitle = "Chime 3-Nada Melodi C-E-G",
                    ampPreTriggerSeconds = 5,
                    ampPostDelaySeconds = 8,
                    targetZones = "KELAS",
                    presetMode = "REGULER",
                    isEnabled = true,
                    orderIndex = 4
                )
            )

            // 10:00 Istirahat Pertama
            sampleSchedules.add(
                BellScheduleEntity(
                    dayOfWeek = day,
                    time = "10:00",
                    title = "Istirahat Pertama (30 Menit)",
                    toneType = "MARS_SEKOLAH",
                    audioTitle = "Mars Sekolah & Musik Relaksasi",
                    ampPreTriggerSeconds = 6,
                    ampPostDelaySeconds = 15,
                    targetZones = "ALL",
                    presetMode = "REGULER",
                    isEnabled = true,
                    orderIndex = 5
                )
            )

            // 10:30 Masuk Istirahat
            sampleSchedules.add(
                BellScheduleEntity(
                    dayOfWeek = day,
                    time = "10:30",
                    title = "Masuk Kembali Selesai Istirahat",
                    toneType = "WESTMINSTER_CHIME",
                    audioTitle = "Westminster Chime 4-Nada",
                    ampPreTriggerSeconds = 5,
                    ampPostDelaySeconds = 8,
                    targetZones = "ALL",
                    presetMode = "REGULER",
                    isEnabled = true,
                    orderIndex = 6
                )
            )

            // 12:00 Istirahat Kedua / Sholat Dzuhur
            sampleSchedules.add(
                BellScheduleEntity(
                    dayOfWeek = day,
                    time = if (day == 5) "11:30" else "12:00",
                    title = if (day == 5) "Persiapan Sholat Jumat" else "Istirahat Ke-2 & Sholat Dzuhur",
                    toneType = "PRAYER_CALL",
                    audioTitle = "Chime Lembut Pengingat Ibadah",
                    ampPreTriggerSeconds = 8,
                    ampPostDelaySeconds = 10,
                    targetZones = "ALL",
                    presetMode = "REGULER",
                    isEnabled = true,
                    orderIndex = 7
                )
            )

            // 13:00 Masuk Siang
            sampleSchedules.add(
                BellScheduleEntity(
                    dayOfWeek = day,
                    time = "13:00",
                    title = "Jam Pelajaran Ke-6 (Sesi Siang)",
                    toneType = "THREE_TONE_MELODY",
                    audioTitle = "Chime 3-Nada Melodi C-E-G",
                    ampPreTriggerSeconds = 5,
                    ampPostDelaySeconds = 8,
                    targetZones = "KELAS",
                    presetMode = "REGULER",
                    isEnabled = true,
                    orderIndex = 8
                )
            )

            // 14:30 Bel Pulang Sekolah
            sampleSchedules.add(
                BellScheduleEntity(
                    dayOfWeek = day,
                    time = if (day == 5) "13:45" else "14:30",
                    title = "Bel Pulang Sekolah & Doa Penutup",
                    toneType = "NATIONAL_ANTHEM",
                    audioTitle = "Lagu Nasional & Doa Selesai KBM",
                    ampPreTriggerSeconds = 10,
                    ampPostDelaySeconds = 20,
                    targetZones = "ALL",
                    presetMode = "REGULER",
                    isEnabled = true,
                    orderIndex = 9
                )
            )
        }

        bellScheduleDao.insertAll(sampleSchedules)
    }

    private suspend fun seedInitialLogs() {
        val logs = listOf(
            ActivityLogEntity(
                timestamp = System.currentTimeMillis() - 3600_000,
                eventType = "BELL_TRIGGERED",
                source = "SCHEDULE_ENGINE",
                message = "Bel Terjadwal 'Pergantian Jam Pelajaran Ke-2' berhasil dieksekusi",
                level = "SUCCESS",
                metadata = "Zone: KELAS, Tone: THREE_TONE_MELODY, Amp: 5s pre-trigger"
            ),
            ActivityLogEntity(
                timestamp = System.currentTimeMillis() - 3605_000,
                eventType = "AMP_POWER_ON",
                source = "ESP32_RELAY",
                message = "Relay GPIO 23 ON (Pre-Chime warmup 5s dipicu)",
                level = "INFO",
                metadata = "Node: ESP32-HUB-01, Target: Amplifier TOA 240W"
            ),
            ActivityLogEntity(
                timestamp = System.currentTimeMillis() - 7200_000,
                eventType = "NTP_SYNC",
                source = "NTP_CLIENT",
                message = "Sinkronisasi waktu NTP berhasil terhadap id.pool.ntp.org (Offset: +3ms)",
                level = "SUCCESS",
                metadata = "Stratum: 2, Jitter: 0.38ms, Round-trip: 14ms"
            ),
            ActivityLogEntity(
                timestamp = System.currentTimeMillis() - 10800_000,
                eventType = "MQTT_MESSAGE",
                source = "MQTT_BROKER",
                message = "Menerima payload pembaruan jadwal dari Dashboard Web Operator",
                level = "INFO",
                metadata = "Topic: school/bell/schedule, QoS: 1, PayloadSize: 1.4KB"
            )
        )
        for (log in logs) {
            activityLogDao.insertLog(log)
        }
    }

    // Operations
    suspend fun saveSchedule(schedule: BellScheduleEntity) {
        if (schedule.id == 0) {
            bellScheduleDao.insertSchedule(schedule)
        } else {
            bellScheduleDao.updateSchedule(schedule)
        }
        logActivity(
            eventType = "SCHEDULE_UPDATED",
            source = "ADMIN_APP",
            message = "Jadwal '${schedule.title}' (${schedule.time}) diperbarui/disimpan",
            level = "INFO"
        )
        // Publish to MQTT broker
        MqttManager.publishCommand(
            "schedule",
            "UPDATE_SCHEDULE",
            mapOf("id" to schedule.id, "time" to schedule.time, "title" to schedule.title)
        )
    }

    suspend fun deleteSchedule(schedule: BellScheduleEntity) {
        bellScheduleDao.deleteSchedule(schedule)
        logActivity(
            eventType = "SCHEDULE_DELETED",
            source = "ADMIN_APP",
            message = "Jadwal '${schedule.title}' (${schedule.time}) dihapus",
            level = "WARNING"
        )
        MqttManager.publishCommand("schedule", "DELETE_SCHEDULE", mapOf("id" to schedule.id))
    }

    suspend fun toggleScheduleEnabled(schedule: BellScheduleEntity) {
        val updated = schedule.copy(isEnabled = !schedule.isEnabled)
        bellScheduleDao.updateSchedule(updated)
    }

    suspend fun toggleAmplifierRelay(nodeId: String, newState: Boolean) {
        microcontrollerNodeDao.updateAmplifierState(nodeId, newState)
        val actionStr = if (newState) "AMP_ON" else "AMP_OFF"
        logActivity(
            eventType = if (newState) "AMP_POWER_ON" else "AMP_POWER_OFF",
            source = "ESP32_RELAY",
            message = "Relay Amplifier pada node '$nodeId' diubah menjadi ${if (newState) "AKTIF (ON)" else "NONAKTIF (STANDBY)"}",
            level = if (newState) "SUCCESS" else "INFO"
        )
        MqttManager.publishCommand("amp", actionStr, mapOf("nodeId" to nodeId, "relayState" to newState))
    }

    suspend fun syncNtpTime(nodeId: String, serverHost: String): NtpStatus {
        val status = NtpSyncManager.triggerManualSync(serverHost)
        microcontrollerNodeDao.updateNtpSync(nodeId, status.lastSyncTime, status.driftMs)
        logActivity(
            eventType = "NTP_SYNC",
            source = "NTP_CLIENT",
            message = "Sinkronisasi waktu NTP berhasil via $serverHost (Offset: ${status.driftMs}ms, Jitter: ${String.format("%.2f", status.jitterMs)}ms)",
            level = "SUCCESS",
            metadata = "Server: $serverHost, Stratum: ${status.stratum}"
        )
        MqttManager.publishCommand("telemetry", "NTP_SYNC_ACK", mapOf("server" to serverHost, "offset" to status.driftMs))
        return status
    }

    suspend fun toggleSpeakerConnection(speakerId: String, currentConnected: Boolean) {
        bluetoothSpeakerDao.updateConnectionState(speakerId, !currentConnected)
    }

    suspend fun updateSpeakerVolume(speakerId: String, volume: Int) {
        bluetoothSpeakerDao.updateVolume(speakerId, volume)
    }

    suspend fun setActivePreset(presetId: String) {
        presetModeDao.setActivePreset(presetId)
        logActivity(
            eventType = "PRESET_CHANGED",
            source = "ADMIN_APP",
            message = "Mode jadwal aktif sistem diubah menjadi '$presetId'",
            level = "INFO"
        )
        MqttManager.publishCommand("config", "SWITCH_PRESET", mapOf("preset" to presetId))
    }

    suspend fun logActivity(eventType: String, source: String, message: String, level: String = "INFO", metadata: String = "") {
        activityLogDao.insertLog(
            ActivityLogEntity(
                timestamp = System.currentTimeMillis(),
                eventType = eventType,
                source = source,
                message = message,
                level = level,
                metadata = metadata
            )
        )
    }

    suspend fun clearLogs() {
        activityLogDao.clearLogs()
    }
}
