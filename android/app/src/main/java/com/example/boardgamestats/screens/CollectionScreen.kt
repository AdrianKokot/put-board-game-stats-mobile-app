package com.example.boardgamestats.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.ui.components.LazyNullableList
import com.example.boardgamestats.ui.components.ListItemWithAsyncImage
import com.example.boardgamestats.ui.components.PullToSyncBox
import com.example.boardgamestats.ui.extensions.customTabIndicatorOffset
import com.example.boardgamestats.utils.toDaysAgo


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
        TabRow(selectedTabIndex = state, indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.customTabIndicatorOffset(tabPositions[state], tabWidths[state])
            )
        }) {
            CollectionScreenTabs.titles.forEachIndexed { index, title ->
                Tab(modifier = Modifier.width(tabWidths[index]),
                    selected = state == index,
                    onClick = { state = index },
                    text = {
                        Text(text = title, onTextLayout = { textLayoutResult ->
                            tabWidths[index] = with(density) { textLayoutResult.size.width.toDp() }
                        })
                    })
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


@Composable
fun GamesCollection(
    navigateToDetails: (Int) -> Unit, context: Context = LocalContext.current
) {
    LazyNullableList(
        BoardGameDatabase.getDatabase(context).boardGameDao().getBoardGamesCollectionWithPlayInformation()
            .collectAsState(null).value
    ) {
        ListItemWithAsyncImage(headlineContent = { Text(it.boardGame.name) },
            modifier = Modifier.clickable { navigateToDetails(it.boardGame.id) },
            model = it.boardGame.thumbnail,
            supportingContent = {
                if (it.playsCount > 0) {
                    Text("Last played ${it.lastPlay.toDaysAgo(context).lowercase()}")
                } else {
                    Text("Released in ${it.boardGame.publishYear}")
                }
            })
    }
}

@Composable
fun ExpansionsCollection() {
    LazyNullableList(
        BoardGameDatabase.getDatabase(LocalContext.current).boardGameDao().getExpansionsCollection()
            .collectAsState(null).value
    ) { item ->
        ListItemWithAsyncImage(headlineContent = { Text(item.name) }, supportingContent = {
            Text("Released in ${item.publishYear}")
        }, model = item.thumbnail, contentDescription = item.name
        )
    }
}


