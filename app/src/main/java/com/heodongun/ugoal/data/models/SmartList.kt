package com.heodongun.ugoal.data.models

import kotlinx.serialization.Serializable

@Serializable
data class SmartList(
    val id: String = "",
    val name: String = "",
    val icon: String = "📋",
    val color: String = "#3182F6",
    val filters: ListFilter = ListFilter(),
    val sortBy: SortCriteria = SortCriteria.DUE_DATE,
    val isSystem: Boolean = false, // System lists like Today, Upcoming
    val order: Int = 0,
    val createdAt: Long = 0L
)

@Serializable
data class ListFilter(
    val priorities: List<Priority> = emptyList(),
    val tags: List<String> = emptyList(),
    val dateRange: DateRange? = null,
    val status: List<TodoStatus> = emptyList(),
    val goalIds: List<String> = emptyList(),
    val hasSubtasks: Boolean? = null,
    val hasDueDate: Boolean? = null,
    val isRecurring: Boolean? = null
)

@Serializable
data class DateRange(
    val start: String? = null, // yyyy-MM-dd
    val end: String? = null,   // yyyy-MM-dd
    val type: DateRangeType = DateRangeType.CUSTOM
)

@Serializable
enum class DateRangeType {
    TODAY,
    TOMORROW,
    THIS_WEEK,
    NEXT_WEEK,
    THIS_MONTH,
    OVERDUE,
    CUSTOM
}

@Serializable
enum class SortCriteria {
    DUE_DATE,
    PRIORITY,
    CREATED_DATE,
    TITLE,
    CUSTOM_ORDER
}

@Serializable
enum class TodoStatus {
    ACTIVE,
    COMPLETED,
    OVERDUE
}
