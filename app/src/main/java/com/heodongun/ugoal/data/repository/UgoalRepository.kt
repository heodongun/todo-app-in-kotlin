package com.heodongun.ugoal.data.repository

import android.util.Log
import com.heodongun.ugoal.data.models.*
import com.heodongun.ugoal.data.remote.MongoDbClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class UgoalRepository(private val mongoClient: MongoDbClient) {

    private val _userData = MutableStateFlow(EnhancedUserData())
    val userData: Flow<EnhancedUserData> = _userData.asStateFlow()

    suspend fun loadUserData(): Result<EnhancedUserData> {
        val result = mongoClient.getEnhancedUserData()
        result.onSuccess { data ->
            Log.d("UgoalRepository", "=== LOADING USER DATA ===")
            Log.d("UgoalRepository", "Raw bigGoals count: ${data.bigGoals.size}")
            Log.d("UgoalRepository", "Raw dailyGoals count: ${data.dailyGoals.size}")
            Log.d("UgoalRepository", "Raw todos count: ${data.todos.size}")

            // Find duplicates BEFORE cleaning
            val goalDuplicates = data.bigGoals.groupBy { it.id }.filter { it.value.size > 1 }
            val todoDuplicates = data.todos.groupBy { it.id }.filter { it.value.size > 1 }
            val dailyGoalDuplicates = data.dailyGoals.groupBy { it.id }.filter { it.value.size > 1 }

            if (goalDuplicates.isNotEmpty()) {
                Log.e("UgoalRepository", "🚨 DUPLICATE GOALS FOUND:")
                goalDuplicates.forEach { (id, goals) ->
                    Log.e("UgoalRepository", "  ID: $id appears ${goals.size} times")
                    goals.forEachIndexed { index, goal ->
                        Log.e("UgoalRepository", "    [$index] title='${goal.title}', color=${goal.color}, icon=${goal.icon}")
                    }
                }
            }

            if (todoDuplicates.isNotEmpty()) {
                Log.e("UgoalRepository", "🚨 DUPLICATE TODOS FOUND:")
                todoDuplicates.forEach { (id, todos) ->
                    Log.e("UgoalRepository", "  ID: $id appears ${todos.size} times")
                }
            }

            if (dailyGoalDuplicates.isNotEmpty()) {
                Log.e("UgoalRepository", "🚨 DUPLICATE DAILY GOALS FOUND:")
                dailyGoalDuplicates.forEach { (id, goals) ->
                    Log.e("UgoalRepository", "  ID: $id appears ${goals.size} times")
                }
            }

            // Deduplicate all arrays when loading from database
            val cleanedData = data.copy(
                bigGoals = data.bigGoals.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                dailyGoals = data.dailyGoals.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                todos = data.todos.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                smartLists = data.smartLists.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                habits = data.habits.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                calendarEvents = data.calendarEvents.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                comments = data.comments.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                sharedLists = data.sharedLists.filter { it.id.isNotEmpty() }.distinctBy { it.id }
            )

            Log.d("UgoalRepository", "After cleaning bigGoals count: ${cleanedData.bigGoals.size}")
            Log.d("UgoalRepository", "After cleaning dailyGoals count: ${cleanedData.dailyGoals.size}")
            Log.d("UgoalRepository", "After cleaning todos count: ${cleanedData.todos.size}")

            _userData.value = cleanedData
            // Update database with cleaned data to fix existing duplicates
            mongoClient.updateEnhancedUserData(cleanedData)
        }
        return result
    }

    suspend fun addBigGoal(goal: BigGoal): Result<Boolean> {
        _userData.update { current ->
            // Check if goal already exists, if so don't add duplicate
            if (current.bigGoals.any { it.id == goal.id }) {
                current
            } else {
                current.copy(bigGoals = current.bigGoals + goal)
            }
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun updateBigGoal(goal: BigGoal): Result<Boolean> {
        _userData.update { current ->
            current.copy(
                bigGoals = current.bigGoals.map { if (it.id == goal.id) goal else it }
            )
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun deleteBigGoal(goalId: String): Result<Boolean> {
        _userData.update { current ->
            current.copy(bigGoals = current.bigGoals.filter { it.id != goalId })
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun addDailyGoal(dailyGoal: DailyGoal): Result<Boolean> {
        _userData.update { current ->
            // Check if daily goal already exists, if so don't add duplicate
            if (current.dailyGoals.any { it.id == dailyGoal.id }) {
                current
            } else {
                current.copy(dailyGoals = current.dailyGoals + dailyGoal)
            }
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun updateDailyGoal(dailyGoal: DailyGoal): Result<Boolean> {
        _userData.update { current ->
            current.copy(
                dailyGoals = current.dailyGoals.map {
                    if (it.id == dailyGoal.id) dailyGoal else it
                }
            )
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun addTodo(todo: EnhancedTodo): Result<Boolean> {
        _userData.update { current ->
            // Check if todo already exists, if so don't add duplicate
            if (current.todos.any { it.id == todo.id }) {
                current
            } else {
                current.copy(todos = current.todos + todo)
            }
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun updateTodo(todo: EnhancedTodo): Result<Boolean> {
        _userData.update { current ->
            current.copy(
                todos = current.todos.map { if (it.id == todo.id) todo else it }
            )
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun deleteTodo(todoId: String): Result<Boolean> {
        _userData.update { current ->
            current.copy(todos = current.todos.filter { it.id != todoId })
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun toggleTodoComplete(todoId: String): Result<Boolean> {
        val todo = _userData.value.todos.find { it.id == todoId } ?: return Result.failure(Exception("Todo not found"))
        val updatedTodo = todo.copy(
            isCompleted = !todo.isCompleted,
            completedAt = if (!todo.isCompleted) System.currentTimeMillis() else null
        )
        return updateTodo(updatedTodo)
    }

    suspend fun getTodosForDate(date: String): List<EnhancedTodo> {
        return _userData.value.todos.filter { it.date == date }
    }

    suspend fun getDailyGoalForDate(date: String): DailyGoal? {
        return _userData.value.dailyGoals.find { it.date == date }
    }

    // Smart Lists
    suspend fun addSmartList(smartList: SmartList): Result<Boolean> {
        _userData.update { current ->
            current.copy(smartLists = current.smartLists + smartList)
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun updateSmartList(smartList: SmartList): Result<Boolean> {
        _userData.update { current ->
            current.copy(
                smartLists = current.smartLists.map {
                    if (it.id == smartList.id) smartList else it
                }
            )
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun deleteSmartList(listId: String): Result<Boolean> {
        _userData.update { current ->
            current.copy(smartLists = current.smartLists.filter { it.id != listId })
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    // Habits
    suspend fun addHabit(habit: Habit): Result<Boolean> {
        _userData.update { current ->
            current.copy(habits = current.habits + habit)
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun updateHabit(habit: Habit): Result<Boolean> {
        _userData.update { current ->
            current.copy(
                habits = current.habits.map {
                    if (it.id == habit.id) habit else it
                }
            )
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun deleteHabit(habitId: String): Result<Boolean> {
        _userData.update { current ->
            current.copy(habits = current.habits.filter { it.id != habitId })
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun completeHabit(habitId: String, date: String): Result<Boolean> {
        _userData.update { current ->
            current.copy(
                habits = current.habits.map { habit ->
                    if (habit.id == habitId) {
                        val currentCount = habit.completionHistory[date] ?: 0
                        val newHistory = habit.completionHistory.toMutableMap()
                        newHistory[date] = currentCount + 1

                        // Update streak
                        val newStreak = calculateStreak(newHistory, date)

                        habit.copy(
                            completionHistory = newHistory,
                            streakCount = newStreak,
                            longestStreak = maxOf(habit.longestStreak, newStreak)
                        )
                    } else habit
                }
            )
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    private fun calculateStreak(history: Map<String, Int>, currentDate: String): Int {
        var streak = 0
        var date = java.time.LocalDate.parse(currentDate)

        while (history.containsKey(date.toString()) && history[date.toString()]!! > 0) {
            streak++
            date = date.minusDays(1)
        }

        return streak
    }

    // Calendar Events
    suspend fun addCalendarEvent(event: CalendarEvent): Result<Boolean> {
        _userData.update { current ->
            current.copy(calendarEvents = current.calendarEvents + event)
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun updateCalendarEvent(event: CalendarEvent): Result<Boolean> {
        _userData.update { current ->
            current.copy(
                calendarEvents = current.calendarEvents.map {
                    if (it.id == event.id) event else it
                }
            )
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun deleteCalendarEvent(eventId: String): Result<Boolean> {
        _userData.update { current ->
            current.copy(calendarEvents = current.calendarEvents.filter { it.id != eventId })
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    // Comments
    suspend fun addComment(comment: Comment): Result<Boolean> {
        _userData.update { current ->
            current.copy(comments = current.comments + comment)
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun updateComment(comment: Comment): Result<Boolean> {
        _userData.update { current ->
            current.copy(
                comments = current.comments.map {
                    if (it.id == comment.id) comment else it
                }
            )
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun deleteComment(commentId: String): Result<Boolean> {
        _userData.update { current ->
            current.copy(comments = current.comments.filter { it.id != commentId })
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun getCommentsForTodo(todoId: String): List<Comment> {
        return _userData.value.comments.filter { it.todoId == todoId }
            .sortedBy { it.timestamp }
    }

    // Shared Lists
    suspend fun shareList(sharedList: SharedList): Result<Boolean> {
        _userData.update { current ->
            current.copy(sharedLists = current.sharedLists + sharedList)
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun updateSharedList(sharedList: SharedList): Result<Boolean> {
        _userData.update { current ->
            current.copy(
                sharedLists = current.sharedLists.map {
                    if (it.id == sharedList.id) sharedList else it
                }
            )
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun removeSharedList(sharedListId: String): Result<Boolean> {
        _userData.update { current ->
            current.copy(sharedLists = current.sharedLists.filter { it.id != sharedListId })
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    // Tags
    suspend fun addTag(tag: String): Result<Boolean> {
        if (tag in _userData.value.tags) return Result.success(true)

        _userData.update { current ->
            current.copy(tags = current.tags + tag)
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    suspend fun removeTag(tag: String): Result<Boolean> {
        _userData.update { current ->
            current.copy(tags = current.tags.filter { it != tag })
        }
        return mongoClient.updateEnhancedUserData(_userData.value)
    }

    fun getAllTags(): List<String> {
        return _userData.value.tags.sorted()
    }

    /**
     * Test MongoDB Atlas connectivity
     */
    suspend fun testDatabaseConnection(): Result<String> {
        return mongoClient.testConnection()
    }
}
