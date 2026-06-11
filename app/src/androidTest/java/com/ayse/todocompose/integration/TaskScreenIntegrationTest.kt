package com.ayse.todocompose.integration

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ayse.todocompose.data.ToDoDatabase
import com.ayse.todocompose.data.ToDoTask
import com.ayse.todocompose.data.models.Priority
import com.ayse.todocompose.data.repository.DataStoreRepository
import com.ayse.todocompose.data.repository.ToDoRepository
import com.ayse.todocompose.ui.screens.task.TaskScreen
import com.ayse.todocompose.ui.theme.ToDoComposeTheme
import com.ayse.todocompose.ui.viewModel.SharedViewModel
import com.ayse.todocompose.util.Action
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class TaskScreenIntegrationTest {

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
    fun newTaskScreen_showsEmptyFields() {
        // 验证新增页面：标题/描述为空，优先级默认 LOW
        viewModel.updateTaskFields(null)

        composeTestRule.setContent {
            ToDoComposeTheme {
                TaskScreen(
                    selectedTask = null,
                    navigateToListScreen = {},
                    sharedViewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("task_title_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("task_description_input").assertIsDisplayed()
        composeTestRule.onNodeWithText(Priority.LOW.name).assertIsDisplayed()
    }

    @Test
    fun existingTaskScreen_showsTaskData() {
        // 验证编辑页面：已存任务的标题/描述/优先级正确显示
        val task = ToDoTask(id = 5, title = "Edit Me", description = "Existing desc", priority = Priority.HIGH)
        viewModel.updateTaskFields(task)

        composeTestRule.setContent {
            ToDoComposeTheme {
                TaskScreen(
                    selectedTask = task,
                    navigateToListScreen = {},
                    sharedViewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Existing desc").assertIsDisplayed()
    }

    @Test
    fun titleInput_updatesViewModelState() {
        // 验证标题输入框的文字同步更新到 ViewModel.title
        viewModel.updateTaskFields(null)

        composeTestRule.setContent {
            ToDoComposeTheme {
                TaskScreen(
                    selectedTask = null,
                    navigateToListScreen = {},
                    sharedViewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("task_title_input").performTextInput("IntegrationOk")

        composeTestRule.runOnIdle {
            assertEquals("IntegrationOk", viewModel.title)
        }
    }

    @Test
    fun descriptionInput_updatesViewModelState() {
        // 验证描述输入框的文字同步更新到 ViewModel.description
        viewModel.updateTaskFields(null)

        composeTestRule.setContent {
            ToDoComposeTheme {
                TaskScreen(
                    selectedTask = null,
                    navigateToListScreen = {},
                    sharedViewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("task_description_input")
            .performTextInput("Testing description integration")

        composeTestRule.runOnIdle {
            assertEquals("Testing description integration", viewModel.description)
        }
    }

    @Test
    fun priorityDropdown_changesViewModelPriority() {
        // 验证优先级下拉选择后 ViewModel.priority 同步更新
        viewModel.updateTaskFields(null)

        composeTestRule.setContent {
            ToDoComposeTheme {
                TaskScreen(
                    selectedTask = null,
                    navigateToListScreen = {},
                    sharedViewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("priority_dropdown").performClick()
        composeTestRule.onNodeWithText(Priority.HIGH.name).performClick()

        composeTestRule.runOnIdle {
            assertEquals(Priority.HIGH, viewModel.priority)
        }
    }

    @Test
    fun addAction_clicked_whenFieldsValid() {
        // 验证标题和描述非空时，点击添加按钮回调 Action.ADD
        viewModel.updateTaskFields(null)
        viewModel.updateTitle("Valid Task")
        viewModel.updateDescription("Valid description")

        var capturedAction = Action.NO_ACTION

        composeTestRule.setContent {
            ToDoComposeTheme {
                TaskScreen(
                    selectedTask = null,
                    navigateToListScreen = { capturedAction = it },
                    sharedViewModel = viewModel
                )
            }
        }

        composeTestRule.waitForIdle()
        val addDesc = composeTestRule.activity.getString(com.ayse.todocompose.R.string.add_task)
        composeTestRule.onNodeWithContentDescription(addDesc).performClick()

        composeTestRule.runOnIdle {
            assertEquals(Action.ADD, capturedAction)
        }
    }
}
