package com.ayse.todocompose.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ActionTest {

    @Test
    fun `enum has six values`() {
        assertEquals(6, Action.values().size)
    }

    @Test
    fun `toAction returns NO_ACTION for null`() {
        val input: String? = null
        assertEquals(Action.NO_ACTION, input.toAction())
    }

    @Test
    fun `toAction returns NO_ACTION for empty string`() {
        assertEquals(Action.NO_ACTION, "".toAction())
    }

    @Test
    fun `toAction parses valid action names`() {
        assertEquals(Action.ADD, "ADD".toAction())
        assertEquals(Action.UPDATE, "UPDATE".toAction())
        assertEquals(Action.DELETE, "DELETE".toAction())
        assertEquals(Action.DELETE_ALL, "DELETE_ALL".toAction())
        assertEquals(Action.UNDO, "UNDO".toAction())
        assertEquals(Action.NO_ACTION, "NO_ACTION".toAction())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toAction throws for invalid name`() {
        "INVALID".toAction()
    }
}
