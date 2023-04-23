package com.example.boardgamestats.screens

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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.SubcomposeAsyncImage
import com.example.boardgamestats.api.queryXmlApi
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.models.GameplayWithPlayers
import com.example.boardgamestats.navigation.GameNavigation
import com.example.boardgamestats.ui.animations.SkeletonAnimatedColor
import com.example.boardgamestats.ui.components.ExpandableText
import com.example.boardgamestats.utils.toDaysAgo
import com.example.boardgamestats.utils.toTimeString
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GameDetailsScreen(popBackStack: () -> Unit, gameId: Int, navController: NavHostController) {
    val db = BoardGameDatabase.getDatabase(LocalContext.current)
    val boardGameDao = db.boardGameDao()
    val formatter = DateFormat.getDateFormat(LocalContext.current)

    val gameplayDao = db.gameplayDao()

    var boardGameDetailsJob by remember { mutableStateOf<Job?>(null) }

    val lazyListState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()
    val isScrollable by remember { derivedStateOf { lazyListState.canScrollForward || topAppBarState.collapsedFraction > 0 } }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState, canScroll = { isScrollable })

    val scope = rememberCoroutineScope()

    boardGameDao.get(gameId).collectAsState(null).value?.let { boardGame ->
        if (boardGameDetailsJob == null && !boardGame.hasDetails) {
            boardGameDetailsJob = GlobalScope.launch {
                queryXmlApi("https://www.boardgamegeek.com/xmlapi2/thing?id=$gameId").first().let {
                    boardGameDao.updateDetails(it.id, it.thumbnail!!, it.image!!, it.description!!, it.isExpansion)
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
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else {
                    val plays = gameplayDao.getAllForGame(gameId).collectAsState(emptyList()).value
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

                                    })
                                GameplayStatisticsOverview(plays = plays)
                                Spacer(modifier = Modifier.height(56.dp))
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

@Composable
fun GameplayStatisticsOverview(
    plays: List<GameplayWithPlayers>,
    gridTitleTextStyle: TextStyle = MaterialTheme.typography.titleMedium,
    gridLabelTextStyle: TextStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal)
) {
    val context = LocalContext.current

    if (plays.isEmpty()) {
        return
    }

    val statistics by remember {
        derivedStateOf {
            listOf(
                listOf(
                    "Highest score" to plays.flatMap { it.playerResults }.maxByOrNull { it.score }?.score.toString(),
                    "Avg score" to plays.flatMap { it.playerResults }.map { it.score }.average().toInt().toString(),
                    "Lowest score" to plays.flatMap { it.playerResults }.minByOrNull { it.score }?.score.toString()
                ),
                listOf(
                    "Last played" to plays.maxByOrNull { it.gameplay.date }!!.gameplay.date.toDaysAgo(context),
                    "Avg playtime" to plays.mapNotNull { it.gameplay.playtime }
                        .filter { it > 0 }
                        .average()
                        .let { if (it.isNaN()) "No data" else it.toLong().toTimeString() },
                    "Total plays" to plays.size.toString()
                )
            )
        }
    }

    statistics.forEach { list ->
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(top = gridTitleTextStyle.lineHeight.value.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            list.forEach { pair ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        pair.second,
                        style = gridTitleTextStyle
                    )
                    Text(
                        pair.first,
                        style = gridLabelTextStyle
                            .copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.titleLarge,
    title: String,
    onArrowClick: (() -> Unit)? = null
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            title,
            style = textStyle
        )

        if (onArrowClick != null) {
            IconButton(
                onClick = onArrowClick,
                modifier = Modifier.size(textStyle.lineHeight.value.dp),
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    contentDescription = null,
                    modifier = Modifier.size(textStyle.fontSize.value.dp)
                )
            }
        }
    }
}