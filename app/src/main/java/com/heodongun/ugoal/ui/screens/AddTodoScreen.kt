package com.heodongun.ugoal.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.heodongun.ugoal.ui.components.DatePickerDialog
import com.heodongun.ugoal.ui.theme.*
import com.heodongun.ugoal.utils.HapticFeedback
import com.heodongun.ugoal.viewmodel.GoalsViewModel
import com.heodongun.ugoal.viewmodel.HomeViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoScreen(
    homeViewModel: HomeViewModel,
    goalsViewModel: GoalsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val goalsUiState by goalsViewModel.uiState.collectAsState()

    var titleText by remember { mutableStateOf("") }
    var selectedGoalId by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf<String?>(null) }
    var showGoalPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val displayDateFormatter = DateTimeFormatter.ofPattern("M월 d일")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("새로운 할 일", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = {
                        HapticFeedback.performClick(context)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundWhite
                )
            )
        },
        containerColor = BackgroundWhite
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Title input
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "할 일",
                    style = MaterialTheme.typography.titleMedium,
                    color = TossGray900
                )
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    placeholder = { Text("무엇을 할까요?") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TossBlue,
                        unfocusedBorderColor = TossGray200
                    )
                )
            }

            // Date selection
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "날짜",
                    style = MaterialTheme.typography.titleMedium,
                    color = TossGray900
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, TossGray200)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = selectedDate?.let {
                                val date = LocalDate.parse(it, dateFormatter)
                                val today = LocalDate.now()
                                val tomorrow = today.plusDays(1)
                                when (date) {
                                    today -> "오늘"
                                    tomorrow -> "내일"
                                    else -> date.format(displayDateFormatter)
                                }
                            } ?: "오늘",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedDate != null) TossGray900 else TossGray500
                        )
                    }
                }
            }

            // Link to goal
            if (goalsUiState.bigGoals.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "목표와 연결",
                        style = MaterialTheme.typography.titleMedium,
                        color = TossGray900
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showGoalPicker = true },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, TossGray200)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = selectedGoalId?.let { id ->
                                    goalsUiState.bigGoals.find { it.id == id }?.title ?: "연결 안 함"
                                } ?: "연결 안 함",
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (selectedGoalId != null) TossGray900 else TossGray500
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            Button(
                onClick = {
                    if (titleText.isNotBlank()) {
                        HapticFeedback.performClick(context)
                        val finalDate = selectedDate ?: LocalDate.now().format(dateFormatter)
                        homeViewModel.addTodo(
                            title = titleText.trim(),
                            goalId = selectedGoalId,
                            date = finalDate
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = titleText.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TossBlue,
                    disabledContainerColor = TossGray200
                )
            ) {
                Text(
                    text = "저장",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }
    }

    // Goal picker dialog
    if (showGoalPicker) {
        AlertDialog(
            onDismissRequest = { showGoalPicker = false },
            title = { Text("목표 선택", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column {
                    // No goal option
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedGoalId = null
                                showGoalPicker = false
                            },
                        color = if (selectedGoalId == null) TossBlue.copy(alpha = 0.1f) else Color.Transparent
                    ) {
                        Text(
                            text = "연결 안 함",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TossGray700,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    Divider(color = TossGray200)
                    
                    // Goals list
                    goalsUiState.bigGoals.forEach { goal ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedGoalId = goal.id
                                    showGoalPicker = false
                                },
                            color = if (selectedGoalId == goal.id) TossBlue.copy(alpha = 0.1f) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = goal.icon, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = goal.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TossGray900
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { date ->
                selectedDate = date
            },
            initialDate = selectedDate?.let { LocalDate.parse(it, dateFormatter) } ?: LocalDate.now()
        )
    }
}
