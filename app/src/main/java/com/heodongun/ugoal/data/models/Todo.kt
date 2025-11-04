package com.heodongun.ugoal.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Todo(
    val id: String = "",
    val title: String,
    val isCompleted: Boolean = false,
    val goalId: String? = null, // Link to BigGoal
    val date: String? = null, // Format: yyyy-MM-dd (for daily todos)
    val createdAt: Long = 0L,
    val completedAt: Long? = null
)
