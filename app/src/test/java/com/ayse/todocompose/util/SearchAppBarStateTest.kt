package com.ayse.todocompose.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchAppBarStateTest {

    @Test
    fun `enum has three values`() {
        assertEquals(3, SearchAppBarState.values().size)
    }

    @Test
    fun `values are correct`() {
        val expected = listOf(
            SearchAppBarState.OPENED,
            SearchAppBarState.CLOSED,
            SearchAppBarState.TRIGGERED
        )
        assertEquals(expected, SearchAppBarState.values().toList())
    }
}
