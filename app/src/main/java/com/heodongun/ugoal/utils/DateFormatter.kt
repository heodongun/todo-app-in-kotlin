package com.heodongun.ugoal.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {
    
    private val koreanFormatter = DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN)
    
    fun formatToday(): String {
        return LocalDate.now().format(koreanFormatter)
    }
    
    fun formatDate(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            date.format(koreanFormatter)
        } catch (e: Exception) {
            dateString
        }
    }
    
    fun getTodayString(): String {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
}
