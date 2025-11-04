package com.heodongun.ugoal.data.models

import kotlinx.serialization.Serializable

@Serializable
data class PomodoroSession(
    val id: String = "",
    val todoId: String = "",
    val startTime: Long = 0L,
    val endTime: Long? = null,
    val duration: Int = 25, // minutes
    val completed: Boolean = false,
    val date: String = "" // yyyy-MM-dd
)

@Serializable
data class PomodoroStats(
    val totalSessions: Int = 0,
    val completedSessions: Int = 0,
    val totalFocusMinutes: Int = 0,
    val todayFocusMinutes: Int = 0,
    val weekFocusMinutes: Int = 0,
    val monthFocusMinutes: Int = 0
)
