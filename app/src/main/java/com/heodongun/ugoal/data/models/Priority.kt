package com.heodongun.ugoal.data.models

import kotlinx.serialization.Serializable

@Serializable
enum class Priority {
    NONE,
    LOW,
    MEDIUM,
    HIGH
}
