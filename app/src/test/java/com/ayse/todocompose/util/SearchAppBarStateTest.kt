package com.ayse.todocompose.util

import org.junit.Assert.*
import org.junit.Test

class SearchAppBarStateTest {

    @Test
    fun `enum has exactly 3 values`() {
        assertEquals(3, SearchAppBarState.entries.size)
    }

    @Test
    fun `ordinal order is correct`() {
        assertEquals(0, SearchAppBarState.OPENED.ordinal)
        assertEquals(1, SearchAppBarState.CLOSED.ordinal)
        assertEquals(2, SearchAppBarState.TRIGGERED.ordinal)
    }

    @Test
    fun `names are correct`() {
        assertEquals("OPENED", SearchAppBarState.OPENED.name)
        assertEquals("CLOSED", SearchAppBarState.CLOSED.name)
        assertEquals("TRIGGERED", SearchAppBarState.TRIGGERED.name)
    }

    @Test
    fun `valueOf returns correct enum`() {
        assertEquals(SearchAppBarState.OPENED, SearchAppBarState.valueOf("OPENED"))
        assertEquals(SearchAppBarState.CLOSED, SearchAppBarState.valueOf("CLOSED"))
        assertEquals(SearchAppBarState.TRIGGERED, SearchAppBarState.valueOf("TRIGGERED"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `valueOf with invalid name throws exception`() {
        SearchAppBarState.valueOf("INVALID")
    }
}
