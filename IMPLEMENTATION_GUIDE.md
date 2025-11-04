# Ugoal Expansion - Implementation Guide

This guide provides code templates and patterns for implementing the remaining features.

## 1. Enhanced TodoDetailScreen Template

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoDetailScreen(
    todoId: String?,
    viewModel: TodoDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (todoId == null) "새 할 일" else "할 일 수정") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveTodo() }) {
                        Icon(Icons.Default.Check, "저장")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title input
            item {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text("제목") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Description input
            item {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text("설명") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
            
            // Priority selector
            item {
                PriorityPicker(
                    selected = uiState.priority,
                    onSelect = { viewModel.updatePriority(it) }
                )
            }
            
            // Due date/time picker
            item {
                DateTimePicker(
                    date = uiState.dueDate,
                    time = uiState.dueTime,
                    onDateChange = { viewModel.updateDueDate(it) },
                    onTimeChange = { viewModel.updateDueTime(it) }
                )
            }
            
            // Tags
            item {
                TagPicker(
                    selectedTags = uiState.tags,
                    availableTags = uiState.availableTags,
                    onTagsChange = { viewModel.updateTags(it) }
                )
            }
            
            // Repeat configuration
            item {
                RepeatPicker(
                    repeat = uiState.repeat,
                    onChange = { viewModel.updateRepeat(it) }
                )
            }
            
            // Reminder
            item {
                ReminderPicker(
                    reminderTime = uiState.reminderTime,
                    onChange = { viewModel.updateReminder(it) }
                )
            }
            
            // Subtasks section
            item {
                Text("하위 작업", style = MaterialTheme.typography.titleMedium)
            }
            
            items(uiState.subtasks) { subtask ->
                SubtaskItem(
                    subtask = subtask,
                    onToggle = { viewModel.toggleSubtask(subtask.id) },
                    onDelete = { viewModel.deleteSubtask(subtask.id) }
                )
            }
            
            item {
                OutlinedButton(
                    onClick = { viewModel.addSubtask() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, "추가")
                    Spacer(Modifier.width(8.dp))
                    Text("하위 작업 추가")
                }
            }
            
            // Notes section
            item {
                Text("메모", style = MaterialTheme.typography.titleMedium)
            }
            
            item {
                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = { viewModel.updateNote(it) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5,
                    placeholder = { Text("메모를 입력하세요...") }
                )
            }
        }
    }
}
```

## 2. TodoDetailViewModel Template

```kotlin
class TodoDetailViewModel(
    private val repository: EnhancedTodoRepository,
    private val todoId: String?
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TodoDetailUiState())
    val uiState: StateFlow<TodoDetailUiState> = _uiState.asStateFlow()
    
    init {
        todoId?.let { loadTodo(it) }
    }
    
    private fun loadTodo(id: String) {
        viewModelScope.launch {
            repository.getTodoById(id)?.let { todo ->
                _uiState.update { 
                    it.copy(
                        title = todo.title,
                        description = todo.description,
                        priority = todo.priority,
                        dueDate = todo.dueDate,
                        dueTime = todo.dueTime,
                        tags = todo.tags,
                        subtasks = todo.subtasks,
                        repeat = todo.repeat,
                        reminderTime = todo.reminderTime,
                        note = todo.note
                    )
                }
            }
        }
    }
    
    fun updateTitle(title: String) {
        _uiState.update { it.copy(title = title) }
    }
    
    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }
    
    fun updatePriority(priority: Priority) {
        _uiState.update { it.copy(priority = priority) }
    }
    
    fun updateDueDate(date: String?) {
        _uiState.update { it.copy(dueDate = date) }
    }
    
    fun updateDueTime(time: String?) {
        _uiState.update { it.copy(dueTime = time) }
    }
    
    fun updateTags(tags: List<String>) {
        _uiState.update { it.copy(tags = tags) }
    }
    
    fun updateRepeat(repeat: RepeatConfig) {
        _uiState.update { it.copy(repeat = repeat) }
    }
    
    fun updateReminder(reminderTime: String?) {
        _uiState.update { it.copy(reminderTime = reminderTime) }
    }
    
    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }
    
    fun addSubtask() {
        val newSubtask = Subtask(title = "")
        _uiState.update { 
            it.copy(subtasks = it.subtasks + newSubtask)
        }
    }
    
    fun toggleSubtask(subtaskId: String) {
        _uiState.update { state ->
            state.copy(
                subtasks = state.subtasks.map { subtask ->
                    if (subtask.id == subtaskId) {
                        subtask.copy(isCompleted = !subtask.isCompleted)
                    } else subtask
                }
            )
        }
    }
    
    fun deleteSubtask(subtaskId: String) {
        _uiState.update { state ->
            state.copy(subtasks = state.subtasks.filter { it.id != subtaskId })
        }
    }
    
    fun saveTodo() {
        viewModelScope.launch {
            val state = _uiState.value
            val todo = EnhancedTodo(
                id = todoId ?: UUID.randomUUID().toString(),
                title = state.title,
                description = state.description,
                priority = state.priority,
                dueDate = state.dueDate,
                dueTime = state.dueTime,
                tags = state.tags,
                subtasks = state.subtasks,
                repeat = state.repeat,
                reminderTime = state.reminderTime,
                note = state.note
            )
            
            if (todoId == null) {
                repository.addTodo(todo)
            } else {
                repository.updateTodo(todo)
            }
            
            // Schedule reminder if set
            if (todo.reminderTime != null) {
                scheduleReminder(todo)
            }
        }
    }
    
    private fun scheduleReminder(todo: EnhancedTodo) {
        // Use AlarmReceiver.scheduleTodoReminder()
    }
}

