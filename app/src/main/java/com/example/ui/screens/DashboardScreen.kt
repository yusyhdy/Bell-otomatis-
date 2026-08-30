package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MicrocontrollerNodeEntity
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.BellViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: BellViewModel,
    onNavigateToSchedule: () -> Unit,
    onNavigateToMqtt: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTimeMillis by viewModel.currentTimeMillis.collectAsState()
    val upcomingBell by viewModel.upcomingBell.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()
    val ntpStatus by viewModel.ntpStatus.collectAsState()
    val isPlayingTone by viewModel.isPlayingTone.collectAsState()
    val mqttConnected by viewModel.mqttConnected.collectAsState()

    var showEmergencyDialog by remember { mutableStateOf(false) }

    val clockFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // 1. Live NTP Synchronized Clock & Network Time Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ntp_clock_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.4f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    CyanPrimary.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(AccentGreen, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "NTP TIME SYNCED",
                                    color = AccentGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                            StatusBadge(
                                text = "Drift: +${ntpStatus.driftMs}ms (Stratum ${ntpStatus.stratum})",
                                statusType = "SUCCESS"
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Large Digital Clock
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    text = clockFormat.format(Date(currentTimeMillis)),
                                    color = Color.White,
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 2.sp
                                )
                                Text(
                                    text = dateFormat.format(Date(currentTimeMillis)),
                                    color = TextSecondaryDark,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            FilledTonalButton(
                                onClick = { allNodes.firstOrNull()?.let { viewModel.syncNtp(it.id) } },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = CyanPrimary.copy(alpha = 0.2f),
                                    contentColor = CyanPrimary
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync NTP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = DarkBorder)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Server: ${ntpStatus.activeServer}",
                                color = TextMutedDark,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "MQTT Broker: ${if (mqttConnected) "Connected (TLS)" else "Offline"}",
                                color = if (mqttConnected) AccentGreen else AccentRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 2. Upcoming Bell Countdown Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upcoming_bell_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = BorderStroke(1.dp, if (upcomingBell != null) AccentAmber.copy(alpha = 0.5f) else DarkBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = AccentAmber,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "JADWAL BEL BERIKUTNYA",
                                color = AccentAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        TextButton(
                            onClick = onNavigateToSchedule,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Lihat Matriks", color = CyanPrimary, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (upcomingBell != null) {
                        val item = upcomingBell!!
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusBadge(text = "Pukul ${item.time} WIB", statusType = "CYAN")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusBadge(text = "Zona: ${item.targetZones}", statusType = "WARNING")
                                }
                            }

                            // Countdown Box
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = DeepNavy,
                                border = BorderStroke(1.dp, AccentAmber.copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "HITUNG MUNDUR",
                                        color = TextMutedDark,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = viewModel.formatCountdown(item.secondsRemaining),
                                        color = AccentAmber,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Pre-Trigger Amplifier Warning
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkCard, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Power,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Auto Pre-Trigger Relay Amplifier: 5 detik sebelum bel (${viewModel.formatCountdown(item.preTriggerCountdown)})",
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Seluruh Bel Terjadwal Hari Ini Selesai",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Sistem tetap siaga mendengarkan perintah MQTT & sinkronisasi NTP.",
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. Microcontroller Nodes & Relay Status Cards
        item {
            Text(
                text = "Status Node Mikrokontroler & Relay",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(allNodes) { node ->
            MicrocontrollerNodeCard(
                node = node,
                onToggleAmplifier = { viewModel.toggleAmplifier(node.id, node.amplifierState) },
                onSyncNtp = { viewModel.syncNtp(node.id) },
                onRestart = { viewModel.restartNode(node) }
            )
        }

        // 4. Quick Chime Playback Test Bench
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_chime_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Uji Coba Cepat Nada Bel (Synthesizer)",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isPlayingTone != null) {
                            IconButton(
                                onClick = { viewModel.stopAudioPreview() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.StopCircle,
                                    contentDescription = "Stop",
                                    tint = AccentRed,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val quickTones = listOf(
                        Triple("WESTMINSTER_CHIME", "Westminster 4-Nada", Icons.Default.Notifications),
                        Triple("THREE_TONE_MELODY", "Chime 3-Nada (Do-Mi-Sol)", Icons.Default.MusicNote),
                        Triple("NATIONAL_ANTHEM", "Lagu Kebangsaan", Icons.Default.Flag),
                        Triple("ANNOUNCEMENT", "Ding-Dong Pengumuman", Icons.Default.Campaign)
                    )

                    quickTones.forEach { (type, label, icon) ->
                        val isPlaying = isPlayingTone == type
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isPlaying) CyanPrimary.copy(alpha = 0.15f) else DarkCard,
                            border = BorderStroke(1.dp, if (isPlaying) CyanPrimary else DarkBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    if (isPlaying) viewModel.stopAudioPreview() else viewModel.previewTone(type)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isPlaying) CyanPrimary else TextSecondaryDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = label,
                                        color = if (isPlaying) CyanPrimary else Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.VolumeUp else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (isPlaying) CyanPrimary else AccentAmber,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Emergency Siren Activation
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("emergency_banner_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AccentRed.copy(alpha = 0.12f)),
                border = BorderStroke(1.5.dp, AccentRed.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = AccentRed, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SIRINE DARURAT / EVAKUASI",
                                color = AccentRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Aktifkan relay seluruh amplifier & bunyikan sirine darurat otomatis ke semua zona.",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { showEmergencyDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        modifier = Modifier.testTag("emergency_siren_button")
                    ) {
                        Text("SIRINE", fontWeight = FontWeight.Black, color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showEmergencyDialog) {
        AlertDialog(
            onDismissRequest = { showEmergencyDialog = false },
            title = { Text("Konfirmasi Sirine Evakuasi Darurat", color = AccentRed, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Apakah Anda yakin ingin memicu sirine darurat? Perintah MQTT akan dikirim ke seluruh mikrokontroler dan relay amplifier otomatis aktif.",
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEmergencyDialog = false
                        viewModel.triggerEmergencyAlarm()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("AKTIFKAN SIRINE", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showEmergencyDialog = false }) {
                    Text("Batal", color = TextSecondaryDark)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun MicrocontrollerNodeCard(
    node: MicrocontrollerNodeEntity,
    onToggleAmplifier: () -> Unit,
    onSyncNtp: () -> Unit,
    onRestart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("node_card_${node.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, if (node.amplifierState) AccentGreen.copy(alpha = 0.5f) else DarkBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = node.nodeName,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${node.chipModel} • IP: ${node.ipAddress}",
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                }

                StatusBadge(
                    text = if (node.isOnline) "ONLINE" else "OFFLINE",
                    statusType = if (node.isOnline) "SUCCESS" else "ERROR"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Amplifier Relay Switch Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (node.amplifierState) AccentGreen.copy(alpha = 0.12f) else DarkCard,
                border = BorderStroke(1.dp, if (node.amplifierState) AccentGreen.copy(alpha = 0.4f) else DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (node.amplifierState) AccentGreen.copy(alpha = 0.2f) else TextMutedDark.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Power,
                                contentDescription = null,
                                tint = if (node.amplifierState) AccentGreen else TextMutedDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Power Relay Amplifier (GPIO ${node.relayPin})",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (node.amplifierState) "RELAY AKTIF (TOA 240W ON)" else "STANDBY (Hemat Daya)",
                                color = if (node.amplifierState) AccentGreen else TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = node.amplifierState,
                        onCheckedChange = { onToggleAmplifier() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentGreen,
                            uncheckedThumbColor = TextMutedDark,
                            uncheckedTrackColor = DarkSurface
                        ),
                        modifier = Modifier.testTag("switch_amp_${node.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metrics row: RSSI & Last NTP Sync
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "WiFi RSSI: ${node.rssi} dBm",
                    color = TextMutedDark,
                    fontSize = 11.sp
                )
                Text(
                    text = "FW: ${node.firmwareVersion}",
                    color = TextMutedDark,
                    fontSize = 11.sp
                )
                Text(
                    text = "Reboot",
                    color = CyanPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onRestart() }
                )
            }
        }
    }
}
