package com.heodongun.ugoal.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Subtask(
    val id: String = "",
    val title: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = 0L,
    val completedAt: Long? = null
)
