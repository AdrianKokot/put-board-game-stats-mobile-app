package com.example.boardgamestats.navigation

import androidx.navigation.*
import androidx.navigation.compose.composable
import com.example.boardgamestats.screens.GameDetailsScreen
import com.example.boardgamestats.screens.NewGameplayScreen

object GameNavigation {
    const val RootRoute = "GameNavigation_RootRoute"
    const val DetailsScreen = "Game_Navigation_DetailsScreen/{gameId}"
    const val NewGameplayScreen = "Game_Navigation_NewGameplayScreen/{gameId}"

    fun DetailsScreen(gameId: Int) = DetailsScreen.replace("{gameId}", gameId.toString())
    fun NewGameplayScreen(gameId: Int) = NewGameplayScreen.replace("{gameId}", gameId.toString())
}

fun NavGraphBuilder.GameNavigationGraph(navController: NavHostController) {
    navigation(route = GameNavigation.RootRoute, startDestination = GameNavigation.DetailsScreen) {
        composable(
            GameNavigation.DetailsScreen,
            arguments = listOf(navArgument("gameId") { type = NavType.IntType })
        ) {
            it.arguments?.getInt("gameId")?.let { it1 ->
                GameDetailsScreen(popBackStack = {
                    navController.popBackStack()
                }, it1, navController)
            }
        }

        composable(
            GameNavigation.NewGameplayScreen,
            arguments = listOf(navArgument("gameId") { type = NavType.IntType })
        ) {
            it.arguments?.getInt("gameId")?.let { it1 ->
                NewGameplayScreen(popBackStack = {
                    navController.popBackStack()
                }, it1)
            }
        }
    }
}

