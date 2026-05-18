package com.ayse.todocompose.data.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PriorityTest {

    @Test
    fun `enum has exactly four values`() {
        assertEquals(4, Priority.values().size)
    }

    @Test
    fun `values are in correct order`() {
        val expected = listOf(Priority.HIGH, Priority.MEDIUM, Priority.LOW, Priority.NONE)
        assertEquals(expected, Priority.values().toList())
    }

    @Test
    fun `names match constants`() {
        assertEquals("HIGH", Priority.HIGH.name)
        assertEquals("MEDIUM", Priority.MEDIUM.name)
        assertEquals("LOW", Priority.LOW.name)
        assertEquals("NONE", Priority.NONE.name)
    }

    @Test
    fun `ordinal values are sequential`() {
        assertEquals(0, Priority.HIGH.ordinal)
        assertEquals(1, Priority.MEDIUM.ordinal)
        assertEquals(2, Priority.LOW.ordinal)
        assertEquals(3, Priority.NONE.ordinal)
    }
}
