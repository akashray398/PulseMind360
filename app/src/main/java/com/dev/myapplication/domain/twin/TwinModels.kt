package com.dev.myapplication.domain.twin

data class TwinDeviceCapabilities(
    val hasWifi: Boolean = true,
    val hasCellular: Boolean = true,
    val hasBiometrics: Boolean = true,
    val hasKeystoreVault: Boolean = true,
    val totalRamMb: Long = 6000L
)

data class TwinStateTransition(
    val id: Long,
    val stateType: String,
    val previousValue: String,
    val currentValue: String,
    val effectiveTimestamp: Long
)

data class SimulationResult(
    val scenarioName: String,
    val simulatedBatteryLevel: Int,
    val simulatedTemp: Float,
    val simulatedRamUsagePct: Int,
    val systemImpactText: String,
    val timestamp: Long = System.currentTimeMillis()
)
