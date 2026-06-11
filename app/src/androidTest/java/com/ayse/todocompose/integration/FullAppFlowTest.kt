package com.ayse.todocompose.integration

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class FullAppFlowTest {

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
    fun addTask_taskAppearsInList() {
        // 端到端：通过 ViewModel 添加任务 → 验证 UI 实时显示
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

        viewModel.updateTitle("Buy Coffee")
        viewModel.updateDescription("Get dark roast beans")
        viewModel.updatePriority(Priority.HIGH)
        viewModel.handleDatabaseActions(Action.ADD)

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Buy Coffee").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithText("Buy Coffee").assertIsDisplayed()
        composeTestRule.onNodeWithText("Get dark roast beans").assertIsDisplayed()
    }

    @Test
    fun deleteTask_taskRemovedFromList() {
        // 端到端：预置任务 → ViewModel 删除 → 验证 UI 实时消失
        runBlocking {
            database.toDoDAO().addTask(
                ToDoTask(title = "Temp Task", description = "Will be deleted", priority = Priority.LOW)
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
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Temp Task").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        viewModel.updateTaskFields(
            ToDoTask(id = 1, title = "Temp Task", description = "Will be deleted", priority = Priority.LOW)
        )
        viewModel.handleDatabaseActions(Action.DELETE)

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Temp Task").assertDoesNotExist()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithText("Temp Task").assertDoesNotExist()
    }

    @Test
    fun addThenDeleteTask_fullRoundTrip() {
        // 端到端：完整轮转——添加 → 出现 → 删除 → 消失
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

        viewModel.updateTitle("Round Trip")
        viewModel.updateDescription("Testing add and delete")
        viewModel.updatePriority(Priority.MEDIUM)
        viewModel.handleDatabaseActions(Action.ADD)

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Round Trip").assertIsDisplayed()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        viewModel.updateTaskFields(
            ToDoTask(id = 1, title = "Round Trip", description = "Testing add and delete", priority = Priority.MEDIUM)
        )
        viewModel.handleDatabaseActions(Action.DELETE)

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Round Trip").assertDoesNotExist()
                true
            } catch (e: AssertionError) {
                false
            }
        }

        composeTestRule.onNodeWithText("Round Trip").assertDoesNotExist()
    }
}
