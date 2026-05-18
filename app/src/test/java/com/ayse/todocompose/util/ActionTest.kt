package com.ayse.todocompose.util

import org.junit.Assert.*
import org.junit.Test

class ActionTest {

    @Test
    fun `enum has exactly 6 values`() {
        assertEquals(6, Action.entries.size)
    }

    @Test
    fun `ordinal order is correct`() {
        assertEquals(0, Action.ADD.ordinal)
        assertEquals(1, Action.UPDATE.ordinal)
        assertEquals(2, Action.DELETE.ordinal)
        assertEquals(3, Action.DELETE_ALL.ordinal)
        assertEquals(4, Action.UNDO.ordinal)
        assertEquals(5, Action.NO_ACTION.ordinal)
    }

    @Test
    fun `toAction returns ADD for ADD string`() {
        assertEquals(Action.ADD, "ADD".toAction())
    }

    @Test
    fun `toAction returns UPDATE for UPDATE string`() {
        assertEquals(Action.UPDATE, "UPDATE".toAction())
    }

    @Test
    fun `toAction returns DELETE for DELETE string`() {
        assertEquals(Action.DELETE, "DELETE".toAction())
    }

    @Test
    fun `toAction returns DELETE_ALL for DELETE_ALL string`() {
        assertEquals(Action.DELETE_ALL, "DELETE_ALL".toAction())
    }

    @Test
    fun `toAction returns UNDO for UNDO string`() {
        assertEquals(Action.UNDO, "UNDO".toAction())
    }

    @Test
    fun `toAction returns NO_ACTION for NO_ACTION string`() {
        assertEquals(Action.NO_ACTION, "NO_ACTION".toAction())
    }

    @Test
    fun `toAction returns NO_ACTION for null input`() {
        val input: String? = null
        assertEquals(Action.NO_ACTION, input.toAction())
    }

    @Test
    fun `toAction returns NO_ACTION for empty string`() {
        assertEquals(Action.NO_ACTION, "".toAction())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `toAction throws exception for invalid string`() {
        "INVALID_ACTION".toAction()
    }
}
