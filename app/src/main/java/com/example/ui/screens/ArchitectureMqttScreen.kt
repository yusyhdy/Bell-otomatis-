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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.mqtt.MqttBrokerConfig
import com.example.data.mqtt.MqttLogMessage
import com.example.data.mqtt.MqttManager
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.BellViewModel

@Composable
fun ArchitectureMqttScreen(
    viewModel: BellViewModel,
    modifier: Modifier = Modifier
) {
    val mqttConfig by viewModel.mqttConfig.collectAsState()
    val mqttConnected by viewModel.mqttConnected.collectAsState()
    val mqttMessages by viewModel.mqttMessages.collectAsState()
    val ntpStatus by viewModel.ntpStatus.collectAsState()

    var showConfigDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Arsitektur, 1=MQTT Console

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Screen Top Tab Switcher
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = CyanPrimary,
            divider = {},
            indicator = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier
                    .padding(4.dp)
                    .background(if (selectedTab == 0) CyanPrimary.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AccountTree, contentDescription = null, tint = if (selectedTab == 0) CyanPrimary else TextMutedDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Arsitektur", color = if (selectedTab == 0) CyanPrimary else TextSecondaryDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier
                    .padding(4.dp)
                    .background(if (selectedTab == 1) AccentOrange.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Power, contentDescription = null, tint = if (selectedTab == 1) AccentOrange else TextMutedDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Panduan Amp", color = if (selectedTab == 1) AccentOrange else TextSecondaryDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                modifier = Modifier
                    .padding(4.dp)
                    .background(if (selectedTab == 2) CyanPrimary.copy(alpha = 0.15f) else Color.Transparent, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = if (selectedTab == 2) CyanPrimary else TextMutedDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("MQTT Console", color = if (selectedTab == 2) CyanPrimary else TextSecondaryDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> {
                // Architecture Diagram & Microcontroller Specs View
                ArchitectureTopologyView(
                    mqttConnected = mqttConnected,
                    ntpServer = ntpStatus.activeServer,
                    onOpenMqttConfig = { showConfigDialog = true }
                )
            }
            1 -> {
                // Interactive Amplifier Auto-On & Relay Wiring Documentation View
                AmplifierWiringGuideView(
                    viewModel = viewModel
                )
            }
            else -> {
                // Live MQTT Message Stream Console & Command Bench
                MqttConsoleView(
                    config = mqttConfig,
                    isConnected = mqttConnected,
                    messages = mqttMessages,
                    onToggleConnection = { viewModel.toggleMqttConnection() },
                    onSendCommand = { action, params -> viewModel.sendMqttCommand(action, params) },
                    onOpenConfig = { showConfigDialog = true }
                )
            }
        }
    }

    if (showConfigDialog) {
        MqttConfigDialog(
            initialConfig = mqttConfig,
            onDismiss = { showConfigDialog = false },
            onSave = {
                viewModel.updateMqttConfig(it)
                showConfigDialog = false
            }
        )
    }
}

@Composable
fun ArchitectureTopologyView(
    mqttConnected: Boolean,
    ntpServer: String,
    onOpenMqttConfig: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Topologi Sistem Otomasi Bel Sekolah",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onOpenMqttConfig) {
                            Icon(Icons.Default.Settings, contentDescription = "Konfigurasi", tint = CyanPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Flow Pipeline Architecture Steps
                    TopologyPipelineStep(
                        stepNumber = "1",
                        title = "Waktu Presisi NTP (Network Time Protocol)",
                        desc = "Sinkronisasi mikrodetik via $ntpServer (UDP Port 123) untuk akurasi jam pelajaran tanpa drift.",
                        badgeText = "STRATUM 2",
                        badgeColor = AccentGreen,
                        icon = Icons.Default.Schedule
                    )

                    TopologyConnectorLine()

                    TopologyPipelineStep(
                        stepNumber = "2",
                        title = "Jalur Komunikasi MQTT Broker Real-Time",
                        desc = "Protokol pub/sub asinkron untuk update jadwal instan dari Web Dashboard & Mobile Admin.",
                        badgeText = if (mqttConnected) "BROKER TLS ACTIVE" else "OFFLINE",
                        badgeColor = if (mqttConnected) CyanPrimary else AccentRed,
                        icon = Icons.Default.CloudSync
                    )

                    TopologyConnectorLine()

                    TopologyPipelineStep(
                        stepNumber = "3",
                        title = "Mikrokontroler ESP32 IoT Gateway Node",
                        desc = "Dual Core 240MHz + FreeRTOS task scheduler memicu timer bel, decoding audio PCM, dan GPIO relay.",
                        badgeText = "ESP32-WROOM-32D",
                        badgeColor = AccentAmber,
                        icon = Icons.Default.Memory
                    )

                    TopologyConnectorLine()

                    TopologyPipelineStep(
                        stepNumber = "4",
                        title = "Optocoupler Relay Kontrol Amplifier",
                        desc = "Otomatis mengaktifkan relay GPIO 23 (5s Pre-trigger pemanasan dan auto-off setelah audio selesai).",
                        badgeText = "GPIO 23 RELAY",
                        badgeColor = AccentOrange,
                        icon = Icons.Default.Power
                    )

                    TopologyConnectorLine()

                    TopologyPipelineStep(
                        stepNumber = "5",
                        title = "Multi-Bluetooth Audio Router (Zone)",
                        desc = "Distribusi audio bel, musik pengiring, dan pengumuman suara ke multi speaker kelas & lapangan.",
                        badgeText = "4 ZONA AKTIF",
                        badgeColor = AccentPurple,
                        icon = Icons.Default.BluetoothAudio
                    )
                }
            }
        }

        // Hardware Specs Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Spesifikasi Perangkat Keras & Pinout",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val hardwareSpecs = listOf(
                        "Mikrokontroler Hub" to "ESP32-WROOM-32D Dual-Core Xtensa LX6 240MHz",
                        "Memori & Flash" to "520 KB SRAM, 4 MB SPI Flash Memory",
                        "Relay Modul Power" to "5V Optocoupler Relay Module (Pin GPIO 23)",
                        "DAC / Audio Decoder" to "I2S MAX98357A / Bluetooth A2DP Dual Mode",
                        "Protokol Jaringan" to "Wi-Fi 802.11 b/g/n, MQTT v3.1.1 TLS, NTP RFC 5905",
                        "Daya Cadangan" to "12V 7Ah UPS Battery Backup (Auto-failover)"
                    )

                    hardwareSpecs.forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = label, color = TextSecondaryDark, fontSize = 12.sp)
                            Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        HorizontalDivider(color = DarkBorder.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TopologyPipelineStep(
    stepNumber: String,
    title: String,
    desc: String,
    badgeText: String,
    badgeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = DarkCard,
        border = BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(badgeColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    StatusBadge(text = badgeText, statusType = "SUCCESS")
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = desc, color = TextSecondaryDark, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun TopologyConnectorLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 30.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(14.dp)
                .background(CyanPrimary.copy(alpha = 0.5f))
        )
    }
}

