package com.ayse.todocompose.ui.viewModel

import android.util.Log
import com.ayse.todocompose.data.ToDoTask
import com.ayse.todocompose.data.models.Priority
import com.ayse.todocompose.data.repository.DataStoreRepository
import com.ayse.todocompose.data.repository.ToDoRepository
import com.ayse.todocompose.util.Action
import com.ayse.todocompose.util.RequestState
import com.ayse.todocompose.util.SearchAppBarState
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SharedViewModelTest {
    private val mockRepo = mockk<ToDoRepository>()
    private val mockDataStoreRepo = mockk<DataStoreRepository>()
    private lateinit var viewModel: SharedViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        clearAllMocks()
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        every { mockRepo.getAllTask } returns flowOf(emptyList())
        every { mockRepo.sortByLowPriority } returns flowOf(emptyList())
        every { mockRepo.sortByHighPriority } returns flowOf(emptyList())
        every { mockDataStoreRepo.readSortState } returns flowOf(Priority.NONE.name)
        coEvery { mockDataStoreRepo.persistSortState(any()) } returns Unit

        viewModel = SharedViewModel(mockRepo, mockDataStoreRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun `init loads allTasks as Success with empty list`() {
        assertEquals(RequestState.Success(emptyList<ToDoTask>()), viewModel.allTask.value)
    }

    @Test
    fun `init does not set sortState to Error`() {
        assertFalse(viewModel.sortState.value is RequestState.Error)
    }

    @Test
    fun `updateTitle sets title`() {
        viewModel.updateTitle("New Title")
        assertEquals("New Title", viewModel.title)
    }

    @Test
    fun `updateTitle truncates when exceeds max length`() {
        viewModel.updateTitle("1234567890123456789012345")
        assertEquals("", viewModel.title)
    }

    @Test
    fun `updateTitle allows up to max length`() {
        viewModel.updateTitle("1234567890123456789")
        assertEquals("1234567890123456789", viewModel.title)
    }

    @Test
    fun `validateFields returns false when title not set`() {
        viewModel.updateDescription("desc")
        assertFalse(viewModel.validateFields())
    }

    @Test
    fun `validateFields returns false when description not set`() {
        viewModel.updateTitle("title")
        assertFalse(viewModel.validateFields())
    }

    @Test
    fun `validateFields returns true when both filled`() {
        viewModel.updateTitle("title")
        viewModel.updateDescription("desc")
        assertTrue(viewModel.validateFields())
    }

    @Test
    fun `updateAction changes action`() {
        viewModel.updateAction(Action.DELETE)
        assertEquals(Action.DELETE, viewModel.action)
    }

    @Test
    fun `updatePriority changes priority`() {
        viewModel.updatePriority(Priority.HIGH)
        assertEquals(Priority.HIGH, viewModel.priority)
    }

    @Test
    fun `updateDescription sets description`() {
        viewModel.updateDescription("Some desc")
        assertEquals("Some desc", viewModel.description)
    }

    @Test
    fun `updateAppbarState changes searchAppBarState`() {
        viewModel.updateAppbarState(SearchAppBarState.OPENED)
        assertEquals(SearchAppBarState.OPENED, viewModel.searchAppBarState)
    }

    @Test
    fun `updateSearchTextState sets search text`() {
        viewModel.updateSearchTextState("query")
        assertEquals("query", viewModel.searchTextState)
    }

    @Test
    fun `updateTaskFields populates fields from task`() {
        val task = ToDoTask(id = 5, title = "Task", description = "Detail", priority = Priority.HIGH)
        viewModel.updateTaskFields(task)
        assertEquals(5, viewModel.id)
        assertEquals("Task", viewModel.title)
        assertEquals("Detail", viewModel.description)
        assertEquals(Priority.HIGH, viewModel.priority)
    }

    @Test
    fun `updateTaskFields resets fields when task is null`() {
        viewModel.updateTaskFields(ToDoTask(id = 5, title = "T", description = "D", priority = Priority.HIGH))
        viewModel.updateTaskFields(null)
        assertEquals(0, viewModel.id)
        assertEquals("", viewModel.title)
        assertEquals("", viewModel.description)
        assertEquals(Priority.LOW, viewModel.priority)
    }

    @Test
    fun `handleDatabaseActions ADD calls addTask`() {
        viewModel.updateTitle("t")
        viewModel.updateDescription("d")
        coEvery { mockRepo.addTask(any()) } returns Unit
        viewModel.handleDatabaseActions(Action.ADD)
        Thread.sleep(50)
        coVerify { mockRepo.addTask(any()) }
        assertEquals(SearchAppBarState.CLOSED, viewModel.searchAppBarState)
    }

    @Test
    fun `handleDatabaseActions DELETE calls deleteTask`() {
        coEvery { mockRepo.deleteTask(any()) } returns Unit
        viewModel.handleDatabaseActions(Action.DELETE)
        Thread.sleep(50)
        coVerify { mockRepo.deleteTask(any()) }
    }



    @Test
    fun `handleDatabaseActions UNDO calls addTask`() {
        viewModel.updateTitle("t")
        viewModel.updateDescription("d")
        coEvery { mockRepo.addTask(any()) } returns Unit
        viewModel.handleDatabaseActions(Action.UNDO)
        Thread.sleep(50)
        coVerify { mockRepo.addTask(any()) }
    }

    @Test
    fun `handleDatabaseActions UPDATE calls updateTask`() {
        coEvery { mockRepo.updateTask(any()) } returns Unit
        viewModel.handleDatabaseActions(Action.UPDATE)
        Thread.sleep(50)
        coVerify { mockRepo.updateTask(any()) }
    }

    @Test
    fun `handleDatabaseActions DELETE_ALL calls deleteAllTask`() {
        coEvery { mockRepo.deleteAllTask() } returns Unit
        viewModel.handleDatabaseActions(Action.DELETE_ALL)
        Thread.sleep(50)
        coVerify { mockRepo.deleteAllTask() }
    }

    @Test
    fun `handleDatabaseActions NO_ACTION does nothing`() {
        viewModel.handleDatabaseActions(Action.NO_ACTION)
        coVerify(exactly = 0) { mockRepo.addTask(any()) }
    }

    @Test
    fun `persistSortState delegates to dataStoreRepository`() {
        viewModel.persistSortState(Priority.HIGH)
        Thread.sleep(50)
        coVerify { mockDataStoreRepo.persistSortState(Priority.HIGH) }
    }

    @Test
    fun `searchDatabase updates searchedTasks`() {
        val results = listOf(ToDoTask(1, "Match", "Desc", Priority.LOW))
        every { mockRepo.searchDatabase("%query%") } returns flowOf(results)
        viewModel.searchDatabase("query")
        assertEquals(RequestState.Success(results), viewModel.searchedTasks.value)
        assertEquals(SearchAppBarState.TRIGGERED, viewModel.searchAppBarState)
    }

    @Test
    fun `getSelectedTask updates selectedTask`() {
        val task = ToDoTask(3, "Selected", "Desc", Priority.HIGH)
        every { mockRepo.getSelectedTask(3) } returns flowOf(task)
        viewModel.getSelectedTask(3)
        assertEquals(task, viewModel.selectedTask.value)
    }

    @Test
    fun `initial action is NO_ACTION`() {
        assertEquals(Action.NO_ACTION, viewModel.action)
    }

    @Test
    fun `initial id is 0`() {
        assertEquals(0, viewModel.id)
    }

    @Test
    fun `initial searchAppBarState is CLOSED`() {
        assertEquals(SearchAppBarState.CLOSED, viewModel.searchAppBarState)
    }

    @Test
    fun `initial priority is LOW`() {
        assertEquals(Priority.LOW, viewModel.priority)
    }
}
