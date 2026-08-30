package com.example.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BluetoothSpeakerEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.BellViewModel

@Composable
fun AudioBluetoothScreen(
    viewModel: BellViewModel,
    modifier: Modifier = Modifier
) {
    val allSpeakers by viewModel.allSpeakers.collectAsState()
    val isPlayingTone by viewModel.isPlayingTone.collectAsState()

    var announcementText by remember { mutableStateOf("") }
    var selectedBroadcastZone by remember { mutableStateOf("ALL") }

    val soundLibrary = listOf(
        Triple("WESTMINSTER_CHIME", "Westminster Chime (4-Nada)", "Melodi bel klasik sekolah penanda masuk & pergantian jam"),
        Triple("THREE_TONE_MELODY", "Chime 3-Nada (Do - Mi - Sol)", "Nada singkat melodi C5-E5-G5 untuk pergantian jam KBM"),
        Triple("NATIONAL_ANTHEM", "Lagu Kebangsaan / Upacara", "Fanfare pembuka upacara bendera hari Senin & apel pagi"),
        Triple("MARS_SEKOLAH", "Mars Sekolah & Relaksasi", "Musik ritmis penyemangat di jam istirahat sekolah"),
        Triple("PRAYER_CALL", "Pengingat Ibadah & Doa", "Chime nada lembut waktu persiapan sholat Dzuhur & Dhuha"),
        Triple("ANNOUNCEMENT", "Ding-Dong Pengumuman", "Dua nada A5-D5 untuk pembuka siaran pengumuman penting"),
        Triple("EMERGENCY_SIREN", "Sirine Tanggap Darurat", "Nada osilasi frekuensi 440-900Hz untuk evakuasi darurat")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // 1. Live PA Announcement Broadcast Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            Icon(Icons.Default.Mic, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Siaran Audio / Pengumuman Langsung (PA)",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        StatusBadge(text = "LIVE MIC BROADCAST", statusType = "CYAN")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = announcementText,
                        onValueChange = { announcementText = it },
                        label = { Text("Teks Pesan Pengumuman Sekolah") },
                        placeholder = { Text("Contoh: Seluruh ketua kelas 10 agar berkumpul di ruang OSIS...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Zone Selector Chip
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Target:", color = TextMutedDark, fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            val zones = listOf("ALL", "KELAS", "LAPANGAN", "GURU")
                            zones.forEach { zone ->
                                val isSelected = selectedBroadcastZone == zone
                                Text(
                                    text = zone,
                                    color = if (isSelected) CyanPrimary else TextSecondaryDark,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .clickable { selectedBroadcastZone = zone }
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val text = if (announcementText.isNotBlank()) announcementText else "Perhatian, ada pengumuman penting."
                                viewModel.broadcastAnnouncement(text, selectedBroadcastZone)
                                announcementText = ""
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pancarkan", color = DeepNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 2. Multi-Bluetooth Audio Router
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Multi Bluetooth Speaker Devices (${allSpeakers.size} Speaker)",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(text = "Dual A2DP Sinkron", statusType = "SUCCESS")
            }
        }

        items(allSpeakers, key = { it.id }) { speaker ->
            BluetoothSpeakerCard(
                speaker = speaker,
                onToggleConnect = { viewModel.toggleSpeaker(speaker.id, speaker.isConnected) },
                onVolumeChange = { viewModel.setSpeakerVolume(speaker.id, it) }
            )
        }

        // 3. Built-in Chime Sound Library
        item {
            Text(
                text = "Pustaka Melodi & Suara Bel (Built-in Synthesizer)",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(soundLibrary) { (toneType, title, desc) ->
            val isPlaying = isPlayingTone == toneType
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sound_item_$toneType"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPlaying) CyanPrimary.copy(alpha = 0.15f) else DarkSurface
                ),
                border = BorderStroke(1.dp, if (isPlaying) CyanPrimary else DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(if (isPlaying) CyanPrimary else DarkCard, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.VolumeUp else Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = if (isPlaying) DeepNavy else AccentAmber,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = title,
                                color = if (isPlaying) CyanPrimary else Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = desc,
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                viewModel.stopAudioPreview()
                            } else {
                                viewModel.previewTone(toneType)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.StopCircle else Icons.Default.PlayCircleFilled,
                            contentDescription = "Putar Audio",
                            tint = if (isPlaying) AccentRed else CyanPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BluetoothSpeakerCard(
    speaker: BluetoothSpeakerEntity,
    onToggleConnect: () -> Unit,
    onVolumeChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("speaker_card_${speaker.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, if (speaker.isConnected) AccentBlue.copy(alpha = 0.4f) else DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(
                                if (speaker.isConnected) AccentBlue.copy(alpha = 0.2f) else DarkCard,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speaker,
                            contentDescription = null,
                            tint = if (speaker.isConnected) AccentBlue else TextMutedDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = speaker.name,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "MAC: ${speaker.macAddress} • Latency: ${speaker.latencyMs}ms",
                            color = TextSecondaryDark,
                            fontSize = 10.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(
                        text = "Zona: ${speaker.zone}",
                        statusType = "WARNING"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = speaker.isConnected,
                        onCheckedChange = { onToggleConnect() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentBlue
                        )
                    )
                }
            }

            if (speaker.isConnected) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeDown,
                        contentDescription = null,
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Slider(
                        value = speaker.volume.toFloat(),
                        onValueChange = { onVolumeChange(it.toInt()) },
                        valueRange = 0f..100f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = AccentBlue,
                            activeTrackColor = AccentBlue,
                            inactiveTrackColor = DarkBorder
                        )
                    )
                    Text(
                        text = "${speaker.volume}%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
