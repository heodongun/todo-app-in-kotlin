package com.heodongun.ugoal.data.models

import kotlinx.serialization.Serializable

@Serializable
enum class RepeatType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM
}

@Serializable
data class RepeatConfig(
    val type: RepeatType = RepeatType.NONE,
    val interval: Int = 1, // Every N days/weeks/months
    val daysOfWeek: List<Int> = emptyList(), // For weekly: 1=Mon, 7=Sun
    val endDate: String? = null // Optional end date for recurrence
)
