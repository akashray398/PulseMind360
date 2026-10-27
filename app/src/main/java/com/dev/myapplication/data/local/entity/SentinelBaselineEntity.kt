package com.dev.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sentinel_baselines")
data class SentinelBaselineEntity(
    @PrimaryKey
    val id: Int = 1,
    val avgDrainRatePerHour: Float = 7.5f,
    val avgTemperature: Float = 31.0f,
    val avgNetworkKbps: Float = 120.0f,
    val sampleCount: Int = 1,
    val lastUpdated: Long = System.currentTimeMillis()
)
