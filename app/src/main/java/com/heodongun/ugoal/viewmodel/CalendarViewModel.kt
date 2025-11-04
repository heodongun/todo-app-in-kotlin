package com.heodongun.ugoal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heodongun.ugoal.data.models.CalendarEvent
import com.heodongun.ugoal.data.models.EnhancedTodo
import com.heodongun.ugoal.data.repository.UgoalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val currentWeek: Pair<LocalDate, LocalDate> = getCurrentWeek(),
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonthYear: String = "",
    val viewType: CalendarViewType = CalendarViewType.MONTH,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class CalendarViewType {
    MONTH,
    WEEK,
    DAY,
    AGENDA
}

class CalendarViewModel(
    private val repository: UgoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    // Get todos with dates as calendar events
    val events: StateFlow<List<EnhancedTodo>> = repository.userData
        .map { userData ->
            userData.todos
                .filter { it.date != null || it.dueDate != null }
                .sortedBy { it.dueDate ?: it.date }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Get todos for selected date
    val todosForSelectedDate: StateFlow<List<EnhancedTodo>> = combine(
        events,
        _uiState
    ) { todos, state ->
        val selectedDateStr = state.selectedDate.toString()
        todos.filter {
            it.date == selectedDateStr || it.dueDate == selectedDateStr
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        updateMonthYear()
    }

    fun previousMonth() {
        _uiState.update {
            it.copy(currentMonth = it.currentMonth.minusMonths(1))
        }
        updateMonthYear()
    }

    fun nextMonth() {
        _uiState.update {
            it.copy(currentMonth = it.currentMonth.plusMonths(1))
        }
        updateMonthYear()
    }

    fun previousWeek() {
        _uiState.update {
            val (start, end) = it.currentWeek
            val newStart = start.minusWeeks(1)
            val newEnd = end.minusWeeks(1)
            it.copy(currentWeek = Pair(newStart, newEnd))
        }
    }

    fun nextWeek() {
        _uiState.update {
            val (start, end) = it.currentWeek
            val newStart = start.plusWeeks(1)
            val newEnd = end.plusWeeks(1)
            it.copy(currentWeek = Pair(newStart, newEnd))
        }
    }

    fun goToToday() {
        _uiState.update {
            it.copy(
                currentMonth = YearMonth.now(),
                currentWeek = getCurrentWeek(),
                selectedDate = LocalDate.now()
            )
        }
        updateMonthYear()
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun setViewType(viewType: CalendarViewType) {
        _uiState.update { it.copy(viewType = viewType) }
    }

    private fun updateMonthYear() {
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월")
        _uiState.update {
            it.copy(currentMonthYear = it.currentMonth.format(formatter))
        }
    }

    fun getEventsForDate(date: LocalDate): List<EnhancedTodo> {
        val dateStr = date.toString()
        return events.value.filter {
            it.date == dateStr || it.dueDate == dateStr
        }
    }

    fun getEventsForWeek(startDate: LocalDate, endDate: LocalDate): Map<LocalDate, List<EnhancedTodo>> {
        val result = mutableMapOf<LocalDate, List<EnhancedTodo>>()
        var current = startDate

        while (!current.isAfter(endDate)) {
            result[current] = getEventsForDate(current)
            current = current.plusDays(1)
        }

        return result
    }
}

private fun getCurrentWeek(): Pair<LocalDate, LocalDate> {
    val today = LocalDate.now()
    val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
    val endOfWeek = startOfWeek.plusDays(6)
    return Pair(startOfWeek, endOfWeek)
}
