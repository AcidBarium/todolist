package com.ayse.todocompose.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ayse.todocompose.data.models.Priority
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ToDoDaoTest {

    private lateinit var database: ToDoDatabase
    private lateinit var dao: ToDoDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ToDoDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.toDoDAO()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveTask() = runBlocking {
        val task = ToDoTask(
            title = "Test Title",
            description = "Test Description",
            priority = Priority.HIGH
        )
        dao.addTask(task)

        val tasks = dao.getAllTasks().first()
        assertEquals(1, tasks.size)
        assertEquals("Test Title", tasks[0].title)
        assertEquals("Test Description", tasks[0].description)
        assertEquals(Priority.HIGH, tasks[0].priority)
    }

    @Test
    fun insertMultipleTasks() = runBlocking {
        dao.addTask(ToDoTask(title = "A", description = "a", priority = Priority.HIGH))
        dao.addTask(ToDoTask(title = "B", description = "b", priority = Priority.MEDIUM))
        dao.addTask(ToDoTask(title = "C", description = "c", priority = Priority.LOW))

        val tasks = dao.getAllTasks().first()
        assertEquals(3, tasks.size)
    }

    @Test
    fun updateExistingTask() = runBlocking {
        dao.addTask(ToDoTask(title = "Original", description = "Desc", priority = Priority.LOW))

        val inserted = dao.getAllTasks().first().first()
        val updated = inserted.copy(title = "Updated Title")
        dao.updateTask(updated)

        val result = dao.getAllTasks().first()
        assertEquals(1, result.size)
        assertEquals("Updated Title", result[0].title)
    }

    @Test
    fun deleteTask() = runBlocking {
        dao.addTask(ToDoTask(title = "Delete Me", description = "Gone", priority = Priority.MEDIUM))

        val inserted = dao.getAllTasks().first().first()
        dao.deleteTask(inserted)

        assertTrue(dao.getAllTasks().first().isEmpty())
    }

    @Test
    fun deleteAllTasks() = runBlocking {
        dao.addTask(ToDoTask(title = "A", description = "a", priority = Priority.HIGH))
        dao.addTask(ToDoTask(title = "B", description = "b", priority = Priority.LOW))

        dao.deleteAllTasks()

        assertTrue(dao.getAllTasks().first().isEmpty())
    }

    @Test
    fun searchDatabaseByTitle() = runBlocking {
        dao.addTask(ToDoTask(title = "Buy groceries", description = "Milk and eggs", priority = Priority.HIGH))
        dao.addTask(ToDoTask(title = "Meeting", description = "Project discussion", priority = Priority.MEDIUM))

        val results = dao.searchDatabase("%groceries%").first()
        assertEquals(1, results.size)
        assertEquals("Buy groceries", results[0].title)
    }

    @Test
    fun searchDatabaseByDescription() = runBlocking {
        dao.addTask(ToDoTask(title = "Shopping", description = "Get vegetables", priority = Priority.LOW))
        dao.addTask(ToDoTask(title = "Workout", description = "Go for a run", priority = Priority.HIGH))

        val results = dao.searchDatabase("%vegetables%").first()
        assertEquals(1, results.size)
        assertEquals("Shopping", results[0].title)
    }

    @Test
    fun searchDatabaseReturnsEmptyForNoMatch() = runBlocking {
        dao.addTask(ToDoTask(title = "Existing", description = "Task", priority = Priority.LOW))

        val results = dao.searchDatabase("%nonexistent%").first()
        assertTrue(results.isEmpty())
    }

    @Test
    fun getSelectedTaskById() = runBlocking {
        dao.addTask(ToDoTask(title = "Target", description = "Find me", priority = Priority.HIGH))
        dao.addTask(ToDoTask(title = "Other", description = "Not me", priority = Priority.LOW))

        val tasks = dao.getAllTasks().first()
        val targetId = tasks.first { it.title == "Target" }.id

        val selected = dao.getSelectedTask(targetId).first()
        assertEquals("Target", selected.title)
    }

    @Test
    fun sortByLowPriority() = runBlocking {
        dao.addTask(ToDoTask(title = "High", description = "d1", priority = Priority.HIGH))
        dao.addTask(ToDoTask(title = "Low", description = "d2", priority = Priority.LOW))
        dao.addTask(ToDoTask(title = "Medium", description = "d3", priority = Priority.MEDIUM))

        val sorted = dao.sortByLowPriority().first()
        assertEquals("Low", sorted[0].title)
        assertEquals("Medium", sorted[1].title)
        assertEquals("High", sorted[2].title)
    }

    @Test
    fun sortByHighPriority() = runBlocking {
        dao.addTask(ToDoTask(title = "High", description = "d1", priority = Priority.HIGH))
        dao.addTask(ToDoTask(title = "Low", description = "d2", priority = Priority.LOW))
        dao.addTask(ToDoTask(title = "Medium", description = "d3", priority = Priority.MEDIUM))

        val sorted = dao.sortByHighPriority().first()
        assertEquals("High", sorted[0].title)
        assertEquals("Medium", sorted[1].title)
        assertEquals("Low", sorted[2].title)
    }
}
