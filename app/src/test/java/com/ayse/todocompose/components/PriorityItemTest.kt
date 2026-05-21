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
class PriorityItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `priority item displays HIGH text`() {
        composeTestRule.setContent { PriorityItem(priority = Priority.HIGH) }
        composeTestRule.onNodeWithText("HIGH").assertExists()
    }

    @Test
    fun `priority item displays MEDIUM text`() {
        composeTestRule.setContent { PriorityItem(priority = Priority.MEDIUM) }
        composeTestRule.onNodeWithText("MEDIUM").assertExists()
    }

    @Test
    fun `priority item displays LOW text`() {
        composeTestRule.setContent { PriorityItem(priority = Priority.LOW) }
        composeTestRule.onNodeWithText("LOW").assertExists()
    }

    @Test
    fun `priority item displays NONE text`() {
        composeTestRule.setContent { PriorityItem(priority = Priority.NONE) }
        composeTestRule.onNodeWithText("NONE").assertExists()
    }
}
