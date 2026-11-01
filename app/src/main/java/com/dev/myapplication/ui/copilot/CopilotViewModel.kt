package com.dev.myapplication.ui.copilot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.myapplication.data.monitor.BatteryMonitor
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkMonitor
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.domain.AiCopilotEngine
import com.dev.myapplication.domain.CopilotResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class ChatSender { USER, AI }

data class CopilotChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: ChatSender,
    val timestamp: Long = System.currentTimeMillis(),
    val response: CopilotResponse? = null,
    val text: String? = null
)

@HiltViewModel
class CopilotViewModel @Inject constructor(
    private val aiCopilotEngine: AiCopilotEngine,
    private val batteryMonitor: BatteryMonitor,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    val batteryState: StateFlow<BatteryState> = batteryMonitor.batteryState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BatteryState()
    )

    val networkState: StateFlow<NetworkState> = networkMonitor.networkState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NetworkState()
    )

    private val _messages = MutableStateFlow<List<CopilotChatMessage>>(emptyList())
    val messages: StateFlow<List<CopilotChatMessage>> = _messages.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    val quickPrompts = listOf(
        "🔋 Why is my battery draining faster today?",
        "🌡️ What caused my device temperature to rise?",
        "📶 How is my network data usage performing?",
        "🚨 Explain my recent critical alerts",
        "⚡ What should I do to improve device health?"
    )

    init {
        // Welcome message initialization
        val initialGreeting = CopilotChatMessage(
            sender = ChatSender.AI,
            text = "👋 Hello! I am **PulseMind AI Copilot**.\n\nI correlate real-time battery telemetry, thermal readings, network throughput, and logged system incidents to give you evidence-based insights.\n\nAsk me anything or select a prompt below to get started!"
        )
        _messages.value = listOf(initialGreeting)
    }

    fun onInputTextChanged(newText: String) {
        _inputText.value = newText
    }

    fun sendQuery(userText: String = _inputText.value) {
        val query = userText.trim()
        if (query.isEmpty()) return

        val userMsg = CopilotChatMessage(
            sender = ChatSender.USER,
            text = query
        )

        _messages.value = _messages.value + userMsg
        _inputText.value = ""
        _isThinking.value = true

        viewModelScope.launch {
            // Simulate slight AI thinking latency for realistic UX
            delay(600)
            val currentBattery = batteryState.value
            val currentNetwork = networkState.value
            val response = aiCopilotEngine.answerQuery(query, currentBattery, currentNetwork)

            val aiMsg = CopilotChatMessage(
                sender = ChatSender.AI,
                response = response
            )

            _isThinking.value = false
            _messages.value = _messages.value + aiMsg
        }
    }

    fun clearHistory() {
        _messages.value = listOf(
            CopilotChatMessage(
                sender = ChatSender.AI,
                text = "Chat history reset. How can I assist you with your device vitals?"
            )
        )
    }
}
