package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PushNotificationBanner
import com.example.ui.components.StatusBadge
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BellViewModel

enum class BellNavigationItem(
    val label: String,
    val icon: ImageVector,
    val testTag: String
) {
    DASHBOARD("Dasbor", Icons.Default.Dashboard, "nav_dashboard"),
    SCHEDULE("Jadwal", Icons.Default.Schedule, "nav_schedule"),
    IOT_MQTT("IoT & MQTT", Icons.Default.AccountTree, "nav_iot_mqtt"),
    AUDIO_BT("Audio & BT", Icons.Default.SpeakerGroup, "nav_audio_bt"),
    ANALYTICS("Analitik", Icons.Default.Insights, "nav_analytics"),
    SECURITY("Cloud Sync", Icons.Default.CloudSync, "nav_security")
}

class MainActivity : ComponentActivity() {
    private val viewModel: BellViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScaffold(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: BellViewModel) {
    var currentNav by remember { mutableStateOf(BellNavigationItem.DASHBOARD) }
    val adminPushAlerts by viewModel.adminPushAlerts.collectAsState()
    val mqttConnected by viewModel.mqttConnected.collectAsState()
    val allNodes by viewModel.allNodes.collectAsState()

    val onlineNodesCount = remember(allNodes) { allNodes.count { it.isOnline } }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepNavy,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SmartBell IoT",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = "SMAN 1 Automated Bell System • ESP32 Hub",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    }
                },
                actions = {
                    StatusBadge(
                        text = if (mqttConnected) "$onlineNodesCount Node Online" else "MQTT Offline",
                        statusType = if (mqttConnected) "SUCCESS" else "ERROR",
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                BellNavigationItem.values().forEach { item ->
                    val isSelected = currentNav == item
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentNav = item },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) CyanPrimary else TextMutedDark,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                color = if (isSelected) CyanPrimary else TextMutedDark,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CyanPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DeepNavy)
        ) {
            // Live Admin Push Notification Toast
            PushNotificationBanner(
                alert = adminPushAlerts.firstOrNull(),
                onDismiss = { viewModel.dismissPushAlert(it) }
            )

            // Screen Content
            when (currentNav) {
                BellNavigationItem.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToSchedule = { currentNav = BellNavigationItem.SCHEDULE },
                    onNavigateToMqtt = { currentNav = BellNavigationItem.IOT_MQTT }
                )
                BellNavigationItem.SCHEDULE -> ScheduleMatrixScreen(viewModel = viewModel)
                BellNavigationItem.IOT_MQTT -> ArchitectureMqttScreen(viewModel = viewModel)
                BellNavigationItem.AUDIO_BT -> AudioBluetoothScreen(viewModel = viewModel)
                BellNavigationItem.ANALYTICS -> AnalyticsLogsScreen(viewModel = viewModel)
                BellNavigationItem.SECURITY -> CloudSecurityScreen(viewModel = viewModel)
            }
        }
    }
}
