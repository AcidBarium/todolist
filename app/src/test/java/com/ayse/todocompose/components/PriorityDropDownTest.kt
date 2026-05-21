package com.ayse.todocompose.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.ayse.todocompose.data.models.Priority
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Config.TARGET_SDK])
@LooperMode(LooperMode.Mode.PAUSED)
class PriorityDropDownTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `dropdown shows current priority HIGH`() {
        composeTestRule.setContent {
            PriorityDropDown(priority = Priority.HIGH, onPrioritySelected = {})
        }
        composeTestRule.onNodeWithText("HIGH").assertExists()
    }

    @Test
    fun `dropdown shows current priority MEDIUM`() {
        composeTestRule.setContent {
            PriorityDropDown(priority = Priority.MEDIUM, onPrioritySelected = {})
        }
        composeTestRule.onNodeWithText("MEDIUM").assertExists()
    }

    @Test
    fun `dropdown shows current priority LOW`() {
        composeTestRule.setContent {
            PriorityDropDown(priority = Priority.LOW, onPrioritySelected = {})
        }
        composeTestRule.onNodeWithText("LOW").assertExists()
    }
}
