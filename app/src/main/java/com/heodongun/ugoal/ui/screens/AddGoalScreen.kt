package com.heodongun.ugoal.ui.screens

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.heodongun.ugoal.ui.components.goalColorOptions
import com.heodongun.ugoal.ui.components.goalIconOptions
import com.heodongun.ugoal.ui.theme.*
import com.heodongun.ugoal.utils.HapticFeedback
import com.heodongun.ugoal.viewmodel.GoalsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalScreen(
    viewModel: GoalsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var titleText by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#3182F6") }
    var selectedIcon by remember { mutableStateOf("🎯") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("새로운 목표", style = MaterialTheme.typography.titleLarge) },
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
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Title input
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "목표 이름",
                    style = MaterialTheme.typography.titleMedium,
                    color = TossGray900
                )
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    placeholder = { Text("예: 앱 출시, 영어 공부") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TossBlue,
                        unfocusedBorderColor = TossGray200
                    )
                )
            }

            // Icon selection
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "아이콘 선택",
                    style = MaterialTheme.typography.titleMedium,
                    color = TossGray900
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(240.dp)
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
                                .clickable {
                                    HapticFeedback.performClick(context)
                                    selectedIcon = icon
                                },
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

            // Color selection
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "색상 선택",
                    style = MaterialTheme.typography.titleMedium,
                    color = TossGray900
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(120.dp)
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
                                .clickable {
                                    HapticFeedback.performClick(context)
                                    selectedColor = hex
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save button
            Button(
                onClick = {
                    if (titleText.isNotBlank()) {
                        HapticFeedback.performClick(context)
                        viewModel.addGoal(
                            title = titleText.trim(),
                            color = selectedColor,
                            icon = selectedIcon
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
}
