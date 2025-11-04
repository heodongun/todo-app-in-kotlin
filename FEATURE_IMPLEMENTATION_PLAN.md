# Ugoal Advanced Features - Implementation Plan

## 📋 Executive Summary

This document outlines the phased implementation of advanced task management features for Ugoal, transforming it from a basic goal tracker into a comprehensive productivity system comparable to TickTick, Todoist, and Any.do.

### Current State ✅
- Basic MVVM architecture with MongoDB Atlas sync
- EnhancedTodo data model with priority, tags, subtasks, repeat
- Pomodoro timer service
- Basic notification system
- Statistics tracking foundation
- User settings management

### Implementation Phases

**Phase 1** (2-3 weeks): Calendar & Smart Lists - Core productivity features
**Phase 2** (2-3 weeks): Enhanced UX - Drag-drop, advanced notifications
**Phase 3** (2-3 weeks): Visualization - Kanban, habit tracking, analytics
**Phase 4** (3-4 weeks): Collaboration - Comments, sharing, attachments
**Phase 5** (2-3 weeks): Intelligence - Natural language, automation

---

## Phase 1: Calendar Integration & Smart Lists (Priority: HIGH)

### 1.1 Calendar Data Models

#### CalendarEvent.kt
```kotlin
package com.heodongun.ugoal.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CalendarEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val todoId: String? = null, // Link to todo if applicable
    val title: String,
    val description: String = "",
    val startTime: Long, // Unix timestamp
    val endTime: Long, // Unix timestamp
    val location: String? = null,
    val isAllDay: Boolean = false,
    val color: String = "#3182F6",
    val calendarSource: CalendarSource = CalendarSource.UGOAL,
    val externalId: String? = null, // For Google Calendar sync
    val attendees: List<String> = emptyList(),
    val reminders: List<ReminderConfig> = emptyList()
)

@Serializable
enum class CalendarSource {
    UGOAL,          // Native Ugoal events
    GOOGLE,         // Google Calendar
    EXTERNAL        // Other calendar sources
}

@Serializable
data class ReminderConfig(
    val minutesBefore: Int,
    val method: ReminderMethod = ReminderMethod.NOTIFICATION
)

@Serializable
enum class ReminderMethod {
    NOTIFICATION,
    EMAIL,
    ALARM
}
```

#### SmartList.kt
```kotlin
package com.heodongun.ugoal.data.models

import kotlinx.serialization.Serializable

@Serializable
data class SmartList(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val icon: String = "📋",
    val color: String = "#3182F6",
    val filters: ListFilter,
    val sortBy: SortCriteria = SortCriteria.DUE_DATE,
    val isSystem: Boolean = false, // System lists like Today, Upcoming
    val order: Int = 0
)

@Serializable
data class ListFilter(
    val priorities: List<Priority> = emptyList(),
    val tags: List<String> = emptyList(),
    val dateRange: DateRange? = null,
    val status: List<TodoStatus> = emptyList(),
    val goalIds: List<String> = emptyList(),
    val hasSubtasks: Boolean? = null,
    val hasDueDate: Boolean? = null,
    val isRecurring: Boolean? = null
)

@Serializable
data class DateRange(
    val start: String? = null, // yyyy-MM-dd
    val end: String? = null,   // yyyy-MM-dd
    val type: DateRangeType = DateRangeType.CUSTOM
)

@Serializable
enum class DateRangeType {
    TODAY,
    TOMORROW,
    THIS_WEEK,
    NEXT_WEEK,
    THIS_MONTH,
    OVERDUE,
    CUSTOM
}

@Serializable
enum class SortCriteria {
    DUE_DATE,
    PRIORITY,
    CREATED_DATE,
    TITLE,
    CUSTOM_ORDER
}

@Serializable
enum class TodoStatus {
    ACTIVE,
    COMPLETED,
    OVERDUE
}
```

### 1.2 Calendar Screens

