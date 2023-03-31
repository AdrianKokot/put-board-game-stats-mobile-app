package com.example.boardgamestats.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.boardgamestats.models.BoardGame
import com.example.boardgamestats.ui.extensions.customTabIndicatorOffset

@Composable
fun CollectionScreen() {
    var state by remember { mutableStateOf(0) }
    val titles = listOf("Games", "Expansions")

    val density = LocalDensity.current
    val tabWidths = remember {
        val tabWidthStateList = mutableStateListOf<Dp>()
        repeat(titles.size) {
            tabWidthStateList.add(0.dp)
        }
        tabWidthStateList
    }

    var searchResults by remember { mutableStateOf(emptyList<BoardGame>()) }

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

        LazyColumn {
            items(200) {
                ListItem(
                    headlineContent = { Text("Item $it") },
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
}