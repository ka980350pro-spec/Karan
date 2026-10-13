package com.example.data

import com.example.data.dao.AnalysisDao
import com.example.data.dao.FeedbackDao
import com.example.data.model.AnalysisRecord
import com.example.data.model.UserFeedback
import kotlinx.coroutines.flow.Flow

class GameRepository(
    private val analysisDao: AnalysisDao,
    private val feedbackDao: FeedbackDao
) {
    val allAnalysisRecords: Flow<List<AnalysisRecord>> = analysisDao.getAllRecords()
    val allFeedback: Flow<List<UserFeedback>> = feedbackDao.getAllFeedback()

    fun getRecordsForCycle(cycleId: Int): Flow<List<AnalysisRecord>> {
        return analysisDao.getRecordsForCycle(cycleId)
    }

    suspend fun getRecordByCycleAndPeriod(cycleId: Int, periodNumber: String): AnalysisRecord? {
        return analysisDao.getRecordByCycleAndPeriod(cycleId, periodNumber)
    }

    suspend fun insertRecord(record: AnalysisRecord): Long {
        return analysisDao.insertRecord(record)
    }

    suspend fun updateStatus(id: Long, status: String) {
        analysisDao.updateStatus(id, status)
    }

    suspend fun updateUserFeedback(id: Long, feedback: String?) {
        analysisDao.updateUserFeedback(id, feedback)
    }

    suspend fun updateStatusAndFeedback(id: Long, status: String, feedback: String?) {
        analysisDao.updateStatusAndFeedback(id, status, feedback)
    }

    fun getRecordsByPredictedPeriod(predictedPeriod: String): Flow<List<AnalysisRecord>> {
        return analysisDao.getRecordsByPredictedPeriod(predictedPeriod)
    }

    fun getRecordsWithFeedback(): Flow<List<AnalysisRecord>> {
        return analysisDao.getRecordsWithFeedback()
    }

    suspend fun clearHistory() {
        analysisDao.clearAll()
    }

    suspend fun getMaxCycleId(): Int? {
        return analysisDao.getMaxCycleId()
    }

    suspend fun insertFeedback(feedback: UserFeedback): Long {
        return feedbackDao.insertFeedback(feedback)
    }
}
