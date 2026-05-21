package com.ayse.todocompose.ui.screens.list

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
class EmptyContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `empty content displays sad face message`() {
        composeTestRule.setContent { EmptyContent() }
        composeTestRule.onNodeWithText("No Task Found").assertExists()
    }
}
