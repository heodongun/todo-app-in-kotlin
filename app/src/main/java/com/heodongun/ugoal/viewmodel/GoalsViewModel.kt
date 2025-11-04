package com.heodongun.ugoal.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heodongun.ugoal.data.models.BigGoal
import com.heodongun.ugoal.data.models.EnhancedTodo
import com.heodongun.ugoal.data.repository.UgoalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GoalsUiState(
    val bigGoals: List<BigGoal> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class GoalsViewModel(private val repository: UgoalRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
    }

    private fun loadGoals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.loadUserData()
                .onSuccess {
                    repository.userData
                        .distinctUntilChanged()
                        .collectLatest { userData ->
                            Log.d("GoalsViewModel", "╔═══════════════════════════════════════════════════════")
                            Log.d("GoalsViewModel", "║ GOALS VIEWMODEL COLLECT START")
                            Log.d("GoalsViewModel", "║ Thread: ${Thread.currentThread().name}")
                            Log.d("GoalsViewModel", "║ Timestamp: ${System.currentTimeMillis()}")
                            Log.d("GoalsViewModel", "║ Received bigGoals count: ${userData.bigGoals.size}")

                            // Log ALL received goals with full details
                            Log.d("GoalsViewModel", "║ RAW GOALS FROM REPOSITORY:")
                            userData.bigGoals.forEachIndexed { index, goal ->
                                Log.d("GoalsViewModel", "║   [$index] id=${goal.id}, title='${goal.title}', color=${goal.color}")
                            }

                            // Check for duplicates in received data
                            val duplicates = userData.bigGoals.groupBy { it.id }.filter { it.value.size > 1 }
                            if (duplicates.isNotEmpty()) {
                                Log.e("GoalsViewModel", "║ 🚨🚨🚨 DUPLICATES DETECTED IN RAW DATA:")
                                duplicates.forEach { (id, goals) ->
                                    Log.e("GoalsViewModel", "║   ID: $id appears ${goals.size} times")
                                    goals.forEachIndexed { idx, goal ->
                                        Log.e("GoalsViewModel", "║     [$idx] title='${goal.title}', hashCode=${goal.hashCode()}")
                                    }
                                }
                            } else {
                                Log.d("GoalsViewModel", "║ ✅ NO duplicates in raw data")
                            }

                            // Filter out goals with empty IDs, remove duplicates
                            val validGoals = userData.bigGoals
                                .filter { it.id.isNotEmpty() }
                                .distinctBy { it.id }

                            Log.d("GoalsViewModel", "║ AFTER FILTER/DISTINCT: ${validGoals.size} goals")
                            validGoals.forEachIndexed { index, goal ->
                                Log.d("GoalsViewModel", "║   [$index] id=${goal.id}, title='${goal.title}'")
                            }

                            // Check if valid goals have any duplicates
                            val validDuplicates = validGoals.groupBy { it.id }.filter { it.value.size > 1 }
                            if (validDuplicates.isNotEmpty()) {
                                Log.e("GoalsViewModel", "║ 🚨🚨🚨 DUPLICATES STILL PRESENT AFTER distinctBy:")
                                validDuplicates.forEach { (id, goals) ->
                                    Log.e("GoalsViewModel", "║   ID: $id appears ${goals.size} times")
                                }
                            } else {
                                Log.d("GoalsViewModel", "║ ✅ NO duplicates after distinctBy")
                            }

                            val goalsWithTodos = validGoals.map { goal ->
                                val goalTodos = userData.todos
                                    .filter { it.goalId == goal.id && it.id.isNotEmpty() }
                                    .distinctBy { it.id }
                                goal.copy(todos = goalTodos)
                            }

                            Log.d("GoalsViewModel", "║ FINAL GOALS WITH TODOS: ${goalsWithTodos.size} goals")
                            goalsWithTodos.forEachIndexed { index, goal ->
                                Log.d("GoalsViewModel", "║   [$index] id=${goal.id}, title='${goal.title}', todos=${goal.todos.size}")
                            }

                            // Check final list for duplicates before UI update
                            val finalDuplicates = goalsWithTodos.groupBy { it.id }.filter { it.value.size > 1 }
                            if (finalDuplicates.isNotEmpty()) {
                                Log.e("GoalsViewModel", "║ 🚨🚨🚨 DUPLICATES IN FINAL LIST:")
                                finalDuplicates.forEach { (id, goals) ->
                                    Log.e("GoalsViewModel", "║   ID: $id appears ${goals.size} times")
                                }
                            } else {
                                Log.d("GoalsViewModel", "║ ✅ NO duplicates in final list")
                            }

                            Log.d("GoalsViewModel", "║ UPDATING UI STATE NOW...")
                            _uiState.update {
                                val newState = it.copy(
                                    bigGoals = goalsWithTodos.sortedBy { goal -> goal.createdAt },
                                    isLoading = false,
                                    error = null
                                )
                                Log.d("GoalsViewModel", "║ UI STATE UPDATED - bigGoals count: ${newState.bigGoals.size}")
                                newState
                            }
                            Log.d("GoalsViewModel", "╚═══════════════════════════════════════════════════════")
                            Log.d("GoalsViewModel", "")
                        }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message
                        )
                    }
                }
        }
    }

    fun addGoal(title: String, color: String = "#3182F6", icon: String = "🎯") {
        viewModelScope.launch {
            val goal = BigGoal(
                id = java.util.UUID.randomUUID().toString(),
                title = title,
                color = color,
                icon = icon,
                createdAt = System.currentTimeMillis()
            )
            
            repository.addBigGoal(goal)
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun updateGoal(goal: BigGoal) {
        viewModelScope.launch {
            repository.updateBigGoal(goal)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            bigGoals = it.bigGoals.map { g ->
                                if (g.id == goal.id) goal else g
                            }.sortedBy { g -> g.createdAt }
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            repository.deleteBigGoal(goalId)
                .onSuccess {
                    _uiState.update {
                        it.copy(bigGoals = it.bigGoals.filter { goal -> goal.id != goalId })
                    }
                }
        }
    }

    fun addTodoToGoal(goalId: String, title: String) {
        viewModelScope.launch {
            val todo = EnhancedTodo(
                id = java.util.UUID.randomUUID().toString(),
                title = title,
                goalId = goalId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            repository.addTodo(todo)
        }
    }

    fun toggleGoalTodoComplete(goalId: String, todoId: String) {
        viewModelScope.launch {
            repository.toggleTodoComplete(todoId)
        }
    }
}
