package com.heodongun.ugoal.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.heodongun.ugoal.data.remote.MongoDbClient
import com.heodongun.ugoal.data.repository.UgoalRepository
import com.heodongun.ugoal.ui.screens.HomeScreen
import com.heodongun.ugoal.ui.theme.UgoalTheme
import com.heodongun.ugoal.viewmodel.HomeViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysDateHeader() {
        val repository = UgoalRepository(MongoDbClient())
        val viewModel = HomeViewModel(repository)

        composeTestRule.setContent {
            UgoalTheme {
                HomeScreen(
                    viewModel = viewModel,
                    onAddTodoClick = {}
                )
            }
        }

        // Check if date is displayed
        composeTestRule.onNodeWithText("오늘을 설계해보세요").assertExists()
    }

    @Test
    fun homeScreen_showsAddTodoButton() {
        val repository = UgoalRepository(MongoDbClient())
        val viewModel = HomeViewModel(repository)

        composeTestRule.setContent {
            UgoalTheme {
                HomeScreen(
                    viewModel = viewModel,
                    onAddTodoClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("할 일 추가").assertExists()
    }

    @Test
    fun homeScreen_addTodoButton_isClickable() {
        val repository = UgoalRepository(MongoDbClient())
        val viewModel = HomeViewModel(repository)
        var clicked = false

        composeTestRule.setContent {
            UgoalTheme {
                HomeScreen(
                    viewModel = viewModel,
                    onAddTodoClick = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("할 일 추가").performClick()
        assert(clicked)
    }
}
