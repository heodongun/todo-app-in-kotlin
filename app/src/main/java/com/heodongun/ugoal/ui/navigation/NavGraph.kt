package com.heodongun.ugoal.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FlagCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.heodongun.ugoal.R
import com.heodongun.ugoal.ui.screens.*
import com.heodongun.ugoal.ui.theme.*
import com.heodongun.ugoal.utils.HapticFeedback
import com.heodongun.ugoal.viewmodel.CalendarViewModel
import com.heodongun.ugoal.viewmodel.GoalsViewModel
import com.heodongun.ugoal.viewmodel.HomeViewModel
import com.heodongun.ugoal.viewmodel.SmartListsViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Goals : Screen("goals")
    object Calendar : Screen("calendar")
    object SmartLists : Screen("smart_lists")
    object SmartListDetail : Screen("smart_list/{listId}") {
        fun createRoute(listId: String) = "smart_list/$listId"
    }
    object TodoDetail : Screen("todo_detail/{todoId}") {
        fun createRoute(todoId: String) = "todo_detail/$todoId"
    }
    object AddGoal : Screen("add_goal")
    object AddTodo : Screen("add_todo")
}

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun UgoalNavHost(
    homeViewModel: HomeViewModel,
    goalsViewModel: GoalsViewModel,
    calendarViewModel: CalendarViewModel,
    smartListsViewModel: SmartListsViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home.route, Icons.Default.CalendarToday, stringResource(R.string.nav_home)),
        BottomNavItem(Screen.Calendar.route, Icons.Default.CalendarMonth, "캘린더"),
        BottomNavItem(Screen.SmartLists.route, Icons.Default.List, "리스트"),
        BottomNavItem(Screen.Goals.route, Icons.Default.FlagCircle, stringResource(R.string.nav_goals))
    )

    Scaffold(
        bottomBar = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

            if (currentRoute in bottomNavItems.map { it.route }) {
                NavigationBar(
                    containerColor = BackgroundWhite,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, style = MaterialTheme.typography.labelMedium) },
                            selected = currentRoute == item.route,
                            onClick = {
                                HapticFeedback.performClick(context)
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TossBlue,
                                selectedTextColor = TossBlue,
                                unselectedIconColor = TossGray400,
                                unselectedTextColor = TossGray400,
                                indicatorColor = TossBlue.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
            }
        },
        containerColor = BackgroundWhite
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = modifier.padding(padding),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 2 },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 2 },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onAddTodoClick = { navController.navigate(Screen.AddTodo.route) }
                )
            }

            composable(Screen.Goals.route) {
                GoalsScreen(
                    viewModel = goalsViewModel,
                    onAddGoalClick = { navController.navigate(Screen.AddGoal.route) },
                    onGoalClick = { goalId ->
                        // Navigate to goal detail if needed
                    }
                )
            }

            composable(Screen.AddGoal.route) {
                AddGoalScreen(
                    viewModel = goalsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AddTodo.route) {
                AddTodoScreen(
                    homeViewModel = homeViewModel,
                    goalsViewModel = goalsViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Calendar.route) {
                CalendarScreen(
                    viewModel = calendarViewModel,
                    onTodoClick = { todoId ->
                        navController.navigate(Screen.TodoDetail.createRoute(todoId))
                    },
                    onAddClick = { navController.navigate(Screen.AddTodo.route) }
                )
            }

            composable(Screen.SmartLists.route) {
                SmartListsScreen(
                    viewModel = smartListsViewModel,
                    onListClick = { smartList ->
                        smartListsViewModel.selectSmartList(smartList)
                        navController.navigate(Screen.SmartListDetail.createRoute(smartList.id))
                    },
                    onCreateList = { /* TODO: Navigate to create list screen */ },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.SmartListDetail.route) {
                SmartListDetailScreen(
                    viewModel = smartListsViewModel,
                    onTodoClick = { todoId ->
                        navController.navigate(Screen.TodoDetail.createRoute(todoId))
                    },
                    onAddTodo = { navController.navigate(Screen.AddTodo.route) },
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
