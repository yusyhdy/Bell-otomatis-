package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.data.local.entity.BellScheduleEntity
import com.example.ui.components.AddEditScheduleDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.BellViewModel

@Composable
fun ScheduleMatrixScreen(
    viewModel: BellViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDay by viewModel.selectedDay.collectAsState()
    val daySchedules by viewModel.daySchedules.collectAsState()
    val allPresets by viewModel.allPresets.collectAsState()
    val activePreset by viewModel.activePreset.collectAsState()
    val isPlayingTone by viewModel.isPlayingTone.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<BellScheduleEntity?>(null) }
    var scheduleToDelete by remember { mutableStateOf<BellScheduleEntity?>(null) }

    val daysList = listOf(
        1 to "Senin",
        2 to "Selasa",
        3 to "Rabu",
        4 to "Kamis",
        5 to "Jumat",
        6 to "Sabtu",
        7 to "Minggu"
    )

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
        ) {
            // 1. Preset Mode Switcher
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Mode Preset Jadwal Bel",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(
                                text = "Aktif: ${activePreset?.name ?: "REGULER"}",
                                statusType = "CYAN"
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(allPresets.size) { index ->
                                val preset = allPresets[index]
                                val isSelected = (activePreset?.id == preset.id)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) CyanPrimary else DarkCard,
                                    border = BorderStroke(1.dp, if (isSelected) CyanPrimary else DarkBorder),
                                    modifier = Modifier.clickable { viewModel.switchPreset(preset.id) }
                                ) {
                                    Text(
                                        text = preset.name,
                                        color = if (isSelected) DeepNavy else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Day Selector Tabs
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(daysList.size) { index ->
                        val (dayNum, dayName) = daysList[index]
                        val isSelected = selectedDay == dayNum
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) AccentAmber else DarkSurface,
                            border = BorderStroke(1.dp, if (isSelected) AccentAmber else DarkBorder),
                            modifier = Modifier
                                .clickable { viewModel.setSelectedDay(dayNum) }
                                .testTag("day_tab_$dayNum")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dayName,
                                    color = if (isSelected) DeepNavy else Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 3. Schedule List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Matriks Bel ${daysList.find { it.first == selectedDay }?.second ?: ""} (${daySchedules.size} Sesi)",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Gunakan panah untuk urutan",
                        color = TextMutedDark,
                        fontSize = 11.sp
                    )
                }
            }

            // 4. Schedule Items
            if (daySchedules.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventBusy,
                                contentDescription = null,
                                tint = TextMutedDark,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Belum Ada Jadwal Bel",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Klik tombol '+' di bawah untuk menambahkan jadwal baru.",
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(daySchedules, key = { _, item -> item.id }) { index, schedule ->
                    ScheduleRowCard(
                        schedule = schedule,
                        index = index,
                        totalCount = daySchedules.size,
                        isPlaying = isPlayingTone == schedule.toneType,
                        onMoveUp = { viewModel.reorderSchedules(index, index - 1) },
                        onMoveDown = { viewModel.reorderSchedules(index, index + 1) },
                        onToggle = { viewModel.toggleSchedule(schedule) },
                        onEdit = { scheduleToEdit = schedule },
                        onDelete = { scheduleToDelete = schedule },
                        onPlay = {
                            if (isPlayingTone == schedule.toneType) {
                                viewModel.stopAudioPreview()
                            } else {
                                viewModel.previewTone(schedule.toneType)
                            }
                        }
                    )
                }
            }
        }

        // Floating Action Button to Add Schedule
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = CyanPrimary,
            contentColor = DeepNavy,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 20.dp)
                .testTag("add_schedule_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Jadwal", modifier = Modifier.size(28.dp))
        }
    }

    if (showAddDialog) {
        AddEditScheduleDialog(
            initialSchedule = null,
            currentDay = selectedDay,
            currentPreset = activePreset?.id ?: "REGULER",
            onDismiss = { showAddDialog = false },
            onSave = {
                viewModel.saveSchedule(it)
                showAddDialog = false
            },
            onPlayTone = { viewModel.previewTone(it) }
        )
    }

    if (scheduleToEdit != null) {
        AddEditScheduleDialog(
            initialSchedule = scheduleToEdit,
            currentDay = selectedDay,
            currentPreset = activePreset?.id ?: "REGULER",
            onDismiss = { scheduleToEdit = null },
            onSave = {
                viewModel.saveSchedule(it)
                scheduleToEdit = null
            },
            onPlayTone = { viewModel.previewTone(it) }
        )
    }

    if (scheduleToDelete != null) {
        val schedule = scheduleToDelete!!
        AlertDialog(
            onDismissRequest = { scheduleToDelete = null },
            title = { Text("Hapus Jadwal Bel?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus '${schedule.title}' (${schedule.time})?", color = TextSecondaryDark) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSchedule(schedule)
                        scheduleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { scheduleToDelete = null }) {
                    Text("Batal", color = TextSecondaryDark)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun ScheduleRowCard(
    schedule: BellScheduleEntity,
    index: Int,
    totalCount: Int,
    isPlaying: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("schedule_card_${schedule.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (schedule.isEnabled) DarkSurface else DarkCard.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, if (isPlaying) CyanPrimary else DarkBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reorder controls
                Column(
                    modifier = Modifier.padding(end = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Naikkan Urutan",
                            tint = if (index > 0) CyanPrimary else TextMutedDark.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < totalCount - 1,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Turunkan Urutan",
                            tint = if (index < totalCount - 1) CyanPrimary else TextMutedDark.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Time Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (schedule.isEnabled) CyanPrimary.copy(alpha = 0.15f) else DarkCard,
                    border = BorderStroke(1.dp, if (schedule.isEnabled) CyanPrimary.copy(alpha = 0.4f) else DarkBorder)
                ) {
                    Text(
                        text = schedule.time,
                        color = if (schedule.isEnabled) CyanPrimary else TextMutedDark,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title & Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.title,
                        color = if (schedule.isEnabled) Color.White else TextMutedDark,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${schedule.audioTitle} • Pre-trigger: ${schedule.ampPreTriggerSeconds}s",
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                }

                // Enable Switch
                Switch(
                    checked = schedule.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag("toggle_schedule_${schedule.id}")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DarkBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Action row: Target Zone Badge, Play preview, Edit, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(text = "Zona: ${schedule.targetZones}", statusType = "WARNING")

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPlay, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.StopCircle else Icons.Default.PlayCircleFilled,
                            contentDescription = "Preview Tone",
                            tint = if (isPlaying) AccentRed else CyanPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondaryDark, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = AccentRed.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
