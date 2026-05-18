package com.ayse.todocompose.data

import com.ayse.todocompose.data.models.Priority
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ToDoTaskTest {

    private val sampleTask = ToDoTask(
        title = "Test Task",
        description = "A sample task",
        priority = Priority.HIGH
    )

    @Test
    fun `default id is zero`() {
        val task = ToDoTask(title = "T", description = "D", priority = Priority.LOW)
        assertEquals(0, task.id)
    }

    @Test
    fun `copy creates identical copy`() {
        val copy = sampleTask.copy()
        assertEquals(sampleTask, copy)
    }

    @Test
    fun `copy with changed id`() {
        val modified = sampleTask.copy(id = 42)
        assertEquals(42, modified.id)
        assertEquals(sampleTask.title, modified.title)
        assertEquals(sampleTask.description, modified.description)
        assertEquals(sampleTask.priority, modified.priority)
    }

    @Test
    fun `copy with changed title`() {
        val modified = sampleTask.copy(title = "Updated")
        assertEquals("Updated", modified.title)
    }

    @Test
    fun `copy with changed description`() {
        val modified = sampleTask.copy(description = "New desc")
        assertEquals("New desc", modified.description)
    }

    @Test
    fun `copy with changed priority`() {
        val modified = sampleTask.copy(priority = Priority.LOW)
        assertEquals(Priority.LOW, modified.priority)
    }

    @Test
    fun `equals works correctly`() {
        val a = ToDoTask(1, "X", "Y", Priority.HIGH)
        val b = ToDoTask(1, "X", "Y", Priority.HIGH)
        assertEquals(a, b)
    }

    @Test
    fun `equals with different id`() {
        val a = ToDoTask(1, "X", "Y", Priority.HIGH)
        val b = ToDoTask(2, "X", "Y", Priority.HIGH)
        assertNotEquals(a, b)
    }

    @Test
    fun `toString contains title`() {
        val s = sampleTask.toString()
        assert(s.contains("Test Task"))
    }

    @Test
    fun `different priorities create different tasks`() {
        val high = ToDoTask(title = "T", description = "D", priority = Priority.HIGH)
        val low = ToDoTask(title = "T", description = "D", priority = Priority.LOW)
        assertNotEquals(high, low)
    }
}
