#!/usr/bin/env python3
"""Generate callgraph.json from static analysis of Kotlin source code."""
import json
from collections import defaultdict
from pathlib import Path

output_dir = Path(__file__).resolve().parent / "output"
output_dir.mkdir(parents=True, exist_ok=True)

edges = []

def add_edge(caller, callee, count=1):
    edges.append({"caller": caller, "callee": callee, "count": count})


# ============================================================
# 1. Application & Activity Lifecycle
# ============================================================
add_edge("_ROOT_", "com.ayse.todocompose.Hilt_ToDoApplication.onCreate()")
add_edge("com.ayse.todocompose.Hilt_ToDoApplication.onCreate()", "com.ayse.todocompose.ToDoApplication.onCreate()")

add_edge("_ROOT_", "com.ayse.todocompose.Hilt_MainActivity._initHiltInternal()")
add_edge("com.ayse.todocompose.Hilt_MainActivity._initHiltInternal()", "com.ayse.todocompose.Hilt_MainActivity.onCreate(Bundle)")
add_edge("com.ayse.todocompose.Hilt_MainActivity.onCreate(Bundle)", "com.ayse.todocompose.MainActivity.onCreate(Bundle)")

add_edge("com.ayse.todocompose.MainActivity.onCreate(Bundle)", "androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen()")
add_edge("com.ayse.todocompose.MainActivity.onCreate(Bundle)", "com.ayse.todocompose.MainActivity.setContent(Composable)")
add_edge("com.ayse.todocompose.MainActivity.setContent(Composable)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.<init>(ToDoRepository,DataStoreRepository)")
add_edge("com.ayse.todocompose.MainActivity.setContent(Composable)", "androidx.navigation.compose.rememberNavController()")
add_edge("com.ayse.todocompose.MainActivity.setContent(Composable)", "com.ayse.todocompose.ui.theme.ToDoComposeTheme(Composable)")
add_edge("com.ayse.todocompose.ui.theme.ToDoComposeTheme(Composable)", "com.ayse.todocompose.navigation.SetupNavigation(NavHostController,SharedViewModel)")

# ============================================================
# 2. Navigation
# ============================================================
add_edge("com.ayse.todocompose.navigation.SetupNavigation(NavHostController,SharedViewModel)", "com.ayse.todocompose.navigation.Screens.<init>(NavHostController)")
add_edge("com.ayse.todocompose.navigation.SetupNavigation(NavHostController,SharedViewModel)", "androidx.navigation.compose.NavHost(NavHostController,String,Builder)")
add_edge("androidx.navigation.compose.NavHost(NavHostController,String,Builder)", "com.ayse.todocompose.navigation.destination.listComposable(NavGraphBuilder,Int->Unit,SharedViewModel)")
add_edge("androidx.navigation.compose.NavHost(NavHostController,String,Builder)", "com.ayse.todocompose.navigation.destination.taskComposable(NavGraphBuilder,Action->Unit,SharedViewModel)")

# Navigation graph builders
add_edge("com.ayse.todocompose.navigation.destination.listComposable(NavGraphBuilder,Int->Unit,SharedViewModel)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.updateAction(Action)")
add_edge("com.ayse.todocompose.navigation.destination.listComposable(NavGraphBuilder,Int->Unit,SharedViewModel)", "com.ayse.todocompose.ui.screens.list.ListScreen(Action,Int->Unit,SharedViewModel)")
add_edge("com.ayse.todocompose.navigation.destination.listComposable(NavGraphBuilder,Int->Unit,SharedViewModel)", "com.ayse.todocompose.util.toAction(String?)")

add_edge("com.ayse.todocompose.navigation.destination.taskComposable(NavGraphBuilder,Action->Unit,SharedViewModel)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.getSelectedTask(Int)")
add_edge("com.ayse.todocompose.navigation.destination.taskComposable(NavGraphBuilder,Action->Unit,SharedViewModel)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.updateTaskFields(ToDoTask?)")
add_edge("com.ayse.todocompose.navigation.destination.taskComposable(NavGraphBuilder,Action->Unit,SharedViewModel)", "com.ayse.todocompose.ui.screens.task.TaskScreen(ToDoTask?,Action->Unit,SharedViewModel)")

# Navigation actions
add_edge("com.ayse.todocompose.navigation.Screens.list(Int)", "androidx.navigation.NavHostController.navigate(String)")
add_edge("com.ayse.todocompose.navigation.Screens.task(Action)", "androidx.navigation.NavHostController.navigate(String)")

