package com.heodongun.ugoal.utils

import com.heodongun.ugoal.data.models.*
import java.time.LocalDate

object FilterEngine {

    fun applyFilter(
        todos: List<EnhancedTodo>,
        filter: ListFilter,
        sortBy: SortCriteria = SortCriteria.DUE_DATE
    ): List<EnhancedTodo> {
        var filtered = todos

        // Filter by priority
        if (filter.priorities.isNotEmpty()) {
            filtered = filtered.filter { it.priority in filter.priorities }
        }

        // Filter by tags
        if (filter.tags.isNotEmpty()) {
            filtered = filtered.filter { todo ->
                filter.tags.any { tag -> tag in todo.tags }
            }
        }

        // Filter by date range
        filter.dateRange?.let { range ->
            filtered = when (range.type) {
                DateRangeType.TODAY -> {
                    val today = LocalDate.now().toString()
                    filtered.filter { it.dueDate == today || it.date == today }
                }
                DateRangeType.TOMORROW -> {
                    val tomorrow = LocalDate.now().plusDays(1).toString()
                    filtered.filter { it.dueDate == tomorrow }
                }
                DateRangeType.THIS_WEEK -> {
                    val today = LocalDate.now()
                    val endOfWeek = today.plusDays(7 - today.dayOfWeek.value.toLong())
                    filtered.filter { todo ->
                        todo.dueDate?.let { dueDate ->
                            try {
                                val date = LocalDate.parse(dueDate)
                                date in today..endOfWeek
                            } catch (e: Exception) {
                                false
                            }
                        } ?: false
                    }
                }
                DateRangeType.NEXT_WEEK -> {
                    val today = LocalDate.now()
                    val startOfNextWeek = today.plusDays(8 - today.dayOfWeek.value.toLong())
                    val endOfNextWeek = startOfNextWeek.plusDays(6)
                    filtered.filter { todo ->
                        todo.dueDate?.let { dueDate ->
                            try {
                                val date = LocalDate.parse(dueDate)
                                date in startOfNextWeek..endOfNextWeek
                            } catch (e: Exception) {
                                false
                            }
                        } ?: false
                    }
                }
                DateRangeType.THIS_MONTH -> {
                    val today = LocalDate.now()
                    val startOfMonth = today.withDayOfMonth(1)
                    val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
                    filtered.filter { todo ->
                        todo.dueDate?.let { dueDate ->
                            try {
                                val date = LocalDate.parse(dueDate)
                                date in startOfMonth..endOfMonth
                            } catch (e: Exception) {
                                false
                            }
                        } ?: false
                    }
                }
                DateRangeType.OVERDUE -> {
                    filtered.filter { it.isOverdue }
                }
                DateRangeType.CUSTOM -> {
                    filtered.filter { todo ->
                        todo.dueDate?.let { dueDate ->
                            try {
                                val date = LocalDate.parse(dueDate)
                                val start = range.start?.let { LocalDate.parse(it) }
                                val end = range.end?.let { LocalDate.parse(it) }

                                when {
                                    start != null && end != null -> date in start..end
                                    start != null -> date >= start
                                    end != null -> date <= end
                                    else -> true
                                }
                            } catch (e: Exception) {
                                false
                            }
                        } ?: false
                    }
                }
            }
        }

        // Filter by status
        if (filter.status.isNotEmpty()) {
            filtered = filtered.filter { todo ->
                filter.status.any { status ->
                    when (status) {
                        TodoStatus.COMPLETED -> todo.isCompleted
                        TodoStatus.OVERDUE -> todo.isOverdue && !todo.isCompleted
                        TodoStatus.ACTIVE -> !todo.isCompleted && !todo.isOverdue
                    }
                }
            }
        }

        // Filter by goal
        if (filter.goalIds.isNotEmpty()) {
            filtered = filtered.filter { it.goalId in filter.goalIds }
        }

        // Filter by subtasks presence
        filter.hasSubtasks?.let { hasSubtasks ->
            filtered = filtered.filter {
                (it.subtasks.isNotEmpty()) == hasSubtasks
            }
        }

        // Filter by due date presence
        filter.hasDueDate?.let { hasDue ->
            filtered = filtered.filter {
                (it.dueDate != null) == hasDue
            }
        }

        // Filter by recurring
        filter.isRecurring?.let { isRecurring ->
            filtered = filtered.filter {
                (it.repeat.type != RepeatType.NONE) == isRecurring
            }
        }

        // Apply sorting
        return when (sortBy) {
            SortCriteria.DUE_DATE -> filtered.sortedWith(
                compareBy(nullsLast()) { it.dueDate }
            )
            SortCriteria.PRIORITY -> filtered.sortedByDescending { it.priority }
            SortCriteria.CREATED_DATE -> filtered.sortedBy { it.createdAt }
            SortCriteria.TITLE -> filtered.sortedBy { it.title.lowercase() }
            SortCriteria.CUSTOM_ORDER -> filtered.sortedBy { it.order }
        }
    }

