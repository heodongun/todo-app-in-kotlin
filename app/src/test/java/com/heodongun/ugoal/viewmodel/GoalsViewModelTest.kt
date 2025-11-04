package com.heodongun.ugoal.viewmodel

import app.cash.turbine.test
import com.heodongun.ugoal.data.models.BigGoal
import com.heodongun.ugoal.data.remote.MongoDbClient
import com.heodongun.ugoal.data.repository.UgoalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class GoalsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: UgoalRepository
    private lateinit var viewModel: GoalsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = UgoalRepository(MongoDbClient())
        viewModel = GoalsViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addGoal should create goal with correct data`() = runTest {
        val testTitle = "Test Goal"
        val testColor = "#3182F6"
        val testIcon = "🎯"
        
        viewModel.addGoal(testTitle, testColor, testIcon)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.bigGoals.any { 
                it.title == testTitle && it.color == testColor && it.icon == testIcon 
            })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `goal progress should calculate correctly`() {
        val goal = BigGoal(
            id = "test-1",
            title = "Test Goal",
            todos = listOf(
                com.heodongun.ugoal.data.models.Todo(id = "1", title = "Todo 1", isCompleted = true),
                com.heodongun.ugoal.data.models.Todo(id = "2", title = "Todo 2", isCompleted = false)
            )
        )
        
        assertEquals(50, goal.progress)
    }

    @Test
    fun `deleteGoal should remove goal from list`() = runTest {
        viewModel.addGoal("Test Goal")
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            val goalId = state.bigGoals.first().id
            
            viewModel.deleteGoal(goalId)
            advanceUntilIdle()
            
            val updatedState = awaitItem()
            assertFalse(updatedState.bigGoals.any { it.id == goalId })
            cancelAndIgnoreRemainingEvents()
        }
    }
}