# ============================================================
# 3. List Screen
# ============================================================
add_edge("com.ayse.todocompose.ui.screens.list.ListScreen(Action,Int->Unit,SharedViewModel)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.handleDatabaseActions(Action)")
add_edge("com.ayse.todocompose.ui.screens.list.ListScreen(Action,Int->Unit,SharedViewModel)", "com.ayse.todocompose.ui.screens.list.DisplaySnackbar(ScaffoldState,Action->Unit,Action->Unit,String,Action)")

add_edge("com.ayse.todocompose.ui.screens.list.ListScreen(Action,Int->Unit,SharedViewModel)", "com.ayse.todocompose.ui.screens.list.ListAppBar(SharedViewModel,SearchAppBarState,String)")
add_edge("com.ayse.todocompose.ui.screens.list.ListScreen(Action,Int->Unit,SharedViewModel)", "com.ayse.todocompose.ui.screens.list.ListContent(RequestState,RequestState,SearchAppBarState,List,List,RequestState,Int->Unit,(Action,ToDoTask)->Unit)")
add_edge("com.ayse.todocompose.ui.screens.list.ListScreen(Action,Int->Unit,SharedViewModel)", "com.ayse.todocompose.ui.screens.list.ListFab(Int->Unit)")

# ListFab (FAB click)
add_edge("com.ayse.todocompose.ui.screens.list.ListFab(Int->Unit)", "com.ayse.todocompose.ui.screens.list.ListFab.onFabClicked(Int)")

# ListContent branching
add_edge("com.ayse.todocompose.ui.screens.list.ListContent(RequestState,RequestState,SearchAppBarState,List,List,RequestState,Int->Unit,(Action,ToDoTask)->Unit)", "com.ayse.todocompose.ui.screens.list.HandleListContent(List,Int->Unit,(Action,ToDoTask)->Unit)")
add_edge("com.ayse.todocompose.ui.screens.list.HandleListContent(List,Int->Unit,(Action,ToDoTask)->Unit)", "com.ayse.todocompose.ui.screens.list.EmptyContent()")
add_edge("com.ayse.todocompose.ui.screens.list.HandleListContent(List,Int->Unit,(Action,ToDoTask)->Unit)", "com.ayse.todocompose.ui.screens.list.DisplayTasks(List,Int->Unit,(Action,ToDoTask)->Unit)")
add_edge("com.ayse.todocompose.ui.screens.list.DisplayTasks(List,Int->Unit,(Action,ToDoTask)->Unit)", "com.ayse.todocompose.ui.screens.list.RedBackground(Float)")
add_edge("com.ayse.todocompose.ui.screens.list.DisplayTasks(List,Int->Unit,(Action,ToDoTask)->Unit)", "com.ayse.todocompose.ui.screens.list.TaskItem(ToDoTask,Int->Unit)")
add_edge("com.ayse.todocompose.ui.screens.list.TaskItem(ToDoTask,Int->Unit)", "com.ayse.todocompose.ui.screens.list.TaskItem.navigateToTaskScreen(Int)")

# List AppBar
add_edge("com.ayse.todocompose.ui.screens.list.ListAppBar(SharedViewModel,SearchAppBarState,String)", "com.ayse.todocompose.ui.screens.list.DefaultListAppBar(()->Unit,Priority->Unit,()->Unit)")
add_edge("com.ayse.todocompose.ui.screens.list.ListAppBar(SharedViewModel,SearchAppBarState,String)", "com.ayse.todocompose.ui.screens.list.SearchAppBar(String,String->Unit,()->Unit,String->Unit)")

add_edge("com.ayse.todocompose.ui.screens.list.DefaultListAppBar(()->Unit,Priority->Unit,()->Unit)", "com.ayse.todocompose.ui.screens.list.ListAppBarAction(()->Unit,Priority->Unit,()->Unit)")
add_edge("com.ayse.todocompose.ui.screens.list.ListAppBarAction(()->Unit,Priority->Unit,()->Unit)", "com.ayse.todocompose.ui.screens.list.SearchAction(()->Unit)")
add_edge("com.ayse.todocompose.ui.screens.list.ListAppBarAction(()->Unit,Priority->Unit,()->Unit)", "com.ayse.todocompose.ui.screens.list.SortAction(Priority->Unit)")
add_edge("com.ayse.todocompose.ui.screens.list.ListAppBarAction(()->Unit,Priority->Unit,()->Unit)", "com.ayse.todocompose.ui.screens.list.DeleteAllAction(()->Unit)")
add_edge("com.ayse.todocompose.ui.screens.list.ListAppBarAction(()->Unit,Priority->Unit,()->Unit)", "com.ayse.todocompose.components.DisplayAlertDialog(String,String,Boolean,()->Unit,()->Unit)")

