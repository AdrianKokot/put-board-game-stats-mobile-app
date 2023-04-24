package com.example.boardgamestats.screens

import android.content.Context
import android.text.Html
import android.text.format.DateFormat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.boardgamestats.ui.components.GameplayStatisticsOverview
import com.example.boardgamestats.ui.components.SectionTitle
import com.example.boardgamestats.utils.NetworkManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GameDetailsScreen(
    popBackStack: () -> Unit,
    gameId: Int,
    navController: NavHostController,
    context: Context = LocalContext.current
) {
    val db = BoardGameDatabase.getDatabase(context)
    val boardGameDao = db.boardGameDao()
    val formatter = DateFormat.getDateFormat(context)

    val gameplayDao = db.gameplayDao()

    var boardGameDetailsJob by remember { mutableStateOf<Job?>(null) }

    val lazyListState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()
    val isScrollable by remember { derivedStateOf { lazyListState.canScrollForward || topAppBarState.collapsedFraction > 0 } }

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState, canScroll = { isScrollable })

    val scope = rememberCoroutineScope()

    boardGameDao.get(gameId).collectAsState(null).value?.let { boardGame ->
        if (boardGameDetailsJob == null && !boardGame.hasDetails) {
            if (NetworkManager.isNetworkAvailable(context)) {

                boardGameDetailsJob = GlobalScope.launch {
                    queryXmlApi("https://www.boardgamegeek.com/xmlapi2/thing?id=$gameId").first().let {
                        boardGameDao.updateDetails(it.id, it.thumbnail!!, it.image!!, it.description!!, it.isExpansion)
                    }
                }
            }
        }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                MediumTopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(
                            boardGame.name, maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = popBackStack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = null)
                        }
                    })
            }
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                if (!boardGame.hasDetails) {
                    if (NetworkManager.isNetworkAvailable(context)) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    } else {
                        Text(
                            "No internet connection",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                } else {
                    val plays = gameplayDao.getAllForGame(gameId).collectAsState(emptyList()).value
                    val stats = boardGameDao.getBoardGamePlaysStats(gameId).collectAsState(null).value
                    LazyColumn(state = lazyListState) {
                        item {
                            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                SubcomposeAsyncImage(model = boardGame.image,
                                    contentDescription = boardGame.name,
                                    modifier = Modifier.fillMaxWidth().height(250.dp).padding(bottom = 16.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop,
                                    loading = {
                                        Box(Modifier.matchParentSize().background(SkeletonAnimatedColor()))
                                    })

                                ExpandableText(
                                    text = Html.fromHtml(boardGame.description, Html.FROM_HTML_MODE_COMPACT).toString(),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )

                            }

                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            boardGameDao.updateCollection(gameId, !boardGame.inCollection)
                                        }
                                    }
                                ) {
                                    Text(if (boardGame.inCollection) "Remove from collection" else "Add to collection")
                                }

                                FilledTonalButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        navController.navigate(GameNavigation.newGameplayScreen(gameId))
                                    }
                                ) {
                                    Text("Add play")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (plays.isNotEmpty()) {
                            item {
                                SectionTitle(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    title = "Statistics",
                                    onArrowClick = {
                                        navController.navigate(GameNavigation.gameplayStatsScreen(gameId))
                                    }
                                )
                                GameplayStatisticsOverview(stats)
                                Spacer(modifier = Modifier.height(32.dp))
                            }

                            stickyHeader {
                                SectionTitle(
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 8.dp
                                    ), title = "Recent plays", onArrowClick = {
                                        navController.navigate(GameNavigation.gameplayListScreen(gameId))
                                    })
                            }

                            items(plays.take(2)) { gameplay ->
                                ListItem(
                                    headlineContent = { Text(formatter.format(gameplay.gameplay.date)) },
                                    supportingContent = {
                                        Text(
                                            gameplay.playerResults.sortedByDescending { it.score }
                                                .joinToString(", ") { it.playerName + " (" + it.score + ")" }
                                        )
                                    },
                                    modifier = Modifier.clickable {
                                        navController.navigate(GameNavigation.gameplayDetailsScreen(gameplay.gameplay.id))
                                    }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

