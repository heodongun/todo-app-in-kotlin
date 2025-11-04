package com.heodongun.ugoal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.heodongun.ugoal.data.models.SmartList
import com.heodongun.ugoal.ui.theme.BackgroundWhite
import com.heodongun.ugoal.ui.theme.TossBlue
import com.heodongun.ugoal.ui.theme.TossGray400
import com.heodongun.ugoal.ui.theme.parseColor
import com.heodongun.ugoal.viewmodel.SmartListsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartListDetailScreen(
    viewModel: SmartListsViewModel,
    onTodoClick: (String) -> Unit,
    onAddTodo: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val smartList by viewModel.selectedSmartList.collectAsState()
    val todos by viewModel.filteredTodos.collectAsState()

    smartList?.let { list ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(list.icon)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(list.name)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, "뒤로")
                        }
                    },
                    actions = {
                        if (!list.isSystem) {
                            IconButton(onClick = { /* Edit list */ }) {
                                Icon(Icons.Default.Edit, "편집")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BackgroundWhite
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddTodo,
                    containerColor = parseColor(list.color)
                ) {
                    Icon(Icons.Default.Add, "할 일 추가", tint = Color.White)
                }
            }
        ) { padding ->
            if (todos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = list.icon,
                            style = MaterialTheme.typography.displayLarge
                        )
                        Text(
                            text = "할 일이 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TossGray400
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(todos, key = { it.id }) { todo ->
                        com.heodongun.ugoal.ui.components.TodoItem(
                            todo = todo,
                            onToggleComplete = { viewModel.toggleTodoComplete(todo.id) },
                            onDelete = { viewModel.deleteTodo(todo.id) }
                        )
                    }
                }
            }
        }
    }
}


