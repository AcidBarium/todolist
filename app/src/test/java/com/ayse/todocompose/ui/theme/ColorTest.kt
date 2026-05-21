package com.ayse.todocompose.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Config.TARGET_SDK])
class ColorTest {

    @Test
    fun `purple constants have correct values`() {
        assertEquals(Color(0xFFBB86FC), Purple200)
        assertEquals(Color(0xFF6200EE), Purple500)
        assertEquals(Color(0xFF3700B3), Purple700)
    }

    @Test
    fun `teal constant has correct value`() {
        assertEquals(Color(0xFF03DAC5), Teal200)
    }

    @Test
    fun `gray constants have correct values`() {
        assertEquals(Color(0xFFFCFCFC), LightGray)
        assertEquals(Color(0xFF9C9C9C), MediumGray)
        assertEquals(Color(0xFF141414), DarkGray)
    }

    @Test
    fun `priority colors have correct values`() {
        assertEquals(Color(0XFF00C980), LowPriorityColor)
        assertEquals(Color(0xFFFFC114), MediumPriorityColor)
        assertEquals(Color(0xFFFF4646), HighPriorityColor)
    }

    @Test
    fun `none priority color equals medium gray`() {
        assertEquals(MediumGray, NonePriorityColor)
    }
}
