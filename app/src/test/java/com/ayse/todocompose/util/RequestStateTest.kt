package com.ayse.todocompose.util

import org.junit.Assert.*
import org.junit.Test

class RequestStateTest {

    @Test
    fun `Idle is instance of RequestState`() {
        assertTrue(RequestState.Idle is RequestState<*>)
    }

    @Test
    fun `Loading is instance of RequestState`() {
        assertTrue(RequestState.Loading is RequestState<*>)
    }

    @Test
    fun `Success holds data correctly`() {
        val state = RequestState.Success("test data")
        assertEquals("test data", state.data)
    }

    @Test
    fun `Success with integer data`() {
        val state = RequestState.Success(42)
        assertEquals(42, state.data)
    }

    @Test
    fun `Error holds throwable correctly`() {
        val exception = RuntimeException("test error")
        val state = RequestState.Error(exception)
        assertEquals(exception, state.data)
        assertEquals("test error", state.data.message)
    }

    @Test
    fun `Idle and Loading are not equal`() {
        assertNotEquals(RequestState.Idle, RequestState.Loading)
    }

    @Test
    fun `Two Success with same data are equal`() {
        val a = RequestState.Success("hello")
        val b = RequestState.Success("hello")
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `Two Success with different data are not equal`() {
        val a = RequestState.Success("hello")
        val b = RequestState.Success("world")
        assertNotEquals(a, b)
    }

    @Test
    fun `Two Error with same throwable are equal`() {
        val e = RuntimeException("err")
        val a = RequestState.Error(e)
        val b = RequestState.Error(e)
        assertEquals(a, b)
    }

    @Test
    fun `Success component1 returns data`() {
        val state = RequestState.Success("data")
        assertEquals("data", state.component1())
    }

    @Test
    fun `Error component1 returns throwable`() {
        val e = RuntimeException("err")
        val state = RequestState.Error(e)
        assertEquals(e, state.component1())
    }

    @Test
    fun `Success toString contains data`() {
        val state = RequestState.Success("value")
        assertTrue(state.toString().contains("value"))
    }

    @Test
    fun `Error toString contains error message`() {
        val state = RequestState.Error(RuntimeException("fail"))
        assertTrue(state.toString().contains("fail"))
    }
}
