package com.example.boardgamestats.navigation

import androidx.navigation.*
import androidx.navigation.compose.composable
import com.example.boardgamestats.screens.*

object GameNavigation {
    const val RootRoute = "GameNavigation_RootRoute"
    const val DetailsScreen = "Game_Navigation_DetailsScreen/{gameId}"
    const val NewGameplayScreen = "Game_Navigation_NewGameplayScreen/{gameId}"
    const val GameplayDetailsScreen = "Game_Navigation_GameplayDetailsScreen/{gameplayId}"
    const val GameplayListScreen = "Game_Navigation_GameplayListScreen/{gameId}"
    const val GameplayStatsScreen = "Game_Navigation_GameplayStatsScreen/{gameId}"

    fun detailsScreen(gameId: Int) = DetailsScreen.replace("{gameId}", gameId.toString())
    fun newGameplayScreen(gameId: Int) = NewGameplayScreen.replace("{gameId}", gameId.toString())
    fun gameplayDetailsScreen(gameplayId: Int) = GameplayDetailsScreen.replace("{gameplayId}", gameplayId.toString())
    fun gameplayListScreen(gameId: Int) = GameplayListScreen.replace("{gameId}", gameId.toString())
    fun gameplayStatsScreen(gameId: Int) = GameplayStatsScreen.replace("{gameId}", gameId.toString())
}

fun NavGraphBuilder.GameNavigationGraph(navController: NavHostController) {
    navigation(route = GameNavigation.RootRoute, startDestination = GameNavigation.DetailsScreen) {
        composable(
            GameNavigation.DetailsScreen,
            arguments = listOf(navArgument("gameId") { type = NavType.IntType })
        ) {
            it.arguments?.getInt("gameId")?.let { gameId ->
                GameDetailsScreen(popBackStack = {
                    navController.popBackStack()
                }, gameId, navController)
            }
        }

        composable(
            GameNavigation.NewGameplayScreen,
            arguments = listOf(navArgument("gameId") { type = NavType.IntType })
        ) {
            it.arguments?.getInt("gameId")?.let { gameId ->
                NewGameplayScreen(popBackStack = {
                    navController.popBackStack()
                }, gameId = gameId)
            }
        }

        composable(
            GameNavigation.GameplayDetailsScreen,
            arguments = listOf(navArgument("gameplayId") { type = NavType.IntType })
        ) {
            it.arguments?.getInt("gameplayId")?.let { gameplayId ->
                GameplayDetailsScreen(gameplayId, navController)
            }
        }

        composable(
            GameNavigation.GameplayListScreen,
            arguments = listOf(navArgument("gameId") { type = NavType.IntType })
        ) {
            it.arguments?.getInt("gameId")?.let { gameplayId ->
                GameplayListScreen(gameplayId, navController)
            }
        }

        composable(
            GameNavigation.GameplayStatsScreen,
            arguments = listOf(navArgument("gameId") { type = NavType.IntType })
        ) {
            it.arguments?.getInt("gameId")?.let { gameId ->
                GameplayStatsScreen(gameId = gameId, navController = navController)
            }
        }
    }
}

