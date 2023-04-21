package com.example.boardgamestats.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.boardgamestats.MainActivity
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.database.daos.BoardGameDao
import com.example.boardgamestats.models.BoardGame
import com.example.boardgamestats.models.BoardGameWithPlaysInfo
import com.example.boardgamestats.ui.animations.SkeletonAnimatedColor
import com.example.boardgamestats.ui.extensions.customTabIndicatorOffset
import com.example.boardgamestats.utils.toDaysAgo
import com.example.boardgamestats.vm.MainViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


object CollectionScreenTabs {
    const val GAMES = 0
    const val EXPANSIONS = 1

    val titles = listOf("Games", "Expansions")
}

@Composable
fun CollectionScreenTabs(content: @Composable() ((tab: Int) -> Unit)) {
    var state by rememberSaveable { mutableStateOf(CollectionScreenTabs.GAMES) }

    val density = LocalDensity.current
    val tabWidths = remember {
        val tabWidthStateList = mutableStateListOf<Dp>()
        repeat(CollectionScreenTabs.titles.size) {
            tabWidthStateList.add(0.dp)
        }
        tabWidthStateList
    }

    Column {
        TabRow(
            selectedTabIndex = state,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.customTabIndicatorOffset(tabPositions[state], tabWidths[state])
                )
            }
        ) {
            CollectionScreenTabs.titles.forEachIndexed { index, title ->
                Tab(
                    modifier = Modifier.width(tabWidths[index]),
                    selected = state == index,
                    onClick = { state = index },
                    text = {
                        Text(
                            text = title,
                            onTextLayout = { textLayoutResult ->
                                tabWidths[index] =
                                    with(density) { textLayoutResult.size.width.toDp() }
                            }
                        )
                    }
                )
            }
        }

        content(state)
    }
}

@Composable
fun CollectionScreen(navigateToDetails: (Int) -> Unit) {
    CollectionScreenTabs { tab ->
        PullToSyncBox {
            when (tab) {
                CollectionScreenTabs.GAMES -> GamesCollection(navigateToDetails)
                CollectionScreenTabs.EXPANSIONS -> ExpansionsCollection()
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PullToSyncBox(
    mainViewModel: MainViewModel = viewModel((LocalContext.current as MainActivity)),
    content: @Composable() (BoxScope.() -> Unit),
) {
    val syncState = mainViewModel.syncState.collectAsState().value

    val state = rememberPullRefreshState(syncState.isSyncing, onRefresh = {
        mainViewModel.startSync()
    })

    Box(Modifier.pullRefresh(state, enabled = syncState.isSyncEnabled)) {
        content(this)

        PullRefreshIndicator(
            syncState.isSyncing, state,
            Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}


@Composable
fun GamesCollection(
    navigateToDetails: (Int) -> Unit,
    context: Context = LocalContext.current
) {
    LazyNullableList(
        BoardGameDatabase.getDatabase(context)
            .boardGameDao()
            .getBoardGamesCollectionWithPlayInformation()
            .collectAsState(null).value
    ) {
        ListItemWithAsyncImage(
            headlineContent = { Text(it.boardGame.name) },
            modifier = Modifier.clickable { navigateToDetails(it.boardGame.id) },
            model = it.boardGame.thumbnail,
            supportingContent = {
                if (it.playsCount > 0) {
                    Text("Last played ${it.lastPlay.toDaysAgo(context).lowercase()}")
                } else {
                    Text("Released in ${it.boardGame.publishYear}")
                }
            }
        )
    }
}

@Composable
fun <T> LazyNullableList(
    list: List<T>? = null,
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp),
    itemContent: @Composable LazyItemScope.(T) -> Unit
) {
    LazyColumn(contentPadding = contentPadding) {
        if (list == null) {
            items(5) {
                ListItem(
                    headlineContent = {
                        Text("", Modifier.width(160.dp).padding(bottom = 2.dp).background(SkeletonAnimatedColor()))
                    },
                    supportingContent = {
                        Text("", Modifier.width(128.dp).background(SkeletonAnimatedColor()))
                    },
                    leadingContent = {
                        Box(
                            Modifier.width(64.dp).height(64.dp).clip(MaterialTheme.shapes.extraSmall)
                                .background(SkeletonAnimatedColor())
                        )
                    }
                )
            }
        } else {
            items(list, itemContent = itemContent)
        }
    }
}

@Composable
fun ListItemWithAsyncImage(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    overlineContent: @Composable() (() -> Unit)? = null,
    supportingContent: @Composable() (() -> Unit)? = null,
    trailingContent: @Composable() (() -> Unit)? = null,
    model: Any? = null,
    contentDescription: String? = null
) {
    ListItem(
        headlineContent,
        modifier,
        overlineContent, supportingContent,
        leadingContent = {
            SubcomposeAsyncImage(
                modifier = Modifier.width(64.dp).height(64.dp).clip(MaterialTheme.shapes.extraSmall),
                model = model,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                loading = {
                    Box(Modifier.matchParentSize().background(SkeletonAnimatedColor()))
                }
            )
        },
        trailingContent
    )
}

@Composable
fun ExpansionsCollection() {
    LazyNullableList(
        BoardGameDatabase.getDatabase(LocalContext.current)
            .boardGameDao()
            .getExpansionsCollection()
            .collectAsState(null).value
    ) { item ->
        ListItemWithAsyncImage(
            headlineContent = { Text(item.name) },
            supportingContent = {
                Text("Released in ${item.publishYear}")
            },
            model = item.thumbnail,
            contentDescription = item.name
        )
    }
}


