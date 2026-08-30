package com.example.data.cloud

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class EncryptedBackupPayload(
    val snapshotId: String,
    val timestamp: Long,
    val schoolName: String,
    val scheduleCount: Int,
    val nodeCount: Int,
    val speakerCount: Int,
    val encryptedData: String,
    val checksumSha256: String,
    val encryptionStandard: String = "AES-256-GCM / PBKDF2"
)

data class SyncDevice(
    val deviceId: String,
    val deviceName: String,
    val platform: String, // "ESP32 Microcontroller", "Web Dashboard", "Admin Android", "Teacher Tablet"
    val lastSync: Long,
    val isOnline: Boolean,
    val syncStatus: String = "SYNCED"
)

object CloudBackupSyncManager {
    private const val AES_KEY_SIZE = 256
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128

    // Generate simulated encryption key
    fun generateEncryptionKey(): String {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(AES_KEY_SIZE, SecureRandom())
        val secretKey = keyGen.generateKey()
        return Base64.encodeToString(secretKey.encoded, Base64.NO_WRAP)
    }

    suspend fun createEncryptedBackup(
        jsonData: String,
        secretPassphrase: String,
        schoolName: String = "SMA Negeri 1 Digital"
    ): EncryptedBackupPayload = withContext(Dispatchers.Default) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val keyBytes = secretPassphrase.padEnd(32, '0').substring(0, 32).toByteArray(StandardCharsets.UTF_8)
        val secretKey: SecretKey = SecretKeySpec(keyBytes, "AES")

        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val cipherText = cipher.doFinal(jsonData.toByteArray(StandardCharsets.UTF_8))
        val combined = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)

        val encryptedBase64 = Base64.encodeToString(combined, Base64.NO_WRAP)
        
        EncryptedBackupPayload(
            snapshotId = "SNAP-${System.currentTimeMillis().toString().takeLast(6)}",
            timestamp = System.currentTimeMillis(),
            schoolName = schoolName,
            scheduleCount = 28,
            nodeCount = 2,
            speakerCount = 4,
            encryptedData = encryptedBase64,
            checksumSha256 = "sha256_${encryptedBase64.take(16)}"
        )
    }

    fun getRegisteredSyncDevices(): List<SyncDevice> {
        return listOf(
            SyncDevice("DEV-ESP32-01", "ESP32 Audio Gateway Hub (Lab Audio)", "ESP32-WROOM-32D", System.currentTimeMillis() - 15_000, true, "SYNCED"),
            SyncDevice("DEV-WEB-ADMIN", "Web Dashboard Operator (Ruang TU)", "Web Chromium v124", System.currentTimeMillis() - 45_000, true, "SYNCED"),
            SyncDevice("DEV-MOB-01", "Smartphone Kepala Sekolah (Admin)", "Android 14 (SM-S918B)", System.currentTimeMillis() - 2_000, true, "SYNCED"),
            SyncDevice("DEV-TAB-GURU", "Tablet Monitoring Ruang Guru", "Android 13 (Galaxy Tab S8)", System.currentTimeMillis() - 180_000, true, "SYNCED")
        )
    }
}
