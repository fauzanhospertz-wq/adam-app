package com.adam.fitness.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.adam.fitness.ADAMApplication
import com.adam.fitness.data.ActivityType
import com.adam.fitness.data.UnitSystem
import com.adam.fitness.ui.activeworkout.ActiveWorkoutScreen
import com.adam.fitness.ui.activeworkout.ActiveWorkoutViewModel
import com.adam.fitness.ui.activitystart.StartActivityScreen
import com.adam.fitness.ui.detail.WorkoutDetailScreen
import com.adam.fitness.ui.history.HistoryScreen
import com.adam.fitness.ui.history.HistoryViewModel
import com.adam.fitness.ui.home.HomeScreen
import com.adam.fitness.ui.home.HomeViewModel
import com.adam.fitness.ui.settings.SettingsScreen
import com.adam.fitness.ui.settings.SettingsViewModel
import com.adam.fitness.ui.stats.StatsScreen
import com.adam.fitness.ui.stats.StatsViewModel
import com.adam.fitness.ui.summary.WorkoutSummaryScreen

private object Routes {
    const val HOME = "home"
    const val START_ACTIVITY = "start_activity"
    const val ACTIVE_WORKOUT = "active_workout/{type}"
    const val SUMMARY = "summary/{id}"
    const val HISTORY = "history"
    const val DETAIL = "detail/{id}"
    const val STATS = "stats"
    const val SETTINGS = "settings"

    fun activeWorkout(type: ActivityType) = "active_workout/${type.name}"
    fun summary(id: Long) = "summary/$id"
    fun detail(id: Long) = "detail/$id"
}

private data class BottomTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomTabs = listOf(
    BottomTab(Routes.HOME, "Home", Icons.Default.Home),
    BottomTab(Routes.HISTORY, "History", Icons.Default.History),
    BottomTab(Routes.STATS, "Stats", Icons.Default.BarChart),
    BottomTab(Routes.SETTINGS, "Settings", Icons.Default.Settings)
)

@Composable
fun AdamNavHost(app: ADAMApplication) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomTabs.map { it.route }

    val units by app.settingsRepository.unitSystem.collectAsState(initial = UnitSystem.KM)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomTabs.forEach { tab ->
                        val selected = backStackEntry?.destination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                val vm: HomeViewModel = viewModel(factory = viewModelFactory { HomeViewModel(app.database.workoutDao()) })
                HomeScreen(
                    viewModel = vm,
                    units = units,
                    onStartActivity = { navController.navigate(Routes.START_ACTIVITY) },
                    onOpenWorkout = { id -> navController.navigate(Routes.detail(id)) }
                )
            }

            composable(Routes.START_ACTIVITY) {
                StartActivityScreen(
                    onPick = { type -> navController.navigate(Routes.activeWorkout(type)) { popUpTo(Routes.HOME) } },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                Routes.ACTIVE_WORKOUT,
                arguments = listOf(navArgument("type") { type = NavType.StringType })
            ) { backEntry ->
                val typeName = backEntry.arguments?.getString("type") ?: ActivityType.RUN.name
                val vm: ActiveWorkoutViewModel = viewModel(
                    factory = viewModelFactory {
                        ActiveWorkoutViewModel(app, app.database.workoutDao(), app.settingsRepository)
                    }
                )
                ActiveWorkoutScreen(
                    activityType = ActivityType.valueOf(typeName),
                    units = units,
                    viewModel = vm,
                    onFinished = { id ->
                        navController.navigate(Routes.summary(id)) {
                            popUpTo(Routes.HOME)
                        }
                    },
                    onCancelNoPermission = { navController.popBackStack(Routes.HOME, inclusive = false) }
                )
            }

            composable(
                Routes.SUMMARY,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backEntry ->
                val id = backEntry.arguments?.getLong("id") ?: -1L
                WorkoutSummaryScreen(
                    workoutId = id,
                    dao = app.database.workoutDao(),
                    units = units,
                    onDone = { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) { inclusive = true } } }
                )
            }

            composable(Routes.HISTORY) {
                val vm: HistoryViewModel = viewModel(factory = viewModelFactory { HistoryViewModel(app.database.workoutDao()) })
                HistoryScreen(viewModel = vm, units = units, onOpen = { id -> navController.navigate(Routes.detail(id)) })
            }

            composable(
                Routes.DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backEntry ->
                val id = backEntry.arguments?.getLong("id") ?: -1L
                WorkoutDetailScreen(workoutId = id, dao = app.database.workoutDao(), units = units, onBack = { navController.popBackStack() })
            }

            composable(Routes.STATS) {
                val vm: StatsViewModel = viewModel(factory = viewModelFactory { StatsViewModel(app.database.workoutDao()) })
                StatsScreen(viewModel = vm, units = units)
            }

            composable(Routes.SETTINGS) {
                val vm: SettingsViewModel = viewModel(factory = viewModelFactory { SettingsViewModel(app.settingsRepository) })
                SettingsScreen(viewModel = vm, dao = app.database.workoutDao())
            }
        }
    }
}

private inline fun <VM : androidx.lifecycle.ViewModel> viewModelFactory(crossinline build: () -> VM) =
    object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T = build() as T
    }
