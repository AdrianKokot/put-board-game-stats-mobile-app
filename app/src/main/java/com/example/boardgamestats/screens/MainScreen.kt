package com.example.boardgamestats.screens

import android.text.Html
import android.text.format.DateFormat
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import com.example.boardgamestats.api.queryXmlApi
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.database.Gameplay
import com.example.boardgamestats.database.PlayerWithScoreDto
import com.example.boardgamestats.ui.animations.SkeletonAnimatedColor
import com.example.boardgamestats.ui.components.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Instant


@Composable
fun MainScreen(navigateToDetails: (Int) -> Unit) {
    val navController = rememberNavController()

    Scaffold(bottomBar = {
        BottomNavigationBar(navController)
    }, topBar = {
        BoardGamesSearchBar(navigateToDetails)
    }) { padding ->
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

    navHostController.navigate(GameDetailsScreenRoute.AddNewGamePlay.route.replace("{gameId}", "224517"))
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
                }, it1, navController)
            }
        }

        composable(
            GameDetailsScreenRoute.AddNewGamePlay.route,
            arguments = listOf(navArgument("gameId") { type = NavType.IntType })
        ) {
            it.arguments?.getInt("gameId")?.let { it1 ->
                AddNewGamePlayScreen(popBackStack = {
                    navController.popBackStack()
                }, it1)
            }
        }
    }
}

data class GameplayPlayer(var name: String, var score: String)

class NewGamePlayViewModel : ViewModel() {
    private val _players = mutableStateListOf<GameplayPlayer>()
    val players: List<GameplayPlayer> = _players

    fun addPlayer(player: GameplayPlayer) {
        _players.add(player)
    }

    fun removePlayer(player: GameplayPlayer) {
        _players.remove(player)
    }

