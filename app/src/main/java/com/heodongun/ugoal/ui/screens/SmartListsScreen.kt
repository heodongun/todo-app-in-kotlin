package com.heodongun.ugoal.ui.screens

import androidx.compose.animation.*
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
import com.heodongun.ugoal.data.models.SmartList
import com.heodongun.ugoal.ui.theme.*
import com.heodongun.ugoal.viewmodel.SmartListsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartListsScreen(
    viewModel: SmartListsViewModel,
    onListClick: (SmartList) -> Unit,
    onCreateList: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val systemLists by viewModel.systemLists.collectAsState()
    val customLists by viewModel.customLists.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("스마트 리스트") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = onCreateList) {
                        Icon(Icons.Default.Add, "리스트 만들기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundWhite)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // System lists (non-editable)
            item {
                Text(
                    text = "시스템 리스트",
                    style = MaterialTheme.typography.titleSmall,
                    color = TossGray400
                )
            }

            items(systemLists, key = { it.id }) { list ->
                SmartListCard(
                    smartList = list,
                    todoCount = viewModel.getTodoCount(list),
                    onClick = { onListClick(list) }
                )
            }

            // Custom smart lists
            if (customLists.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "내 리스트",
                            style = MaterialTheme.typography.titleSmall,
                            color = TossGray400
                        )
                        Text(
                            text = "${customLists.size}개",
                            style = MaterialTheme.typography.bodySmall,
                            color = TossGray400
                        )
                    }
                }

                items(customLists, key = { it.id }) { list ->
                    SmartListCard(
                        smartList = list,
                        todoCount = viewModel.getTodoCount(list),
                        onClick = { onListClick(list) },
                        onEdit = { /* Show edit dialog */ },
                        onDelete = { viewModel.deleteSmartList(list.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
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
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = TossGray50
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with color
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = parseColor(smartList.color).copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = smartList.icon,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name and count
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = smartList.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TossGray900
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (todoCount > 0) "${todoCount}개 항목" else "항목 없음",
                    style = MaterialTheme.typography.bodySmall,
                    color = TossGray400
                )
            }

            // Count badge
            if (todoCount > 0) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = parseColor(smartList.color).copy(alpha = 0.1f)
                ) {
                    Text(
                        text = todoCount.toString(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = parseColor(smartList.color)
                    )
                }
            }

            // Actions (only for custom lists)
            if (!smartList.isSystem && (onEdit != null || onDelete != null)) {
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            "더보기",
                            tint = TossGray400
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (onEdit != null) {
                            DropdownMenuItem(
                                text = { Text("편집") },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, "편집")
                                }
                            )
                        }
                        if (onDelete != null) {
                            DropdownMenuItem(
                                text = { Text("삭제", color = ErrorRed) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, "삭제", tint = ErrorRed)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartListDetailScreen(
    smartList: SmartList,
    viewModel: SmartListsViewModel,
    onTodoClick: (String) -> Unit,
    onAddTodo: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val todos = remember(smartList) {
        viewModel.getFilteredTodos(smartList)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(smartList.icon)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(smartList.name)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "뒤로")
                    }
                },
                actions = {
                    if (!smartList.isSystem) {
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
                containerColor = parseColor(smartList.color)
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
                        text = smartList.icon,
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
                        onToggleComplete = { /* Handle */ },
                        onDelete = { /* Handle */ }
                    )
                }
            }
        }
    }
}

private fun parseColor(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        TossBlue
    }
}
