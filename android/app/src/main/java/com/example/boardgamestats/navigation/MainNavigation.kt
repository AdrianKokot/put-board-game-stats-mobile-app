package com.example.boardgamestats.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.boardgamestats.screens.CollectionScreen
import com.example.boardgamestats.screens.MainScreen
import com.example.boardgamestats.screens.PlayedGamesScreen
import com.example.boardgamestats.screens.UserSettingsScreen

object MainNavigation {
    const val RootRoute = "MainNavigation_RootRoute"
    const val MainScreen = "MainNavigation_MainScreen"
    const val GameListScreen = "MainNavigation_GameListScreen"
    const val GameplayListScreen = "MainNavigation_GameplayListScreen"
    const val UserSettingsScreen = "MainNavigation_UserSettingsScreen"
}

fun NavGraphBuilder.MainNavigationGraph(navController: NavHostController) {
    navigation(route = MainNavigation.RootRoute, startDestination = MainNavigation.MainScreen) {
        composable(
            MainNavigation.MainScreen
        ) {
            MainScreen(navController)
        }
        composable(MainNavigation.UserSettingsScreen) {
            UserSettingsScreen(navController = navController)
        }
    }
}

@Composable
fun BottomNavigationGraph(bottomNavigationController: NavHostController, rootNavController: NavController) {
    NavHost(
        navController = bottomNavigationController,
        startDestination = MainNavigation.GameListScreen
    ) {
        composable(MainNavigation.GameListScreen) {
            CollectionScreen {
                rootNavController.navigate(GameNavigation.detailsScreen(it))
            }
        }
        composable(MainNavigation.GameplayListScreen) {
            PlayedGamesScreen {
                rootNavController.navigate(GameNavigation.gameplayDetailsScreen(it))
            }
        }
    }
}