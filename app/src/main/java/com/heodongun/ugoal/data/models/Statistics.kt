package com.heodongun.ugoal.data.models

import kotlinx.serialization.Serializable

@Serializable
data class DailyStats(
    val date: String, // yyyy-MM-dd
    val completedTodos: Int = 0,
    val totalTodos: Int = 0,
    val focusMinutes: Int = 0,
    val productivity: Float = 0f // 0-100
)

@Serializable
data class WeeklyStats(
    val weekStart: String, // yyyy-MM-dd
    val completedTodos: Int = 0,
    val totalTodos: Int = 0,
    val focusMinutes: Int = 0,
    val dailyBreakdown: List<DailyStats> = emptyList(),
    val productivity: Float = 0f
)

@Serializable
data class MonthlyStats(
    val month: String, // yyyy-MM
    val completedTodos: Int = 0,
    val totalTodos: Int = 0,
    val focusMinutes: Int = 0,
    val weeklyBreakdown: List<WeeklyStats> = emptyList(),
    val productivity: Float = 0f,
    val topTags: List<Pair<String, Int>> = emptyList()
)

@Serializable
data class UserStatistics(
    val userId: String = "default_user",
    val totalCompleted: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalFocusHours: Int = 0,
    val dailyStats: List<DailyStats> = emptyList(),
    val weeklyStats: List<WeeklyStats> = emptyList(),
    val monthlyStats: List<MonthlyStats> = emptyList()
)
