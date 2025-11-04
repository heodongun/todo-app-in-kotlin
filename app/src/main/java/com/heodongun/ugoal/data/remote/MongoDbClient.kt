package com.heodongun.ugoal.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.heodongun.ugoal.data.models.*
import kotlinx.coroutines.tasks.await

/**
 * Firebase Firestore Client
 *
 * Firestore 구조:
 * Collection: users
 * Document: default_user
 * Fields: userId, bigGoals[], dailyGoals[], todos[], etc.
 */
class MongoDbClient {
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val defaultUserId = "default_user"

    suspend fun getEnhancedUserData(userId: String = defaultUserId): Result<EnhancedUserData> {
        return try {
            val document = usersCollection.document(userId).get().await()

            if (document.exists()) {
                val userData = document.toObject(EnhancedUserData::class.java)

                // CRITICAL: Deduplicate when reading from Firestore too!
                val cleanedData = userData?.copy(
                    bigGoals = userData.bigGoals.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                    dailyGoals = userData.dailyGoals.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                    todos = userData.todos.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                    smartLists = userData.smartLists.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                    habits = userData.habits.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                    calendarEvents = userData.calendarEvents.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                    comments = userData.comments.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                    sharedLists = userData.sharedLists.filter { it.id.isNotEmpty() }.distinctBy { it.id }
                ) ?: EnhancedUserData()

                Result.success(cleanedData)
            } else {
                // 문서가 없으면 기본값으로 생성
                val defaultData = EnhancedUserData(userId = userId)
                usersCollection.document(userId).set(defaultData).await()
                Result.success(defaultData)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getUserData(userId: String = defaultUserId): Result<UserData> {
        return try {
            val result = getEnhancedUserData(userId)
            result.map { enhanced ->
                UserData(
                    userId = enhanced.userId,
                    bigGoals = enhanced.bigGoals,
                    dailyGoals = enhanced.dailyGoals,
                    todos = enhanced.todos.map { Todo(it.id, it.title, it.isCompleted, it.goalId) }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun updateEnhancedUserData(userData: EnhancedUserData): Result<Boolean> {
        return try {
            // CRITICAL: Deduplicate all arrays before saving to Firestore
            val cleanedData = userData.copy(
                bigGoals = userData.bigGoals.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                dailyGoals = userData.dailyGoals.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                todos = userData.todos.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                smartLists = userData.smartLists.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                habits = userData.habits.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                calendarEvents = userData.calendarEvents.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                comments = userData.comments.filter { it.id.isNotEmpty() }.distinctBy { it.id },
                sharedLists = userData.sharedLists.filter { it.id.isNotEmpty() }.distinctBy { it.id }
            )

            usersCollection.document(cleanedData.userId)
                .set(cleanedData, SetOptions.merge())
                .await()
            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun updateUserData(userData: UserData): Result<Boolean> {
        val enhanced = EnhancedUserData(
            userId = userData.userId,
            bigGoals = userData.bigGoals,
            dailyGoals = userData.dailyGoals,
            todos = userData.todos.map {
                EnhancedTodo(
                    id = it.id,
                    title = it.title,
                    isCompleted = it.isCompleted,
                    goalId = it.goalId
                )
            }
        )
        return updateEnhancedUserData(enhanced)
    }

    suspend fun addBigGoal(goal: BigGoal): Result<Boolean> {
        return try {
            val docRef = usersCollection.document(defaultUserId)

            // Firestore는 배열에 추가하는 FieldValue.arrayUnion 사용
            docRef.update("bigGoals", com.google.firebase.firestore.FieldValue.arrayUnion(goal))
                .await()

            Result.success(true)
        } catch (e: Exception) {
            // 문서가 없으면 새로 생성
            try {
                val userData = EnhancedUserData(
                    userId = defaultUserId,
                    bigGoals = listOf(goal)
                )
                usersCollection.document(defaultUserId).set(userData).await()
                Result.success(true)
            } catch (e2: Exception) {
                e2.printStackTrace()
                Result.failure(e2)
            }
        }
    }

    suspend fun addTodo(todo: Todo): Result<Boolean> {
        return try {
            val enhancedTodo = EnhancedTodo(
                id = todo.id,
                title = todo.title,
                isCompleted = todo.isCompleted,
                goalId = todo.goalId
            )

            val docRef = usersCollection.document(defaultUserId)
            docRef.update("todos", com.google.firebase.firestore.FieldValue.arrayUnion(enhancedTodo))
                .await()

            Result.success(true)
        } catch (e: Exception) {
            // 문서가 없으면 새로 생성
            try {
                val enhancedTodo = EnhancedTodo(
                    id = todo.id,
                    title = todo.title,
                    isCompleted = todo.isCompleted,
                    goalId = todo.goalId
                )
                val userData = EnhancedUserData(
                    userId = defaultUserId,
                    todos = listOf(enhancedTodo)
                )
                usersCollection.document(defaultUserId).set(userData).await()
                Result.success(true)
            } catch (e2: Exception) {
                e2.printStackTrace()
                Result.failure(e2)
            }
        }
    }

    suspend fun addDailyGoal(dailyGoal: DailyGoal): Result<Boolean> {
        return try {
            val docRef = usersCollection.document(defaultUserId)
            docRef.update("dailyGoals", com.google.firebase.firestore.FieldValue.arrayUnion(dailyGoal))
                .await()

            Result.success(true)
        } catch (e: Exception) {
            // 문서가 없으면 새로 생성
            try {
                val userData = EnhancedUserData(
                    userId = defaultUserId,
                    dailyGoals = listOf(dailyGoal)
                )
                usersCollection.document(defaultUserId).set(userData).await()
                Result.success(true)
            } catch (e2: Exception) {
                e2.printStackTrace()
                Result.failure(e2)
            }
        }
    }

    suspend fun testConnection(): Result<String> {
        return try {
            // Firestore 연결 테스트
            val testDoc = usersCollection.document("test").get().await()
            Result.success("Firebase Firestore connected successfully!")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Firestore connection error: ${e.message}", e))
        }
    }

    fun close() {
        // Firestore는 자동으로 관리되므로 close 불필요
    }
}
