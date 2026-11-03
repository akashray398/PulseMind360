package com.dev.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "digital_twin_states")
data class DigitalTwinStateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val stateType: String,
    val previousValue: String = "",
    val currentValue: String,
    val effectiveTimestamp: Long = System.currentTimeMillis(),
    val isSimulation: Boolean = false
)

@Entity(tableName = "digital_twin_simulations")
data class SimulationSessionEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val scenarioName: String = "WHAT_IF_LOAD_SPIKE",
    val simulatedBatteryDelta: Float = -5.0f,
    val simulatedThermalDelta: Float = +2.5f,
    val createdAt: Long = System.currentTimeMillis()
)
