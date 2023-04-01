package com.example.boardgamestats.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import coil.compose.AsyncImage
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.ui.extensions.customTabIndicatorOffset

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
fun GamesCollection(navigateToDetails: (Int) -> Unit){
    val list = BoardGameDatabase.getDatabase(LocalContext.current)
        .boardGameDao()
        .getCollection()
        .collectAsState(initial = emptyList())

    LazyColumn {
        itemsIndexed(list.value) { index, boardGame ->
            ListItem(
                headlineContent = { Text(boardGame.name) },
                supportingContent = { Text(boardGame.publishYear.toString()) },
                leadingContent = {
                    AsyncImage(
                        model = boardGame.thumbnail,
                        contentDescription = boardGame.name
                    )
                },
                modifier = Modifier.clickable { navigateToDetails(boardGame.id) }
            )
        }
    }
}


@Composable
fun ExpansionsCollection() {
    LazyColumn {
        items(200) {
            ListItem(
                headlineContent = { Text("Expansion $it") },
                supportingContent = { Text("Supporting text") },
                leadingContent = {
                    AsyncImage(
                        model = "https://api.lorem.space/image?w=150&h=180",
                        contentDescription = "Translated description of what the image contains"
                    )
                }
            )
        }
    }
}