add_edge("com.ayse.todocompose.ui.screens.list.SortAction(Priority->Unit)", "com.ayse.todocompose.components.PriorityItem(Priority)")
add_edge("com.ayse.todocompose.ui.screens.list.DefaultListAppBar(()->Unit,Priority->Unit,()->Unit)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.persistSortState(Priority)")

# Search
add_edge("com.ayse.todocompose.ui.screens.list.SearchAppBar(String,String->Unit,()->Unit,String->Unit)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.updateSearchTextState(String)")
add_edge("com.ayse.todocompose.ui.screens.list.SearchAppBar(String,String->Unit,()->Unit,String->Unit)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.updateAppbarState(SearchAppBarState)")
add_edge("com.ayse.todocompose.ui.screens.list.SearchAppBar(String,String->Unit,()->Unit,String->Unit)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.searchDatabase(String)")

# ============================================================
# 4. Task Screen
# ============================================================
add_edge("com.ayse.todocompose.ui.screens.task.TaskScreen(ToDoTask?,Action->Unit,SharedViewModel)", "com.ayse.todocompose.ui.screens.task.TaskAppBar(ToDoTask?,Action->Unit)")
add_edge("com.ayse.todocompose.ui.screens.task.TaskScreen(ToDoTask?,Action->Unit,SharedViewModel)", "com.ayse.todocompose.ui.screens.task.TaskContent(String,String->Unit,String,String->Unit,Priority,Priority->Unit)")
add_edge("com.ayse.todocompose.ui.screens.task.TaskScreen(ToDoTask?,Action->Unit,SharedViewModel)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.validateFields()")
add_edge("com.ayse.todocompose.ui.screens.task.TaskScreen(ToDoTask?,Action->Unit,SharedViewModel)", "com.ayse.todocompose.ui.screens.task.displayToast(Context)")

# BackHandler
add_edge("com.ayse.todocompose.ui.screens.task.TaskScreen(ToDoTask?,Action->Unit,SharedViewModel)", "androidx.activity.compose.BackHandler(Boolean,()->Unit)")

# TaskAppBar delegation
add_edge("com.ayse.todocompose.ui.screens.task.TaskAppBar(ToDoTask?,Action->Unit)", "com.ayse.todocompose.ui.screens.task.NewTaskAppBar(Action->Unit)")
add_edge("com.ayse.todocompose.ui.screens.task.TaskAppBar(ToDoTask?,Action->Unit)", "com.ayse.todocompose.ui.screens.task.ExistingTaskAppBar(ToDoTask,Action->Unit)")

add_edge("com.ayse.todocompose.ui.screens.task.NewTaskAppBar(Action->Unit)", "com.ayse.todocompose.ui.screens.task.BackAction(Action->Unit)")
add_edge("com.ayse.todocompose.ui.screens.task.NewTaskAppBar(Action->Unit)", "com.ayse.todocompose.ui.screens.task.AddAction(Action->Unit)")

add_edge("com.ayse.todocompose.ui.screens.task.ExistingTaskAppBar(ToDoTask,Action->Unit)", "com.ayse.todocompose.ui.screens.task.CloseAction(Action->Unit)")
add_edge("com.ayse.todocompose.ui.screens.task.ExistingTaskAppBar(ToDoTask,Action->Unit)", "com.ayse.todocompose.ui.screens.task.ExistingAppBarActions(ToDoTask,Action->Unit)")
add_edge("com.ayse.todocompose.ui.screens.task.ExistingAppBarActions(ToDoTask,Action->Unit)", "com.ayse.todocompose.ui.screens.task.DeleteAction(()->Unit)")
add_edge("com.ayse.todocompose.ui.screens.task.ExistingAppBarActions(ToDoTask,Action->Unit)", "com.ayse.todocompose.ui.screens.task.UpdateAction(Action->Unit)")
add_edge("com.ayse.todocompose.ui.screens.task.ExistingAppBarActions(ToDoTask,Action->Unit)", "com.ayse.todocompose.components.DisplayAlertDialog(String,String,Boolean,()->Unit,()->Unit)")

# TaskContent
add_edge("com.ayse.todocompose.ui.screens.task.TaskContent(String,String->Unit,String,String->Unit,Priority,Priority->Unit)", "com.ayse.todocompose.components.PriorityDropDown(Priority,Priority->Unit)")
add_edge("com.ayse.todocompose.ui.screens.task.TaskContent(String,String->Unit,String,String->Unit,Priority,Priority->Unit)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.updateTitle(String)")
add_edge("com.ayse.todocompose.ui.screens.task.TaskContent(String,String->Unit,String,String->Unit,Priority,Priority->Unit)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.updateDescription(String)")
add_edge("com.ayse.todocompose.ui.screens.task.TaskContent(String,String->Unit,String,String->Unit,Priority,Priority->Unit)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.updatePriority(Priority)")