data class TodoDetailUiState(
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.NONE,
    val dueDate: String? = null,
    val dueTime: String? = null,
    val tags: List<String> = emptyList(),
    val subtasks: List<Subtask> = emptyList(),
    val repeat: RepeatConfig = RepeatConfig(),
    val reminderTime: String? = null,
    val note: String = "",
    val availableTags: List<String> = emptyList()
)
```

## 3. PomodoroScreen Template

```kotlin
@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel,
    onNavigateBack: () -> Unit
) {
    val timerState by viewModel.timerState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("포모도로") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Timer display
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = if (timerState is Running && timerState.isBreak) {
                                listOf(SuccessGreen.copy(alpha = 0.2f), Color.Transparent)
                            } else {
                                listOf(TossBlue.copy(alpha = 0.2f), Color.Transparent)
                            }
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (val state = timerState) {
                    is Running -> {
                        val minutes = state.remainingSeconds / 60
                        val seconds = state.remainingSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                            color = if (state.isBreak) SuccessGreen else TossBlue
                        )
                    }
                    is Paused -> {
                        val minutes = state.remainingSeconds / 60
                        val seconds = state.remainingSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                            color = TossGray500
                        )
                    }
                    else -> {
                        Text(
                            text = "25:00",
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp),
                            color = TossGray400
                        )
                    }
                }
            }
            
            // Status text
            Text(
                text = when (timerState) {
                    is Running -> if ((timerState as Running).isBreak) "휴식 중" else "집중 중"
                    is Paused -> "일시정지"
                    else -> "집중할 준비가 되셨나요?"
                },
                style = MaterialTheme.typography.titleLarge,
                color = when (timerState) {
                    is Running -> if ((timerState as Running).isBreak) SuccessGreen else TossBlue
                    else -> TossGray600
                }
            )
            
            // Control buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (timerState) {
                    is Idle -> {
                        Button(
                            onClick = { viewModel.startWork() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("시작")
                        }
                    }
                    is Running -> {
                        Button(
                            onClick = { viewModel.pause() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("일시정지")
                        }
                        OutlinedButton(
                            onClick = { viewModel.stop() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("종료")
                        }
                    }
                    is Paused -> {
                        Button(
                            onClick = { viewModel.resume() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("계속")
                        }
                        OutlinedButton(
                            onClick = { viewModel.stop() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("종료")
                        }
                    }
                    else -> {}
                }
            }
            
            // Statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    label = "오늘",
                    value = "${uiState.todayMinutes}분"
                )
                StatCard(
                    label = "이번 주",
                    value = "${uiState.weekMinutes}분"
                )
                StatCard(
                    label = "완료",
                    value = "${uiState.completedSessions}"
                )
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String) {
    Surface(
        modifier = Modifier.size(100.dp),
        shape = RoundedCornerShape(16.dp),
        color = TossGray50
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = TossBlue
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TossGray600
            )
        }
    }
}
```

## 4. Reusable Component Templates

### PriorityPicker
```kotlin
@Composable
fun PriorityPicker(
    selected: Priority,
    onSelect: (Priority) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("우선순위", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Priority.values().forEach { priority ->
                FilterChip(
                    selected = selected == priority,
                    onClick = { onSelect(priority) },
                    label = { 
                        Text(when (priority) {
                            Priority.NONE -> "없음"
                            Priority.LOW -> "낮음"
                            Priority.MEDIUM -> "보통"
                            Priority.HIGH -> "높음"
                        })
                    },
                    leadingIcon = {
                        if (priority != Priority.NONE) {
                            Icon(
                                Icons.Default.Flag,
                                contentDescription = null,
                                tint = when (priority) {
                                    Priority.HIGH -> ErrorRed
                                    Priority.MEDIUM -> WarningYellow
                                    else -> TossGray400
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}
```

### TagPicker
```kotlin
@Composable
fun TagPicker(
    selectedTags: List<String>,
    availableTags: List<String>,
    onTagsChange: (List<String>) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("태그", style = MaterialTheme.typography.titleSmall)
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "태그 추가")
            }
        }
        
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            selectedTags.forEach { tag ->
                AssistChip(
                    onClick = { 
                        onTagsChange(selectedTags - tag)
                    },
                    label = { Text(tag) },
                    trailingIcon = {
                        Icon(Icons.Default.Close, "제거", modifier = Modifier.size(16.dp))
                    }
                )
            }
        }
    }
}
```

## 5. Calendar View Template

```kotlin
@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("오늘", "주간", "월간")
    
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        when (selectedTab) {
            0 -> TodayView(viewModel)
            1 -> WeekView(viewModel)
            2 -> MonthView(viewModel)
        }
    }
}