@Composable
fun MqttConsoleView(
    config: MqttBrokerConfig,
    isConnected: Boolean,
    messages: List<MqttLogMessage>,
    onToggleConnection: () -> Unit,
    onSendCommand: (String, Map<String, Any>) -> Unit,
    onOpenConfig: () -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // MQTT Connection & Command Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Broker: ${config.host}:${config.port}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Base Topic: ${config.baseTopic}/# • TLS Enabled",
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        }

                        Row {
                            IconButton(onClick = onOpenConfig) {
                                Icon(Icons.Default.Tune, contentDescription = "Setting", tint = CyanPrimary)
                            }
                            Button(
                                onClick = onToggleConnection,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isConnected) AccentGreen else AccentRed
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(if (isConnected) "ONLINE" else "CONNECT", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Command Actions
                    Text(text = "Kirim Perintah Uji Coba MQTT:", color = TextMutedDark, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { onSendCommand("TRIGGER_BELL", mapOf("chime" to "WESTMINSTER")) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text("Trigger Bel", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = { onSendCommand("TOGGLE_RELAY", mapOf("pin" to 23, "state" to "TOGGLE")) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text("Toggle Relay", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = { onSendCommand("REQ_NTP_SYNC", mapOf("source" to "ADMIN")) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text("Req Sync NTP", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Live Log Messages
        item {
            Text(
                text = "Live MQTT Log Stream (${messages.size} Payload):",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        items(messages, key = { it.id }) { msg ->
            MqttMessageItem(msg = msg)
        }
    }
}

@Composable
fun MqttMessageItem(msg: MqttLogMessage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DeepNavy),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(
                        text = if (msg.isIncoming) "INCOMING" else "PUBLISHED",
                        statusType = if (msg.isIncoming) "CYAN" else "SUCCESS"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = msg.topic,
                        color = AccentAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = MqttManager.formatTime(msg.timestamp),
                    color = TextMutedDark,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DarkSurface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = msg.payload,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun MqttConfigDialog(
    initialConfig: MqttBrokerConfig,
    onDismiss: () -> Unit,
    onSave: (MqttBrokerConfig) -> Unit
) {
    var host by remember { mutableStateOf(initialConfig.host) }
    var port by remember { mutableStateOf(initialConfig.port.toString()) }
    var clientId by remember { mutableStateOf(initialConfig.clientId) }
    var baseTopic by remember { mutableStateOf(initialConfig.baseTopic) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Konfigurasi MQTT Broker", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("MQTT Broker Host") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, focusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("Port (8883 / 1883)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, focusedTextColor = Color.White)
                )
                OutlinedTextField(
                    value = baseTopic,
                    onValueChange = { baseTopic = it },
                    label = { Text("Base Topic (contoh: school/bell)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanPrimary, focusedTextColor = Color.White)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newConfig = initialConfig.copy(
                        host = host,
                        port = port.toIntOrNull() ?: 8883,
                        clientId = clientId,
                        baseTopic = baseTopic
                    )
                    onSave(newConfig)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("Simpan", color = DeepNavy, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Batal", color = TextSecondaryDark) }
        },
        containerColor = DarkSurface
    )
}

@Composable
fun AmplifierWiringGuideView(
    viewModel: BellViewModel
) {
    val nodes by viewModel.allNodes.collectAsState()
    val isPlayingTone by viewModel.isPlayingTone.collectAsState()
    val mainNode = nodes.firstOrNull()
    val isAmpOn = mainNode?.amplifierState ?: false

    var selectedWiringDiagramTab by remember { mutableIntStateOf(0) } // 0=Pinout, 1=AC Power, 2=Audio Jack

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Hero Card: Live Amplifier Control & State
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, if (isAmpOn) AccentOrange.copy(alpha = 0.6f) else DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (isAmpOn) AccentOrange.copy(alpha = 0.2f) else DarkSurfaceVariant,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Power,
                                    contentDescription = null,
                                    tint = if (isAmpOn) AccentOrange else TextMutedDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Status Otomasi Relay Amplifier",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (isAmpOn) "⚡ DAYA ON (Relay GPIO 23 Aktif)" else "💤 STANDBY (Hemat Energi & Hening)",
                                    color = if (isAmpOn) AccentOrange else AccentGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        StatusBadge(
                            text = if (isAmpOn) "RELAY ACTIVE" else "STANDBY",
                            statusType = if (isAmpOn) "WARNING" else "SUCCESS"
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Relay modul secara cerdas memutus & menyambungkan jalur daya AC 220V ke Amplifier TOA / PA. Sistem mengaktifkan daya 5 detik sebelum audio berbunyi (Pre-Trigger) dan otomatis mematikan daya setelah audio selesai untuk menghemat listrik dan mencegah dengung speaker.",
                        color = TextSecondaryDark,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (mainNode != null) {
                                    viewModel.toggleAmplifier(mainNode.id, isAmpOn)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAmpOn) AccentRed.copy(alpha = 0.2f) else AccentOrange.copy(alpha = 0.2f),
                                contentColor = if (isAmpOn) AccentRed else AccentOrange
                            ),
                            border = BorderStroke(1.dp, if (isAmpOn) AccentRed.copy(alpha = 0.5f) else AccentOrange.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isAmpOn) "Matikan Manual" else "Nyalakan Manual", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.previewTone("WESTMINSTER_CHIME")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isPlayingTone != null) "Membunyikan..." else "Uji Siklus Bel", color = DeepNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section 1: Cara Kerja Siklus Otomasi Relay (3 Fase)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Siklus Kerja Otomasi Relay 3-Fase",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val phases = listOf(
                        Triple(
                            "Fase 1: Pre-Trigger Warmup (T - 5 detik)",
                            "ESP32 mengaktifkan Relay GPIO 23 sebelum waktu bel tiba. Hal ini memberi waktu bagi kapasitor power supply amplifier untuk mengisi daya penuh sehingga tidak terjadi lonjakan suara letupan ('thump/pop') saat nada bel mulai dimainkan.",
                            AccentOrange
                        ),
                        Triple(
                            "Fase 2: Pemutaran Nada Bel / Audio Chime (T = 0)",
                            "Engine audio (I2S DAC / Bluetooth Audio) mengalirkan sinyal suara jernih ke jalur AUX Amplifier tanpa distorsi. Seluruh speaker di kelas & lapangan membunyikan nada dengan volume stabil.",
                            CyanPrimary
                        ),
                        Triple(
                            "Fase 3: Post-Bell Cooldown & Auto-Standby (T + 3 detik)",
                            "Setelah audio selesai, mikrokontroler menunggu jeda pendinginan selama 3 detik untuk meredam gema sisa, lalu memutus relay GPIO 23 ke posisi Standby (0 Watt daya terbuang).",
                            AccentGreen
                        )
                    )

                    phases.forEachIndexed { idx, (title, desc, color) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(color.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    color = color,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = title, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = desc, color = TextSecondaryDark, fontSize = 11.sp, lineHeight = 16.sp)
                            }
                        }
                        if (idx < phases.size - 1) {
                            HorizontalDivider(color = DarkBorder.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }

        // Section 2: Skema Wiring & Sambungan Fisik (Pinout Table & Guide)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Skema Sambungan Kabel & Pinout Perangkat",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Tab Diagram Switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceVariant, RoundedCornerShape(10.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val tabs = listOf("ESP32 ke Relay", "Relay ke Amplifier", "Jalur Audio")
                        tabs.forEachIndexed { index, title ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (selectedWiringDiagramTab == index) CyanPrimary else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedWiringDiagramTab = index }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    color = if (selectedWiringDiagramTab == index) DeepNavy else TextSecondaryDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when (selectedWiringDiagramTab) {
                        0 -> {
                            // Wiring ESP32 to Relay
                            val esp32Wiring = listOf(
                                Triple("ESP32 5V (VIN)", "Modul Relay VCC", "Suplai daya 5V untuk koil relay"),
                                Triple("ESP32 GND", "Modul Relay GND", "Ground bersama (Common Ground)"),
                                Triple("ESP32 GPIO 23", "Modul Relay IN1", "Sinyal pemicu aktif (Active HIGH / LOW)"),
                                Triple("Jumper Opto (VCC-JDVCC)", "Terhubung (Default)", "Optocoupler isolator tegangan")
                            )

                            esp32Wiring.forEach { (pinA, pinB, note) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(0.45f)) {
                                        Text(text = pinA, color = CyanPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(14.dp))
                                    Column(modifier = Modifier.weight(0.45f), horizontalAlignment = Alignment.End) {
                                        Text(text = pinB, color = AccentAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        Text(text = note, color = TextMutedDark, fontSize = 10.sp)
                                    }
                                }
                                HorizontalDivider(color = DarkBorder.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                        1 -> {
                            // Relay to Amplifier AC Power
                            val acWiring = listOf(
                                Triple("Kabel Fasa AC (PLN 220V)", "Terminal COM Relay", "Jalur arus utama PLN"),
                                Triple("Terminal NO (Normally Open)", "Kabel Power Amplifier L", "Arus hanya mengalir saat bel aktif"),
                                Triple("Kabel Netral AC (PLN 220V)", "Kabel Power Amplifier N", "Tersambung langsung ke Amplifier"),
                                Triple("Saklar Fisik Amplifier", "Posisi ON Terus", "Relay menggantikan fungsi saklar fisik")
                            )

                            acWiring.forEach { (src, dest, desc) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(0.45f)) {
                                        Text(text = src, color = AccentOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(14.dp))
                                    Column(modifier = Modifier.weight(0.45f), horizontalAlignment = Alignment.End) {
                                        Text(text = dest, color = AccentGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(text = desc, color = TextMutedDark, fontSize = 10.sp)
                                    }
                                }
                                HorizontalDivider(color = DarkBorder.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                        else -> {
                            // Audio Cable Routing
                            val audioWiring = listOf(
                                Triple("I2S DAC / Bluetooth Out", "Kabel RCA / Jack 3.5mm", "Keluaran audio stereo dari ESP32"),
                                Triple("Input AUX 1 Amplifier", "Line In Port TOA", "Pengaturan Volume AUX diatur pada 60%"),
                                Triple("Output 100V High-Z TOA", "Speaker Kolom Ruang Kelas", "Distribusi suara jangkauan jauh"),
                                Triple("Output 4-16 Ohm Low-Z", "Speaker Horn Lapangan", "Khusus area luar ruangan terbuka")
                            )

                            audioWiring.forEach { (src, dest, desc) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 5.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(0.45f)) {
                                        Text(text = src, color = AccentPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(14.dp))
                                    Column(modifier = Modifier.weight(0.45f), horizontalAlignment = Alignment.End) {
                                        Text(text = dest, color = CyanPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(text = desc, color = TextMutedDark, fontSize = 10.sp)
                                    }
                                }
                                HorizontalDivider(color = DarkBorder.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Langkah Konfigurasi Praktis di Sekolah
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Langkah-Langkah Instalasi & Konfigurasi",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val steps = listOf(
                        "1. Siapkan Modul Relay 5V dengan isolasi Optocoupler (disarankan tipe Songle 10A 250VAC).",
                        "2. Hubungkan pin kendali IN relay ke Pin GPIO 23 ESP32, serta pin daya ke VCC 5V dan GND.",
                        "3. Sambungkan salah satu kabel daya AC PLN 220V (Fasa/Live) ke terminal COM, dan kabel menuju steker amplifier ke terminal NO (Normally Open).",
                        "4. Pastikan saklar tombol power pada unit Amplifier TOA dalam keadaan ON terus menerus.",
                        "5. Atur nilai 'Pre-Trigger Warmup' di aplikasi ke 5 detik agar amplifier memiliki waktu pemanasan sirkuit sebelum file MP3 / PCM diputar.",
                        "6. Lakukan uji coba dengan tombol 'Uji Siklus Bel' untuk memastikan amplifier menyala, memutar suara, lalu otomatis mati."
                    )

                    steps.forEach { stepText ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stepText,
                                color = TextSecondaryDark,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Contoh Kode Firmware ESP32 (C++/Arduino)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.dp, DarkBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Contoh Firmware ESP32 (C++ / Arduino)",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        StatusBadge(text = "C++ ARDUINO", statusType = "CYAN")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val codeSnippet = """
#define RELAY_PIN 23
#define PRE_TRIGGER_MS 5000
#define POST_TRIGGER_MS 3000

void setup() {
  pinMode(RELAY_PIN, OUTPUT);
  digitalWrite(RELAY_PIN, LOW); // Standby OFF
}

void triggerBellSchedule(String audioFile) {
  // 1. Fase Pre-Trigger: Nyalakan Amplifier
  digitalWrite(RELAY_PIN, HIGH);
  delay(PRE_TRIGGER_MS); 
  
  // 2. Fase Playback: Putar Audio Bel
  playAudioPCM(audioFile); 
  
  // 3. Fase Post-Trigger: Matikan kembali
  delay(POST_TRIGGER_MS);
  digitalWrite(RELAY_PIN, LOW); // Auto Standby
}
                    """.trimIndent()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = codeSnippet,
                            color = AccentGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

