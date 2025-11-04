package com.heodongun.ugoal.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.heodongun.ugoal.data.models.EnhancedTodo
import com.heodongun.ugoal.data.models.Priority
import com.heodongun.ugoal.ui.theme.*
import com.heodongun.ugoal.utils.HapticFeedback
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.Flag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoItem(
    todo: EnhancedTodo,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {},
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var offsetX by remember { mutableStateOf(0f) }
    var isCompleted by remember { mutableStateOf(todo.isCompleted) }
    
    val scale by animateFloatAsState(
        targetValue = if (isCompleted) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX < -200f) {
                            onDelete()
                        } else {
                            offsetX = 0f
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX = (offsetX + dragAmount).coerceIn(-300f, 0f)
                    }
                )
            }
    ) {
        // Delete background
        if (offsetX < 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(ErrorRed, RoundedCornerShape(16.dp))
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "삭제",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale),
            shape = RoundedCornerShape(16.dp),
            color = if (isCompleted) TossGray50 else Color.White,
            tonalElevation = if (isCompleted) 0.dp else 1.dp,
            shadowElevation = if (isCompleted) 0.dp else 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Checkbox icon
                Icon(
                    imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.Circle,
                    contentDescription = if (isCompleted) "Completed" else "Not completed",
                    tint = if (isCompleted) TossBlue else TossGray300,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            HapticFeedback.performClick(context)
                            isCompleted = !isCompleted
                            onToggleComplete()
                            if (isCompleted) {
                                HapticFeedback.performSuccess(context)
                            }
                        }
                )

                // Todo text
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isCompleted) TossGray500 else TossGray900,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            HapticFeedback.performClick(context)
                            onClick()
                        }
                )

                // Priority Indicator
                if (todo.priority != Priority.NONE) {
                    val priorityColor = when (todo.priority) {
                        Priority.HIGH -> ErrorRed
                        Priority.MEDIUM -> Orange500
                        Priority.LOW -> TossBlue
                        else -> Color.Transparent
                    }
                    Icon(
                        imageVector = Icons.Filled.Flag,
                        contentDescription = "Priority ${todo.priority.name}",
                        tint = priorityColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Edit button
                IconButton(
                    onClick = {
                        HapticFeedback.performClick(context)
                        onEdit()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = TossGray400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
