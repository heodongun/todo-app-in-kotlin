package com.heodongun.ugoal.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CalendarEvent(
    val id: String = "",
    val todoId: String? = null, // Link to todo if applicable
    val title: String = "",
    val description: String = "",
    val startTime: Long = 0L, // Unix timestamp
    val endTime: Long = 0L, // Unix timestamp
    val location: String? = null,
    val isAllDay: Boolean = false,
    val color: String = "#3182F6",
    val calendarSource: CalendarSource = CalendarSource.UGOAL,
    val externalId: String? = null, // For Google Calendar sync
    val attendees: List<String> = emptyList(),
    val reminders: List<ReminderConfig> = emptyList(),
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)

@Serializable
enum class CalendarSource {
    UGOAL,          // Native Ugoal events
    GOOGLE,         // Google Calendar
    EXTERNAL        // Other calendar sources
}

@Serializable
data class ReminderConfig(
    val minutesBefore: Int = 0,
    val method: ReminderMethod = ReminderMethod.NOTIFICATION
)

@Serializable
enum class ReminderMethod {
    NOTIFICATION,
    EMAIL,
    ALARM,
    SMS
}
