package com.heodongun.ugoal.utils

import com.heodongun.ugoal.data.models.Priority
import java.time.LocalDate
import java.time.LocalTime
import java.util.regex.Pattern

object NaturalLanguageParser {

    private val timePattern = Pattern.compile("(\\d{1,2})(시|:)(\\d{2})?(분)?")
    private val datePatterns = mapOf(
        "오늘" to 0,
        "내일" to 1,
        "모레" to 2,
        "글피" to 3,
        "다음주" to 7,
        "담주" to 7,
        "이번주" to 0
    )

    private val dayOfWeekPatterns = mapOf(
        "월요일" to 1,
        "화요일" to 2,
        "수요일" to 3,
        "목요일" to 4,
        "금요일" to 5,
        "토요일" to 6,
        "일요일" to 7,
        "월" to 1,
        "화" to 2,
        "수" to 3,
        "목" to 4,
        "금" to 5,
        "토" to 6,
        "일" to 7
    )

    private val priorityKeywords = mapOf(
        "중요" to Priority.HIGH,
        "긴급" to Priority.HIGH,
        "급함" to Priority.HIGH,
        "!!!" to Priority.HIGH,
        "!!" to Priority.MEDIUM,
        "!" to Priority.LOW
    )

    fun parse(input: String): TodoParseResult {
        var title = input
        var dueDate: String? = null
        var dueTime: String? = null
        var priority = Priority.NONE
        val tags = mutableListOf<String>()

        // Extract time (예: "3시 30분", "15:00", "3시")
        val timeMatcher = timePattern.matcher(input)
        if (timeMatcher.find()) {
            val hour = timeMatcher.group(1).toInt()
            val minute = timeMatcher.group(3)?.toInt() ?: 0
            dueTime = String.format("%02d:%02d", hour, minute)
            title = title.replace(timeMatcher.group(), "").trim()
        }

        // Extract date keywords
        datePatterns.forEach { (keyword, daysOffset) ->
            if (input.contains(keyword)) {
                dueDate = LocalDate.now().plusDays(daysOffset.toLong()).toString()
                title = title.replace(keyword, "").trim()
                return@forEach // 첫 번째 매칭만 사용
            }
        }

        // Extract day of week (예: "다음 수요일")
        dayOfWeekPatterns.forEach { (dayName, dayNumber) ->
            if (input.contains(dayName)) {
                val today = LocalDate.now()
                val todayDayOfWeek = today.dayOfWeek.value
                val daysUntil = if (dayNumber > todayDayOfWeek) {
                    dayNumber - todayDayOfWeek
                } else {
                    7 - todayDayOfWeek + dayNumber
                }
                dueDate = today.plusDays(daysUntil.toLong()).toString()
                title = title.replace(dayName, "").replace("다음", "").trim()
                return@forEach
            }
        }

        // Extract priority
        priorityKeywords.forEach { (keyword, priorityLevel) ->
            if (input.contains(keyword)) {
                priority = priorityLevel
                title = title.replace(keyword, "").trim()
            }
        }

        // Extract hashtags as tags
        val hashtagPattern = Pattern.compile("#([가-힣\\w]+)")
        val hashtagMatcher = hashtagPattern.matcher(input)
        while (hashtagMatcher.find()) {
            tags.add(hashtagMatcher.group(1))
            title = title.replace(hashtagMatcher.group(), "").trim()
        }

        // Extract @mentions for goal assignment
        val mentionPattern = Pattern.compile("@([가-힣\\w]+)")
        var goalName: String? = null
        val mentionMatcher = mentionPattern.matcher(input)
        if (mentionMatcher.find()) {
            goalName = mentionMatcher.group(1)
            title = title.replace(mentionMatcher.group(), "").trim()
        }

        // Clean up extra whitespace
        title = title.replace(Regex("\\s+"), " ").trim()

        return TodoParseResult(
            title = title,
            dueDate = dueDate,
            dueTime = dueTime,
            priority = priority,
            tags = tags,
            goalName = goalName
        )
    }

    /**
     * Parse natural language time description into LocalTime
     * Examples: "아침", "오후 3시", "저녁", "점심"
     */
    fun parseTimeDescription(description: String): LocalTime? {
        return when {
            description.contains("아침") -> LocalTime.of(9, 0)
            description.contains("점심") -> LocalTime.of(12, 0)
            description.contains("오후") && description.contains("3") -> LocalTime.of(15, 0)
            description.contains("저녁") -> LocalTime.of(18, 0)
            description.contains("밤") -> LocalTime.of(21, 0)
            else -> null
        }
    }
}

data class TodoParseResult(
    val title: String,
    val dueDate: String?,
    val dueTime: String?,
    val priority: Priority,
    val tags: List<String>,
    val goalName: String?
)
