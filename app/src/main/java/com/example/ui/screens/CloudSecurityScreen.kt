package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.cloud.AcademicCalendarEvent
import com.example.data.cloud.SyncDevice
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.BellViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CloudSecurityScreen(
    viewModel: BellViewModel,
    modifier: Modifier = Modifier
) {
    val backupPayload by viewModel.lastBackupPayload.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()
    val connectedDevices by viewModel.connectedDevices.collectAsState()

    var passphraseInput by remember { mutableStateOf("SMAN1-SecureKey-2026") }
    var isAutoMuteHolidayEnabled by remember { mutableStateOf(true) }

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // 1. End-to-End Encrypted Cloud Backup
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cloud_backup_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EnhancedEncryption, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Backup Cloud & Enkripsi End-to-End",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        StatusBadge(text = "AES-256-GCM", statusType = "CYAN")
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Data jadwal pelajaran dan konfigurasi dienkripsi dengan standar militer AES-256 sebelum diunggah ke cloud storage.",
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = passphraseInput,
                        onValueChange = { passphraseInput = it },
                        label = { Text("Kunci Rahasia Enkripsi (Passphrase)") },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = AccentAmber) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.performEncryptedCloudBackup(passphraseInput) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("backup_now_button")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = DeepNavy)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buat Backup Terenkripsi Sekarang", color = DeepNavy, fontWeight = FontWeight.Bold)
                    }

                    if (backupPayload != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkCard,
                            border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Snapshot: ${backupPayload!!.snapshotId}", color = AccentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "SHA-256 Verified", color = AccentGreen, fontSize = 10.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Cipher: ${backupPayload!!.encryptedData.take(38)}...",
                                    color = TextMutedDark,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Google Calendar Synchronization Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("google_calendar_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Integrasi Google Calendar Sekolah",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = { viewModel.syncGoogleCalendar() }) {
                            Icon(Icons.Default.Sync, contentDescription = "Sync", tint = AccentAmber)
                        }
                    }

                    Text(
                        text = "Sinkronisasi otomatis dengan kalender akademik Google untuk mendeteksi ujian, upacara, dan hari libur nasional.",
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkCard, RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Auto-Mute Bel Saat Hari Libur",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = isAutoMuteHolidayEnabled,
                            onCheckedChange = { isAutoMuteHolidayEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentAmber
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Agenda Akademik Tersinkron:", color = TextMutedDark, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    calendarEvents.forEach { event ->
                        AcademicCalendarEventRow(event = event)
                    }
                }
            }
        }

        // 3. Multi-Device Synchronization Matrix
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Multi-Device Synchronization (${connectedDevices.size} Terhubung)",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(text = "Real-Time Mesh", statusType = "SUCCESS")
            }
        }

        items(connectedDevices, key = { it.deviceId }) { device ->
            SyncDeviceCard(device = device, timeFormat = timeFormat)
        }
    }
}

@Composable
fun AcademicCalendarEventRow(event: AcademicCalendarEvent) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = DarkCard,
        border = BorderStroke(1.dp, DarkBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = event.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = event.dateString, color = TextSecondaryDark, fontSize = 10.sp)
            }
            StatusBadge(
                text = "Preset: ${event.affectedPreset}",
                statusType = if (event.affectedPreset == "MUTED") "WARNING" else "CYAN"
            )
        }
    }
}

@Composable
fun SyncDeviceCard(device: SyncDevice, timeFormat: SimpleDateFormat) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(
                        if (device.isOnline) AccentGreen.copy(alpha = 0.15f) else TextMutedDark.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        device.platform.contains("ESP32") -> Icons.Default.Memory
                        device.platform.contains("Web") -> Icons.Default.Computer
                        device.platform.contains("Android") -> Icons.Default.Smartphone
                        else -> Icons.Default.Tablet
                    },
                    contentDescription = null,
                    tint = if (device.isOnline) AccentGreen else TextMutedDark,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = device.deviceName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    StatusBadge(text = device.syncStatus, statusType = "SUCCESS")
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${device.platform} • Last Sync: ${timeFormat.format(Date(device.lastSync))}",
                    color = TextSecondaryDark,
                    fontSize = 10.sp
                )
            }
        }
    }
}
