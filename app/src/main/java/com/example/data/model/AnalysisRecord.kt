package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Entity schema for storing analysis results.
 * Includes fields for:
 * - [timestamp]: When the analysis result was generated/saved
 * - [predictedPeriod]: The round/period analyzed and predicted (e.g. "042", "777")
 * - [userFeedback]: Optional user feedback text/notes regarding the prediction outcome
 */
@Entity(
    tableName = "analysis_history",
    indices = [
        Index(value = ["cycleId", "period_number"], unique = true),
        Index(value = ["predicted_period"]),
        Index(value = ["timestamp"])
    ]
)
data class AnalysisRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cycleId: Int = 1,
    @ColumnInfo(name = "period_number")
    val periodNumber: String, // "000" to "999"
    @ColumnInfo(name = "predicted_period")
    val predictedPeriod: String = periodNumber, // Explicit field for predicted period
    val resultNumber: Int,
    val classification: String, // "BIG" or "SMALL"
    val percentage: Float, // e.g. 92.5
    val status: String = "PENDING", // "PENDING", "CORRECT", "WRONG"
    @ColumnInfo(name = "user_feedback")
    val userFeedback: String? = null, // User feedback on the analysis result
    val timestamp: Long = System.currentTimeMillis() // Timestamp when analysis was recorded
)

// Convenient typealias for AnalysisResult nomenclature
typealias AnalysisResult = AnalysisRecord
