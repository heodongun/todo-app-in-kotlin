package com.heodongun.ugoal.data.models

import kotlinx.serialization.Serializable

@Serializable
data class BigGoal(
    val id: String = "",
    val title: String = "",
    val color: String = "#3182F6",
    val icon: String = "🎯",
    val createdAt: Long = 0L,
    val todos: List<EnhancedTodo> = emptyList()
) {
    val progress: Int
        get() = if (todos.isEmpty()) 0 else {
            (todos.count { it.isCompleted } * 100) / todos.size
        }
}
