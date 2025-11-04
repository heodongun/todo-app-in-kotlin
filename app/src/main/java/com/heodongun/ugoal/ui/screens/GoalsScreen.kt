package com.heodongun.ugoal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.heodongun.ugoal.data.models.BigGoal
import com.heodongun.ugoal.ui.components.GoalCard
import com.heodongun.ugoal.ui.components.GoalEditDialog
import com.heodongun.ugoal.ui.theme.*
import com.heodongun.ugoal.utils.HapticFeedback
import com.heodongun.ugoal.viewmodel.GoalsViewModel

@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel,
    onAddGoalClick: () -> Unit,
    onGoalClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var goalToEdit by remember { mutableStateOf<BigGoal?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = BackgroundWhite,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    HapticFeedback.performClick(context)
                    onAddGoalClick()
                },
                containerColor = TossBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "목표 추가")
                Spacer(modifier = Modifier.width(8.dp))
                Text("목표 추가", style = MaterialTheme.typography.labelLarge)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            // Header
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "큰 목표",
                        style = MaterialTheme.typography.headlineLarge,
                        color = TossGray900
                    )
                    Text(
                        text = "달성하고 싶은 목표를 세워보세요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TossGray500
                    )
                }
            }

            if (uiState.bigGoals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "아직 목표가 없어요",
                                style = MaterialTheme.typography.titleMedium,
                                color = TossGray500
                            )
                            Text(
                                text = "큰 목표를 세워보세요",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TossGray400
                            )
                        }
                    }
                }
            } else {
                items(
                    items = uiState.bigGoals,
                    key = { it.id }
                ) { goal ->
                    GoalCard(
                        goal = goal,
                        onClick = {
                            HapticFeedback.performClick(context)
                            onGoalClick(goal.id)
                        },
                        onEdit = {
                            goalToEdit = goal
                        }
                    )
                }
            }
        }
    }

    // Goal Edit Dialog
    goalToEdit?.let { goal ->
        GoalEditDialog(
            goal = goal,
            onDismiss = { goalToEdit = null },
            onSave = { updatedGoal ->
                viewModel.updateGoal(updatedGoal)
            },
            onDelete = {
                viewModel.deleteGoal(goal.id)
            }
        )
    }
}
