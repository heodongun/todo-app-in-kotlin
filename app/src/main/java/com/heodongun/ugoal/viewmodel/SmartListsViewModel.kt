package com.heodongun.ugoal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heodongun.ugoal.data.models.*
import com.heodongun.ugoal.data.repository.UgoalRepository
import com.heodongun.ugoal.utils.FilterEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SmartListsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

class SmartListsViewModel(
    private val repository: UgoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartListsUiState())
    val uiState: StateFlow<SmartListsUiState> = _uiState.asStateFlow()

    // System smart lists (predefined)
    val systemLists: StateFlow<List<SmartList>> = flowOf(
        FilterEngine.createSystemLists()
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Custom user-created smart lists
    val customLists: StateFlow<List<SmartList>> = repository.userData
        .map { userData ->
            userData.smartLists.sortedBy { it.order }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // All todos for filtering
    private val allTodos: StateFlow<List<EnhancedTodo>> = repository.userData
        .map { it.todos }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedSmartList = MutableStateFlow<SmartList?>(null)
    val selectedSmartList: StateFlow<SmartList?> = _selectedSmartList.asStateFlow()

    val filteredTodos: StateFlow<List<EnhancedTodo>> = combine(
        allTodos,
        selectedSmartList
    ) { todos, selectedList ->
        if (selectedList != null) {
            FilterEngine.applyFilter(todos, selectedList.filters, selectedList.sortBy)
        } else {
            emptyList()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectSmartList(smartList: SmartList) {
        _selectedSmartList.value = smartList
    }

    fun getTodoCount(smartList: SmartList): Int {
        return FilterEngine.getTodoCount(allTodos.value, smartList)
    }

    fun getFilteredTodos(smartList: SmartList): List<EnhancedTodo> {
        return FilterEngine.applyFilter(
            allTodos.value,
            smartList.filters,
            smartList.sortBy
        )
    }

    fun toggleTodoComplete(todoId: String) {
        viewModelScope.launch {
            repository.toggleTodoComplete(todoId)
        }
    }

    fun deleteTodo(todoId: String) {
        viewModelScope.launch {
            repository.deleteTodo(todoId)
        }
    }

    fun createSmartList(smartList: SmartList) {
        viewModelScope.launch {
            repository.addSmartList(smartList)
        }
    }

    fun updateSmartList(smartList: SmartList) {
        viewModelScope.launch {
            repository.updateSmartList(smartList)
        }
    }

    fun deleteSmartList(listId: String) {
        viewModelScope.launch {
            repository.deleteSmartList(listId)
        }
    }

    fun searchTodos(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun getSearchResults(): List<EnhancedTodo> {
        val query = _uiState.value.searchQuery
        return if (query.isBlank()) {
            allTodos.value
        } else {
            FilterEngine.searchTodos(allTodos.value, query)
        }
    }
}
