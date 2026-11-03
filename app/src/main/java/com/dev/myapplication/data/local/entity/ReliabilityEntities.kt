package com.dev.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reliability_assessments")
data class ReliabilityAssessmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val componentName: String, // BATTERY, THERMAL, MEMORY, NETWORK, SYSTEM
    val reliabilityScore: Int = 100, // 0 - 100
    val coveragePct: Int = 100,
    val evidenceJson: String = "[]",
    val timestamp: Long = System.currentTimeMillis()
)
