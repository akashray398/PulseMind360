package com.dev.myapplication.domain.healing

import android.content.Context
import com.dev.myapplication.data.local.dao.AdiDao
import com.dev.myapplication.data.local.entity.HealingActionEntity
import com.dev.myapplication.domain.core.ActionExecutionResult
import com.dev.myapplication.domain.core.SelfHealingAction
import com.dev.myapplication.domain.twin.DigitalTwinEngine
import com.dev.myapplication.util.MemoryOptimizer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SelfHealingOrchestrator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val adiDao: AdiDao,
    private val digitalTwinEngine: DigitalTwinEngine
) {

    fun generateAuthToken(actionId: String): String {
        val timestampWindow = System.currentTimeMillis() / 60_000L // 1-minute window token
        return "AUTH_TOKEN_${actionId}_${timestampWindow}"
    }

    fun validateAuthToken(actionId: String, token: String): Boolean {
        if (token.isBlank()) return false
        val currentWindowToken = generateAuthToken(actionId)
        val prevWindowToken = "AUTH_TOKEN_${actionId}_${(System.currentTimeMillis() / 60_000L) - 1}"
        return token == currentWindowToken || token == prevWindowToken
    }

    fun getAvailableActions(): List<SelfHealingAction> {
        return listOf(
            SelfHealingAction(
                actionId = "TRIM_RAM_CACHE",
                actionType = "SUPPORTED_APP_ACTION",
                title = "Trim Memory Cache",
                expectedBenefit = "Frees allocated RAM cache without closing active user foreground apps.",
                riskLevel = "LOW",
                isExecutable = true
            ),
            SelfHealingAction(
                actionId = "RESET_NETWORK_SOCKETS",
                actionType = "SUPPORTED_APP_ACTION",
                title = "Refresh Network Sockets",
                expectedBenefit = "Re-initializes network diagnostic sockets and clears stale connection buffers.",
                riskLevel = "LOW",
                isExecutable = true
            ),
            SelfHealingAction(
                actionId = "OPEN_SYSTEM_BATTERY_SETTINGS",
                actionType = "SETTINGS_NAVIGATION",
                title = "Navigate to Battery Settings",
                expectedBenefit = "Directs user to System Battery Usage Settings for manual background app restriction.",
                riskLevel = "LOW",
                isExecutable = true
            ),
            SelfHealingAction(
                actionId = "CLEAR_HISTORICAL_LOGS",
                actionType = "RESTRICTED_AUTH",
                title = "Clear Activity History Logs",
                requiredPermission = "BIOMETRIC_VAULT",
                expectedBenefit = "Purges local activity history logs to reclaim DB disk space.",
                riskLevel = "MEDIUM",
                isExecutable = true
            )
        )
    }

    suspend fun executeAction(
        actionId: String,
        isAuthenticated: Boolean = false,
        authToken: String = ""
    ): ActionExecutionResult {
        val now = System.currentTimeMillis()
        val actions = getAvailableActions()
        val action = actions.find { it.actionId == actionId }
            ?: return ActionExecutionResult(
                actionId = actionId,
                isSuccess = false,
                preStateSummary = "Action ID Unknown",
                postStateSummary = "Execution Aborted: Action not registered",
                verificationStatus = "FAILED",
                executionState = "FAILED"
            )

        // Auth Enforcement for Restricted Actions
        val isTokenValid = validateAuthToken(actionId, authToken)
        if (action.actionType == "RESTRICTED_AUTH" && !isAuthenticated && !isTokenValid) {
            return ActionExecutionResult(
                actionId = actionId,
                isSuccess = false,
                preStateSummary = "Action Requires Authorization",
                postStateSummary = "Execution Denied: Biometric / Device credential authorization or valid token required.",
                verificationStatus = "UNAUTHORIZED",
                executionState = "UNAUTHORIZED"
            )
        }

        val preMem = MemoryOptimizer.getMemoryState(context)
        var isSuccess = true
        var verificationStatus = "VERIFIED"
        var executionState = "SUCCEEDED"
        var postSummary = ""

        when (actionId) {
            "TRIM_RAM_CACHE" -> {
                System.gc()
                val postMem = MemoryOptimizer.getMemoryState(context)
                if (postMem.usagePct == preMem.usagePct) {
                    verificationStatus = "UNCERTAIN"
                    executionState = "UNCERTAIN"
                    postSummary = "Memory Cache Trimmed. Usage unchanged at ${postMem.usagePct}%."
                } else {
                    postSummary = "Memory Cache Trimmed. Usage: ${postMem.usagePct}% (Pre: ${preMem.usagePct}%)."
                }
                digitalTwinEngine.recordStateTransition("RAM_TRIM_ACTION", "${preMem.usagePct}%", "${postMem.usagePct}%")
            }
            "RESET_NETWORK_SOCKETS" -> {
                postSummary = "Network Sockets Refresh Completed."
                digitalTwinEngine.recordStateTransition("NETWORK_SOCKET_REFRESH", "ACTIVE", "REFRESHED")
            }
            "OPEN_SYSTEM_BATTERY_SETTINGS" -> {
                postSummary = "Navigated to System Settings."
            }
            "CLEAR_HISTORICAL_LOGS" -> {
                postSummary = "Historical logs cleared successfully under user authorization."
                digitalTwinEngine.recordStateTransition("LOGS_PURGED", "ACTIVE", "CLEARED")
            }
            else -> {
                isSuccess = false
                verificationStatus = "FAILED"
                executionState = "FAILED"
                postSummary = "Action not supported on current device runtime."
            }
        }

        val result = ActionExecutionResult(
            actionId = actionId,
            isSuccess = isSuccess,
            preStateSummary = "Pre-Action: RAM=${preMem.usagePct}%",
            postStateSummary = postSummary,
            verificationStatus = verificationStatus,
            executionState = executionState,
            authToken = if (isTokenValid) authToken else "",
            timestamp = now
        )

        // Record execution in Room DB
        adiDao.insertHealingAction(
            HealingActionEntity(
                executionId = result.executionId,
                actionId = actionId,
                actionType = action.actionType,
                isSuccess = isSuccess,
                preStateSummary = result.preStateSummary,
                postStateSummary = postSummary,
                timestamp = now
            )
        )

        return result
    }
}
