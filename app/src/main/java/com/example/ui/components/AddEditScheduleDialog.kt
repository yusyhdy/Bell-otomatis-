package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.BellScheduleEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScheduleDialog(
    initialSchedule: BellScheduleEntity?,
    currentDay: Int,
    currentPreset: String,
    onDismiss: () -> Unit,
    onSave: (BellScheduleEntity) -> Unit,
    onPlayTone: (String) -> Unit
) {
    var title by remember { mutableStateOf(initialSchedule?.title ?: "") }
    var timeText by remember { mutableStateOf(initialSchedule?.time ?: "08:00") }
    var selectedTone by remember { mutableStateOf(initialSchedule?.toneType ?: "THREE_TONE_MELODY") }
    var ampPreTrigger by remember { mutableFloatStateOf((initialSchedule?.ampPreTriggerSeconds ?: 5).toFloat()) }
    var ampPostDelay by remember { mutableFloatStateOf((initialSchedule?.ampPostDelaySeconds ?: 8).toFloat()) }
    var selectedZone by remember { mutableStateOf(initialSchedule?.targetZones ?: "ALL") }
    var isEnabled by remember { mutableStateOf(initialSchedule?.isEnabled ?: true) }

    val toneOptions = listOf(
        "WESTMINSTER_CHIME" to "Westminster Chime (4-Nada)",
        "THREE_TONE_MELODY" to "Chime 3-Nada (Pergantian Jam)",
        "NATIONAL_ANTHEM" to "Lagu Kebangsaan / Upacara",
        "MARS_SEKOLAH" to "Mars Sekolah / Apel",
        "PRAYER_CALL" to "Pengingat Sholat / Ibadah",
        "ANNOUNCEMENT" to "Ding-Dong Pengumuman",
        "EMERGENCY_SIREN" to "Sirine Darurat"
    )

    val zoneOptions = listOf(
        "ALL" to "Seluruh Sekolah (Semua Zona)",
        "KELAS" to "Gedung Ruang Kelas (Lt 1 & 2)",
        "LAPANGAN" to "Lapangan Utama & Hall",
        "KORIDOR" to "Koridor & Laboratorium",
        "GURU" to "Ruang Guru & Tata Usaha"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialSchedule == null) "Tambah Jadwal Bel" else "Edit Jadwal Bel",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = TextMutedDark)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama / Deskripsi Jadwal Bel") },
                    placeholder = { Text("Contoh: Jam Pelajaran Ke-4") },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null, tint = CyanPrimary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("schedule_title_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Time Input
                OutlinedTextField(
                    value = timeText,
                    onValueChange = { timeText = it },
                    label = { Text("Waktu Bel (Format HH:mm)") },
                    placeholder = { Text("07:00") },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = AccentAmber) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("schedule_time_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tone selection
                Text(
                    text = "Pilih Nada / Melodi Bel:",
                    color = TextSecondaryDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                toneOptions.forEach { (type, label) ->
                    val isSelected = selectedTone == type
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .background(
                                if (isSelected) CyanPrimary.copy(alpha = 0.15f) else DarkCard,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedTone = type }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedTone = type },
                            colors = RadioButtonDefaults.colors(selectedColor = CyanPrimary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            color = if (isSelected) CyanPrimary else Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onPlayTone(type) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleFilled,
                                contentDescription = "Dengarkan",
                                tint = if (isSelected) CyanPrimary else AccentAmber,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Target Zone Selector
                Text(
                    text = "Target Zona Speaker Bluetooth:",
                    color = TextSecondaryDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                zoneOptions.forEach { (zone, label) ->
                    val isSelected = selectedZone == zone
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .background(
                                if (isSelected) AccentBlue.copy(alpha = 0.15f) else DarkCard,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedZone = zone }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedZone = zone },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentBlue)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            color = if (isSelected) AccentBlue else Color.White,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amplifier Pre-Trigger Slider
                Text(
                    text = "Otomatis Pemanasan Relay Amplifier (Pre-Trigger): ${ampPreTrigger.toInt()} Detik",
                    color = TextSecondaryDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Slider(
                    value = ampPreTrigger,
                    onValueChange = { ampPreTrigger = it },
                    valueRange = 1f..20f,
                    steps = 18,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentAmber,
                        activeTrackColor = AccentAmber,
                        inactiveTrackColor = DarkBorder
                    )
                )
                Text(
                    text = "Amplifier akan aktif ${ampPreTrigger.toInt()} detik sebelum audio bel berbunyi untuk stabilisasi daya & mencegah dengung.",
                    color = TextMutedDark,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save button
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val audioTitle = toneOptions.find { it.first == selectedTone }?.second ?: "Bel Sekolah"
                            val schedule = (initialSchedule ?: BellScheduleEntity(
                                dayOfWeek = currentDay,
                                time = timeText,
                                title = title,
                                toneType = selectedTone,
                                audioTitle = audioTitle,
                                ampPreTriggerSeconds = ampPreTrigger.toInt(),
                                ampPostDelaySeconds = ampPostDelay.toInt(),
                                targetZones = selectedZone,
                                presetMode = currentPreset,
                                isEnabled = isEnabled
                            )).copy(
                                title = title,
                                time = timeText,
                                toneType = selectedTone,
                                audioTitle = audioTitle,
                                ampPreTriggerSeconds = ampPreTrigger.toInt(),
                                ampPostDelaySeconds = ampPostDelay.toInt(),
                                targetZones = selectedZone,
                                isEnabled = isEnabled
                            )
                            onSave(schedule)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("save_schedule_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = DeepNavy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simpan Jadwal Bel",
                        color = DeepNavy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}