    fun createSystemLists(): List<SmartList> {
        return listOf(
            SmartList(
                id = "system_inbox",
                name = "받은편지함",
                icon = "📥",
                filters = ListFilter(),
                sortBy = SortCriteria.CREATED_DATE,
                isSystem = true,
                order = 0
            ),
            SmartList(
                id = "system_today",
                name = "오늘",
                icon = "📅",
                filters = ListFilter(
                    dateRange = DateRange(type = DateRangeType.TODAY)
                ),
                isSystem = true,
                order = 1
            ),
            SmartList(
                id = "system_tomorrow",
                name = "내일",
                icon = "➡️",
                filters = ListFilter(
                    dateRange = DateRange(type = DateRangeType.TOMORROW)
                ),
                isSystem = true,
                order = 2
            ),
            SmartList(
                id = "system_week",
                name = "이번 주",
                icon = "📆",
                filters = ListFilter(
                    dateRange = DateRange(type = DateRangeType.THIS_WEEK)
                ),
                isSystem = true,
                order = 3
            ),
            SmartList(
                id = "system_overdue",
                name = "기한 지남",
                icon = "⚠️",
                filters = ListFilter(
                    dateRange = DateRange(type = DateRangeType.OVERDUE),
                    status = listOf(TodoStatus.OVERDUE)
                ),
                isSystem = true,
                order = 4
            ),
            SmartList(
                id = "system_high_priority",
                name = "중요",
                icon = "🔴",
                filters = ListFilter(
                    priorities = listOf(Priority.HIGH)
                ),
                isSystem = true,
                order = 5
            ),
            SmartList(
                id = "system_completed",
                name = "완료됨",
                icon = "✅",
                filters = ListFilter(
                    status = listOf(TodoStatus.COMPLETED)
                ),
                isSystem = true,
                order = 6
            ),
            SmartList(
                id = "system_all",
                name = "모든 할 일",
                icon = "📋",
                filters = ListFilter(),
                isSystem = true,
                order = 7
            )
        )
    }

    /**
     * Search todos by keyword in title, description, notes, and tags
     */
    fun searchTodos(todos: List<EnhancedTodo>, query: String): List<EnhancedTodo> {
        if (query.isBlank()) return todos

        val lowerQuery = query.lowercase()
        return todos.filter { todo ->
            todo.title.lowercase().contains(lowerQuery) ||
            todo.description.lowercase().contains(lowerQuery) ||
            todo.note.lowercase().contains(lowerQuery) ||
            todo.tags.any { it.lowercase().contains(lowerQuery) }
        }
    }

    /**
     * Get todo count for a smart list
     */
    fun getTodoCount(todos: List<EnhancedTodo>, smartList: SmartList): Int {
        return applyFilter(todos, smartList.filters, smartList.sortBy).size
    }
}
