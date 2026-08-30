package com.example.data.mqtt

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MqttBrokerConfig(
    val host: String = "broker.hivemq.com",
    val port: Int = 8883,
    val clientId: String = "SmartBell-MobileAdmin-01",
    val useTls: Boolean = true,
    val username: String = "admin_school",
    val keepAliveSeconds: Int = 60,
    val baseTopic: String = "school/bell"
)

data class MqttLogMessage(
    val id: String,
    val topic: String,
    val payload: String,
    val qos: Int = 1,
    val isIncoming: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

object MqttManager {
    private val _config = MutableStateFlow(MqttBrokerConfig())
    val config: StateFlow<MqttBrokerConfig> = _config.asStateFlow()

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _messageStream = MutableSharedFlow<MqttLogMessage>(replay = 20)
    val messageStream: SharedFlow<MqttLogMessage> = _messageStream.asSharedFlow()

    private val _liveMessages = MutableStateFlow<List<MqttLogMessage>>(emptyList())
    val liveMessages: StateFlow<List<MqttLogMessage>> = _liveMessages.asStateFlow()

    init {
        // Initial simulated broker handshake log
        val initMsg = MqttLogMessage(
            id = "INIT-001",
            topic = "school/bell/status",
            payload = "{\"event\":\"BROKER_CONNECTED\",\"broker\":\"broker.hivemq.com:8883\",\"tls\":true,\"client\":\"SmartBell-MobileAdmin-01\"}",
            isIncoming = true
        )
        _liveMessages.value = listOf(initMsg)
    }

    fun updateConfig(newConfig: MqttBrokerConfig) {
        _config.value = newConfig
        logMessage(
            topic = "${newConfig.baseTopic}/config",
            payload = "{\"action\":\"CONFIG_UPDATED\",\"host\":\"${newConfig.host}\",\"port\":${newConfig.port}}",
            isIncoming = false
        )
    }

    fun setConnected(connected: Boolean) {
        _isConnected.value = connected
        logMessage(
            topic = "${_config.value.baseTopic}/status",
            payload = if (connected) "{\"status\":\"ONLINE\",\"qos\":1}" else "{\"status\":\"OFFLINE\",\"reason\":\"NETWORK_DISCONNECTED\"}",
            isIncoming = true
        )
    }

    suspend fun publishCommand(
        topicSuffix: String,
        action: String,
        parameters: Map<String, Any> = emptyMap()
    ): String {
        val topic = "${_config.value.baseTopic}/$topicSuffix"
        val paramsJson = parameters.entries.joinToString(",") { "\"${it.key}\":\"${it.value}\"" }
        val payload = "{\"action\":\"$action\",\"timestamp\":${System.currentTimeMillis()},\"sender\":\"ADMIN_MOBILE\"${if (paramsJson.isNotEmpty()) ",$paramsJson" else ""}}"
        
        logMessage(topic, payload, isIncoming = false)
        return payload
    }

    fun logMessage(topic: String, payload: String, isIncoming: Boolean, qos: Int = 1) {
        val message = MqttLogMessage(
            id = "MQTT-${System.currentTimeMillis()}-${(100..999).random()}",
            topic = topic,
            payload = payload,
            qos = qos,
            isIncoming = isIncoming
        )
        _messageStream.tryEmit(message)
        val current = _liveMessages.value.toMutableList()
        current.add(0, message)
        if (current.size > 50) current.removeAt(current.size - 1)
        _liveMessages.value = current
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