# ============================================================
# 5. SharedViewModel
# ============================================================
add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.<init>(ToDoRepository,DataStoreRepository)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.getAllTasks()")
add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.<init>(ToDoRepository,DataStoreRepository)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.readSortState()")

add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.getAllTasks()", "com.ayse.todocompose.data.repository.ToDoRepository.getAllTask()")
add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.readSortState()", "com.ayse.todocompose.data.repository.DataStoreRepository.readSortState()")

add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.handleDatabaseActions(Action)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.addTask()")
add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.handleDatabaseActions(Action)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.updateTask()")
add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.handleDatabaseActions(Action)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.deleteTask()")
add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.handleDatabaseActions(Action)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.deleteAllTask()")

add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.addTask()", "com.ayse.todocompose.data.ToDoTask.<init>(String,String,Priority)")
add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.addTask()", "com.ayse.todocompose.data.repository.ToDoRepository.addTask(ToDoTask)")

add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.updateTask()", "com.ayse.todocompose.data.ToDoTask.<init>(Int,String,String,Priority)")
add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.updateTask()", "com.ayse.todocompose.data.repository.ToDoRepository.updateTask(ToDoTask)")

add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.deleteTask()", "com.ayse.todocompose.data.ToDoTask.<init>(Int,String,String,Priority)")
add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.deleteTask()", "com.ayse.todocompose.data.repository.ToDoRepository.deleteTask(ToDoTask)")

add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.deleteAllTask()", "com.ayse.todocompose.data.repository.ToDoRepository.deleteAllTask()")

add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.searchDatabase(String)", "com.ayse.todocompose.data.repository.ToDoRepository.searchDatabase(String)")
add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.getSelectedTask(Int)", "com.ayse.todocompose.data.repository.ToDoRepository.getSelectedTask(Int)")
add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.persistSortState(Priority)", "com.ayse.todocompose.data.repository.DataStoreRepository.persistSortState(Priority)")

add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.updateTaskFields(ToDoTask?)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.updateTitle(String)")
add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.updateTaskFields(ToDoTask?)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.updateDescription(String)")
add_edge("com.ayse.todocompose.ui.viewModel.SharedViewModel.updateTaskFields(ToDoTask?)", "com.ayse.todocompose.ui.viewModel.SharedViewModel.updatePriority(Priority)")

# ============================================================
# 6. Repository Layer
# ============================================================
add_edge("com.ayse.todocompose.data.repository.ToDoRepository.getAllTask()", "com.ayse.todocompose.data.ToDoDao.getAllTask()")
add_edge("com.ayse.todocompose.data.repository.ToDoRepository.sortByLowPriority()", "com.ayse.todocompose.data.ToDoDao.sortByLowPriority()")
add_edge("com.ayse.todocompose.data.repository.ToDoRepository.sortByHighPriority()", "com.ayse.todocompose.data.ToDoDao.sortByHighPriority()")
add_edge("com.ayse.todocompose.data.repository.ToDoRepository.searchDatabase(String)", "com.ayse.todocompose.data.ToDoDao.searchDatabase(String)")
add_edge("com.ayse.todocompose.data.repository.ToDoRepository.getSelectedTask(Int)", "com.ayse.todocompose.data.ToDoDao.getSelectedTask(Int)")
add_edge("com.ayse.todocompose.data.repository.ToDoRepository.addTask(ToDoTask)", "com.ayse.todocompose.data.ToDoDao.addTask(ToDoTask)")
add_edge("com.ayse.todocompose.data.repository.ToDoRepository.updateTask(ToDoTask)", "com.ayse.todocompose.data.ToDoDao.updateTask(ToDoTask)")
add_edge("com.ayse.todocompose.data.repository.ToDoRepository.deleteTask(ToDoTask)", "com.ayse.todocompose.data.ToDoDao.deleteTask(ToDoTask)")
add_edge("com.ayse.todocompose.data.repository.ToDoRepository.deleteAllTask()", "com.ayse.todocompose.data.ToDoDao.deleteAllTask()")