#### File: `CalendarScreen.kt`
```kotlin
package com.heodongun.ugoal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.heodongun.ugoal.data.models.*
import com.heodongun.ugoal.ui.components.*
import com.heodongun.ugoal.ui.theme.*
import com.heodongun.ugoal.viewmodel.CalendarViewModel
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onTodoClick: (String) -> Unit,
    onAddClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val events by viewModel.events.collectAsState()

    var selectedView by remember { mutableStateOf(CalendarViewType.MONTH) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.currentMonthYear) },
                actions = {
                    // View switcher
                    IconButton(onClick = { viewModel.showViewSelector() }) {
                        Icon(Icons.Default.ViewModule, "View Type")
                    }

                    // Today button
                    TextButton(onClick = { viewModel.goToToday() }) {
                        Text("오늘")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = TossBlue
            ) {
                Icon(Icons.Default.Add, "Add Todo")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Month navigation
            MonthNavigator(
                currentMonth = uiState.currentMonth,
                onPrevious = { viewModel.previousMonth() },
                onNext = { viewModel.nextMonth() }
            )

            // Calendar view based on selection
            when (selectedView) {
                CalendarViewType.MONTH -> MonthCalendarView(
                    currentMonth = uiState.currentMonth,
                    events = events,
                    selectedDate = uiState.selectedDate,
                    onDateClick = { date -> viewModel.selectDate(date) }
                )
                CalendarViewType.WEEK -> WeekCalendarView(
                    currentWeek = uiState.currentWeek,
                    events = events,
                    onDateClick = { date -> viewModel.selectDate(date) }
                )
                CalendarViewType.AGENDA -> AgendaView(
                    events = events,
                    onTodoClick = onTodoClick
                )
                CalendarViewType.DAY -> DayView(
                    selectedDate = uiState.selectedDate,
                    events = events.filter { it.date == uiState.selectedDate },
                    onTodoClick = onTodoClick
                )
            }
        }
    }
}

@Composable
fun MonthCalendarView(
    currentMonth: YearMonth,
    events: List<EnhancedTodo>,
    selectedDate: LocalDate?,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value

    Column(modifier = Modifier.fillMaxWidth()) {
        // Week day headers
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("월", "화", "수", "목", "금", "토", "일").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = TossGray400
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar grid
        val weeks = (daysInMonth + firstDayOfWeek - 1) / 7 + 1
        repeat(weeks) { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { dayOfWeek ->
                    val dayNumber = week * 7 + dayOfWeek - firstDayOfWeek + 2

                    if (dayNumber in 1..daysInMonth) {
                        val date = currentMonth.atDay(dayNumber)
                        val eventsForDay = events.filter {
                            it.date == date.toString()
                        }

                        CalendarDayCell(
                            day = dayNumber,
                            date = date,
                            eventCount = eventsForDay.size,
                            isSelected = date == selectedDate,
                            isToday = date == LocalDate.now(),
                            onClick = { onDateClick(date) }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    day: Int,
    date: LocalDate,
    eventCount: Int,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            .background(
                color = when {
                    isSelected -> TossBlue.copy(alpha = 0.1f)
                    isToday -> TossGray50
                    else -> Color.Transparent
                },
                shape = MaterialTheme.shapes.small
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isToday -> TossBlue
                    else -> TossGray900
                }
            )

            if (eventCount > 0) {
                Text(
                    text = "•".repeat(minOf(eventCount, 3)),
                    style = MaterialTheme.typography.labelSmall,
                    color = TossBlue
                )
            }
        }
    }
}

enum class CalendarViewType {
    MONTH,
    WEEK,
    AGENDA,
    DAY
}
```

### 1.3 Calendar ViewModel

