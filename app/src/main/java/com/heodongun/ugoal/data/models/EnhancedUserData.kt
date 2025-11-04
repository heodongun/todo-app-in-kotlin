package com.heodongun.ugoal.data.models

import kotlinx.serialization.Serializable

@Serializable
data class EnhancedUserData(
    val userId: String = "default_user",
    val bigGoals: List<BigGoal> = emptyList(),
    val dailyGoals: List<DailyGoal> = emptyList(),
    val todos: List<EnhancedTodo> = emptyList(),
    val smartLists: List<SmartList> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val calendarEvents: List<CalendarEvent> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val sharedLists: List<SharedList> = emptyList(),
    val pomodoroHistory: List<PomodoroSession> = emptyList(),
    val statistics: UserStatistics = UserStatistics(),
    val settings: UserSettings = UserSettings(),
    val tags: List<String> = emptyList() // All available tags
)
