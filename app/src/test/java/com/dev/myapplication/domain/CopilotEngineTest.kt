package com.dev.myapplication.domain

import com.dev.myapplication.domain.copilot.CopilotIntent
import com.dev.myapplication.domain.copilot.CopilotIntentEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CopilotEngineTest {

    private val intentEngine = CopilotIntentEngine()

    @Test
    fun classifyIntent_correctlyIdentifiesBatteryDrainQueries() {
        val query = "Why is my battery draining faster today?"
        val intent = intentEngine.classifyIntent(query)
        assertEquals(CopilotIntent.BATTERY_DRAIN_WHY, intent)
    }

    @Test
    fun classifyIntent_correctlyIdentifiesThermalQueries() {
        val query = "Why is my phone heating?"
        val intent = intentEngine.classifyIntent(query)
        assertEquals(CopilotIntent.THERMAL_HEATING_WHY, intent)
    }

    @Test
    fun classifyIntent_correctlyIdentifiesNetworkQueries() {
        val query = "Is my network stable?"
        val intent = intentEngine.classifyIntent(query)
        assertEquals(CopilotIntent.NETWORK_STABILITY, intent)
    }

    @Test
    fun classifyIntent_correctlyIdentifiesAnomalyQueries() {
        val query = "What are my latest anomalies?"
        val intent = intentEngine.classifyIntent(query)
        assertEquals(CopilotIntent.LATEST_ANOMALIES, intent)
    }

    @Test
    fun classifyIntent_correctlyIdentifiesFollowUpHistoryQueries() {
        val query = "Has this happened before?"
        val intent = intentEngine.classifyIntent(query)
        assertEquals(CopilotIntent.FOLLOW_UP_HISTORY, intent)
    }

    @Test
    fun classifyIntent_correctlyHandlesUnsupportedQueries() {
        val query = "What is the capital of France?"
        val intent = intentEngine.classifyIntent(query)
        assertEquals(CopilotIntent.UNSUPPORTED, intent)
    }
}
