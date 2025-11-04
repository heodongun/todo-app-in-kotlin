package com.heodongun.ugoal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.heodongun.ugoal.data.models.EnhancedTodo
import com.heodongun.ugoal.data.models.Priority
import com.heodongun.ugoal.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoEditDialog(
    todo: EnhancedTodo,
    onDismiss: () -> Unit,
    onSave: (EnhancedTodo) -> Unit
) {
    var title by remember { mutableStateOf(todo.title) }
    var description by remember { mutableStateOf(todo.description) }
    var priority by remember { mutableStateOf(todo.priority) }
    var dueDate by remember { mutableStateOf(todo.dueDate ?: "") }
    var note by remember { mutableStateOf(todo.note) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "할 일 수정",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TossGray900
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = TossGray600
                        )
                    }
                }

                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("제목") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TossBlue,
                        focusedLabelColor = TossBlue
                    )
                )

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("설명") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TossBlue,
                        focusedLabelColor = TossBlue
                    )
                )

                // Priority selector
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "우선순위",
                        style = MaterialTheme.typography.titleSmall,
                        color = TossGray900
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Priority.values().forEach { p ->
                            PriorityChip(
                                priority = p,
                                isSelected = priority == p,
                                onClick = { priority = p },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Date picker
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TossBlue
                    )
                ) {
                    Text(
                        text = if (dueDate.isBlank()) "날짜 선택" else dueDate,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Note field
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("메모") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TossBlue,
                        focusedLabelColor = TossBlue
                    )
                )

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("취소")
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(
                                    todo.copy(
                                        title = title.trim(),
                                        description = description.trim(),
                                        priority = priority,
                                        dueDate = if (dueDate.isBlank()) null else dueDate,
                                        note = note.trim(),
                                        updatedAt = System.currentTimeMillis()
                                    )
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TossBlue
                        ),
                        enabled = title.isNotBlank()
                    ) {
                        Text("저장")
                    }
                }
            }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            onDateSelected = { selectedDate ->
                dueDate = selectedDate
                showDatePicker = false
            },
            initialDate = if (dueDate.isBlank()) LocalDate.now() else LocalDate.parse(dueDate, dateFormatter)
        )
    }
}

@Composable
fun PriorityChip(
    priority: Priority,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (priority) {
        Priority.HIGH -> "높음" to ErrorRed
        Priority.MEDIUM -> "중간" to WarningOrange
        Priority.LOW -> "낮음" to TossBlue
        Priority.NONE -> "없음" to TossGray400
    }

    Surface(
        modifier = modifier
            .height(40.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) color.copy(alpha = 0.1f) else TossGray100,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, color) else null
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) color else TossGray600
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (String) -> Unit,
    initialDate: LocalDate = LocalDate.now()
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochDay() * 86400000
    )

    androidx.compose.material3.DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = LocalDate.ofEpochDay(millis / 86400000)
                        onDateSelected(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                    }
                }
            ) {
                Text("확인", color = TossBlue)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("취소", color = TossGray600)
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = TossBlue,
                todayContentColor = TossBlue,
                todayDateBorderColor = TossBlue
            )
        )
    }
}
