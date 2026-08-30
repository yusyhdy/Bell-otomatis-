package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.audio.ToneAudioEngine
import com.example.data.cloud.AcademicCalendarEvent
import com.example.data.cloud.CloudBackupSyncManager
import com.example.data.cloud.EncryptedBackupPayload
import com.example.data.cloud.GoogleCalendarSyncManager
import com.example.data.cloud.SyncDevice
import com.example.data.local.dao.AppDatabase
import com.example.data.local.entity.ActivityLogEntity
import com.example.data.local.entity.BellScheduleEntity
import com.example.data.local.entity.BluetoothSpeakerEntity
import com.example.data.local.entity.MicrocontrollerNodeEntity
import com.example.data.local.entity.PresetModeEntity
import com.example.data.mqtt.MqttBrokerConfig
import com.example.data.mqtt.MqttLogMessage
import com.example.data.mqtt.MqttManager
import com.example.data.ntp.NtpStatus
import com.example.data.ntp.NtpSyncManager
import com.example.data.repository.BellSystemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class UpcomingBellInfo(
    val title: String,
    val time: String,
    val toneType: String,
    val targetZones: String,
    val secondsRemaining: Long,
    val preTriggerCountdown: Long
)

data class PushNotificationAlert(
    val id: String,
    val title: String,
    val body: String,
    val type: String, // "BELL", "AMP", "NTP", "ALERT", "SECURITY"
    val timestamp: Long = System.currentTimeMillis()
)

class BellViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = BellSystemRepository(
        database.bellScheduleDao(),
        database.microcontrollerNodeDao(),
        database.activityLogDao(),
        database.bluetoothSpeakerDao(),
        database.presetModeDao()
    )

    // Current Time Flow (updates every second for real-time clock and countdown)
    private val _currentTimeMillis = MutableStateFlow(System.currentTimeMillis())
    val currentTimeMillis: StateFlow<Long> = _currentTimeMillis.asStateFlow()

    // Selected Day in Schedule view (1=Senin..7=Minggu)
    private val _selectedDay = MutableStateFlow(1)
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    // Active Tone Playing State
    private val _isPlayingTone = MutableStateFlow<String?>(null)
    val isPlayingTone: StateFlow<String?> = _isPlayingTone.asStateFlow()

    // Live Push Alerts for Admin
    private val _adminPushAlerts = MutableStateFlow<List<PushNotificationAlert>>(emptyList())
    val adminPushAlerts: StateFlow<List<PushNotificationAlert>> = _adminPushAlerts.asStateFlow()

    // Encrypted Backup State
    private val _lastBackupPayload = MutableStateFlow<EncryptedBackupPayload?>(null)
    val lastBackupPayload: StateFlow<EncryptedBackupPayload?> = _lastBackupPayload.asStateFlow()

    // Google Calendar Events
    private val _calendarEvents = MutableStateFlow(GoogleCalendarSyncManager.getSampleCalendarEvents())
    val calendarEvents: StateFlow<List<AcademicCalendarEvent>> = _calendarEvents.asStateFlow()

    // Multi-device sync list
    private val _connectedDevices = MutableStateFlow(CloudBackupSyncManager.getRegisteredSyncDevices())
    val connectedDevices: StateFlow<List<SyncDevice>> = _connectedDevices.asStateFlow()

    // Repository Flows
    val allSchedules: StateFlow<List<BellScheduleEntity>> = repository.allSchedules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNodes: StateFlow<List<MicrocontrollerNodeEntity>> = repository.allNodes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<ActivityLogEntity>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSpeakers: StateFlow<List<BluetoothSpeakerEntity>> = repository.allSpeakers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPresets: StateFlow<List<PresetModeEntity>> = repository.allPresets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePreset: StateFlow<PresetModeEntity?> = repository.activePreset
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // NTP & MQTT State
    val ntpStatus: StateFlow<NtpStatus> = NtpSyncManager.ntpStatus
    val mqttConfig: StateFlow<MqttBrokerConfig> = MqttManager.config
    val mqttConnected: StateFlow<Boolean> = MqttManager.isConnected
    val mqttMessages: StateFlow<List<MqttLogMessage>> = MqttManager.liveMessages

    // Filtered schedules for currently selected day
    val daySchedules: StateFlow<List<BellScheduleEntity>> = combine(allSchedules, _selectedDay) { schedules, day ->
        schedules.filter { it.dayOfWeek == day }.sortedBy { it.time }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Next upcoming bell calculation
    val upcomingBell: StateFlow<UpcomingBellInfo?> = combine(allSchedules, _currentTimeMillis) { schedules, now ->
        calculateNextUpcomingBell(schedules, now)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            repository.initializeDefaultDataIfEmpty()
            startClockTicker()
        }
    }

    private fun startClockTicker() {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                _currentTimeMillis.value = System.currentTimeMillis()
                delay(1000)
            }
        }
    }

    fun setSelectedDay(day: Int) {
        _selectedDay.value = day
    }

    // Schedule actions
    fun saveSchedule(schedule: BellScheduleEntity) {
        viewModelScope.launch {
            repository.saveSchedule(schedule)
            triggerAdminPush(
                title = "Jadwal Bel Disimpan",
                body = "Jadwal '${schedule.title}' (${schedule.time}) siap dieksekusi",
                type = "BELL"
            )
        }
    }

    fun deleteSchedule(schedule: BellScheduleEntity) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
            triggerAdminPush(
                title = "Jadwal Bel Dihapus",
                body = "Jadwal '${schedule.title}' telah dihapus dari sistem",
                type = "BELL"
            )
        }
    }

    fun toggleSchedule(schedule: BellScheduleEntity) {
        viewModelScope.launch {
            repository.toggleScheduleEnabled(schedule)
        }
    }

    fun reorderSchedules(fromIndex: Int, toIndex: Int) {
        val currentList = daySchedules.value.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            val moved = currentList.removeAt(fromIndex)
            currentList.add(toIndex, moved)
            viewModelScope.launch {
                currentList.forEachIndexed { index, item ->
                    repository.saveSchedule(item.copy(orderIndex = index))
                }
            }
        }
    }

    // Microcontroller & Relay actions
    fun toggleAmplifier(nodeId: String, currentState: Boolean) {
        viewModelScope.launch {
            val newState = !currentState
            repository.toggleAmplifierRelay(nodeId, newState)
            triggerAdminPush(
                title = if (newState) "Amplifier AKTIF (Relay ON)" else "Amplifier STANDBY (Relay OFF)",
                body = "Status relay GPIO pada $nodeId diubah oleh Administrator",
                type = "AMP"
            )
        }
    }

    fun syncNtp(nodeId: String, server: String = "id.pool.ntp.org") {
        viewModelScope.launch {
            val status = repository.syncNtpTime(nodeId, server)
            triggerAdminPush(
                title = "Sinkronisasi NTP Berhasil",
                body = "Waktu mikrokontroler tersinkronisasi (+${status.driftMs}ms offset) dengan $server",
                type = "NTP"
            )
        }
    }

    fun restartNode(node: MicrocontrollerNodeEntity) {
        viewModelScope.launch {
            MqttManager.publishCommand("command", "RESTART_NODE", mapOf("nodeId" to node.id))
            repository.logActivity(
                eventType = "NODE_REBOOT",
                source = "ADMIN_APP",
                message = "Perintah Soft Reboot dikirim ke node mikrokontroler ${node.nodeName} (${node.ipAddress})",
                level = "INFO"
            )
            triggerAdminPush(
                title = "Perintah Reboot Node Dikirim",
                body = "Mikrokontroler ${node.id} sedang melakukan booting ulang...",
                type = "ALERT"
            )
        }
    }

    // Audio Chime & Tone actions
    fun previewTone(toneType: String) {
        viewModelScope.launch {
            _isPlayingTone.value = toneType
            ToneAudioEngine.playTone(toneType) {
                _isPlayingTone.value = null
            }
        }
    }

    fun stopAudioPreview() {
        ToneAudioEngine.stop()
        _isPlayingTone.value = null
    }

    fun triggerEmergencyAlarm() {
        viewModelScope.launch {
            _isPlayingTone.value = "EMERGENCY_SIREN"
            vibrateAlert()
            allNodes.value.forEach { node ->
                repository.toggleAmplifierRelay(node.id, true)
            }
            MqttManager.publishCommand("emergency", "EVACUATION_ALARM", mapOf("reason" to "EMERGENCY_TRIGGERED_BY_ADMIN"))
            repository.logActivity(
                eventType = "EMERGENCY_ALERT",
                source = "ADMIN_APP",
                message = "PERINGATAN DARURAT EVAKUASI / SIRINE MANUAL DIPICU OLEH ADMIN!",
                level = "ERROR"
            )
            triggerAdminPush(
                title = "🚨 SIRINE DARURAT AKTIF!",
                body = "Evakuasi darurat telah dipicu ke seluruh speaker zona sekolah!",
                type = "SECURITY"
            )
            ToneAudioEngine.playTone("EMERGENCY_SIREN") {
                _isPlayingTone.value = null
            }
        }
    }

    fun broadcastAnnouncement(messageText: String, targetZone: String = "ALL") {
        viewModelScope.launch {
            _isPlayingTone.value = "ANNOUNCEMENT"
            allNodes.value.firstOrNull()?.let { repository.toggleAmplifierRelay(it.id, true) }
            MqttManager.publishCommand("announcement", "BROADCAST_AUDIO", mapOf("text" to messageText, "zone" to targetZone))
            repository.logActivity(
                eventType = "ANNOUNCEMENT_SENT",
                source = "ADMIN_APP",
                message = "Pengumuman audio dipancarkan: '$messageText' (Zona: $targetZone)",
                level = "INFO"
            )
            triggerAdminPush(
                title = "Pengumuman Dipancarkan",
                body = "Zona: $targetZone - \"$messageText\"",
                type = "BELL"
            )
            ToneAudioEngine.playTone("ANNOUNCEMENT") {
                _isPlayingTone.value = null
            }
        }
    }

    // Bluetooth speaker actions
    fun toggleSpeaker(speakerId: String, currentConnected: Boolean) {
        viewModelScope.launch {
            repository.toggleSpeakerConnection(speakerId, currentConnected)
        }
    }

    fun setSpeakerVolume(speakerId: String, volume: Int) {
        viewModelScope.launch {
            repository.updateSpeakerVolume(speakerId, volume)
        }
    }

    // Preset actions
    fun switchPreset(presetId: String) {
        viewModelScope.launch {
            repository.setActivePreset(presetId)
            triggerAdminPush(
                title = "Mode Jadwal Berubah",
                body = "Sistem beralih ke konfigurasi mode '$presetId'",
                type = "BELL"
            )
        }
    }

    // Cloud Backup with End-to-End AES-256 Encryption
    fun performEncryptedCloudBackup(passphrase: String = "SMAN1-SecureKey-2026") {
        viewModelScope.launch {
            val schedules = allSchedules.value
            val jsonExport = "{\"schedulesCount\":${schedules.size},\"exportedAt\":${System.currentTimeMillis()},\"appVersion\":\"2.4.1\"}"
            val payload = CloudBackupSyncManager.createEncryptedBackup(jsonExport, passphrase)
            _lastBackupPayload.value = payload
            repository.logActivity(
                eventType = "BACKUP_RESTORE",
                source = "ADMIN_APP",
                message = "Backup cloud terenkripsi AES-256-GCM berhasil dibuat (Snapshot ID: ${payload.snapshotId})",
                level = "SUCCESS",
                metadata = "Checksum: ${payload.checksumSha256}"
            )
            triggerAdminPush(
                title = "Backup Cloud Terenkripsi Sukses",
                body = "Data jadwal aman tersimpan di cloud (Snapshot: ${payload.snapshotId})",
                type = "SECURITY"
            )
        }
    }

    // Google Calendar Sync
    fun syncGoogleCalendar() {
        viewModelScope.launch {
            delay(1200) // Simulate fast network sync
            val events = GoogleCalendarSyncManager.getSampleCalendarEvents()
            _calendarEvents.value = events
            repository.logActivity(
                eventType = "CALENDAR_SYNC",
                source = "GOOGLE_CALENDAR",
                message = "Sinkronisasi kalender akademik Google berhasil (${events.size} agenda terupdate)",
                level = "SUCCESS"
            )
            triggerAdminPush(
                title = "Kalender Google Tersinkron",
                body = "${events.size} agenda sekolah & penyesuaian libur otomatis diperbarui",
                type = "BELL"
            )
        }
    }

    // MQTT actions
    fun sendMqttCommand(action: String, params: Map<String, Any> = emptyMap()) {
        viewModelScope.launch {
            MqttManager.publishCommand("command", action, params)
        }
    }

    fun toggleMqttConnection() {
        val current = mqttConnected.value
        MqttManager.setConnected(!current)
    }

    fun updateMqttConfig(config: MqttBrokerConfig) {
        MqttManager.updateConfig(config)
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    // Push notification trigger
    private fun triggerAdminPush(title: String, body: String, type: String) {
        val alert = PushNotificationAlert(
            id = "ALERT-${System.currentTimeMillis()}",
            title = title,
            body = body,
            type = type
        )
        val current = _adminPushAlerts.value.toMutableList()
        current.add(0, alert)
        if (current.size > 20) current.removeAt(current.size - 1)
        _adminPushAlerts.value = current
    }

    fun dismissPushAlert(alertId: String) {
        _adminPushAlerts.value = _adminPushAlerts.value.filter { it.id != alertId }
    }

    private fun vibrateAlert() {
        try {
            val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(500)
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun calculateNextUpcomingBell(schedules: List<BellScheduleEntity>, nowMillis: Long): UpcomingBellInfo? {
        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val currentDay = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMinute = cal.get(Calendar.MINUTE)
        val currentSeconds = cal.get(Calendar.SECOND)
        val currentTotalSec = currentHour * 3600 + currentMinute * 60 + currentSeconds

        // Filter enabled schedules for today that haven't passed yet
        val todaysSchedules = schedules.filter { it.dayOfWeek == currentDay && it.isEnabled }
        for (item in todaysSchedules.sortedBy { it.time }) {
            val parts = item.time.split(":")
            if (parts.size == 2) {
                val itemHour = parts[0].toIntOrNull() ?: 0
                val itemMin = parts[1].toIntOrNull() ?: 0
                val itemTotalSec = itemHour * 3600 + itemMin * 60
                if (itemTotalSec > currentTotalSec) {
                    val diffSec = itemTotalSec - currentTotalSec
                    return UpcomingBellInfo(
                        title = item.title,
                        time = item.time,
                        toneType = item.toneType,
                        targetZones = item.targetZones,
                        secondsRemaining = diffSec.toLong(),
                        preTriggerCountdown = (diffSec - item.ampPreTriggerSeconds).coerceAtLeast(0).toLong()
                    )
                }
            }
        }
        return null
    }

    fun formatCountdown(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return if (h > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", m, s)
        }
    }
}
