package com.example.boardgamestats.screens

import android.text.Html
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import com.example.boardgamestats.api.queryXmlApi
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.ui.animations.SkeletonAnimatedColor
import com.example.boardgamestats.ui.components.BoardGamesSearchBar
import com.example.boardgamestats.ui.components.BottomNavigationBar
import com.example.boardgamestats.ui.components.BottomNavigationGraph
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun MainScreen(navigateToDetails: (Int) -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        },
        topBar = {
            BoardGamesSearchBar(navigateToDetails)
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
            it.arguments?.getInt("gameId")?.let { it1 ->
                GameDetailsScreen(popBackStack = {
                    navController.popBackStack()
                }, it1)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailsScreen(popBackStack: () -> Unit, gameId: Int) {
    val snackbarHostState = remember { SnackbarHostState() }

    val dao = BoardGameDatabase.getDatabase(LocalContext.current)
        .boardGameDao()
    var boardGameDetailsJob by remember { mutableStateOf<Job?>(null) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    dao.get(gameId).collectAsState(null).value?.let { boardGame ->
        if (boardGameDetailsJob == null && !boardGame.hasDetails) {
            boardGameDetailsJob = GlobalScope.launch {
                queryXmlApi("https://www.boardgamegeek.com/xmlapi2/thing?type=boardgame&id=$gameId").first()
                    .let { dao.updateBoardGameDetails(it.id, it.thumbnail!!, it.image!!, it.description!!) }
            }
        }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                MediumTopAppBar(
                    scrollBehavior = scrollBehavior,
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
                    },
                    actions = {
                        Crossfade(boardGame.inCollection) { isInCollection ->
                            PlainTooltipBox(
                                tooltip = {
                                    Text(if (isInCollection) "Remove from collection" else "Add to collection")
                                }) {
                                IconButton(onClick = {
                                    GlobalScope.launch {
                                        dao.updateCollection(gameId, !isInCollection)

                                        snackbarHostState.showSnackbar(
                                            message = if (isInCollection) "Removed from collection" else "Added to collection",
                                            duration = SnackbarDuration.Short,
                                            withDismissAction = true
                                        )
                                    }
                                }, modifier = Modifier.tooltipAnchor()) {
                                    if (isInCollection) {
                                        Icon(Icons.Filled.BookmarkAdded, contentDescription = "Remove from collection")
                                    } else {
                                        Icon(Icons.Outlined.BookmarkAdd, contentDescription = "Add to collection")
                                    }
                                }
                            }
                        }

                    }
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                if (!boardGame.hasDetails) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        SubcomposeAsyncImage(
                            model = boardGame.image,
                            contentDescription = boardGame.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .height(250.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(Modifier.matchParentSize().background(SkeletonAnimatedColor()))
                            }
                        )
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                        Text(
                            text = Html.fromHtml(boardGame.description, Html.FROM_HTML_MODE_COMPACT).toString(),
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

sealed class GameDetailsScreenRoute(val route: String) {
    object GameDetails : GameDetailsScreenRoute("game-details/{gameId}")
}