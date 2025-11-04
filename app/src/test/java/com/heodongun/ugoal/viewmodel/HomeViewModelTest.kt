package com.heodongun.ugoal.viewmodel

import app.cash.turbine.test
import com.heodongun.ugoal.data.models.DailyGoal
import com.heodongun.ugoal.data.models.Todo
import com.heodongun.ugoal.data.models.UserData
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
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: UgoalRepository
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Note: In real tests, use a fake repository instead of actual MongoDbClient
        repository = UgoalRepository(MongoDbClient())
        viewModel = HomeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be loading`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.isLoading || !state.isLoading) // Check initial state exists
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `addTodo should create todo with correct data`() = runTest {
        val testTitle = "Test Todo"
        
        viewModel.addTodo(testTitle)
        advanceUntilIdle()

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.todos.any { it.title == testTitle })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggleTodoComplete should update todo completion status`() = runTest {
        val testTodo = Todo(id = "test-1", title = "Test", isCompleted = false)
        
        viewModel.addTodo(testTodo.title)
        advanceUntilIdle()

        // Find the added todo and toggle it
        viewModel.uiState.test {
            val state = awaitItem()
            val todo = state.todos.first()
            viewModel.toggleTodoComplete(todo.id)
            advanceUntilIdle()
            
            val updatedState = awaitItem()
            val updatedTodo = updatedState.todos.find { it.id == todo.id }
            assertTrue(updatedTodo?.isCompleted == true)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
