package com.heodongun.ugoal.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.heodongun.ugoal.data.models.EnhancedTodo
import com.heodongun.ugoal.ui.components.TodoEditDialog
import com.heodongun.ugoal.ui.components.TodoItem
import com.heodongun.ugoal.ui.theme.*
import com.heodongun.ugoal.utils.DateFormatter
import com.heodongun.ugoal.utils.HapticFeedback
import com.heodongun.ugoal.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddTodoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showAddDailyGoalDialog by remember { mutableStateOf(false) }
    var todoToEdit by remember { mutableStateOf<EnhancedTodo?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundWhite,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    HapticFeedback.performClick(context)
                    onAddTodoClick()
                },
                containerColor = TossBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "할 일 추가")
                Spacer(modifier = Modifier.width(8.dp))
                Text("할 일 추가", style = MaterialTheme.typography.labelLarge)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            // Date header
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = DateFormatter.formatToday(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = TossGray900
                    )
                    Text(
                        text = "오늘을 설계해보세요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TossGray500
                    )
                }
            }

            // Daily Focus section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "오늘의 목표",
                        style = MaterialTheme.typography.titleLarge,
                        color = TossGray900
                    )

                    if (uiState.dailyGoal == null) {
                        // Add daily goal prompt
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAddDailyGoalDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            color = TossGray50,
                            tonalElevation = 0.dp
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "오늘 집중할 일을 세워보세요",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TossGray500
                                )
                            }
                        }
                    } else {
                        // Daily goal card
                        val dailyGoal = uiState.dailyGoal
                        if (dailyGoal != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        HapticFeedback.performClick(context)
                                        viewModel.toggleDailyGoalComplete()
                                    },
                                shape = RoundedCornerShape(16.dp),
                                color = if (dailyGoal.isCompleted) TossBlueLight.copy(alpha = 0.1f) else TossBlue.copy(alpha = 0.05f),
                                tonalElevation = 0.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Icon
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (dailyGoal.isCompleted) TossBlue else TossBlue.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (dailyGoal.isCompleted) "✓" else "🎯",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = if (dailyGoal.isCompleted) Color.White else TossBlue
                                        )
                                    }

                                    // Text
                                    Text(
                                        text = dailyGoal.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (dailyGoal.isCompleted) TossGray600 else TossGray900,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Todos section
            item {
                Text(
                    text = "할 일",
                    style = MaterialTheme.typography.titleLarge,
                    color = TossGray900
                )
            }

            if (uiState.todos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "할 일이 없습니다",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TossGray400
                        )
                    }
                }
            } else {
                items(
                    items = uiState.todos,
                    key = { it.id }
                ) { todo ->
                    TodoItem(
                        todo = todo,
                        onToggleComplete = {
                            HapticFeedback.performClick(context)
                            viewModel.toggleTodoComplete(todo.id)
                        },
                        onDelete = {
                            HapticFeedback.performClick(context)
                            viewModel.deleteTodo(todo.id)
                        },
                        onEdit = {
                            todoToEdit = todo
                        },
                        onClick = {
                            todoToEdit = todo
                        }
                    )
                }
            }
        }
    }

    // Add Daily Goal Dialog
    if (showAddDailyGoalDialog) {
        var dailyGoalText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDailyGoalDialog = false },
            title = { Text("오늘의 목표", style = MaterialTheme.typography.titleLarge) },
            text = {
                OutlinedTextField(
                    value = dailyGoalText,
                    onValueChange = { dailyGoalText = it },
                    placeholder = { Text("오늘 집중할 일을 적어주세요") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (dailyGoalText.isNotBlank()) {
                            viewModel.addDailyGoal(dailyGoalText.trim())
                            showAddDailyGoalDialog = false
                            dailyGoalText = ""
                        }
                    }
                ) {
                    Text("추가", color = TossBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDailyGoalDialog = false }) {
                    Text("취소", color = TossGray600)
                }
            }
        )
    }

    // Todo Edit Dialog
    todoToEdit?.let { todo ->
        TodoEditDialog(
            todo = todo,
            onDismiss = { todoToEdit = null },
            onSave = { updatedTodo ->
                viewModel.updateTodo(updatedTodo)
            }
        )
    }
}
