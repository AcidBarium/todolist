package com.ayse.todocompose.ui.screens.list

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ayse.todocompose.R
import com.ayse.todocompose.data.ToDoTask
import com.ayse.todocompose.data.models.Priority
import com.ayse.todocompose.ui.theme.ToDoComposeTheme
import com.ayse.todocompose.util.RequestState
import com.ayse.todocompose.util.SearchAppBarState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ListContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun listContent_showsEmptyContent_whenNoTasks() {
        composeTestRule.setContent {
            ToDoComposeTheme {
                ListContent(
                    allTasks = RequestState.Success(emptyList()),
                    searchedTasks = RequestState.Success(emptyList()),
                    searchAppBarState = SearchAppBarState.CLOSED,
                    lowPriorityTasks = emptyList(),
                    highPriorityTasks = emptyList(),
                    sortState = RequestState.Success(Priority.NONE),
                    navigateToTaskScreen = {},
                    onSwipeToDelete = { _, _ -> }
                )
            }
        }

        val emptyText = composeTestRule.activity.getString(R.string.empty_content)
        composeTestRule.onNodeWithText(emptyText).assertIsDisplayed()
    }

    @Test
    fun listContent_showsTaskItem_whenTasksProvided() {
        val tasks = listOf(
            ToDoTask(id = 1, title = "Title", description = "Desc", priority = Priority.HIGH)
        )

        composeTestRule.setContent {
            ToDoComposeTheme {
                ListContent(
                    allTasks = RequestState.Success(tasks),
                    searchedTasks = RequestState.Success(emptyList()),
                    searchAppBarState = SearchAppBarState.CLOSED,
                    lowPriorityTasks = emptyList(),
                    highPriorityTasks = emptyList(),
                    sortState = RequestState.Success(Priority.NONE),
                    navigateToTaskScreen = {},
                    onSwipeToDelete = { _, _ -> }
                )
            }
        }

        composeTestRule.onNodeWithText("Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Desc").assertIsDisplayed()
    }

    @Test
    fun searchAppBar_closeClearsText_withoutCallingClose() {
        val textState = mutableStateOf("Query")
        var closeClicked = false

        composeTestRule.setContent {
            ToDoComposeTheme {
                SearchAppBar(
                    text = textState.value,
                    onTextChange = { textState.value = it },
                    onCloseClicked = { closeClicked = true },
                    onSearchClicked = {}
                )
            }
        }

        val closeIcon = composeTestRule.activity.getString(R.string.close_icon)
        composeTestRule.onNodeWithContentDescription(closeIcon).performClick()

        composeTestRule.runOnIdle {
            assertEquals("", textState.value)
            assertFalse(closeClicked)
        }
    }

    @Test
    fun searchAppBar_triggersSearchOnImeAction() {
        val textState = mutableStateOf("")
        var query = ""

        composeTestRule.setContent {
            ToDoComposeTheme {
                SearchAppBar(
                    text = textState.value,
                    onTextChange = { textState.value = it },
                    onCloseClicked = {},
                    onSearchClicked = { query = it }
                )
            }
        }

        val node = composeTestRule.onNodeWithTag("search_text_field")
        node.performTextClearance()
        node.performTextInput("Milk")
        node.performImeAction()

        composeTestRule.runOnIdle {
            assertEquals("Milk", query)
        }
    }

    @Test
    fun listFab_callsCallbackWithMinusOne() {
        var receivedId = 0

        composeTestRule.setContent {
            ToDoComposeTheme {
                ListFab(onFabClicked = { receivedId = it })
            }
        }

        composeTestRule.onNodeWithTag("list_fab").performClick()

        composeTestRule.runOnIdle {
            assertEquals(-1, receivedId)
        }
    }
}