#### File: `CalendarViewModel.kt`
```kotlin
package com.heodongun.ugoal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heodongun.ugoal.data.models.EnhancedTodo
import com.heodongun.ugoal.data.repository.UgoalRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val currentWeek: Pair<LocalDate, LocalDate> = getCurrentWeek(),
    val selectedDate: LocalDate = LocalDate.now(),
    val currentMonthYear: String = "",
    val isLoading: Boolean = false
)

class CalendarViewModel(
    private val repository: UgoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    val events: StateFlow<List<EnhancedTodo>> = repository.userData
        .map { userData ->
            userData.todos
                .filter { it.date != null || it.dueDate != null }
                .sortedBy { it.dueDate ?: it.date }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        updateMonthYear()
    }

    fun previousMonth() {
        _uiState.update {
            it.copy(currentMonth = it.currentMonth.minusMonths(1))
        }
        updateMonthYear()
    }

    fun nextMonth() {
        _uiState.update {
            it.copy(currentMonth = it.currentMonth.plusMonths(1))
        }
        updateMonthYear()
    }

    fun goToToday() {
        _uiState.update {
            it.copy(
                currentMonth = YearMonth.now(),
                selectedDate = LocalDate.now()
            )
        }
        updateMonthYear()
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun showViewSelector() {
        // Show bottom sheet or dialog for view selection
    }

    private fun updateMonthYear() {
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월")
        _uiState.update {
            it.copy(currentMonthYear = it.currentMonth.format(formatter))
        }
    }
}

private fun getCurrentWeek(): Pair<LocalDate, LocalDate> {
    val today = LocalDate.now()
    val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
    val endOfWeek = startOfWeek.plusDays(6)
    return Pair(startOfWeek, endOfWeek)
}
```

### 1.4 Smart Lists Implementation

#### File: `SmartListsScreen.kt`
```kotlin
package com.heodongun.ugoal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heodongun.ugoal.data.models.*
import com.heodongun.ugoal.ui.theme.*
import com.heodongun.ugoal.viewmodel.SmartListsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartListsScreen(
    viewModel: SmartListsViewModel,
    onListClick: (String) -> Unit,
    onCreateList: () -> Unit
) {
    val smartLists by viewModel.smartLists.collectAsState()
    val systemLists by viewModel.systemLists.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("스마트 리스트") },
                actions = {
                    IconButton(onClick = onCreateList) {
                        Icon(Icons.Default.Add, "Create List")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // System lists (non-editable)
            item {
                Text(
                    text = "시스템 리스트",
                    style = MaterialTheme.typography.titleMedium,
                    color = TossGray400
                )
            }

            items(systemLists) { list ->
                SmartListCard(
                    smartList = list,
                    todoCount = viewModel.getTodoCount(list),
                    onClick = { onListClick(list.id) }
                )
            }

            // Custom smart lists
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "내 리스트",
                    style = MaterialTheme.typography.titleMedium,
                    color = TossGray400
                )
            }

            items(smartLists) { list ->
                SmartListCard(
                    smartList = list,
                    todoCount = viewModel.getTodoCount(list),
                    onClick = { onListClick(list.id) },
                    onEdit = { viewModel.editList(list.id) },
                    onDelete = { viewModel.deleteList(list.id) }
                )
            }
        }
    }
}

@Composable
fun SmartListCard(
    smartList: SmartList,
    todoCount: Int,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = BackgroundWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Text(
                text = smartList.icon,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Name and count
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = smartList.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$todoCount개 항목",
                    style = MaterialTheme.typography.bodySmall,
                    color = TossGray400
                )
            }

            // Actions (only for custom lists)
            if (!smartList.isSystem) {
                IconButton(onClick = { onEdit?.invoke() }) {
                    Icon(
                        Icons.Default.Edit,
                        "Edit",
                        tint = TossGray400
                    )
                }

                IconButton(onClick = { onDelete?.invoke() }) {
                    Icon(
                        Icons.Default.Delete,
                        "Delete",
                        tint = ErrorRed
                    )
                }
            }
        }
    }
}
```

### 1.5 Filter System Implementation

