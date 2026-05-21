package com.ayse.todocompose.ui.screens.task

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ayse.todocompose.R
import com.ayse.todocompose.data.ToDoTask
import com.ayse.todocompose.data.models.Priority
import com.ayse.todocompose.ui.theme.ToDoComposeTheme
import com.ayse.todocompose.util.Action
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskAppBarTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun newTaskAppBar_showsBackAndAddActions() {
        composeTestRule.setContent {
            ToDoComposeTheme {
                NewTaskAppBar(navigateToListScreen = {})
            }
        }

        val back = composeTestRule.activity.getString(R.string.back_arrow)
        val add = composeTestRule.activity.getString(R.string.add_task)

        composeTestRule.onNodeWithContentDescription(back).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(add).assertIsDisplayed()
    }

    @Test
    fun existingTaskAppBar_showsDeleteAndUpdateActions() {
        val task = ToDoTask(
            id = 1,
            title = "Task",
            description = "Desc",
            priority = Priority.LOW
        )

        composeTestRule.setContent {
            ToDoComposeTheme {
                ExistingTaskAppBar(
                    selectedTask = task,
                    navigateToListScreen = {}
                )
            }
        }

        val deleteIcon = composeTestRule.activity.getString(R.string.delete_icon)
        val updateIcon = composeTestRule.activity.getString(R.string.update_icon)

        composeTestRule.onNodeWithContentDescription(deleteIcon).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(updateIcon).assertIsDisplayed()
    }

    @Test
    fun existingTaskAppBar_deleteShowsConfirmationDialog() {
        val task = ToDoTask(
            id = 1,
            title = "Task",
            description = "Desc",
            priority = Priority.LOW
        )

        composeTestRule.setContent {
            ToDoComposeTheme {
                ExistingTaskAppBar(
                    selectedTask = task,
                    navigateToListScreen = {}
                )
            }
        }

        val deleteIcon = composeTestRule.activity.getString(R.string.delete_icon)
        composeTestRule.onNodeWithContentDescription(deleteIcon).performClick()

        val title = composeTestRule.activity.getString(R.string.delete_task, task.title)
        val message = composeTestRule.activity.getString(R.string.delete_task_confirmation, task.title)

        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(message).assertIsDisplayed()
    }
}
