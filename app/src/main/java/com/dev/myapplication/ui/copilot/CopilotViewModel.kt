package com.dev.myapplication.ui.copilot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.myapplication.data.local.dao.CopilotConversationDao
import com.dev.myapplication.data.local.entity.CopilotConversationEntity
import com.dev.myapplication.data.local.entity.CopilotMessageEntity
import com.dev.myapplication.data.monitor.BatteryMonitor
import com.dev.myapplication.data.monitor.BatteryState
import com.dev.myapplication.data.monitor.NetworkMonitor
import com.dev.myapplication.data.monitor.NetworkState
import com.dev.myapplication.domain.AiCopilotEngine
import com.dev.myapplication.domain.copilot.CopilotIntent
import com.dev.myapplication.domain.copilot.CopilotResponseDetail
import com.dev.myapplication.domain.copilot.UserFeedback
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
    val responseDetail: CopilotResponseDetail? = null,
    val text: String? = null,
    val feedback: UserFeedback = UserFeedback.NONE
)

@HiltViewModel
class CopilotViewModel @Inject constructor(
    private val aiCopilotEngine: AiCopilotEngine,
    private val batteryMonitor: BatteryMonitor,
    private val networkMonitor: NetworkMonitor,
    private val copilotConversationDao: CopilotConversationDao
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

    private val _currentSessionId = MutableStateFlow("default_session")
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    private val _conversations = MutableStateFlow<List<CopilotConversationEntity>>(emptyList())
    val conversations: StateFlow<List<CopilotConversationEntity>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<List<CopilotChatMessage>>(emptyList())
    val messages: StateFlow<List<CopilotChatMessage>> = _messages.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _pendingAuthAction = MutableStateFlow<String?>(null)
    val pendingAuthAction: StateFlow<String?> = _pendingAuthAction.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val quickPrompts = listOf(
        "🔋 Why is my battery draining faster today?",
        "🌡️ Why is my phone heating?",
        "📶 Is my network stable?",
        "🚨 Explain my latest incident",
        "⚡ Is my device behaving normally?",
        "🔍 What should I investigate first?",
        "❓ Has this happened before?"
    )

    init {
        loadSessions()
        loadMessagesForSession(_currentSessionId.value)
    }

    private fun loadSessions() {
        viewModelScope.launch {
            copilotConversationDao.getAllConversations().collect { list ->
                if (list.isEmpty()) {
                    val defaultSession = CopilotConversationEntity(
                        id = "default_session",
                        title = "PulseMind AI Session",
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    copilotConversationDao.insertConversation(defaultSession)
                } else {
                    _conversations.value = list
                }
            }
        }
    }

    fun switchSession(sessionId: String) {
        _currentSessionId.value = sessionId
        loadMessagesForSession(sessionId)
    }

    fun startNewSession() {
        val newId = UUID.randomUUID().toString()
        val newSession = CopilotConversationEntity(
            id = newId,
            title = "Diagnostics Session #${_conversations.value.size + 1}",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        viewModelScope.launch {
            copilotConversationDao.insertConversation(newSession)
            switchSession(newId)
        }
    }

    private fun loadMessagesForSession(sessionId: String) {
        viewModelScope.launch {
            copilotConversationDao.getMessagesForConversation(sessionId).collect { list ->
                if (list.isEmpty()) {
                    val initialGreeting = CopilotChatMessage(
                        sender = ChatSender.AI,
                        text = "👋 Hello! I am **PulseMind AI Copilot**.\n\nI correlate live context telemetry, thermal readings, network throughput, and logged system incidents to give you evidence-based insights.\n\nAsk me anything or select a prompt below to get started!"
                    )
                    _messages.value = listOf(initialGreeting)
                } else {
                    _messages.value = list.map { entity ->
                        if (entity.sender == "USER") {
                            CopilotChatMessage(
                                id = entity.id,
                                sender = ChatSender.USER,
                                timestamp = entity.timestamp,
                                text = entity.queryText
                            )
                        } else {
                            val detail = CopilotResponseDetail(
                                query = entity.queryText,
                                intent = try { CopilotIntent.valueOf(entity.intent) } catch (_: Exception) { CopilotIntent.UNSUPPORTED },
                                severity = entity.severity,
                                summary = entity.summary,
                                measuredFacts = if (entity.factsJson.isNotBlank()) entity.factsJson.split("||") else emptyList(),
                                hypothesisAndEvidence = entity.evidenceText,
                                recommendedActions = if (entity.actionsJson.isNotBlank()) entity.actionsJson.split("||") else emptyList(),
                                relevantMetrics = parseMetricsJson(entity.metricsJson),
                                confidenceScore = entity.confidenceScore,
                                timestamp = entity.timestamp,
                                suggestedFollowUps = if (entity.followUpsJson.isNotBlank()) entity.followUpsJson.split("||") else emptyList()
                            )
                            CopilotChatMessage(
                                id = entity.id,
                                sender = ChatSender.AI,
                                timestamp = entity.timestamp,
                                responseDetail = detail,
                                feedback = try { UserFeedback.valueOf(entity.userFeedback) } catch (_: Exception) { UserFeedback.NONE }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun parseMetricsJson(raw: String): Map<String, String> {
        if (raw.isBlank()) return emptyMap()
        return raw.split("||").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) parts[0] to parts[1] else null
        }.toMap()
    }

    fun onInputTextChanged(newText: String) {
        _inputText.value = newText
    }

    fun sendQuery(userText: String = _inputText.value) {
        val query = userText.trim()
        if (query.isEmpty()) return

        val userMsgId = UUID.randomUUID().toString()
        val userMsg = CopilotChatMessage(
            id = userMsgId,
            sender = ChatSender.USER,
            text = query
        )

        _messages.value = _messages.value + userMsg
        _inputText.value = ""
        _isThinking.value = true

        val sessionId = _currentSessionId.value

        viewModelScope.launch {
            // Save User message to Room
            copilotConversationDao.insertMessage(
                CopilotMessageEntity(
                    id = userMsgId,
                    conversationId = sessionId,
                    sender = "USER",
                    queryText = query
                )
            )

            // Thinking latency
            delay(500)

            val currentBattery = batteryState.value
            val currentNetwork = networkState.value
            val responseDetail = aiCopilotEngine.answerQueryDetailed(query, sessionId, currentBattery, currentNetwork)

            val aiMsg = CopilotChatMessage(
                sender = ChatSender.AI,
                responseDetail = responseDetail
            )

            _isThinking.value = false
            _messages.value = _messages.value + aiMsg
        }
    }

    fun rateMessageFeedback(messageId: String, feedback: UserFeedback) {
        viewModelScope.launch {
            copilotConversationDao.updateMessageFeedback(messageId, feedback.name)
            _messages.value = _messages.value.map { msg ->
                if (msg.id == messageId) msg.copy(feedback = feedback) else msg
            }
        }
    }

    fun requestActionExecution(actionText: String) {
        _pendingAuthAction.value = actionText
    }

    fun confirmActionExecution() {
        _pendingAuthAction.value = null
    }

    fun dismissActionExecution() {
        _pendingAuthAction.value = null
    }

    fun clearCurrentSession() {
        val sessionId = _currentSessionId.value
        viewModelScope.launch {
            copilotConversationDao.deleteMessagesForConversation(sessionId)
            loadMessagesForSession(sessionId)
        }
    }
}