#### File: `FilterEngine.kt`
```kotlin
package com.heodongun.ugoal.utils

import com.heodongun.ugoal.data.models.*
import java.time.LocalDate

object FilterEngine {

    fun applyFilter(
        todos: List<EnhancedTodo>,
        filter: ListFilter,
        sortBy: SortCriteria = SortCriteria.DUE_DATE
    ): List<EnhancedTodo> {
        var filtered = todos

        // Filter by priority
        if (filter.priorities.isNotEmpty()) {
            filtered = filtered.filter { it.priority in filter.priorities }
        }

        // Filter by tags
        if (filter.tags.isNotEmpty()) {
            filtered = filtered.filter { todo ->
                filter.tags.any { tag -> tag in todo.tags }
            }
        }

        // Filter by date range
        filter.dateRange?.let { range ->
            filtered = when (range.type) {
                DateRangeType.TODAY -> {
                    val today = LocalDate.now().toString()
                    filtered.filter { it.dueDate == today }
                }
                DateRangeType.TOMORROW -> {
                    val tomorrow = LocalDate.now().plusDays(1).toString()
                    filtered.filter { it.dueDate == tomorrow }
                }
                DateRangeType.THIS_WEEK -> {
                    val today = LocalDate.now()
                    val endOfWeek = today.plusDays(7 - today.dayOfWeek.value.toLong())
                    filtered.filter { todo ->
                        todo.dueDate?.let { dueDate ->
                            val date = LocalDate.parse(dueDate)
                            date in today..endOfWeek
                        } ?: false
                    }
                }
                DateRangeType.OVERDUE -> {
                    filtered.filter { it.isOverdue }
                }
                DateRangeType.CUSTOM -> {
                    filtered.filter { todo ->
                        todo.dueDate?.let { dueDate ->
                            val date = LocalDate.parse(dueDate)
                            val start = range.start?.let { LocalDate.parse(it) }
                            val end = range.end?.let { LocalDate.parse(it) }

                            when {
                                start != null && end != null -> date in start..end
                                start != null -> date >= start
                                end != null -> date <= end
                                else -> true
                            }
                        } ?: false
                    }
                }
                else -> filtered
            }
        }

        // Filter by status
        if (filter.status.isNotEmpty()) {
            filtered = filtered.filter { todo ->
                when {
                    TodoStatus.COMPLETED in filter.status && todo.isCompleted -> true
                    TodoStatus.OVERDUE in filter.status && todo.isOverdue -> true
                    TodoStatus.ACTIVE in filter.status && !todo.isCompleted && !todo.isOverdue -> true
                    else -> false
                }
            }
        }

        // Filter by goal
        if (filter.goalIds.isNotEmpty()) {
            filtered = filtered.filter { it.goalId in filter.goalIds }
        }

        // Filter by subtasks presence
        filter.hasSubtasks?.let { hasSubtasks ->
            filtered = filtered.filter {
                (it.subtasks.isNotEmpty()) == hasSubtasks
            }
        }

        // Filter by due date presence
        filter.hasDueDate?.let { hasDue ->
            filtered = filtered.filter {
                (it.dueDate != null) == hasDue
            }
        }

        // Filter by recurring
        filter.isRecurring?.let { isRecurring ->
            filtered = filtered.filter {
                (it.repeat.type != RepeatType.NONE) == isRecurring
            }
        }

        // Apply sorting
        return when (sortBy) {
            SortCriteria.DUE_DATE -> filtered.sortedBy { it.dueDate }
            SortCriteria.PRIORITY -> filtered.sortedByDescending { it.priority }
            SortCriteria.CREATED_DATE -> filtered.sortedBy { it.createdAt }
            SortCriteria.TITLE -> filtered.sortedBy { it.title }
            SortCriteria.CUSTOM_ORDER -> filtered.sortedBy { it.order }
        }
    }

    fun createSystemLists(): List<SmartList> {
        return listOf(
            SmartList(
                id = "system_today",
                name = "오늘",
                icon = "📅",
                filters = ListFilter(
                    dateRange = DateRange(type = DateRangeType.TODAY)
                ),
                isSystem = true
            ),
            SmartList(
                id = "system_tomorrow",
                name = "내일",
                icon = "➡️",
                filters = ListFilter(
                    dateRange = DateRange(type = DateRangeType.TOMORROW)
                ),
                isSystem = true
            ),
            SmartList(
                id = "system_week",
                name = "이번 주",
                icon = "📆",
                filters = ListFilter(
                    dateRange = DateRange(type = DateRangeType.THIS_WEEK)
                ),
                isSystem = true
            ),
            SmartList(
                id = "system_overdue",
                name = "미완료",
                icon = "⚠️",
                filters = ListFilter(
                    dateRange = DateRange(type = DateRangeType.OVERDUE)
                ),
                isSystem = true
            ),
            SmartList(
                id = "system_high_priority",
                name = "중요",
                icon = "🔴",
                filters = ListFilter(
                    priorities = listOf(Priority.HIGH)
                ),
                isSystem = true
            ),
            SmartList(
                id = "system_all",
                name = "모든 할 일",
                icon = "📋",
                filters = ListFilter(),
                isSystem = true
            )
        )
    }
}
```

