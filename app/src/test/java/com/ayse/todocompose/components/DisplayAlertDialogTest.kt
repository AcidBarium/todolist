package com.ayse.todocompose.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Config.TARGET_SDK])
@LooperMode(LooperMode.Mode.PAUSED)
class DisplayAlertDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `dialog shows title and message when openDialog is true`() {
        composeTestRule.setContent {
            DisplayAlertDialog(
                title = "Delete Task",
                message = "Are you sure?",
                openDialog = true,
                closeDialog = {},
                onYesClicked = {}
            )
        }
        composeTestRule.onNodeWithText("Delete Task").assertExists()
        composeTestRule.onNodeWithText("Are you sure?").assertExists()
    }

    @Test
    fun `dialog shows Yes and No buttons`() {
        composeTestRule.setContent {
            DisplayAlertDialog(
                title = "Confirm",
                message = "Proceed?",
                openDialog = true,
                closeDialog = {},
                onYesClicked = {}
            )
        }
        composeTestRule.onNodeWithText("Yes").assertExists()
        composeTestRule.onNodeWithText("No").assertExists()
    }

    @Test
    fun `dialog does not render when openDialog is false`() {
        composeTestRule.setContent {
            DisplayAlertDialog(
                title = "Hidden",
                message = "Not visible",
                openDialog = false,
                closeDialog = {},
                onYesClicked = {}
            )
        }
        composeTestRule.onNodeWithText("Hidden").assertDoesNotExist()
    }
}
