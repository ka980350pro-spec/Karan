package com.example.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.GameRepository
import com.example.data.model.AnalysisRecord
import com.example.data.model.UserFeedback
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.Random

class GameViewModel(
    private val repository: GameRepository,
    private val prefs: SharedPreferences? = null
) : ViewModel() {

    private val _currentCycle = MutableStateFlow(
        prefs?.getInt(KEY_CURRENT_CYCLE, 1) ?: 1
    )
    val currentCycle: StateFlow<Int> = _currentCycle.asStateFlow()

    private val _periodInput = MutableStateFlow(
        prefs?.getString(KEY_CURRENT_PERIOD, "000") ?: "000"
    )
    val periodInput: StateFlow<String> = _periodInput.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _currentAnalysis = MutableStateFlow<AnalysisRecord?>(null)
    val currentAnalysis: StateFlow<AnalysisRecord?> = _currentAnalysis.asStateFlow()

    private val _isFixedResult = MutableStateFlow(false)
    val isFixedResult: StateFlow<Boolean> = _isFixedResult.asStateFlow()

    private val _newCycleNotice = MutableStateFlow<String?>(null)
    val newCycleNotice: StateFlow<String?> = _newCycleNotice.asStateFlow()

    private val _selectedCycleFilter = MutableStateFlow<Int?>(null) // null means All Cycles
    val selectedCycleFilter: StateFlow<Int?> = _selectedCycleFilter.asStateFlow()

    private val _reviewRating = MutableStateFlow(5)
    val reviewRating: StateFlow<Int> = _reviewRating.asStateFlow()

    private val _reviewComment = MutableStateFlow("")
    val reviewComment: StateFlow<String> = _reviewComment.asStateFlow()

    private val _feedbackSubmittedMessage = MutableStateFlow<String?>(null)
    val feedbackSubmittedMessage: StateFlow<String?> = _feedbackSubmittedMessage.asStateFlow()

    val historyRecords: StateFlow<List<AnalysisRecord>> = repository.allAnalysisRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val feedbackList: StateFlow<List<UserFeedback>> = repository.allFeedback
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Load initial analysis result for the current period if it already exists
        checkAndLoadExistingForPeriod(_currentCycle.value, _periodInput.value)
    }

    fun onPeriodChanged(input: String) {
        val filtered = input.filter { it.isDigit() }
        if (filtered.length <= 3) {
            _periodInput.value = filtered
            savePeriod(filtered)
            if (filtered.length == 3) {
                checkAndLoadExistingForPeriod(_currentCycle.value, filtered)
            } else {
                _currentAnalysis.value = null
                _isFixedResult.value = false
            }
        }
    }

    private var checkJob: kotlinx.coroutines.Job? = null
    private var analyzeJob: kotlinx.coroutines.Job? = null

    private fun checkAndLoadExistingForPeriod(cycleId: Int, period: String) {
        if (period.length != 3) return
        checkJob?.cancel()
        checkJob = viewModelScope.launch {
            val existing = repository.getRecordByCycleAndPeriod(cycleId, period)
            if (existing != null) {
                _currentAnalysis.value = existing
                _isFixedResult.value = true
            } else {
                _currentAnalysis.value = null
                _isFixedResult.value = false
            }
        }
    }

    fun analyzePeriod(simulateDelay: Boolean = true) {
        val period = _periodInput.value.trim()
        if (period.length != 3) return
        val currentCycleVal = _currentCycle.value

        checkJob?.cancel()
        analyzeJob?.cancel()
        analyzeJob = viewModelScope.launch {
            _isAnalyzing.value = true
            
            // First check if fixed result already exists in this cycle
            val existing = repository.getRecordByCycleAndPeriod(currentCycleVal, period)
            if (existing != null) {
                // Return exact fixed saved result
                if (simulateDelay) delay(350)
                _currentAnalysis.value = existing
                _isFixedResult.value = true
                _isAnalyzing.value = false
                return@launch
            }

            // If not found, simulate neural pattern processing and generate permanent fixed result
            if (simulateDelay) delay(650)

            val periodInt = period.toIntOrNull() ?: 0
            // Deterministic calculation for this cycle and period combination
            val cycleSeed = (currentCycleVal.toLong() * 7919L + periodInt.toLong() * 1013L + 2026L)
            val deterministicRandom = Random(cycleSeed)

            // Result single digit 0-9
            val resultDigit = deterministicRandom.nextInt(10)

            // Classify 0,1,2,3,4 as SMALL and 5,6,7,8,9 as BIG
            val classification = if (resultDigit in 0..4) "SMALL" else "BIG"

            // Confidence percentage (84.0% - 97.9%)
            val basePct = 84.0f + (deterministicRandom.nextInt(140) / 10.0f)
            val formattedPct = (Math.round(basePct * 10.0) / 10.0).toFloat()

            val record = AnalysisRecord(
                cycleId = currentCycleVal,
                periodNumber = period,
                predictedPeriod = period,
                resultNumber = resultDigit,
                classification = classification,
                percentage = formattedPct,
                status = "PENDING",
                userFeedback = null,
                timestamp = System.currentTimeMillis()
            )

            val insertedId = repository.insertRecord(record)
            _currentAnalysis.value = record.copy(id = insertedId)
            _isFixedResult.value = true
            _isAnalyzing.value = false
        }
    }

    /**
     * Increment period by 1.
     * When period 999 rolls over to 000, automatically starts a new cycle!
     */
    fun nextPeriod() {
        val currentVal = _periodInput.value.toIntOrNull() ?: -1

        if (currentVal >= 999 || (_periodInput.value == "999")) {
            // Automatic rollover to 000 and starting a new cycle!
            val nextCycle = _currentCycle.value + 1
            _currentCycle.value = nextCycle
            saveCycle(nextCycle)

            val nextPeriodStr = "000"
            _periodInput.value = nextPeriodStr
            savePeriod(nextPeriodStr)

            _newCycleNotice.value = "New Cycle #$nextCycle Started! (Rolled over 999 → 000)"
            viewModelScope.launch {
                delay(4500)
                if (_newCycleNotice.value?.contains("Cycle #$nextCycle") == true) {
                    _newCycleNotice.value = null
                }
            }

            checkAndLoadExistingForPeriod(nextCycle, nextPeriodStr)
        } else {
            val nextVal = if (currentVal < 0) 0 else currentVal + 1
            val nextPeriodStr = String.format(Locale.US, "%03d", nextVal)
            _periodInput.value = nextPeriodStr
            savePeriod(nextPeriodStr)

            checkAndLoadExistingForPeriod(_currentCycle.value, nextPeriodStr)
        }
    }

    /**
     * Manually start a new cycle if needed
     */
    fun startNewCycle() {
        val nextCycle = _currentCycle.value + 1
        _currentCycle.value = nextCycle
        saveCycle(nextCycle)

        val nextPeriodStr = "000"
        _periodInput.value = nextPeriodStr
        savePeriod(nextPeriodStr)

        _newCycleNotice.value = "Cycle #$nextCycle Initiated (Period 000)"
        viewModelScope.launch {
            delay(4000)
            if (_newCycleNotice.value?.contains("Cycle #$nextCycle") == true) {
                _newCycleNotice.value = null
            }
        }

        checkAndLoadExistingForPeriod(nextCycle, nextPeriodStr)
    }

    fun selectCycleFilter(cycleId: Int?) {
        _selectedCycleFilter.value = cycleId
    }

    fun markStatus(status: String) {
        val current = _currentAnalysis.value ?: return
        viewModelScope.launch {
            repository.updateStatus(current.id, status)
            _currentAnalysis.value = current.copy(status = status)
        }
    }

    fun updateAnalysisFeedback(id: Long, feedback: String?) {
        viewModelScope.launch {
            repository.updateUserFeedback(id, feedback)
            if (_currentAnalysis.value?.id == id) {
                _currentAnalysis.value = _currentAnalysis.value?.copy(userFeedback = feedback)
            }
        }
    }

    fun markRecordStatus(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateStatus(id, status)
            if (_currentAnalysis.value?.id == id) {
                _currentAnalysis.value = _currentAnalysis.value?.copy(status = status)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            _currentAnalysis.value = null
            _isFixedResult.value = false
        }
    }

    fun onRatingChanged(rating: Int) {
        _reviewRating.value = rating.coerceIn(1, 5)
    }

    fun onCommentChanged(comment: String) {
        _reviewComment.value = comment
    }

    fun dismissNewCycleNotice() {
        _newCycleNotice.value = null
    }

    fun submitFeedback() {
        val comment = _reviewComment.value.trim()
        val rating = _reviewRating.value
        if (comment.isBlank() && rating <= 0) return

        viewModelScope.launch {
            val feedback = UserFeedback(
                rating = rating,
                comment = if (comment.isBlank()) "Great analysis accuracy!" else comment
            )
            repository.insertFeedback(feedback)
            _reviewComment.value = ""
            _feedbackSubmittedMessage.value = "Thank you! Your review has been submitted."
            delay(4000)
            _feedbackSubmittedMessage.value = null
        }
    }

    private fun saveCycle(cycle: Int) {
        prefs?.edit()?.putInt(KEY_CURRENT_CYCLE, cycle)?.apply()
    }

    private fun savePeriod(period: String) {
        prefs?.edit()?.putString(KEY_CURRENT_PERIOD, period)?.apply()
    }

    companion object {
        private const val PREFS_NAME = "pro_adx_preferences"
        private const val KEY_CURRENT_CYCLE = "key_current_cycle"
        private const val KEY_CURRENT_PERIOD = "key_current_period"

        fun provideFactory(
            repository: GameRepository,
            context: Context? = null
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    return GameViewModel(repository, prefs) as T
                }
            }
    }
}
