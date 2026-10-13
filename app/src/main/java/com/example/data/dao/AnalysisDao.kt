package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AnalysisRecord
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for storing, querying, and updating analysis results,
 * including predicted period, user feedback, and timestamp.
 */
@Dao
interface AnalysisDao {
    @Query("SELECT * FROM analysis_history ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AnalysisRecord>>

    @Query("SELECT * FROM analysis_history WHERE cycleId = :cycleId ORDER BY timestamp DESC")
    fun getRecordsForCycle(cycleId: Int): Flow<List<AnalysisRecord>>

    @Query("SELECT * FROM analysis_history WHERE cycleId = :cycleId AND (period_number = :periodNumber OR predicted_period = :periodNumber) LIMIT 1")
    suspend fun getRecordByCycleAndPeriod(cycleId: Int, periodNumber: String): AnalysisRecord?

    @Query("SELECT * FROM analysis_history WHERE predicted_period = :predictedPeriod OR period_number = :predictedPeriod ORDER BY timestamp DESC")
    fun getRecordsByPredictedPeriod(predictedPeriod: String): Flow<List<AnalysisRecord>>

    @Query("SELECT * FROM analysis_history WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): AnalysisRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AnalysisRecord): Long

    @Update
    suspend fun updateRecord(record: AnalysisRecord)

    @Query("UPDATE analysis_history SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE analysis_history SET user_feedback = :userFeedback WHERE id = :id")
    suspend fun updateUserFeedback(id: Long, userFeedback: String?)

    @Query("UPDATE analysis_history SET status = :status, user_feedback = :userFeedback WHERE id = :id")
    suspend fun updateStatusAndFeedback(id: Long, status: String, userFeedback: String?)

    @Query("SELECT * FROM analysis_history WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getRecordsByTimestampRange(startTime: Long, endTime: Long): Flow<List<AnalysisRecord>>

    @Query("SELECT * FROM analysis_history WHERE user_feedback IS NOT NULL AND user_feedback != '' ORDER BY timestamp DESC")
    fun getRecordsWithFeedback(): Flow<List<AnalysisRecord>>

    @Query("DELETE FROM analysis_history WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM analysis_history")
    suspend fun clearAll()

    @Query("SELECT MAX(cycleId) FROM analysis_history")
    suspend fun getMaxCycleId(): Int?
}