@Composable
fun TodayView(viewModel: CalendarViewModel) {
    val todos by viewModel.todayTodos.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = DateFormatter.formatToday(),
                style = MaterialTheme.typography.headlineMedium
            )
        }
        
        items(todos) { todo ->
            EnhancedTodoItem(
                todo = todo,
                onToggle = { viewModel.toggleTodo(todo.id) },
                onClick = { viewModel.navigateToDetail(todo.id) }
            )
        }
    }
}
```

## 6. Statistics Screen Template

```kotlin
@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel) {
    val stats by viewModel.statistics.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Productivity score
        item {
            ProductivityCard(score = stats.weeklyProductivity)
        }
        
        // Completion chart
        item {
            CompletionChart(data = stats.weeklyCompletion)
        }
        
        // Focus time chart
        item {
            FocusTimeChart(data = stats.weeklyFocusMinutes)
        }
        
        // Streak
        item {
            StreakCard(
                currentStreak = stats.currentStreak,
                longestStreak = stats.longestStreak
            )
        }
        
        // Top tags
        item {
            TopTagsCard(tags = stats.topTags)
        }
    }
}
```

## 7. Testing Templates

```kotlin
// ViewModel Test
class PomodoroViewModelTest {
    @Test
    fun `startWork should start 25 minute timer`() = runTest {
        val viewModel = PomodoroViewModel(repository)
        
        viewModel.startWork()
        advanceTimeBy(1000)
        
        val state = viewModel.timerState.value
        assertTrue(state is Running)
        assertEquals(24 * 60 + 59, (state as Running).remainingSeconds)
    }
}

// UI Test
class TodoDetailScreenTest {
    @Test
    fun `should save todo with all fields`() {
        composeTestRule.setContent {
            TodoDetailScreen(...)
        }
        
        composeTestRule.onNodeWithText("제목").performTextInput("Test Todo")
        composeTestRule.onNodeWithText("저장").performClick()
        
        // Verify save was called
    }
}
```

## Next Steps

1. Copy these templates into your project
2. Implement each screen/component one at a time
3. Test as you go
4. Integrate with existing code
5. Add polish and animations

For any questions, refer to existing code patterns in the project.
