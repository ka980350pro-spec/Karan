package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.GameRepository
import com.example.ui.GameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var database: AppDatabase
  private lateinit var repository: GameRepository
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    val context = ApplicationProvider.getApplicationContext<Context>()
    val directExecutor = java.util.concurrent.Executor { it.run() }
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .setQueryExecutor(directExecutor)
      .setTransactionExecutor(directExecutor)
      .allowMainThreadQueries()
      .build()
    repository = GameRepository(database.analysisDao(), database.feedbackDao())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    database.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PRO ADX GAME", appName)
  }

  @Test
  fun `test period increment with nextPeriod`() {
    val viewModel = GameViewModel(repository)
    viewModel.onPeriodChanged("042")
    assertEquals("042", viewModel.periodInput.value)

    viewModel.nextPeriod()
    assertEquals("043", viewModel.periodInput.value)
  }

  @Test
  fun `test rollover from 999 to 000 increments cycle`() {
    val viewModel = GameViewModel(repository)
    assertEquals(1, viewModel.currentCycle.value)

    viewModel.onPeriodChanged("999")
    assertEquals("999", viewModel.periodInput.value)

    viewModel.nextPeriod()
    // Rolls over to 000 and starts Cycle #2
    assertEquals("000", viewModel.periodInput.value)
    assertEquals(2, viewModel.currentCycle.value)
    assertTrue(viewModel.newCycleNotice.value?.contains("Cycle #2") == true)
  }

  @Test
  fun `test fixed result per period in a cycle and preservation across re-analysis`() = runTest(testDispatcher) {
    val viewModel = GameViewModel(repository)
    viewModel.onPeriodChanged("777")
    viewModel.analyzePeriod(simulateDelay = false)
    testDispatcher.scheduler.advanceUntilIdle()

    val firstResult = viewModel.currentAnalysis.value
    assertNotNull(firstResult)
    val firstDigit = firstResult!!.resultNumber
    val firstClassification = firstResult.classification
    val firstCycle = firstResult.cycleId
    assertEquals(1, firstCycle)
    assertEquals("777", firstResult.periodNumber)

    // Re-analyzing the same period in the same cycle must return the exact same fixed result!
    viewModel.analyzePeriod(simulateDelay = false)
    testDispatcher.scheduler.advanceUntilIdle()

    val secondResult = viewModel.currentAnalysis.value
    assertNotNull(secondResult)
    assertEquals(firstDigit, secondResult!!.resultNumber)
    assertEquals(firstClassification, secondResult.classification)
    assertEquals(firstResult.id, secondResult.id)
    assertTrue(viewModel.isFixedResult.value)
  }

  @Test
  fun `test room schema and dao stores analysis results with timestamp, predicted period, and user feedback`() = runTest(testDispatcher) {
    val analysisDao = database.analysisDao()
    val testTimestamp = 1726090000000L
    val record = com.example.data.model.AnalysisRecord(
      cycleId = 1,
      periodNumber = "123",
      predictedPeriod = "123",
      resultNumber = 8,
      classification = "BIG",
      percentage = 94.5f,
      status = "PENDING",
      userFeedback = "Accurate prediction!",
      timestamp = testTimestamp
    )

    val id = analysisDao.insertRecord(record)
    assertTrue(id > 0)

    val retrieved = analysisDao.getRecordById(id)
    assertNotNull(retrieved)
    assertEquals("123", retrieved?.predictedPeriod)
    assertEquals(testTimestamp, retrieved?.timestamp)
    assertEquals("Accurate prediction!", retrieved?.userFeedback)

    // Test updating user feedback via DAO
    analysisDao.updateUserFeedback(id, "Updated: Matched perfectly")
    val updated = analysisDao.getRecordById(id)
    assertEquals("Updated: Matched perfectly", updated?.userFeedback)
  }
}