---

## Phase 2: Enhanced User Experience (Priority: HIGH)

### 2.1 Drag and Drop Reordering

#### Dependencies (add to build.gradle.kts)
```kotlin
dependencies {
    // For drag and drop
    implementation("androidx.compose.foundation:foundation:1.6.0")
}
```

#### File: `DraggableTodoList.kt`
```kotlin
package com.heodongun.ugoal.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.heodongun.ugoal.data.models.EnhancedTodo
import com.heodongun.ugoal.utils.HapticFeedback

@Composable
fun DraggableTodoList(
    todos: List<EnhancedTodo>,
    onReorder: (Int, Int) -> Unit,
    onTodoClick: (String) -> Unit,
    onToggleComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var targetIndex by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            items = todos,
            key = { _, todo -> todo.id }
        ) { index, todo ->
            val isDragged = draggedIndex == index
            val isTarget = targetIndex == index

            val elevation by animateDpAsState(
                targetValue = if (isDragged) 8.dp else 0.dp,
                label = "elevation"
            )

            val scale by animateFloatAsState(
                targetValue = if (isDragged) 1.05f else 1f,
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationY = if (isDragged) dragOffset.y else 0f
                    }
                    .zIndex(if (isDragged) 1f else 0f)
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggedIndex = index
                                HapticFeedback.performLongPress(context)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += Offset(0f, dragAmount.y)

                                // Calculate target index
                                val itemHeight = 80.dp.toPx()
                                val offset = dragOffset.y / itemHeight
                                targetIndex = (index + offset.toInt())
                                    .coerceIn(0, todos.size - 1)
                            },
                            onDragEnd = {
                                targetIndex?.let { target ->
                                    if (target != index) {
                                        onReorder(index, target)
                                        HapticFeedback.performClick(context)
                                    }
                                }
                                draggedIndex = null
                                dragOffset = Offset.Zero
                                targetIndex = null
                            },
                            onDragCancel = {
                                draggedIndex = null
                                dragOffset = Offset.Zero
                                targetIndex = null
                            }
                        )
                    }
            ) {
                TodoItemCard(
                    todo = todo,
                    onClick = { onTodoClick(todo.id) },
                    onToggleComplete = { onToggleComplete(todo.id) },
                    elevation = elevation,
                    showDragHandle = true
                )
            }
        }
    }
}
```

### 2.2 Advanced Notification System

