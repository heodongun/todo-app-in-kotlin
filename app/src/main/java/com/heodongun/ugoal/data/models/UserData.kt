package com.heodongun.ugoal.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val userId: String = "default_user", // For now, single user
    val bigGoals: List<BigGoal> = emptyList(),
    val dailyGoals: List<DailyGoal> = emptyList(),
    val todos: List<Todo> = emptyList()
)
