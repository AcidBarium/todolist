package com.ayse.todocompose.data.repository

import com.ayse.todocompose.data.ToDoDao
import com.ayse.todocompose.data.ToDoTask
import com.ayse.todocompose.data.models.Priority
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ToDoRepositoryTest {
    private val mockDao = mockk<ToDoDao>()

    @Before
    fun setup() {
        clearAllMocks()
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    private fun createRepo(
        tasks: Flow<List<ToDoTask>> = flowOf(emptyList()),
        low: Flow<List<ToDoTask>> = flowOf(emptyList()),
        high: Flow<List<ToDoTask>> = flowOf(emptyList())
    ): ToDoRepository {
        every { mockDao.getAllTasks() } returns tasks
        every { mockDao.sortByLowPriority() } returns low
        every { mockDao.sortByHighPriority() } returns high
        return ToDoRepository(mockDao)
    }

    @Test
    fun `getAllTask delegates to dao getAllTasks`() = runTest {
        val tasks = listOf(
            ToDoTask(1, "Title1", "Desc1", Priority.HIGH),
            ToDoTask(2, "Title2", "Desc2", Priority.LOW)
        )
        val repo = createRepo(tasks = flowOf(tasks))
        assertEquals(tasks, repo.getAllTask.first())
    }

    @Test
    fun `getSelectedTask delegates to dao getSelectedTask`() = runTest {
        val task = ToDoTask(1, "Title", "Desc", Priority.MEDIUM)
        val repo = createRepo()
        every { mockDao.getSelectedTask(1) } returns flowOf(task)
        val result = repo.getSelectedTask(1).first()
        assertEquals(task, result)
    }

    @Test
    fun `addTask calls dao addTask`() = runTest {
        val task = ToDoTask(title = "New", description = "Desc", priority = Priority.HIGH)
        val repo = createRepo()
        coEvery { mockDao.addTask(task) } returns Unit
        repo.addTask(task)
        coVerify(exactly = 1) { mockDao.addTask(task) }
    }

    @Test
    fun `updateTask calls dao updateTask`() = runTest {
        val task = ToDoTask(1, "Upd", "Desc", Priority.LOW)
        val repo = createRepo()
        coEvery { mockDao.updateTask(task) } returns Unit
        repo.updateTask(task)
        coVerify(exactly = 1) { mockDao.updateTask(task) }
    }

    @Test
    fun `deleteTask calls dao deleteTask`() = runTest {
        val task = ToDoTask(1, "Del", "Desc", Priority.NONE)
        val repo = createRepo()
        coEvery { mockDao.deleteTask(task) } returns Unit
        repo.deleteTask(task)
        coVerify(exactly = 1) { mockDao.deleteTask(task) }
    }

    @Test
    fun `deleteAllTask calls dao deleteAllTasks`() = runTest {
        val repo = createRepo()
        coEvery { mockDao.deleteAllTasks() } returns Unit
        repo.deleteAllTask()
        coVerify(exactly = 1) { mockDao.deleteAllTasks() }
    }

    @Test
    fun `searchDatabase delegates to dao searchDatabase`() = runTest {
        val query = "%test%"
        val results = listOf(ToDoTask(1, "test", "desc", Priority.HIGH))
        val repo = createRepo()
        every { mockDao.searchDatabase(query) } returns flowOf(results)
        assertEquals(results, repo.searchDatabase(query).first())
    }

    @Test
    fun `sortByLowPriority delegates to dao`() = runTest {
        val tasks = listOf(
            ToDoTask(1, "A", "B", Priority.LOW),
            ToDoTask(2, "C", "D", Priority.HIGH)
        )
        val repo = createRepo(low = flowOf(tasks))
        assertEquals(tasks, repo.sortByLowPriority.first())
    }

    @Test
    fun `sortByHighPriority delegates to dao`() = runTest {
        val tasks = listOf(
            ToDoTask(1, "A", "B", Priority.HIGH),
            ToDoTask(2, "C", "D", Priority.LOW)
        )
        val repo = createRepo(high = flowOf(tasks))
        assertEquals(tasks, repo.sortByHighPriority.first())
    }

    @Test
    fun `addTask passes correct task to dao`() = runTest {
        val repo = createRepo()
        coEvery { mockDao.addTask(any()) } returns Unit
        repo.addTask(ToDoTask(title = "Shopping", description = "Buy groceries", priority = Priority.HIGH))
        coVerify { mockDao.addTask(withArg { task ->
            assertEquals("Shopping", task.title)
            assertEquals("Buy groceries", task.description)
            assertEquals(Priority.HIGH, task.priority)
        }) }
    }

    @Test
    fun `deleteTask passes correct task to dao`() = runTest {
        val task = ToDoTask(id = 5, title = "T", description = "D", priority = Priority.LOW)
        val repo = createRepo()
        coEvery { mockDao.deleteTask(task) } returns Unit
        repo.deleteTask(task)
        coVerify(exactly = 1) { mockDao.deleteTask(task) }
    }
}

private suspend fun <T> Flow<T>.first(): T {
    var result: T? = null
    collect { result = it; return@collect }
    @Suppress("UNCHECKED_CAST")
    return result as T
}
