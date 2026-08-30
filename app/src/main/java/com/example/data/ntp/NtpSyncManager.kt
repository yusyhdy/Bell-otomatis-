package com.example.data.ntp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class NtpServerInfo(
    val serverName: String,
    val host: String,
    val stratum: Int,
    val latencyMs: Int,
    val offsetMs: Long,
    val isPrimary: Boolean = false
)

data class NtpStatus(
    val isSynced: Boolean = true,
    val activeServer: String = "id.pool.ntp.org",
    val stratum: Int = 2,
    val driftMs: Long = 4, // milliseconds
    val jitterMs: Double = 0.42,
    val lastSyncTime: Long = System.currentTimeMillis() - 120_000,
    val syncIntervalMinutes: Int = 15,
    val availableServers: List<NtpServerInfo> = listOf(
        NtpServerInfo("Indonesia NTP Pool", "id.pool.ntp.org", 2, 14, 4, isPrimary = true),
        NtpServerInfo("Asia Regional NTP", "asia.pool.ntp.org", 2, 28, 6),
        NtpServerInfo("Google Public NTP", "time.google.com", 1, 19, 2),
        NtpServerInfo("Cloudflare Time", "time.cloudflare.com", 1, 16, 3)
    )
)

object NtpSyncManager {
    private val _ntpStatus = MutableStateFlow(NtpStatus())
    val ntpStatus: StateFlow<NtpStatus> = _ntpStatus.asStateFlow()

    suspend fun triggerManualSync(serverHost: String = "id.pool.ntp.org"): NtpStatus = withContext(Dispatchers.IO) {
        // Simulate network NTP handshake with microsecond calculation
        val simulatedLatency = (10..25).random()
        val simulatedDrift = (-5L..5L).random()
        val simulatedJitter = (0.2 + (0..5).random() * 0.1)

        val updatedServers = _ntpStatus.value.availableServers.map {
            if (it.host == serverHost) {
                it.copy(isPrimary = true, latencyMs = simulatedLatency, offsetMs = simulatedDrift)
            } else {
                it.copy(isPrimary = false)
            }
        }

        val updated = _ntpStatus.value.copy(
            isSynced = true,
            activeServer = serverHost,
            driftMs = simulatedDrift,
            jitterMs = simulatedJitter,
            lastSyncTime = System.currentTimeMillis(),
            availableServers = updatedServers
        )
        _ntpStatus.value = updated
        updated
    }

    fun getFormattedSyncTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss 'WIB'", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