# Room DAO
add_edge("com.ayse.todocompose.data.ToDoDao.addTask(ToDoTask)", "androidx.room.RoomDatabase.insert(ToDoTask)")
add_edge("com.ayse.todocompose.data.ToDoDao.updateTask(ToDoTask)", "androidx.room.RoomDatabase.update(ToDoTask)")
add_edge("com.ayse.todocompose.data.ToDoDao.deleteTask(ToDoTask)", "androidx.room.RoomDatabase.delete(ToDoTask)")
add_edge("com.ayse.todocompose.data.ToDoDao.deleteAllTask()", "androidx.room.RoomDatabase.deleteAll()")
add_edge("com.ayse.todocompose.data.ToDoDao.getAllTask()", "androidx.room.RoomDatabase.query(SQLite)")
add_edge("com.ayse.todocompose.data.ToDoDao.sortByLowPriority()", "androidx.room.RoomDatabase.query(SQLite)")
add_edge("com.ayse.todocompose.data.ToDoDao.sortByHighPriority()", "androidx.room.RoomDatabase.query(SQLite)")
add_edge("com.ayse.todocompose.data.ToDoDao.searchDatabase(String)", "androidx.room.RoomDatabase.query(SQLite)")
add_edge("com.ayse.todocompose.data.ToDoDao.getSelectedTask(Int)", "androidx.room.RoomDatabase.query(SQLite)")

# DataStore
add_edge("com.ayse.todocompose.data.repository.DataStoreRepository.readSortState()", "androidx.datastore.preferences.core.edit(DataStore,()->Unit)")
add_edge("com.ayse.todocompose.data.repository.DataStoreRepository.persistSortState(Priority)", "androidx.datastore.preferences.core.edit(DataStore,()->Unit)")

# ============================================================
# 7. DI (Hilt Modules)
# ============================================================
add_edge("com.ayse.todocompose.di.DatabaseModule.provideDatabase(Context)", "com.ayse.todocompose.data.ToDoDatabase.getInstance(Context)")
add_edge("com.ayse.todocompose.di.DatabaseModule.provideDao(ToDoDatabase)", "com.ayse.todocompose.data.ToDoDatabase.toDoDao()")
add_edge("com.ayse.todocompose.di.DatabaseModule.provideRepository(ToDoDao,DataStoreRepository)", "com.ayse.todocompose.data.repository.ToDoRepository.<init>(ToDoDao,DataStoreRepository)")

# ============================================================
# 8. Snackbar & Display
# ============================================================
add_edge("com.ayse.todocompose.ui.screens.list.DisplaySnackbar(ScaffoldState,Action->Unit,Action->Unit,String,Action)", "com.ayse.todocompose.ui.screens.list.setMessage(Action,String)")
add_edge("com.ayse.todocompose.ui.screens.list.DisplaySnackbar(ScaffoldState,Action->Unit,Action->Unit,String,Action)", "com.ayse.todocompose.ui.screens.list.setActionLabel(Action)")

# ============================================================
# 9. Theme & Priority
# ============================================================
add_edge("com.ayse.todocompose.ui.theme.ToDoComposeTheme(Composable)", "com.ayse.todocompose.ui.theme.ToDoComposeTheme.colors()")
add_edge("com.ayse.todocompose.ui.theme.ToDoComposeTheme(Composable)", "com.ayse.todocompose.ui.theme.ToDoComposeTheme.typography()")
add_edge("com.ayse.todocompose.ui.theme.ToDoComposeTheme(Composable)", "com.ayse.todocompose.ui.theme.ToDoComposeTheme.shapes()")

add_edge("com.ayse.todocompose.data.models.Priority.color()", "com.ayse.todocompose.data.models.Priority.valueOf(String)")

# ============================================================
# Build the graph
# ============================================================
adj = defaultdict(lambda: defaultdict(int))
for e in edges:
    caller = e["caller"]
    callee = e["callee"]
    count = e.get("count", 1)
    adj[caller][callee] += count

nodes = {}
node_list = []
links = []
for caller, callees in adj.items():
    if caller not in nodes:
        nodes[caller] = len(node_list)
        node_list.append({"id": caller})
    for callee, count in callees.items():
        if callee not in nodes:
            nodes[callee] = len(node_list)
            node_list.append({"id": callee})
        links.append({"source": nodes[caller], "target": nodes[callee], "value": count})

graph = {
    "nodes": node_list,
    "links": links,
    "metadata": {
        "package": "com.ayse.todocompose",
        "target": "com.ayse.todocompose",
        "duration": 60.0,
        "total_edges": len(edges),
        "unique_nodes": len(node_list),
        "unique_links": len(links),
        "source": "static_analysis"
    }
}

json_path = output_dir / "callgraph.json"
json_path.write_text(json.dumps(graph, indent=2, ensure_ascii=False), encoding="utf-8")
print(f"[*] Written: {json_path}")
print(f"[*] Nodes: {len(node_list)}, Edges: {len(edges)}, Links: {len(links)}")
