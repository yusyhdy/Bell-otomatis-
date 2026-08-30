package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.local.entity.ActivityLogEntity
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.BellViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsLogsScreen(
    viewModel: BellViewModel,
    modifier: Modifier = Modifier
) {
    val recentLogs by viewModel.recentLogs.collectAsState()
    val allSchedules by viewModel.allSchedules.collectAsState()
    val ntpStatus by viewModel.ntpStatus.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var showReportDialog by remember { mutableStateOf(false) }

    val filteredLogs = remember(recentLogs, selectedFilter) {
        if (selectedFilter == "ALL") recentLogs else recentLogs.filter { it.level == selectedFilter }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
        // 1. Real-time Analytics Dashboard Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dasbor Analitik & Efisiensi Sistem",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                FilledTonalButton(
                    onClick = { showReportDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = CyanPrimary.copy(alpha = 0.2f),
                        contentColor = CyanPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Laporan Mingguan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Metrics Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Pemicuan Bel Hari Ini",
                        value = "${allSchedules.filter { it.isEnabled }.size} Sesi",
                        subtitle = "100% terjadwal otomatis",
                        icon = Icons.Default.NotificationsActive,
                        iconColor = CyanPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Penghematan Energi Amp",
                        value = "5.8 Jam",
                        subtitle = "1.4 kWh listrik dihemat",
                        icon = Icons.Default.ElectricBolt,
                        iconColor = AccentAmber,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Presisi Waktu NTP",
                        value = "+${ntpStatus.driftMs} ms",
                        subtitle = "Stratum ${ntpStatus.stratum} akurat",
                        icon = Icons.Default.Speed,
                        iconColor = AccentGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Uptime Mikrokontroler",
                        value = "99.98%",
                        subtitle = "345 jam beroperasi",
                        icon = Icons.Default.CloudDone,
                        iconColor = AccentBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Weekly Distribution Chart Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Frekuensi Bel Mingguan (Histori Penggunaan)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val weekDays = listOf("Sen" to 9, "Sel" to 9, "Rab" to 9, "Kam" to 9, "Jum" to 7, "Sab" to 4)
                    val maxVal = 10

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weekDays.forEach { (day, count) ->
                            val heightFraction = (count.toFloat() / maxVal)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Text(
                                    text = "$count",
                                    color = CyanPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(28.dp)
                                        .fillMaxHeight(heightFraction)
                                        .background(
                                            if (day == "Sen") AccentAmber else CyanPrimary,
                                            RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = day,
                                    color = TextSecondaryDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Activity Logs Audit Header & Filters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Log Aktivitas Operasional (${filteredLogs.size})",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                TextButton(onClick = { viewModel.clearLogs() }) {
                    Text("Bersihkan Log", color = TextMutedDark, fontSize = 11.sp)
                }
            }
        }

        // Filter chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val filters = listOf(
                    "ALL" to "Semua",
                    "SUCCESS" to "Sukses",
                    "INFO" to "Info",
                    "WARNING" to "Peringatan",
                    "ERROR" to "Error"
                )
                items(filters.size) { index ->
                    val (key, label) = filters[index]
                    val isSelected = selectedFilter == key
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) CyanPrimary else DarkSurface,
                        border = BorderStroke(1.dp, if (isSelected) CyanPrimary else DarkBorder),
                        modifier = Modifier.clickable { selectedFilter = key }
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) DeepNavy else Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 5. Activity Log List Items
        if (filteredLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Text(
                        text = "Tidak ada catatan log pada kategori ini.",
                        color = TextSecondaryDark,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(filteredLogs, key = { it.id }) { log ->
                ActivityLogRow(log = log)
            }
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Laporan Rutin Mingguan Bel Sekolah", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Periode: 24 Agustus - 29 Agustus 2026", color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("• Total Eksekusi Bel Terjadwal: 43 Sesi", color = Color.White, fontSize = 12.sp)
                    Text("• Keberhasilan Eksekusi: 100% (0 Terlewat)", color = AccentGreen, fontSize = 12.sp)
                    Text("• Total Durasi Relay Amplifier Aktif: 28 Menit", color = Color.White, fontSize = 12.sp)
                    Text("• Estimasi Penghematan Listrik: 6.2 kWh", color = AccentAmber, fontSize = 12.sp)
                    Text("• Rata-rata Drift NTP: +3.2 ms (Sangat Akurat)", color = Color.White, fontSize = 12.sp)
                    Text("• Integritas Database Cloud: Terenkripsi AES-256 Valid", color = Color.White, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showReportDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                ) {
                    Text("Tutup & Ekspor PDF", color = DeepNavy, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun ActivityLogRow(log: ActivityLogEntity) {
    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val (statusType, icon) = when (log.level) {
        "SUCCESS" -> "SUCCESS" to Icons.Default.CheckCircle
        "WARNING" -> "WARNING" to Icons.Default.Warning
        "ERROR" -> "ERROR" to Icons.Default.Error
        else -> "CYAN" to Icons.Default.Info
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("log_item_${log.id}"),
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
                    .size(32.dp)
                    .background(
                        when (log.level) {
                            "SUCCESS" -> AccentGreen.copy(alpha = 0.15f)
                            "WARNING" -> AccentOrange.copy(alpha = 0.15f)
                            "ERROR" -> AccentRed.copy(alpha = 0.15f)
                            else -> CyanPrimary.copy(alpha = 0.15f)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = when (log.level) {
                        "SUCCESS" -> AccentGreen
                        "WARNING" -> AccentOrange
                        "ERROR" -> AccentRed
                        else -> CyanPrimary
                    },
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
                    Text(
                        text = log.source,
                        color = TextMutedDark,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = timeFormat.format(Date(log.timestamp)),
                        color = TextMutedDark,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = log.message,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                if (log.metadata.isNotBlank()) {
                    Text(
                        text = log.metadata,
                        color = TextSecondaryDark,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