#### File: `NotificationManager.kt`
```kotlin
package com.heodongun.ugoal.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.heodongun.ugoal.MainActivity
import com.heodongun.ugoal.R
import com.heodongun.ugoal.data.models.EnhancedTodo
import com.heodongun.ugoal.data.models.ReminderConfig

object NotificationManager {

    private const val CHANNEL_TODO_REMINDERS = "todo_reminders"
    private const val CHANNEL_PERSISTENT = "persistent_reminders"
    private const val CHANNEL_DEADLINE = "deadline_alerts"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_TODO_REMINDERS,
                    "할 일 알림",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "일반 할 일 알림"
                    enableVibration(true)
                    enableLights(true)
                },
                NotificationChannel(
                    CHANNEL_PERSISTENT,
                    "계속 울림 알림",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "완료할 때까지 계속 알림"
                    setSound(null, null) // Custom sound handling
                },
                NotificationChannel(
                    CHANNEL_DEADLINE,
                    "마감 알림",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "마감 시간 알림"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 200, 500)
                }
            )

            val manager = context.getSystemService(NotificationManager::class.java)
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }

    fun scheduleReminders(
        context: Context,
        todo: EnhancedTodo,
        reminders: List<ReminderConfig>
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        reminders.forEachIndexed { index, reminder ->
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("TODO_ID", todo.id)
                putExtra("TODO_TITLE", todo.title)
                putExtra("REMINDER_INDEX", index)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                todo.id.hashCode() + index,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerTime = calculateTriggerTime(todo, reminder)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        }
    }

    fun showPersistentReminder(
        context: Context,
        todo: EnhancedTodo
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("TODO_ID", todo.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PERSISTENT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(todo.title)
            .setContentText("완료할 때까지 알림이 계속됩니다")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Cannot be dismissed
            .setAutoCancel(false)
            .addAction(
                R.drawable.ic_check,
                "완료",
                createCompleteIntent(context, todo.id)
            )
            .addAction(
                R.drawable.ic_snooze,
                "10분 뒤",
                createSnoozeIntent(context, todo.id, 10)
            )
            .build()

        NotificationManagerCompat.from(context)
            .notify(todo.id.hashCode(), notification)
    }

    private fun calculateTriggerTime(
        todo: EnhancedTodo,
        reminder: ReminderConfig
    ): Long {
        // Calculate when to trigger based on due date/time and minutes before
        val dueDateTime = parseDueDateTime(todo.dueDate, todo.dueTime)
        return dueDateTime - (reminder.minutesBefore * 60 * 1000)
    }

    private fun createCompleteIntent(context: Context, todoId: String): PendingIntent {
        val intent = Intent(context, TodoActionReceiver::class.java).apply {
            action = "COMPLETE_TODO"
            putExtra("TODO_ID", todoId)
        }
        return PendingIntent.getBroadcast(
            context,
            todoId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createSnoozeIntent(
        context: Context,
        todoId: String,
        minutes: Int
    ): PendingIntent {
        val intent = Intent(context, TodoActionReceiver::class.java).apply {
            action = "SNOOZE_TODO"
            putExtra("TODO_ID", todoId)
            putExtra("SNOOZE_MINUTES", minutes)
        }
        return PendingIntent.getBroadcast(
            context,
            todoId.hashCode() + 1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun parseDueDateTime(dueDate: String?, dueTime: String?): Long {
        // Parse date and time into timestamp
        return System.currentTimeMillis() // Placeholder
    }
}
```

---

## Phase 3: Visualization & Analytics (Priority: MEDIUM)

### 3.1 Kanban Board View

#### File: `KanbanBoardScreen.kt`
```kotlin
package com.heodongun.ugoal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.heodongun.ugoal.data.models.*
import com.heodongun.ugoal.ui.components.TodoCard
import com.heodongun.ugoal.ui.theme.*
import com.heodongun.ugoal.viewmodel.KanbanViewModel

@Composable
fun KanbanBoardScreen(
    viewModel: KanbanViewModel,
    onTodoClick: (String) -> Unit
) {
    val columns by viewModel.columns.collectAsState()
    val todosByColumn by viewModel.todosByColumn.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        columns.forEach { column ->
            KanbanColumn(
                column = column,
                todos = todosByColumn[column.id] ?: emptyList(),
                onTodoClick = onTodoClick,
                onTodoMoved = { todoId, newColumnId ->
                    viewModel.moveTodo(todoId, newColumnId)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun KanbanColumn(
    column: KanbanColumn,
    todos: List<EnhancedTodo>,
    onTodoClick: (String) -> Unit,
    onTodoMoved: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(TossGray50, MaterialTheme.shapes.medium)
            .padding(12.dp)
    ) {
        // Column header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = column.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = todos.size.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = TossGray400
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tasks in column
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(todos, key = { it.id }) { todo ->
                TodoCard(
                    todo = todo,
                    onClick = { onTodoClick(todo.id) }
                )
            }
        }
    }
}

data class KanbanColumn(
    val id: String,
    val name: String,
    val order: Int
)
```

