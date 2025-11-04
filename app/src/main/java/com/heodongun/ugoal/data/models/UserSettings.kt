package com.heodongun.ugoal.data.models

import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    val userId: String = "default_user",
    val isDarkMode: Boolean = false,
    val defaultReminderTime: String = "09:00", // HH:mm
    val pomodoroWorkDuration: Int = 25, // minutes
    val pomodoroBreakDuration: Int = 5, // minutes
    val enableNotifications: Boolean = true,
    val enableSound: Boolean = true,
    val enableVibration: Boolean = true,
    val firstDayOfWeek: Int = 1, // 1=Monday, 7=Sunday
    val syncEnabled: Boolean = true,
    val lastSyncTime: Long = 0
)
