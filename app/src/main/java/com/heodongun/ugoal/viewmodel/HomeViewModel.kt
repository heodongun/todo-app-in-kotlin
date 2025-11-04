package com.heodongun.ugoal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heodongun.ugoal.data.models.DailyGoal
import com.heodongun.ugoal.data.models.EnhancedTodo
import com.heodongun.ugoal.data.repository.UgoalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HomeUiState(
    val currentDate: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
    val dailyGoal: DailyGoal? = null,
    val todos: List<EnhancedTodo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(private val repository: UgoalRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.loadUserData()
                .onSuccess {
                    repository.userData
                        .distinctUntilChanged()
                        .collectLatest { userData ->
                            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            val dailyGoal = userData.dailyGoals
                                .filter { it.id.isNotEmpty() }
                                .distinctBy { it.id }
                                .find { it.date == today }
                            val todayTodos = userData.todos
                                .filter { it.id.isNotEmpty() && (it.date == today || it.date == null) }
                                .distinctBy { it.id }

                            _uiState.update {
                                it.copy(
                                    dailyGoal = dailyGoal,
                                    todos = todayTodos.sortedBy { todo -> todo.createdAt },
                                    isLoading = false,
                                    error = null
                                )
                            }
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

    fun addDailyGoal(title: String) {
        viewModelScope.launch {
            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val dailyGoal = DailyGoal(
                id = java.util.UUID.randomUUID().toString(),
                date = today,
                title = title,
                createdAt = System.currentTimeMillis()
            )
            
            repository.addDailyGoal(dailyGoal)
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun toggleDailyGoalComplete() {
        viewModelScope.launch {
            val currentGoal = _uiState.value.dailyGoal ?: return@launch
            val updatedGoal = currentGoal.copy(isCompleted = !currentGoal.isCompleted)
            
            repository.updateDailyGoal(updatedGoal)
                .onSuccess {
                    _uiState.update { it.copy(dailyGoal = updatedGoal) }
                }
        }
    }

    fun addTodo(title: String, goalId: String? = null, date: String? = null) {
        viewModelScope.launch {
            val todoDate = date ?: LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val todo = EnhancedTodo(
                id = java.util.UUID.randomUUID().toString(),
                title = title,
                goalId = goalId,
                date = todoDate,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            repository.addTodo(todo)
        }
    }

    fun toggleTodoComplete(todoId: String) {
        viewModelScope.launch {
            repository.toggleTodoComplete(todoId)
        }
    }

    fun updateTodo(todo: EnhancedTodo) {
        viewModelScope.launch {
            repository.updateTodo(todo)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            todos = it.todos.map { t ->
                                if (t.id == todo.id) todo else t
                            }.sortedBy { t -> t.createdAt }
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun deleteTodo(todoId: String) {
        viewModelScope.launch {
            repository.deleteTodo(todoId)
                .onSuccess {
                    _uiState.update {
                        it.copy(todos = it.todos.filter { todo -> todo.id != todoId })
                    }
                }
        }
    }
}
