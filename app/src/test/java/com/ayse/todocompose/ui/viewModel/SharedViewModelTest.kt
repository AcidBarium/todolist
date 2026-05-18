package com.ayse.todocompose.ui.viewModel

import com.ayse.todocompose.data.ToDoTask
import com.ayse.todocompose.data.models.Priority
import com.ayse.todocompose.data.repository.DataStoreRepository
import com.ayse.todocompose.data.repository.ToDoRepository
import com.ayse.todocompose.util.Action
import com.ayse.todocompose.util.SearchAppBarState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SharedViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockRepo = mock<ToDoRepository>()
    private val mockDataStore = mock<DataStoreRepository>()
    private lateinit var viewModel: SharedViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        whenever(mockRepo.getAllTask).thenReturn(flowOf(emptyList()))
        whenever(mockRepo.sortByLowPriority).thenReturn(flowOf(emptyList()))
        whenever(mockRepo.sortByHighPriority).thenReturn(flowOf(emptyList()))
        whenever(mockDataStore.readSortState).thenReturn(flowOf(Priority.NONE.name))
        viewModel = SharedViewModel(mockRepo, mockDataStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `validateFields returns false when title is empty`() {
        viewModel.updateTitle("")
        viewModel.updateDescription("description")
        assertFalse(viewModel.validateFields())
    }

    @Test
    fun `validateFields returns false when description is empty`() {
        viewModel.updateTitle("title")
        viewModel.updateDescription("")
        assertFalse(viewModel.validateFields())
    }

    @Test
    fun `validateFields returns false when both are empty`() {
        viewModel.updateTitle("")
        viewModel.updateDescription("")
        assertFalse(viewModel.validateFields())
    }

    @Test
    fun `validateFields returns true when both fields are filled`() {
        viewModel.updateTitle("title")
        viewModel.updateDescription("description")
        assertTrue(viewModel.validateFields())
    }

    @Test
    fun `updateTitle stores the given title`() {
        viewModel.updateTitle("My Task")
        assertEquals("My Task", viewModel.title)
    }

    @Test
    fun `updateTitle truncates at MAX_TITLE_LENGTH`() {
        viewModel.updateTitle("short")
        assertEquals("short", viewModel.title)

        val longTitle = "A".repeat(25)
        viewModel.updateTitle(longTitle)
        assertEquals("short", viewModel.title)
    }

    @Test
    fun `updateDescription stores the given description`() {
        viewModel.updateDescription("Some details")
        assertEquals("Some details", viewModel.description)
    }

    @Test
    fun `updatePriority stores the given priority`() {
        viewModel.updatePriority(Priority.HIGH)
        assertEquals(Priority.HIGH, viewModel.priority)

        viewModel.updatePriority(Priority.NONE)
        assertEquals(Priority.NONE, viewModel.priority)
    }

    @Test
    fun `updateAction stores the given action`() {
        viewModel.updateAction(Action.ADD)
        assertEquals(Action.ADD, viewModel.action)

        viewModel.updateAction(Action.DELETE_ALL)
        assertEquals(Action.DELETE_ALL, viewModel.action)
    }

    @Test
    fun `updateAppbarState stores the given state`() {
        viewModel.updateAppbarState(SearchAppBarState.OPENED)
        assertEquals(SearchAppBarState.OPENED, viewModel.searchAppBarState)
    }

    @Test
    fun `updateSearchTextState stores the given text`() {
        viewModel.updateSearchTextState("search query")
        assertEquals("search query", viewModel.searchTextState)
    }

    @Test
    fun `updateTaskFields with null resets to defaults`() {
        viewModel.updateTitle("old title")
        viewModel.updateDescription("old description")
        viewModel.updatePriority(Priority.HIGH)
        viewModel.updateAction(Action.UPDATE)

        viewModel.updateTaskFields(null)

        assertEquals(0, viewModel.id)
        assertEquals("", viewModel.title)
        assertEquals("", viewModel.description)
        assertEquals(Priority.LOW, viewModel.priority)
    }

    @Test
    fun `updateTaskFields with task sets all fields`() {
        val task = ToDoTask(
            id = 7,
            title = "Task Title",
            description = "Task Description",
            priority = Priority.MEDIUM
        )

        viewModel.updateTaskFields(task)

        assertEquals(7, viewModel.id)
        assertEquals("Task Title", viewModel.title)
        assertEquals("Task Description", viewModel.description)
        assertEquals(Priority.MEDIUM, viewModel.priority)
    }

    @Test
    fun `searchDatabase sets appbar state to TRIGGERED`() {
        viewModel.updateSearchTextState("test")
        viewModel.searchDatabase("test")
        assertEquals(SearchAppBarState.TRIGGERED, viewModel.searchAppBarState)
    }

    @Test
    fun `handleDatabaseActions ADD does not throw`() {
        viewModel.updateTitle("test")
        viewModel.updateDescription("desc")
        viewModel.handleDatabaseActions(Action.ADD)
    }

    @Test
    fun `handleDatabaseActions UPDATE does not throw`() {
        viewModel.updateTitle("test")
        viewModel.updateDescription("desc")
        viewModel.handleDatabaseActions(Action.UPDATE)
    }

    @Test
    fun `handleDatabaseActions DELETE does not throw`() {
        viewModel.handleDatabaseActions(Action.DELETE)
    }

    @Test
    fun `handleDatabaseActions DELETE_ALL does not throw`() {
        viewModel.handleDatabaseActions(Action.DELETE_ALL)
    }

    @Test
    fun `handleDatabaseActions UNDO does not throw`() {
        viewModel.updateTitle("undo task")
        viewModel.updateDescription("undo desc")
        viewModel.handleDatabaseActions(Action.UNDO)
    }

    @Test
    fun `handleDatabaseActions NO_ACTION does not throw`() {
        viewModel.handleDatabaseActions(Action.NO_ACTION)
    }

    @Test
    fun `default priority is LOW`() {
        assertEquals(Priority.LOW, viewModel.priority)
    }

    @Test
    fun `default action is NO_ACTION`() {
        assertEquals(Action.NO_ACTION, viewModel.action)
    }

    @Test
    fun `default searchAppBarState is CLOSED`() {
        assertEquals(SearchAppBarState.CLOSED, viewModel.searchAppBarState)
    }

    @Test
    fun `persistSortState does not throw`() {
        viewModel.persistSortState(Priority.HIGH)
    }
}
