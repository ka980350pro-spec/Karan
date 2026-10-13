package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AnalysisResultCard
import com.example.ui.components.BrandingHeader
import com.example.ui.components.FeedbackSection
import com.example.ui.components.HistorySection
import com.example.ui.components.PeriodInputCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun MainScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val periodInput by viewModel.periodInput.collectAsStateWithLifecycle()
    val currentCycle by viewModel.currentCycle.collectAsStateWithLifecycle()
    val isFixedResult by viewModel.isFixedResult.collectAsStateWithLifecycle()
    val newCycleNotice by viewModel.newCycleNotice.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val currentAnalysis by viewModel.currentAnalysis.collectAsStateWithLifecycle()
    val historyRecords by viewModel.historyRecords.collectAsStateWithLifecycle()
    val feedbackList by viewModel.feedbackList.collectAsStateWithLifecycle()
    val reviewRating by viewModel.reviewRating.collectAsStateWithLifecycle()
    val reviewComment by viewModel.reviewComment.collectAsStateWithLifecycle()
    val feedbackConfirmation by viewModel.feedbackSubmittedMessage.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = DarkBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF070B13),
                            Color(0xFF0C1322),
                            Color(0xFF080C16)
                        )
                    )
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 560.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Branding Header with Logo Text
                BrandingHeader()

                // Main Dashboard Summary
                com.example.ui.components.DashboardSummaryCard(
                    currentCycle = currentCycle,
                    periodInput = periodInput,
                    onStartNewCycle = { viewModel.startNewCycle() },
                    onAnalyzeNext = { viewModel.analyzePeriod() }
                )

                // Animated New Cycle Banner
                AnimatedVisibility(
                    visible = newCycleNotice != null,
                    enter = fadeIn() + slideInVertically { -it },
                    exit = fadeOut() + slideOutVertically { -it }
                ) {
                    newCycleNotice?.let { noticeText ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF0C243B), Color(0xFF2E1B05))
                                    )
                                )
                                .border(1.dp, NeonAmber, RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ElectricBolt,
                                        contentDescription = null,
                                        tint = NeonAmber,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = noticeText,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.dismissNewCycleNotice() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Period Input Card (accepts exactly 3 digits, Analyze button, Next Period +1)
                PeriodInputCard(
                    periodInput = periodInput,
                    currentCycle = currentCycle,
                    isFixedResult = isFixedResult,
                    onPeriodChanged = { viewModel.onPeriodChanged(it) },
                    onAnalyzeClicked = { viewModel.analyzePeriod() },
                    onNextPeriod = { viewModel.nextPeriod() },
                    onStartNewCycle = { viewModel.startNewCycle() },
                    isAnalyzing = isAnalyzing
                )

                // 3. Analysis Result Card (displayed when result is present for valid period)
                AnimatedVisibility(
                    visible = currentAnalysis != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    currentAnalysis?.let { record ->
                        AnalysisResultCard(
                            record = record,
                            onMarkStatus = { status -> viewModel.markStatus(status) },
                            onNextPeriod = { viewModel.nextPeriod() },
                            onSaveFeedback = { feedback -> viewModel.updateAnalysisFeedback(record.id, feedback) }
                        )
                    }
                }

                // 4. Complete History Section
                HistorySection(
                    records = historyRecords,
                    onMarkRecordStatus = { id, status -> viewModel.markRecordStatus(id, status) },
                    onClearHistory = { viewModel.clearHistory() }
                )

                // 5. User Review & Feedback Section
                FeedbackSection(
                    rating = reviewRating,
                    comment = reviewComment,
                    onRatingChanged = { viewModel.onRatingChanged(it) },
                    onCommentChanged = { viewModel.onCommentChanged(it) },
                    onSubmitFeedback = { viewModel.submitFeedback() },
                    confirmationMessage = feedbackConfirmation,
                    feedbackList = feedbackList
                )

                // App Footer Note
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "PRO ADX GAME v2.5 • Deterministic Period Architecture",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

