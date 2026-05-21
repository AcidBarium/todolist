package com.ayse.todocompose.ui.screens.task

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ayse.todocompose.data.models.Priority
import com.ayse.todocompose.ui.theme.ToDoComposeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun taskContent_updatesTitleAndDescription() {
        val titleState = mutableStateOf("")
        val descriptionState = mutableStateOf("")

        composeTestRule.setContent {
            ToDoComposeTheme {
                TaskContent(
                    title = titleState.value,
                    onTitleChange = { titleState.value = it },
                    description = descriptionState.value,
                    onDescriptionChange = { descriptionState.value = it },
                    priority = Priority.LOW,
                    onPrioritySelected = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("task_title_input").performTextInput("Write tests")
        composeTestRule.onNodeWithTag("task_description_input").performTextInput("Cover list and task screens")

        composeTestRule.runOnIdle {
            assertEquals("Write tests", titleState.value)
            assertEquals("Cover list and task screens", descriptionState.value)
        }
    }

    @Test
    fun taskContent_selectsPriorityFromDropdown() {
        val priorityState = mutableStateOf(Priority.LOW)

        composeTestRule.setContent {
            ToDoComposeTheme {
                TaskContent(
                    title = "",
                    onTitleChange = {},
                    description = "",
                    onDescriptionChange = {},
                    priority = priorityState.value,
                    onPrioritySelected = { priorityState.value = it }
                )
            }
        }

        composeTestRule.onNodeWithTag("priority_dropdown").performClick()
        composeTestRule.onNodeWithText("HIGH").performClick()

        composeTestRule.runOnIdle {
            assertEquals(Priority.HIGH, priorityState.value)
        }
    }

    @Test
    fun taskContent_showsPriorityName() {
        composeTestRule.setContent {
            ToDoComposeTheme {
                TaskContent(
                    title = "",
                    onTitleChange = {},
                    description = "",
                    onDescriptionChange = {},
                    priority = Priority.MEDIUM,
                    onPrioritySelected = {}
                )
            }
        }

        composeTestRule.onNodeWithText("MEDIUM").assertIsDisplayed()
    }
}
