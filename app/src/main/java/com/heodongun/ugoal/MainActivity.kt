package com.heodongun.ugoal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.heodongun.ugoal.ui.navigation.UgoalNavHost
import com.heodongun.ugoal.ui.theme.UgoalTheme
import com.heodongun.ugoal.viewmodel.CalendarViewModel
import com.heodongun.ugoal.viewmodel.GoalsViewModel
import com.heodongun.ugoal.viewmodel.HomeViewModel
import com.heodongun.ugoal.viewmodel.SmartListsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as UgoalApplication).repository

        setContent {
            UgoalTheme {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return HomeViewModel(repository) as T
                        }
                    }
                )

                val goalsViewModel: GoalsViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return GoalsViewModel(repository) as T
                        }
                    }
                )

                val calendarViewModel: CalendarViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return CalendarViewModel(repository) as T
                        }
                    }
                )

                val smartListsViewModel: SmartListsViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return SmartListsViewModel(repository) as T
                        }
                    }
                )

                UgoalNavHost(
                    homeViewModel = homeViewModel,
                    goalsViewModel = goalsViewModel,
                    calendarViewModel = calendarViewModel,
                    smartListsViewModel = smartListsViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
