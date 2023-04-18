package com.example.boardgamestats.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.models.BoardGame
import com.example.boardgamestats.ui.animations.SkeletonAnimatedColor
import com.example.boardgamestats.ui.extensions.customTabIndicatorOffset
import com.example.boardgamestats.utils.toDaysAgo

@Composable
fun CollectionScreen(navigateToDetails: (Int) -> Unit) {
    var state by rememberSaveable { mutableStateOf(0) }
    val titles = listOf("Games", "Expansions")

    val density = LocalDensity.current
    val tabWidths = remember {
        val tabWidthStateList = mutableStateListOf<Dp>()
        repeat(titles.size) {
            tabWidthStateList.add(0.dp)
        }
        tabWidthStateList
    }

    Column {
        TabRow(selectedTabIndex = state, indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.customTabIndicatorOffset(tabPositions[state], tabWidths[state])
            )
        }) {
            titles.forEachIndexed { index, title ->
                Tab(
                    modifier = Modifier.width(tabWidths[index]),
                    selected = state == index,
                    onClick = { state = index },
                    text = {
                        Text(text = title,
                            onTextLayout = { textLayoutResult ->
                                tabWidths[index] =
                                    with(density) { textLayoutResult.size.width.toDp() }
                            })
                    }
                )
            }
        }

        when (state) {
            0 -> GamesCollection(navigateToDetails)
            1 -> ExpansionsCollection()
        }
    }
}

@Composable
fun GamesCollection(navigateToDetails: (Int) -> Unit) {
    val list = BoardGameDatabase.getDatabase(LocalContext.current)
        .boardGameDao()
        .getBoardGamesCollectionWithPlayInformation()
        .collectAsState(emptyList())
        .value

    val context = LocalContext.current

    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        items(list) { item ->
            ListItem(
                headlineContent = { Text(item.boardGame.name) },
                supportingContent = {
                    if (item.playsCount > 0) {
                        Text("Last played ${item.lastPlay.toDaysAgo(context).lowercase()}")
                    } else {
                        Text("Released in ${item.boardGame.publishYear}")
                    }
                },
                leadingContent = {
                    SubcomposeAsyncImage(
                        modifier = Modifier.width(64.dp).height(64.dp).clip(MaterialTheme.shapes.extraSmall),
                        model = item.boardGame.thumbnail,
                        contentDescription = item.boardGame.name,
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(Modifier.matchParentSize().background(SkeletonAnimatedColor()))
                        }
                    )
                },
                modifier = Modifier.clickable { navigateToDetails(item.boardGame.id) }
            )
        }
    }
}

@Composable
fun CollectionList(list: List<BoardGame>, onItemClick: (item: BoardGame) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        items(list) { item ->
            ListItem(
                headlineContent = { Text(item.name) },
                supportingContent = {
                    Text("Released in ${item.publishYear}")
                },
                leadingContent = {
                    SubcomposeAsyncImage(
                        modifier = Modifier.width(64.dp).height(64.dp).clip(MaterialTheme.shapes.extraSmall),
                        model = item.thumbnail,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(Modifier.matchParentSize().background(SkeletonAnimatedColor()))
                        }
                    )
                },
                modifier = Modifier.clickable { onItemClick(item) }
            )
        }
    }
}


@Composable
fun ExpansionsCollection() {
    BoardGameDatabase.getDatabase(LocalContext.current)
        .boardGameDao()
        .getExpansionsCollection()
        .collectAsState(initial = emptyList())
        .let { state ->
            CollectionList(state.value) { }
        }
}


