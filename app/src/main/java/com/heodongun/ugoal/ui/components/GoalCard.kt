package com.heodongun.ugoal.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.heodongun.ugoal.data.models.BigGoal
import com.heodongun.ugoal.ui.theme.*

@Composable
fun GoalCard(
    goal: BigGoal,
    onClick: () -> Unit,
    onEdit: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = goal.progress / 100f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        tonalElevation = 1.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with icon and title
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon container
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(android.graphics.Color.parseColor(goal.color)).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = goal.icon,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                // Title
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = TossGray900,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${goal.todos.size}개의 할 일",
                        style = MaterialTheme.typography.bodySmall,
                        color = TossGray500
                    )
                }

                // Progress percentage
                Text(
                    text = "${goal.progress}%",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(android.graphics.Color.parseColor(goal.color))
                )

                // Edit button
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = TossGray400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(TossGray100)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(android.graphics.Color.parseColor(goal.color)))
                )
            }

            // Completed todos summary
            if (goal.todos.isNotEmpty()) {
                Text(
                    text = "${goal.todos.count { it.isCompleted }}/${goal.todos.size} 완료",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TossGray600
                )
            }
        }
    }
}
