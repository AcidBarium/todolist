package com.ayse.todocompose.data.models

import org.junit.Assert.*
import org.junit.Test

class PriorityTest {

    @Test
    fun `enum has exactly 4 values`() {
        assertEquals(4, Priority.entries.size)
    }

    @Test
    fun `ordinal order is HIGH_MEDIUM_LOW_NONE`() {
        assertEquals(0, Priority.HIGH.ordinal)
        assertEquals(1, Priority.MEDIUM.ordinal)
        assertEquals(2, Priority.LOW.ordinal)
        assertEquals(3, Priority.NONE.ordinal)
    }

    @Test
    fun `names are correct`() {
        assertEquals("HIGH", Priority.HIGH.name)
        assertEquals("MEDIUM", Priority.MEDIUM.name)
        assertEquals("LOW", Priority.LOW.name)
        assertEquals("NONE", Priority.NONE.name)
    }

    @Test
    fun `valueOf returns correct enum`() {
        assertEquals(Priority.HIGH, Priority.valueOf("HIGH"))
        assertEquals(Priority.MEDIUM, Priority.valueOf("MEDIUM"))
        assertEquals(Priority.LOW, Priority.valueOf("LOW"))
        assertEquals(Priority.NONE, Priority.valueOf("NONE"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `valueOf with invalid name throws exception`() {
        Priority.valueOf("INVALID")
    }

    @Test
    fun `each priority has a non-default color`() {
        assertNotNull(Priority.HIGH.color)
        assertNotNull(Priority.MEDIUM.color)
        assertNotNull(Priority.LOW.color)
        assertNotNull(Priority.NONE.color)
    }

    @Test
    fun `all priority colors are distinct`() {
        val colors = Priority.entries.map { it.color }
        assertEquals(colors.toSet().size, colors.size)
    }
}