    fun updatePlayer(player: GameplayPlayer, newName: String? = null, newScore: String? = null) {
        val index = _players.indexOf(player)
        _players[index] = _players[index].copy(name = newName ?: player.name, score = newScore ?: player.score)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewGamePlayScreen(
    popBackStack: () -> Unit, gameId: Int, newGamePlayViewModel: NewGamePlayViewModel = viewModel()
) {
    val dao = BoardGameDatabase.getDatabase(LocalContext.current)
        .playerDao()

    val gameplayDao = BoardGameDatabase.getDatabase(LocalContext.current)
        .gameplayDao()

    val openDialog = remember { mutableStateOf(false) }

    var dateInstant by remember { mutableStateOf(Instant.now()) }
    val datePickerState = rememberDatePickerState(dateInstant.toEpochMilli())

    val players = newGamePlayViewModel.players

    val alreadyExistingPlayerNames = dao.getAll()
        .collectAsState(emptyList()).value
        .map { it.name }.toList()

    if (openDialog.value) {
        val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }
        DatePickerDialog(onDismissRequest = {
            openDialog.value = false
        }, confirmButton = {
            TextButton(
                onClick = {
                    openDialog.value = false
                    dateInstant = Instant.ofEpochMilli(datePickerState.selectedDateMillis!!)
                }, enabled = confirmEnabled.value
            ) {
                Text("Save")
            }
        }, dismissButton = {
            TextButton(onClick = {
                openDialog.value = false
            }) {
                Text("Cancel")
            }
        }) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("New play") }, navigationIcon = {
            IconButton(onClick = { popBackStack() }) {
                Icon(Icons.Filled.Close, contentDescription = "Close")
            }
        }, actions = {
            Button(onClick = {

                GlobalScope.launch {
                    gameplayDao.insert(
                        Gameplay(
                            boardGameId = gameId,
                            date = dateInstant.toEpochMilli()
                        ),
                        players.map {
                            PlayerWithScoreDto(
                                playerName = it.name,
                                score = it.score.toIntOrNull() ?: 0
                            )
                        },
                    )
                }
                popBackStack()
            }, modifier = Modifier.padding(end = 16.dp)) {
                Text("Save")
            }
        })
    }, content = {
        Box(modifier = Modifier.padding(it)) {


            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize()
            ) {
                TextField(interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect {
                            if (it is PressInteraction.Release) {
                                openDialog.value = true
                            }
                        }
                    }
                },
                    value = DateFormat.getDateFormat(LocalContext.current).format(dateInstant.toEpochMilli()),
                    readOnly = true,
                    onValueChange = {},
                    label = { Text("Play date") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Players"
                    )
                    Button(onClick = {
                        newGamePlayViewModel.addPlayer(GameplayPlayer("", ""))
                    }) {
                        Text("Add")
                    }
                }

                LazyColumn {
                    items(players) { player ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                label = { Text("Score") },
                                value = player.score,
                                onValueChange = {
                                    newGamePlayViewModel.updatePlayer(
                                        player, newScore = (it.toIntOrNull() ?: "").toString()
                                    )
                                },
                                modifier = Modifier.width(96.dp).padding(end = 8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            TextFieldWithDropdown(label = { Text("Name") }, value = player.name, onValueChange = {
                                newGamePlayViewModel.updatePlayer(player, newName = it)
                            }, modifier = Modifier.weight(1f).padding(end = 8.dp),
                                options = alreadyExistingPlayerNames
                            )

                            IconButton(onClick = {
                                newGamePlayViewModel.removePlayer(player)
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                }


            }
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GameDetailsScreen(popBackStack: () -> Unit, gameId: Int, navController: NavHostController) {
    val snackbarHostState = remember { SnackbarHostState() }

    val db = BoardGameDatabase.getDatabase(LocalContext.current)
    val boardGameDao = db.boardGameDao()
    val formatter = DateFormat.getDateFormat(LocalContext.current)

    val gameplayDao = db.gameplayDao()

    var boardGameDetailsJob by remember { mutableStateOf<Job?>(null) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    boardGameDao.get(gameId).collectAsState(null).value?.let { boardGame ->
        if (boardGameDetailsJob == null && !boardGame.hasDetails) {
            boardGameDetailsJob = GlobalScope.launch {
                queryXmlApi("https://www.boardgamegeek.com/xmlapi2/thing?id=$gameId").first().let {
                    boardGameDao.updateBoardGameDetails(it.id, it.thumbnail!!, it.image!!, it.description!!)
                }
            }
        }

        Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                MediumTopAppBar(scrollBehavior = scrollBehavior, title = {
                    Text(
                        boardGame.name, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }, navigationIcon = {
                    IconButton(onClick = popBackStack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }, actions = {
                    Crossfade(boardGame.inCollection) { isInCollection ->
                        PlainTooltipBox(tooltip = {
                            Text(if (isInCollection) "Remove from collection" else "Add to collection")
                        }) {
                            IconButton(onClick = {
                                GlobalScope.launch {
                                    boardGameDao.updateCollection(gameId, !isInCollection)

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

                })
            },
            floatingActionButton = {
                FloatingActionButton(onClick = {
                    navController.navigate(
                        GameDetailsScreenRoute.AddNewGamePlay.route.replace(
                            "{gameId}", gameId.toString()
                        )
                    )
                }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                if (!boardGame.hasDetails) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else {
                    val gameplays = gameplayDao.getAllForGame(gameId).collectAsState(emptyList()).value
                    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {

                        item {
                            SubcomposeAsyncImage(model = boardGame.image,
                                contentDescription = boardGame.name,
                                modifier = Modifier.fillMaxWidth().height(250.dp).padding(vertical = 16.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                                loading = {
                                    Box(Modifier.matchParentSize().background(SkeletonAnimatedColor()))
                                })

                            ExpandableText(Html.fromHtml(boardGame.description, Html.FROM_HTML_MODE_COMPACT).toString())

                        }

                        if (gameplays.isNotEmpty()) {
                            item {
                                Text("Gameplays")
                            }


                            items(gameplays) { gameplay ->
                                Text(
                                    "${formatter.format(gameplay.gameplay.date)} - ${
                                        gameplay.playerResults.sortedByDescending { it.score }
                                            .joinToString(", ") { it.playerName + " (" + it.score + ")" }
                                    }"
                                )
                            }
                        }
                    }


                }
            }
        }
    }
}

sealed class GameDetailsScreenRoute(val route: String) {
    object GameDetails : GameDetailsScreenRoute("game-details/{gameId}")
    object AddNewGamePlay : GameDetailsScreenRoute("game-details/{gameId}/add-play")
}