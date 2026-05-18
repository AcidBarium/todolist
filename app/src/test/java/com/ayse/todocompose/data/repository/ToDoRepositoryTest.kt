package com.ayse.todocompose.data.repository

import com.ayse.todocompose.data.ToDoDao
import com.ayse.todocompose.data.ToDoTask
import com.ayse.todocompose.data.models.Priority
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class ToDoRepositoryTest {

    private val mockDao = mock<ToDoDao>()
    private lateinit var repository: ToDoRepository

    @Before
    fun setup() {
        whenever(mockDao.getAllTasks()).thenReturn(flowOf(emptyList()))
        whenever(mockDao.sortByLowPriority()).thenReturn(flowOf(emptyList()))
        whenever(mockDao.sortByHighPriority()).thenReturn(flowOf(emptyList()))
        whenever(mockDao.searchDatabase(anyString())).thenReturn(flowOf(emptyList()))
        whenever(mockDao.getSelectedTask(anyInt())).thenReturn(
            flowOf(ToDoTask(title = "test", description = "desc", priority = Priority.LOW))
        )

        repository = ToDoRepository(mockDao)
    }

    @Test
    fun `getAllTask delegates to DAO during construction`() {
        verify(mockDao, Mockito.times(1)).getAllTasks()
    }

    @Test
    fun `sortByLowPriority delegates to DAO during construction`() {
        verify(mockDao, Mockito.times(1)).sortByLowPriority()
    }

    @Test
    fun `sortByHighPriority delegates to DAO during construction`() {
        verify(mockDao, Mockito.times(1)).sortByHighPriority()
    }

    @Test
    fun `getSelectedTask returns DAO flow`() {
        val result = repository.getSelectedTask(42)
        assertNotNull(result)
    }

    @Test
    fun `getSelectedTask delegates to DAO`() {
        repository.getSelectedTask(99)
        verify(mockDao).getSelectedTask(99)
    }

    @Test
    fun `searchDatabase delegates to DAO`() {
        val result = repository.searchDatabase("%query%")
        assertNotNull(result)
        verify(mockDao).searchDatabase("%query%")
    }

    @Test
    fun `getAllTask flow content comes from DAO`() = runTest {
        val tasks = listOf(
            ToDoTask(title = "A", description = "a", priority = Priority.HIGH)
        )
        whenever(mockDao.getAllTasks()).thenReturn(flowOf(tasks))

        val repo = ToDoRepository(mockDao)
        val result = repo.getAllTask.first()
        assertEquals(1, result.size)
        assertEquals("A", result[0].title)
    }

    @Test
    fun `searchDatabase returns expected results`() = runTest {
        val expected = listOf(
            ToDoTask(title = "Found", description = "match", priority = Priority.MEDIUM)
        )
        whenever(mockDao.searchDatabase("%found%")).thenReturn(flowOf(expected))

        val result = repository.searchDatabase("%found%").first()
        assertEquals(1, result.size)
        assertEquals("Found", result[0].title)
    }
}
