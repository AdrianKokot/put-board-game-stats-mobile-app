package com.example.boardgamestats.screens

import android.text.Html
import android.text.format.DateFormat
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdded
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
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.example.boardgamestats.api.queryXmlApi
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.navigation.GameNavigation
import com.example.boardgamestats.ui.animations.SkeletonAnimatedColor
import com.example.boardgamestats.ui.components.ExpandableText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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
                    navController.navigate(GameNavigation.NewGameplayScreen(gameId))
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
                                Text("${formatter.format(gameplay.gameplay.date)} - ${
                                    gameplay.playerResults.sortedByDescending { it.score }
                                        .joinToString(", ") { it.playerName + " (" + it.score + ")" }
                                }")
                            }
                        }
                    }


                }
            }
        }
    }
}
