package com.example.boardgamestats.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.boardgamestats.screens.*

object MainNavigation {
    const val RootRoute = "MainNavigation_RootRoute"
    const val MainScreen = "MainNavigation_MainScreen"
    const val GameListScreen = "MainNavigation_GameListScreen"
    const val GameplayListScreen = "MainNavigation_GameplayListScreen"
}

fun NavGraphBuilder.MainNavigationGraph(navController: NavHostController) {
    navigation(route = MainNavigation.RootRoute, startDestination = MainNavigation.MainScreen) {
        composable(
            MainNavigation.MainScreen
        ) {
            MainScreen {
                navController.navigate(GameNavigation.DetailsScreen(it))
            }
        }
    }
}

@Composable
fun BottomNavigationGraph(navController: NavHostController, navigateToDetails: (Int) -> Unit) {
    NavHost(
        navController = navController,
        startDestination = MainNavigation.GameListScreen
    ) {
        composable(MainNavigation.GameListScreen) {
            CollectionScreen(navigateToDetails)
        }
        composable(MainNavigation.GameplayListScreen) {
            PlayedGamesScreen()
        }
    }
}