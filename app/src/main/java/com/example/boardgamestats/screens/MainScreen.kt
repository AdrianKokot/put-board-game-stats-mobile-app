package com.example.boardgamestats.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.ui.components.BoardGamesSearchBar
import com.example.boardgamestats.ui.components.BottomNavigationBar
import com.example.boardgamestats.ui.components.BottomNavigationGraph

@Composable
fun MainScreen(navigateToDetails: (Int) -> Unit) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        },
        topBar = {
            BoardGamesSearchBar()
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            BottomNavigationGraph(navController, navigateToDetails)
        }
    }
}

@Composable
fun RootNavigationGraph(navHostController: NavHostController) {
    NavHost(navHostController, route = NavigationGraph.Root, startDestination = NavigationGraph.Main) {
        detailsNavGraph(navHostController)
        composable(route = NavigationGraph.Main) {
            MainScreen {
                navHostController.navigate(GameDetailsScreenRoute.GameDetails.route.replace("{gameId}", it.toString()))
            }
        }
    }
}

object NavigationGraph {
    const val Root = "root_graph"
    const val Main = "main_graph"
    const val GameDetails = "game_details_graph"
}

fun NavGraphBuilder.detailsNavGraph(navController: NavHostController) {
    navigation(route = NavigationGraph.GameDetails, startDestination = GameDetailsScreenRoute.GameDetails.route) {
        composable(
            GameDetailsScreenRoute.GameDetails.route,
            arguments = listOf(navArgument("gameId") { type = NavType.IntType })
        ) {
            GameDetailsScreen(popBackStack = {
                navController.popBackStack()
            }, it.arguments?.getInt("gameId"))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailsScreen(popBackStack: () -> Unit, gameId: Int?) {
    if (gameId == null) return

    BoardGameDatabase.getDatabase(LocalContext.current).boardGameDao().get(gameId)
        .collectAsState(null).value?.let { boardGame ->

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            boardGame.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = popBackStack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                )
            }
        ) { padding ->
            Column(Modifier.padding(padding).fillMaxSize()) {
                AsyncImage(
                    model = boardGame.image,
                    contentDescription = boardGame.name,
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                )
            }
        }
    }
}

sealed class GameDetailsScreenRoute(val route: String) {
    object GameDetails : GameDetailsScreenRoute("game-details/{gameId}")
}