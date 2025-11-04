package com.heodongun.ugoal.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.heodongun.ugoal.data.models.EnhancedTodo
import com.heodongun.ugoal.ui.components.TodoItem
import com.heodongun.ugoal.ui.theme.*
import com.heodongun.ugoal.viewmodel.CalendarViewModel
import com.heodongun.ugoal.viewmodel.CalendarViewType
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onTodoClick: (String) -> Unit,
    onAddClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val events by viewModel.events.collectAsState()
    val todosForSelectedDate by viewModel.todosForSelectedDate.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.currentMonthYear) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goToToday() }) {
                        Icon(Icons.Default.Today, "오늘")
                    }
                },
                actions = {
                    // View type selector
                    IconButton(onClick = {
                        val nextType = when (uiState.viewType) {
                            CalendarViewType.MONTH -> CalendarViewType.WEEK
                            CalendarViewType.WEEK -> CalendarViewType.AGENDA
                            CalendarViewType.AGENDA -> CalendarViewType.DAY
                            CalendarViewType.DAY -> CalendarViewType.MONTH
                        }
                        viewModel.setViewType(nextType)
                    }) {
                        Icon(
                            when (uiState.viewType) {
                                CalendarViewType.MONTH -> Icons.Default.CalendarMonth
                                CalendarViewType.WEEK -> Icons.Default.CalendarViewWeek
                                CalendarViewType.DAY -> Icons.Default.CalendarViewDay
                                CalendarViewType.AGENDA -> Icons.Default.ViewAgenda
                            },
                            "뷰 변경"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = TossBlue
            ) {
                Icon(Icons.Default.Add, "할 일 추가", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundWhite)
        ) {
            // Navigation controls
            MonthNavigator(
                currentMonth = uiState.currentMonthYear,
                onPrevious = {
                    when (uiState.viewType) {
                        CalendarViewType.MONTH -> viewModel.previousMonth()
                        CalendarViewType.WEEK -> viewModel.previousWeek()
                        else -> viewModel.selectDate(uiState.selectedDate.minusDays(1))
                    }
                },
                onNext = {
                    when (uiState.viewType) {
                        CalendarViewType.MONTH -> viewModel.nextMonth()
                        CalendarViewType.WEEK -> viewModel.nextWeek()
                        else -> viewModel.selectDate(uiState.selectedDate.plusDays(1))
                    }
                }
            )

            // Calendar view based on selection
            AnimatedContent(
                targetState = uiState.viewType,
                label = "calendar_view_transition"
            ) { viewType ->
                when (viewType) {
                    CalendarViewType.MONTH -> MonthCalendarView(
                        currentMonth = uiState.currentMonth,
                        events = events,
                        selectedDate = uiState.selectedDate,
                        onDateClick = { date -> viewModel.selectDate(date) }
                    )
                    CalendarViewType.WEEK -> WeekCalendarView(
                        currentWeek = uiState.currentWeek,
                        events = events,
                        selectedDate = uiState.selectedDate,
                        onDateClick = { date -> viewModel.selectDate(date) }
                    )
                    CalendarViewType.AGENDA -> AgendaView(
                        events = events,
                        onTodoClick = onTodoClick
                    )
                    CalendarViewType.DAY -> DayView(
                        selectedDate = uiState.selectedDate,
                        events = todosForSelectedDate,
                        onTodoClick = onTodoClick
                    )
                }
            }

            // Selected date todos (for month and week views)
            if (uiState.viewType in listOf(CalendarViewType.MONTH, CalendarViewType.WEEK)) {
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "${uiState.selectedDate.format(DateTimeFormatter.ofPattern("M월 d일 (E)"))} 할 일",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                if (todosForSelectedDate.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "할 일이 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TossGray400
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(todosForSelectedDate, key = { it.id }) { todo ->
                            TodoItem(
                                todo = todo,
                                onToggleComplete = { /* Handle in parent */ },
                                onDelete = { /* Handle in parent */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthNavigator(
    currentMonth: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, "이전", tint = TossGray900)
        }

        Text(
            text = currentMonth,
            style = MaterialTheme.typography.titleLarge,
            color = TossGray900
        )

        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, "다음", tint = TossGray900)
        }
    }
}

@Composable
fun MonthCalendarView(
    currentMonth: YearMonth,
    events: List<EnhancedTodo>,
    selectedDate: LocalDate,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1 = Monday, 7 = Sunday

    Column(modifier = Modifier.fillMaxWidth()) {
        // Week day headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            listOf("월", "화", "수", "목", "금", "토", "일").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = TossGray400
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar grid
        val weeks = (daysInMonth + firstDayOfWeek - 1) / 7 + 1
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            repeat(weeks) { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { dayOfWeek ->
                        val dayNumber = week * 7 + dayOfWeek - firstDayOfWeek + 2

                        if (dayNumber in 1..daysInMonth) {
                            val date = currentMonth.atDay(dayNumber)
                            val eventsForDay = events.filter {
                                it.date == date.toString() || it.dueDate == date.toString()
                            }

                            CalendarDayCell(
                                day = dayNumber,
                                date = date,
                                eventCount = eventsForDay.size,
                                hasHighPriority = eventsForDay.any { it.priority == com.heodongun.ugoal.data.models.Priority.HIGH },
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
}

@Composable
fun RowScope.CalendarDayCell(
    day: Int,
    date: LocalDate,
    eventCount: Int,
    hasHighPriority: Boolean,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(MaterialTheme.shapes.small)
            .background(
                color = when {
                    isSelected -> TossBlue.copy(alpha = 0.1f)
                    isToday -> TossGray50
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Day number
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .then(
                        if (isToday) Modifier
                            .clip(CircleShape)
                            .background(TossBlue)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        isToday -> Color.White
                        isSelected -> TossBlue
                        else -> TossGray900
                    }
                )
            }

            // Event indicators
            if (eventCount > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.height(6.dp)
                ) {
                    repeat(minOf(eventCount, 3)) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .padding(horizontal = 1.dp)
                                .clip(CircleShape)
                                .background(
                                    if (hasHighPriority) ErrorRed
                                    else TossBlue
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeekCalendarView(
    currentWeek: Pair<LocalDate, LocalDate>,
    events: List<EnhancedTodo>,
    selectedDate: LocalDate,
    onDateClick: (LocalDate) -> Unit
) {
    val (startDate, endDate) = currentWeek
    val dates = mutableListOf<LocalDate>()
    var current = startDate
    while (!current.isAfter(endDate)) {
        dates.add(current)
        current = current.plusDays(1)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        dates.forEach { date ->
            val eventsForDay = events.filter {
                it.date == date.toString() || it.dueDate == date.toString()
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onDateClick(date) }
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("E")),
                    style = MaterialTheme.typography.labelSmall,
                    color = TossGray400
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                date == selectedDate -> TossBlue
                                date == LocalDate.now() -> TossBlue.copy(alpha = 0.1f)
                                else -> Color.Transparent
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (date == selectedDate) Color.White else TossGray900
                    )
                }

                if (eventsForDay.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${eventsForDay.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TossBlue
                    )
                }
            }
        }
    }
}

@Composable
fun AgendaView(
    events: List<EnhancedTodo>,
    onTodoClick: (String) -> Unit
) {
    val groupedEvents = events
        .filter { it.dueDate != null }
        .groupBy { it.dueDate!! }
        .toSortedMap()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        groupedEvents.forEach { (date, todosForDate) ->
            item {
                val localDate = LocalDate.parse(date)
                Text(
                    text = localDate.format(DateTimeFormatter.ofPattern("M월 d일 (E)")),
                    style = MaterialTheme.typography.titleMedium,
                    color = TossGray900
                )
            }

            items(todosForDate, key = { it.id }) { todo ->
                TodoItem(
                    todo = todo,
                    onToggleComplete = { /* Handle */ },
                    onDelete = { /* Handle */ }
                )
            }
        }
    }
}

@Composable
fun DayView(
    selectedDate: LocalDate,
    events: List<EnhancedTodo>,
    onTodoClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Date header
        Text(
            text = selectedDate.format(DateTimeFormatter.ofPattern("M월 d일 (E)")),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(20.dp)
        )

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "할 일이 없습니다",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TossGray400
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(events, key = { it.id }) { todo ->
                    TodoItem(
                        todo = todo,
                        onToggleComplete = { /* Handle */ },
                        onDelete = { /* Handle */ }
                    )
                }
            }
        }
    }
}
