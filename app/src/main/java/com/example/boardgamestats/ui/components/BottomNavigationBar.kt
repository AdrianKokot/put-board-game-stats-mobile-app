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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.boardgamestats.screens.CollectionScreen
import com.example.boardgamestats.screens.NavigationGraph
import com.example.boardgamestats.screens.PlayedGamesScreen

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Collection : BottomBarScreen("collection", "Collection", Icons.Outlined.Widgets, Icons.Filled.Widgets)
    object PlayedGames : BottomBarScreen("played-games", "Played Games", Icons.Outlined.Casino, Icons.Filled.Casino)
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val screens = listOf(
        BottomBarScreen.Collection,
        BottomBarScreen.PlayedGames
    )

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
                            imageVector = if (selected) screen.selectedIcon else screen.icon,
                            contentDescription = null
                        )
                    }
                },
                label = { Text(screen.title) },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route)
                }
            )
        }
    }
}

@Composable
fun BottomNavigationGraph(navController: NavHostController, navigateToDetails: (Int) -> Unit) {
    NavHost(
        navController = navController,
        route = NavigationGraph.Main,
        startDestination = BottomBarScreen.Collection.route
    ) {
        composable(BottomBarScreen.Collection.route) {
            CollectionScreen(navigateToDetails)
        }
        composable(BottomBarScreen.PlayedGames.route) {
            PlayedGamesScreen()
        }
    }
}