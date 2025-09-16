package com.milkit.app.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.milkit.app.presentation.main.calendar.CalendarScreen
import com.milkit.app.presentation.main.home.HomeScreen
import com.milkit.app.presentation.main.settings.SettingsScreen
import com.milkit.app.presentation.main.share.ShareScreen
import com.milkit.app.presentation.main.stats.StatsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentDestination?.route) {
                            MainScreen.Home.route -> "MilkIt"
                            MainScreen.Calendar.route -> "Calendar"
                            MainScreen.Stats.route -> "Statistics"
                            MainScreen.Share.route -> "Share Data"
                            MainScreen.Settings.route -> "Settings"
                            else -> "MilkIt"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainScreen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MainScreen.Home.route) {
                HomeScreen()
            }
            composable(MainScreen.Calendar.route) {
                CalendarScreen()
            }
            composable(MainScreen.Stats.route) {
                StatsScreen()
            }
            composable(MainScreen.Share.route) {
                ShareScreen()
            }
            composable(MainScreen.Settings.route) {
                SettingsScreen(onLogout = onLogout)
            }
        }
    }
}

sealed class MainScreen(val route: String, val label: String, val icon: ImageVector) {
    object Home : MainScreen("home", "Home", Icons.Default.Home)
    object Calendar : MainScreen("calendar", "Calendar", Icons.Default.CalendarMonth)
    object Stats : MainScreen("stats", "Stats", Icons.Default.Analytics)
    object Share : MainScreen("share", "Share", Icons.Default.Share)
    object Settings : MainScreen("settings", "Settings", Icons.Default.Settings)
}

private val bottomNavItems = listOf(
    MainScreen.Home,
    MainScreen.Calendar,
    MainScreen.Stats,
    MainScreen.Share,
    MainScreen.Settings
)
