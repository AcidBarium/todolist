package com.ayse.todocompose.integration

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ayse.todocompose.R
import com.ayse.todocompose.data.ToDoDatabase
import com.ayse.todocompose.data.ToDoTask
import com.ayse.todocompose.data.models.Priority
import com.ayse.todocompose.data.repository.DataStoreRepository
import com.ayse.todocompose.data.repository.ToDoRepository
import com.ayse.todocompose.ui.screens.list.ListScreen
import com.ayse.todocompose.ui.theme.ToDoComposeTheme
import com.ayse.todocompose.ui.viewModel.SharedViewModel
import com.ayse.todocompose.util.Action
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ListScreenIntegrationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var database: ToDoDatabase
    private lateinit var repository: ToDoRepository
    private lateinit var viewModel: SharedViewModel
    private val tempPrefsFiles = mutableListOf<File>()

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            composeTestRule.activity,
            ToDoDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = ToDoRepository(database.toDoDAO())

        val prefsFile = composeTestRule.activity.filesDir
            .resolve("test_prefs_${UUID.randomUUID()}.preferences_pb")
        tempPrefsFiles.add(prefsFile)
        val dataStore = PreferenceDataStoreFactory.create { prefsFile }
        val dataStoreRepo = DataStoreRepository(dataStore)

        viewModel = SharedViewModel(repository, dataStoreRepo)
        composeTestRule.waitForIdle()
    }

    @After
    fun tearDown() {
        database.close()
        tempPrefsFiles.forEach { if (it.exists()) it.delete() }
    }

    @Test
    fun displaysEmptyContent_whenNoTasks() {
        // 验证空数据库时 ListScreen 显示 "No Task Found" 空状态
        composeTestRule.setContent {
            ToDoComposeTheme {
                ListScreen(
                    action = Action.NO_ACTION,
                    navigateToTaskScreen = {},
                    sharedViewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        val emptyText = composeTestRule.activity.getString(R.string.empty_content)
        composeTestRule.onNodeWithText(emptyText).assertIsDisplayed()
    }

    @Test
    fun displaysTasksFromDatabase() {
        // 验证数据库中的任务能通过 ViewModel → Flow → UI 正确渲染
        runBlocking {
            database.toDoDAO().addTask(
                ToDoTask(title = "Buy groceries", description = "Milk and eggs", priority = Priority.HIGH)
            )
        }

        composeTestRule.setContent {
            ToDoComposeTheme {
                ListScreen(
                    action = Action.NO_ACTION,
                    navigateToTaskScreen = {},
                    sharedViewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Buy groceries").assertIsDisplayed()
        composeTestRule.onNodeWithText("Milk and eggs").assertIsDisplayed()
    }

    @Test
    fun fabClick_callsNavigateToTaskScreenWithMinusOne() {
        // 验证 FAB 点击后导航回调传入 -1（表示新增任务）
        var capturedId = Int.MAX_VALUE

        composeTestRule.setContent {
            ToDoComposeTheme {
                ListScreen(
                    action = Action.NO_ACTION,
                    navigateToTaskScreen = { capturedId = it },
                    sharedViewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("list_fab").performClick()
        composeTestRule.runOnIdle {
            assertEquals(-1, capturedId)
        }
    }

    @Test
    fun taskClick_callsNavigateToTaskScreenWithTaskId() {
        // 验证点击任务项后导航回调传入正确的任务主键 ID
        runBlocking {
            database.toDoDAO().addTask(
                ToDoTask(id = 0, title = "Clickable Task", description = "Will be clicked", priority = Priority.LOW)
            )
        }
        var capturedId = Int.MAX_VALUE

        composeTestRule.setContent {
            ToDoComposeTheme {
                ListScreen(
                    action = Action.NO_ACTION,
                    navigateToTaskScreen = { capturedId = it },
                    sharedViewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Clickable Task").performClick()
        composeTestRule.runOnIdle {
            assertEquals(1, capturedId)
        }
    }

    @Test
    fun searchBar_searchFiltersTasks() {
        // 验证搜索功能：输入关键词后只显示匹配任务，不匹配的隐藏
        runBlocking {
            val dao = database.toDoDAO()
            dao.addTask(ToDoTask(title = "Buy milk", description = "Dairy", priority = Priority.LOW))
            dao.addTask(ToDoTask(title = "Walk dog", description = "Park", priority = Priority.MEDIUM))
        }

        composeTestRule.setContent {
            ToDoComposeTheme {
                ListScreen(
                    action = Action.NO_ACTION,
                    navigateToTaskScreen = {},
                    sharedViewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()

        val searchIconDesc = composeTestRule.activity.getString(R.string.search_action)
        composeTestRule.onNodeWithContentDescription(searchIconDesc).performClick()

        composeTestRule.waitForIdle()

        val searchField = composeTestRule.onNodeWithTag("search_text_field")
        searchField.performTextClearance()
        searchField.performTextInput("milk")
        searchField.performImeAction()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Buy milk").assertIsDisplayed()
        composeTestRule.onNodeWithText("Walk dog").assertDoesNotExist()
    }

    @Test
    fun displaysMultipleTasks_withCorrectSortState() {
        // 验证默认排序（NONE）下多条任务全部正常显示
        runBlocking {
            val dao = database.toDoDAO()
            dao.addTask(ToDoTask(title = "Task A", description = "First", priority = Priority.LOW))
            dao.addTask(ToDoTask(title = "Task B", description = "Second", priority = Priority.HIGH))
        }

        composeTestRule.setContent {
            ToDoComposeTheme {
                ListScreen(
                    action = Action.NO_ACTION,
                    navigateToTaskScreen = {},
                    sharedViewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Task A").assertIsDisplayed()
        composeTestRule.onNodeWithText("Task B").assertIsDisplayed()
    }
}
