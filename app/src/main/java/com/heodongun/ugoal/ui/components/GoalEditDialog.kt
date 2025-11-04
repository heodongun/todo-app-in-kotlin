package com.heodongun.ugoal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.heodongun.ugoal.data.models.BigGoal
import com.heodongun.ugoal.ui.theme.*

// Expanded color palette
val goalColorOptions = listOf(
    "#3182F6" to "파랑",
    "#7C4DFF" to "보라",
    "#FF4081" to "핑크",
    "#FF6E40" to "오렌지",
    "#00C853" to "초록",
    "#00BFA5" to "청록",
    "#FFD600" to "노랑",
    "#FF3D00" to "빨강",
    "#1E88E5" to "하늘",
    "#8E24AA" to "자주",
    "#43A047" to "연두",
    "#FB8C00" to "금색",
    "#6D4C41" to "갈색",
    "#546E7A" to "회색"
)

// Expanded icon options
val goalIconOptions = listOf(
    "🎯", "⭐", "🚀", "💪", "📚",
    "🎓", "💼", "🏆", "📈", "🎨",
    "🎵", "⚽", "🏃", "💰", "🏠",
    "✈️", "📱", "💻", "🌟", "🔥",
    "💡", "🎬", "📷", "🍎", "☕",
    "🌈", "🎪", "🎁", "🌺", "🎸"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalEditDialog(
    goal: BigGoal,
    onDismiss: () -> Unit,
    onSave: (BigGoal) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(goal.title) }
    var selectedColor by remember { mutableStateOf(goal.color) }
    var selectedIcon by remember { mutableStateOf(goal.icon) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "목표 수정",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TossGray900
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (onDelete != null) {
                            IconButton(onClick = { showDeleteConfirmation = true }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = ErrorRed
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = TossGray600
                            )
                        }
                    }
                }

                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("목표 제목") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(android.graphics.Color.parseColor(selectedColor)),
                        focusedLabelColor = Color(android.graphics.Color.parseColor(selectedColor))
                    )
                )

                // Icon selector
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "아이콘 선택",
                        style = MaterialTheme.typography.titleMedium,
                        color = TossGray900
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(6),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(160.dp)
                    ) {
                        items(goalIconOptions, key = { it }) { icon ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (icon == selectedIcon)
                                            Color(android.graphics.Color.parseColor(selectedColor)).copy(alpha = 0.1f)
                                        else TossGray100
                                    )
                                    .border(
                                        width = if (icon == selectedIcon) 2.dp else 0.dp,
                                        color = if (icon == selectedIcon)
                                            Color(android.graphics.Color.parseColor(selectedColor))
                                        else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedIcon = icon },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = icon,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }
                    }
                }

                // Color selector
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "색상 선택",
                        style = MaterialTheme.typography.titleMedium,
                        color = TossGray900
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(80.dp)
                    ) {
                        items(goalColorOptions, key = { it.first }) { (hex, _) ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .border(
                                        width = if (hex == selectedColor) 3.dp else 0.dp,
                                        color = if (hex == selectedColor) TossGray900 else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColor = hex }
                            )
                        }
                    }
                }

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
                                    goal.copy(
                                        title = title.trim(),
                                        color = selectedColor,
                                        icon = selectedIcon
                                    )
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(android.graphics.Color.parseColor(selectedColor))
                        ),
                        enabled = title.isNotBlank()
                    ) {
                        Text("저장")
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("목표 삭제", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "이 목표와 관련된 모든 할 일이 삭제됩니다. 정말 삭제하시겠습니까?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TossGray600
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete?.invoke()
                        showDeleteConfirmation = false
                        onDismiss()
                    }
                ) {
                    Text("삭제", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("취소", color = TossGray600)
                }
            }
        )
    }
}
