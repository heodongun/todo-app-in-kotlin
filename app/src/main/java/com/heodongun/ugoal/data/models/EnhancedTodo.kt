package com.heodongun.ugoal.data.models

import kotlinx.serialization.Serializable

@Serializable
data class EnhancedTodo(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val goalId: String? = null,
    val date: String? = null, // yyyy-MM-dd
    val dueDate: String? = null, // yyyy-MM-dd
    val dueTime: String? = null, // HH:mm
    val priority: Priority = Priority.NONE,
    val tags: List<String> = emptyList(),
    val subtasks: List<Subtask> = emptyList(),
    val repeat: RepeatConfig = RepeatConfig(),
    val reminderTime: String? = null, // ISO 8601 format
    val note: String = "",
    val pomodoroCount: Int = 0,
    val order: Int = 0,
    val createdAt: Long = 0L,
    val completedAt: Long? = null,
    val updatedAt: Long = 0L
) {
    val isOverdue: Boolean
        get() {
            if (isCompleted || dueDate == null) return false
            return try {
                java.time.LocalDate.parse(dueDate) < java.time.LocalDate.now()
            } catch (e: Exception) {
                false
            }
        }

    val completionProgress: Float
        get() {
            if (subtasks.isEmpty()) return if (isCompleted) 1f else 0f
            return subtasks.count { it.isCompleted }.toFloat() / subtasks.size
        }
}
