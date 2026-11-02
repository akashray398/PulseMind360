package com.dev.myapplication.domain

import android.content.ContextWrapper
import com.dev.myapplication.domain.privacy.PrivacyRiskEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FakePrivacyContext : ContextWrapper(null) {
    override fun checkPermission(permission: String, pid: Int, uid: Int): Int {
        return android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}

class PrivacyRiskEngineTest {

    private val engine = PrivacyRiskEngine(FakePrivacyContext())

    @Test
    fun generateDataInventory_returnsAllNineDataCategories() {
        val inventory = engine.generateDataInventory()
        assertEquals(9, inventory.size)

        val categories = inventory.map { it.category }
        assertTrue(categories.contains("Battery Telemetry"))
        assertTrue(categories.contains("Thermal Telemetry"))
        assertTrue(categories.contains("Memory Statistics"))
        assertTrue(categories.contains("Network Metadata"))
        assertTrue(categories.contains("Device Diagnostics"))
        assertTrue(categories.contains("Monitoring History"))
        assertTrue(categories.contains("Conversation History"))
        assertTrue(categories.contains("Diagnostic Reports"))
        assertTrue(categories.contains("User Preferences"))
    }

    @Test
    fun evaluatePrivacyRisks_detectsDisabledBiometricVaultAndUnredactedExport() {
        val risks = engine.evaluatePrivacyRisks(
            isBiometricVaultEnabled = false,
            isKeystoreAvailable = true,
            isPdfRedactionEnabled = false
        )

        assertEquals(2, risks.size)
        assertTrue(risks.any { it.affectedComponent == "BiometricVaultLockScreen" })
        assertTrue(risks.any { it.affectedComponent == "ReportGenerator & DiagnosticBundleDialog" })
    }

    @Test
    fun redactPublicIp_correctlyRedactsIPv4Address() {
        val originalIp = "104.28.142.89"
        val redacted = engine.redactPublicIp(originalIp)
        assertEquals("104.28.xxx.xxx", redacted)
    }

    @Test
    fun generateFullReport_calculatesAccuratePrivacyScore() {
        val report = engine.generateFullReport(
            isBiometricVaultEnabled = true,
            isPdfRedactionEnabled = true
        )

        assertTrue(report.score in 50..100)
        assertFalse(report.dataInventory.isEmpty())
        assertFalse(report.permissionSummary.isEmpty())
    }
}
