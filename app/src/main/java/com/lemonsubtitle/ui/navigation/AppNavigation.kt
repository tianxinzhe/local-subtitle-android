package com.lemonsubtitle.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Database
import androidx.compose.material.icons.filled.MovieEdit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material.icons.outlined.Database
import androidx.compose.material.icons.outlined.MovieEdit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Workspaces
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.lemonsubtitle.ui.screens.ModelManagerScreen
import com.lemonsubtitle.ui.screens.SettingsScreen
import com.lemonsubtitle.ui.screens.StudioScreen
import com.lemonsubtitle.ui.screens.SubtitleEditScreen

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val navItems = listOf(
    BottomNavItem(Screen.Studio, "Studio", Icons.Filled.Workspaces, Icons.Outlined.Workspaces),
    BottomNavItem(Screen.SubtitleEdit, "编辑", Icons.Filled.MovieEdit, Icons.Outlined.MovieEdit),
    BottomNavItem(Screen.ModelManager, "模型", Icons.Filled.Database, Icons.Outlined.Database),
    BottomNavItem(Screen.Settings, "设置", Icons.Filled.Settings, Icons.Outlined.Settings)
)

@Composable
fun LemonSubtitleApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                navItems.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Studio.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Studio.route) { StudioScreen() }
            composable(Screen.SubtitleEdit.route) { SubtitleEditScreen() }
            composable(Screen.ModelManager.route) { ModelManagerScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}