### 3.2 Habit Tracking

#### File: `Habit.kt`
```kotlin
package com.heodongun.ugoal.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Habit(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val icon: String = "✓",
    val color: String = "#3182F6",
    val frequency: HabitFrequency,
    val targetCount: Int = 1, // How many times per day/week
    val startDate: String, // yyyy-MM-dd
    val endDate: String? = null,
    val reminders: List<String> = emptyList(), // Times to remind
    val streakCount: Int = 0,
    val longestStreak: Int = 0,
    val completionHistory: Map<String, Int> = emptyMap(), // date -> count
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
enum class HabitFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM
}

@Serializable
data class HabitCompletion(
    val habitId: String,
    val date: String, // yyyy-MM-dd
    val count: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)
```

---

## Phase 4: Collaboration Features (Priority: LOW)

### 4.1 Comments and Sharing

#### File: `Comment.kt`
```kotlin
package com.heodongun.ugoal.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: String = java.util.UUID.randomUUID().toString(),
    val todoId: String,
    val userId: String,
    val userName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isEdited: Boolean = false
)

@Serializable
data class SharedList(
    val id: String,
    val listId: String,
    val ownerId: String,
    val sharedWith: List<String>, // User IDs
    val permissions: SharePermissions,
    val sharedAt: Long = System.currentTimeMillis()
)

@Serializable
data class SharePermissions(
    val canEdit: Boolean = false,
    val canDelete: Boolean = false,
    val canShare: Boolean = false
)
```

---

## Phase 5: Intelligence Features (Priority: LOW)

### 5.1 Natural Language Parser

#### File: `NaturalLanguageParser.kt`
```kotlin
package com.heodongun.ugoal.utils

import com.heodongun.ugoal.data.models.*
import java.time.LocalDate
import java.time.LocalTime
import java.util.regex.Pattern

object NaturalLanguageParser {

    private val timePattern = Pattern.compile("(\\d{1,2})(시|:)(\\d{2})?(분)?")
    private val datePatterns = mapOf(
        "오늘" to 0,
        "내일" to 1,
        "모레" to 2,
        "다음주" to 7
    )
    private val priorityKeywords = mapOf(
        "중요" to Priority.HIGH,
        "긴급" to Priority.HIGH,
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

        // Extract time
        val timeMatcher = timePattern.matcher(input)
        if (timeMatcher.find()) {
            val hour = timeMatcher.group(1)
            val minute = timeMatcher.group(3) ?: "00"
            dueTime = String.format("%02d:%02d", hour.toInt(), minute.toInt())
            title = title.replace(timeMatcher.group(), "").trim()
        }

        // Extract date keywords
        datePatterns.forEach { (keyword, daysOffset) ->
            if (input.contains(keyword)) {
                dueDate = LocalDate.now().plusDays(daysOffset.toLong()).toString()
                title = title.replace(keyword, "").trim()
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
        val hashtagPattern = Pattern.compile("#(\\w+)")
        val hashtagMatcher = hashtagPattern.matcher(input)
        while (hashtagMatcher.find()) {
            tags.add(hashtagMatcher.group(1))
            title = title.replace(hashtagMatcher.group(), "").trim()
        }

        // Extract @mentions for goal assignment
        val mentionPattern = Pattern.compile("@(\\w+)")
        var goalName: String? = null
        val mentionMatcher = mentionPattern.matcher(input)
        if (mentionMatcher.find()) {
            goalName = mentionMatcher.group(1)
            title = title.replace(mentionMatcher.group(), "").trim()
        }

        return TodoParseResult(
            title = title.trim(),
            dueDate = dueDate,
            dueTime = dueTime,
            priority = priority,
            tags = tags,
            goalName = goalName
        )
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
```

---

