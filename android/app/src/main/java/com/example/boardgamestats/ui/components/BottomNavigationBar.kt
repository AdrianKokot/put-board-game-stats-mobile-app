package com.example.boardgamestats.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.boardgamestats.navigation.MainNavigation

sealed class BottomBarScreen(
    val route: String, val title: String, val icon: ImageVector, val selectedIcon: ImageVector
) {
    object Collection :
        BottomBarScreen(MainNavigation.GameListScreen, "Collection", Icons.Outlined.Widgets, Icons.Filled.Widgets)

    object PlayedGames :
        BottomBarScreen(MainNavigation.GameplayListScreen, "Played Games", Icons.Outlined.Casino, Icons.Filled.Casino)

    object Auth : BottomBarScreen(MainNavigation.AuthScreen, "Auth", Icons.Outlined.Casino, Icons.Filled.Casino)
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val screens = listOf(BottomBarScreen.Collection, BottomBarScreen.PlayedGames, BottomBarScreen.Auth)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        screens.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any {
                it.route == screen.route
            } == true

            NavigationBarItem(
                icon = {
                    Crossfade(targetState = selected) { selected ->
                        Icon(
                            imageVector = if (selected) screen.selectedIcon else screen.icon, contentDescription = null
                        )
                    }
                },
                label = { Text(screen.title) }, selected = selected, onClick = {
                    navController.navigate(screen.route)
                }
            )
        }
    }
}