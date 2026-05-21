package com.ayse.todocompose.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.ayse.todocompose.data.models.Priority
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Config.TARGET_SDK])
class ToDoDaoTest {
    private lateinit var database: ToDoDatabase
    private lateinit var dao: ToDoDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ToDoDatabase::class.java)
            .allowMainThreadQueries().build()
        dao = database.toDoDAO()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun insertTask(title: String, desc: String, priority: Priority): Int {
        dao.addTask(ToDoTask(title = title, description = desc, priority = priority))
        return dao.getAllTasks().first().last { it.title == title }.id
    }

    @Test
    fun `getAllTasks returns empty list initially`() = runTest {
        assertTrue(dao.getAllTasks().first().isEmpty())
    }

    @Test
    fun `addTask inserts and getAllTasks returns it`() = runTest {
        val task = ToDoTask(title = "Test", description = "Desc", priority = Priority.HIGH)
        dao.addTask(task)
        val tasks = dao.getAllTasks().first()
        assertEquals(1, tasks.size)
        assertEquals("Test", tasks[0].title)
        assertEquals("Desc", tasks[0].description)
        assertEquals(Priority.HIGH, tasks[0].priority)
    }

    @Test
    fun `addTask auto generates positive id`() = runTest {
        dao.addTask(ToDoTask(title = "T1", description = "D1", priority = Priority.LOW))
        val tasks = dao.getAllTasks().first()
        assertTrue(tasks[0].id > 0)
    }

    @Test
    fun `getSelectedTask returns correct task by id`() = runTest {
        val id = insertTask("T", "D", Priority.MEDIUM)
        val result = dao.getSelectedTask(id).first()
        assertEquals("T", result.title)
        assertEquals(id, result.id)
    }

    @Test
    fun `updateTask modifies existing task`() = runTest {
        val id = insertTask("Old", "Old", Priority.LOW)
        dao.updateTask(ToDoTask(id = id, title = "New", description = "New Desc", priority = Priority.HIGH))
        val updated = dao.getSelectedTask(id).first()
        assertEquals("New", updated.title)
        assertEquals("New Desc", updated.description)
        assertEquals(Priority.HIGH, updated.priority)
    }

    @Test
    fun `deleteTask removes task`() = runTest {
        val id = insertTask("Del", "D", Priority.NONE)
        dao.deleteTask(ToDoTask(id = id, title = "Del", description = "D", priority = Priority.NONE))
        assertTrue(dao.getAllTasks().first().isEmpty())
    }

    @Test
    fun `deleteAllTasks clears all`() = runTest {
        dao.addTask(ToDoTask(title = "A", description = "a", priority = Priority.LOW))
        dao.addTask(ToDoTask(title = "B", description = "b", priority = Priority.HIGH))
        dao.deleteAllTasks()
        assertTrue(dao.getAllTasks().first().isEmpty())
    }

    @Test
    fun `searchDatabase finds by title`() = runTest {
        dao.addTask(ToDoTask(title = "Shopping", description = "Buy milk", priority = Priority.LOW))
        dao.addTask(ToDoTask(title = "Workout", description = "Run 5km", priority = Priority.HIGH))
        val result = dao.searchDatabase("%Shopping%").first()
        assertEquals(1, result.size)
        assertEquals("Shopping", result[0].title)
    }

    @Test
    fun `searchDatabase finds by description`() = runTest {
        dao.addTask(ToDoTask(title = "Task1", description = "Meeting with team", priority = Priority.MEDIUM))
        val result = dao.searchDatabase("%Meeting%").first()
        assertEquals(1, result.size)
    }

    @Test
    fun `sortByLowPriority returns tasks low first`() = runTest {
        dao.addTask(ToDoTask(title = "High", description = "h", priority = Priority.HIGH))
        dao.addTask(ToDoTask(title = "Low", description = "l", priority = Priority.LOW))
        dao.addTask(ToDoTask(title = "Medium", description = "m", priority = Priority.MEDIUM))
        val sorted = dao.sortByLowPriority().first()
        assertTrue(sorted[0].priority == Priority.LOW || sorted[0].priority == Priority.NONE)
        assertTrue(sorted[2].priority == Priority.HIGH)
    }

    @Test
    fun `sortByHighPriority returns tasks high first`() = runTest {
        dao.addTask(ToDoTask(title = "High", description = "h", priority = Priority.HIGH))
        dao.addTask(ToDoTask(title = "Low", description = "l", priority = Priority.LOW))
        val sorted = dao.sortByHighPriority().first()
        assertEquals(Priority.HIGH, sorted[0].priority)
    }

    @Test
    fun `searchDatabase with percent query returns all`() = runTest {
        dao.addTask(ToDoTask(title = "A", description = "a", priority = Priority.LOW))
        dao.addTask(ToDoTask(title = "B", description = "b", priority = Priority.HIGH))
        val result = dao.searchDatabase("%%").first()
        assertEquals(2, result.size)
    }

    @Test
    fun `getAllTasks returns multiple tasks in insert order`() = runTest {
        dao.addTask(ToDoTask(title = "First", description = "d", priority = Priority.LOW))
        dao.addTask(ToDoTask(title = "Second", description = "d", priority = Priority.HIGH))
        val tasks = dao.getAllTasks().first()
        assertEquals(2, tasks.size)
        assertEquals("First", tasks[0].title)
        assertEquals("Second", tasks[1].title)
    }
}