## Testing Strategy

### Unit Tests
```kotlin
// CalendarViewModelTest.kt
class CalendarViewModelTest {
    @Test
    fun `previousMonth updates currentMonth correctly`() {
        val viewModel = CalendarViewModel(mockRepository)
        val initialMonth = viewModel.uiState.value.currentMonth

        viewModel.previousMonth()

        assertEquals(
            initialMonth.minusMonths(1),
            viewModel.uiState.value.currentMonth
        )
    }
}

// FilterEngineTest.kt
class FilterEngineTest {
    @Test
    fun `filter by priority returns only high priority todos`() {
        val todos = listOf(
            mockTodo(priority = Priority.HIGH),
            mockTodo(priority = Priority.LOW),
            mockTodo(priority = Priority.HIGH)
        )

        val filtered = FilterEngine.applyFilter(
            todos,
            ListFilter(priorities = listOf(Priority.HIGH))
        )

        assertEquals(2, filtered.size)
        assertTrue(filtered.all { it.priority == Priority.HIGH })
    }
}
```

---

## Navigation Updates

### Add to NavGraph.kt
```kotlin
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Goals : Screen("goals")
    object Calendar : Screen("calendar")
    object SmartLists : Screen("smart_lists")
    object SmartListDetail : Screen("smart_list/{listId}")
    object Kanban : Screen("kanban")
    object Habits : Screen("habits")
    object Statistics : Screen("statistics")
    object TodoDetail : Screen("todo_detail/{todoId}")
    object AddTodo : Screen("add_todo")
    object AddGoal : Screen("add_goal")
}

// In NavHost
composable(Screen.Calendar.route) {
    val viewModel: CalendarViewModel = viewModel(factory = ...)
    CalendarScreen(
        viewModel = viewModel,
        onTodoClick = { todoId ->
            navController.navigate(Screen.TodoDetail.createRoute(todoId))
        },
        onAddClick = { navController.navigate(Screen.AddTodo.route) }
    )
}
```

---

## MongoDB Schema Updates

### EnhancedUserData
```kotlin
@Serializable
data class EnhancedUserData(
    val userId: String = "default_user",
    val bigGoals: List<BigGoal> = emptyList(),
    val dailyGoals: List<DailyGoal> = emptyList(),
    val todos: List<EnhancedTodo> = emptyList(),
    val smartLists: List<SmartList> = emptyList(),
    val habits: List<Habit> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val sharedLists: List<SharedList> = emptyList(),
    val statistics: UserStatistics = UserStatistics(),
    val settings: UserSettings = UserSettings()
)
```

---

## Implementation Timeline

### Phase 1: Calendar & Smart Lists (2-3 weeks)
- Week 1: Data models + Calendar views
- Week 2: Smart lists + Filter engine
- Week 3: Testing + Polish

### Phase 2: Enhanced UX (2-3 weeks)
- Week 1: Drag-drop implementation
- Week 2: Advanced notifications
- Week 3: Testing + Polish

### Phase 3: Visualization (2-3 weeks)
- Week 1: Kanban board
- Week 2: Habit tracking
- Week 3: Analytics dashboard

### Phase 4: Collaboration (3-4 weeks)
- Week 1-2: Comments + Sharing
- Week 3: File attachments
- Week 4: Testing + Polish

### Phase 5: Intelligence (2-3 weeks)
- Week 1: Natural language parser
- Week 2: Automation features
- Week 3: Testing + Polish

**Total Estimated Time: 11-16 weeks**

---

## Success Metrics

- Calendar views render with <100ms latency
- Smart lists filter 1000+ todos in <50ms
- Drag-drop feels smooth (60fps)
- Notifications trigger within 1 second of scheduled time
- Natural language parsing accuracy >85%
- App remains under 50MB size
- MongoDB sync completes in <2 seconds

---

## Next Steps

1. Review and approve this implementation plan
2. Start with Phase 1: Calendar integration
3. Create feature branches for parallel development
4. Set up testing infrastructure
5. Document API endpoints for collaboration features
