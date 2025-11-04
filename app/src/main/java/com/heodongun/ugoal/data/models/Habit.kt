package com.heodongun.ugoal.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Habit(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val icon: String = "✓",
    val color: String = "#3182F6",
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val targetCount: Int = 1, // How many times per day/week
    val startDate: String = "", // yyyy-MM-dd
    val endDate: String? = null,
    val reminders: List<String> = emptyList(), // Times to remind (HH:mm)
    val streakCount: Int = 0,
    val longestStreak: Int = 0,
    val completionHistory: Map<String, Int> = emptyMap(), // date -> count
    val createdAt: Long = 0L,
    val order: Int = 0
)

@Serializable
enum class HabitFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM
}

@Serializable
data class HabitCompletion(
    val habitId: String = "",
    val date: String = "", // yyyy-MM-dd
    val count: Int = 1,
    val timestamp: Long = 0L,
    val note: String = ""
)
