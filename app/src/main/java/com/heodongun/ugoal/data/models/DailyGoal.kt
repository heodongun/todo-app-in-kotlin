package com.heodongun.ugoal.data.models

import kotlinx.serialization.Serializable

@Serializable
data class DailyGoal(
    val id: String = "",
    val date: String = "", // Format: yyyy-MM-dd
    val title: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = 0L
)
