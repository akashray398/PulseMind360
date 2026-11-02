package com.dev.myapplication.domain.privacy

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.dev.myapplication.util.KeystoreVaultManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrivacyRiskEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private fun isPermissionGranted(permission: String): Boolean {
        return try {
            context.checkPermission(permission, android.os.Process.myPid(), android.os.Process.myUid()) == PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            true
        }
    }

    fun auditPermissions(): List<PermissionAuditItem> {
        val list = mutableListOf<PermissionAuditItem>()

        // 1. Internet Permission
        val internetGranted = isPermissionGranted(android.Manifest.permission.INTERNET)

        list.add(
            PermissionAuditItem(
                permissionName = "android.permission.INTERNET",
                isGranted = internetGranted,
                reasonRequired = "Queries network diagnostics and IP information API.",
                relatedFeature = "Network Diagnostics & Bandwidth Hub",
                riskCategory = RiskCategory.MEDIUM,
                recommendedAction = "Required for network diagnostics. All requests use HTTPS."
            )
        )

        // 2. Access Network State
        val netStateGranted = isPermissionGranted(android.Manifest.permission.ACCESS_NETWORK_STATE)

        list.add(
            PermissionAuditItem(
                permissionName = "android.permission.ACCESS_NETWORK_STATE",
                isGranted = netStateGranted,
                reasonRequired = "Observes network transport type (Wi-Fi/Cellular/Metered) & link speeds.",
                relatedFeature = "Network Monitor & Live Context Engine",
                riskCategory = RiskCategory.LOW,
                recommendedAction = "Required for real-time network state observation."
            )
        )

        // 3. Post Notifications (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notifGranted = isPermissionGranted(android.Manifest.permission.POST_NOTIFICATIONS)

            list.add(
                PermissionAuditItem(
                    permissionName = "android.permission.POST_NOTIFICATIONS",
                    isGranted = notifGranted,
                    reasonRequired = "Displays thermal spike warnings and automation rule notifications.",
                    relatedFeature = "In-App Notifications & Automations",
                    riskCategory = RiskCategory.LOW,
                    recommendedAction = if (notifGranted) "Granted. Toggle off in Settings if alerts are unneeded." else "Grant in system settings if hardware notifications are desired."
                )
            )
        }

        return list
    }

    fun generateDataInventory(): List<DataInventoryItem> {
        return listOf(
            DataInventoryItem("Battery Telemetry", "Discharge rate, temperature, voltage & charge level", "BroadcastReceiver", "Room DB (Local)", "24 Hours Rolling", false, true),
            DataInventoryItem("Thermal Telemetry", "Battery temperature °C & thermal velocity slope", "BatteryManager API", "Room DB (Local)", "24 Hours Rolling", false, true),
            DataInventoryItem("Memory Statistics", "RAM total, available, used MB & usage %", "ActivityManager API", "In-Memory Only", "Real-Time Only", false, true),
            DataInventoryItem("Network Metadata", "Transport type (Wi-Fi/Cellular), link bandwidth", "ConnectivityManager API", "Room DB (Local)", "24 Hours Rolling", false, true),
            DataInventoryItem("Device Diagnostics", "Frame rate FPS, WorkManager task status", "Choreographer API", "In-Memory Only", "Real-Time Only", false, true),
            DataInventoryItem("Monitoring History", "Correlated incidents and system activity logs", "Room Database (companion360.db)", "Room DB (Local)", "User Controlled", false, true),
            DataInventoryItem("Conversation History", "AI Copilot chat queries and diagnostic responses", "Room Database (copilot_messages)", "Room DB (Local)", "User Controlled", false, true),
            DataInventoryItem("Diagnostic Reports", "PDF report exports and diagnostic summaries", "FileProvider Cache", "Local Storage", "On-Demand Only", false, true),
            DataInventoryItem("User Preferences", "App theme, biometric vault settings, telemetry toggles", "Preferences DataStore", "Encrypted DataStore", "Persistent", false, true)
        )
    }

    fun evaluatePrivacyRisks(
        isBiometricVaultEnabled: Boolean,
        isKeystoreAvailable: Boolean,
        isPdfRedactionEnabled: Boolean
    ): List<PrivacyRiskFinding> {
        val risks = mutableListOf<PrivacyRiskFinding>()

        if (!isKeystoreAvailable) {
            risks.add(
                PrivacyRiskFinding(
                    severity = RiskCategory.HIGH,
                    evidence = "Android KeyStore provider returned false for alias 'PulseMind_Vault_Key_AES256'.",
                    affectedComponent = "KeystoreVaultManager",
                    explanation = "Hardware key generation unavailable on current device runtime.",
                    recommendedRemediation = "Verify Android KeyStore hardware backing or device security settings."
                )
            )
        }

        if (!isBiometricVaultEnabled) {
            risks.add(
                PrivacyRiskFinding(
                    severity = RiskCategory.MEDIUM,
                    evidence = "Biometric Vault lock is toggled OFF in User Preferences.",
                    affectedComponent = "BiometricVaultLockScreen",
                    explanation = "Anyone with physical device access can open PulseMind 360 without biometric authentication.",
                    recommendedRemediation = "Enable 'Biometric Lock' in Settings to protect diagnostic records."
                )
            )
        }

        if (!isPdfRedactionEnabled) {
            risks.add(
                PrivacyRiskFinding(
                    severity = RiskCategory.MEDIUM,
                    evidence = "Redact PDF Export Data preference is disabled.",
                    affectedComponent = "ReportGenerator & DiagnosticBundleDialog",
                    explanation = "Exported diagnostic PDF reports will include unredacted public IP address.",
                    recommendedRemediation = "Enable 'Redact PDF Export Data' in Privacy Settings before sharing reports."
                )
            )
        }

        return risks
    }

    fun generateFullReport(
        isBiometricVaultEnabled: Boolean,
        isPdfRedactionEnabled: Boolean
    ): PrivacyAuditReport {
        val permissions = auditPermissions()
        val inventory = generateDataInventory()
        val risks = evaluatePrivacyRisks(isBiometricVaultEnabled, KeystoreVaultManager.isKeyStoreAvailable(), isPdfRedactionEnabled)

        var score = 100
        risks.forEach { r ->
            when (r.severity) {
                RiskCategory.HIGH -> score -= 20
                RiskCategory.MEDIUM -> score -= 10
                RiskCategory.LOW -> score -= 5
            }
        }

        val improvements = listOf(
            "Maintain Android KeyStore AES-256 encrypted local storage.",
            "Keep public IP address redaction enabled during diagnostic report exports.",
            "Enable Biometric Lock to protect conversation history and activity logs."
        )

        return PrivacyAuditReport(
            permissionSummary = permissions,
            dataInventory = inventory,
            privacyRisks = risks,
            recommendedImprovements = improvements,
            score = score.coerceIn(40, 100),
            auditTimestamp = System.currentTimeMillis()
        )
    }

    fun redactPublicIp(ip: String): String {
        if (ip.isBlank() || ip == "N/A" || ip == "Unknown") return ip
        val parts = ip.split(".")
        return if (parts.size == 4) {
            "${parts[0]}.${parts[1]}.xxx.xxx"
        } else {
            "xxx.xxx.xxx.xxx"
        }
    }
}
