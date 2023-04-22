package com.example.boardgamestats.screens

import android.app.Application
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
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.boardgamestats.MainActivity
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.models.BoardGame
import com.example.boardgamestats.models.BoardGameWithPlaysInfo
import com.example.boardgamestats.ui.components.LazyNullableList
import com.example.boardgamestats.ui.components.ListItemWithAsyncImage
import com.example.boardgamestats.ui.components.PullToSyncBox
import com.example.boardgamestats.ui.extensions.customTabIndicatorOffset
import com.example.boardgamestats.utils.toDaysAgo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object CollectionScreenTabs {
    const val GAMES = 0
    const val EXPANSIONS = 1

    val titles = listOf("Games", "Expansions")
}

class CollectionScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val boardGameDao = BoardGameDatabase.getDatabase(application).boardGameDao()

    private val _games = MutableStateFlow<List<BoardGameWithPlaysInfo>?>(null)
    val games = _games.asStateFlow()

    private val _expansions = MutableStateFlow<List<BoardGame>?>(null)
    val expansions = _expansions.asStateFlow()

    init {
        viewModelScope.launch {
            boardGameDao.getBoardGamesCollectionWithPlayInformation().collect(_games)
        }

        viewModelScope.launch {
            boardGameDao.getExpansionsCollection().collect(_expansions)
        }
    }
}

@Composable
fun CollectionScreen(
    context: Context = LocalContext.current,
    viewModel: CollectionScreenViewModel = viewModel(context as MainActivity),
    navigateToDetails: (Int) -> Unit
) {
    val games = viewModel.games.collectAsState().value
    val expansions = viewModel.expansions.collectAsState().value

    CollectionScreenTabs { tab ->
        PullToSyncBox {
            when (tab) {
                CollectionScreenTabs.GAMES -> {
                    LazyNullableList(games) { item ->
                        ListItemWithAsyncImage(headlineContent = { Text(item.boardGame.name) },
                            modifier = Modifier.clickable { navigateToDetails(item.boardGame.id) },
                            model = item.boardGame.thumbnail,
                            supportingContent = {
                                if (item.playsCount > 0) {
                                    Text("Last played ${item.lastPlay.toDaysAgo(context).lowercase()}")
                                } else {
                                    Text("Released in ${item.boardGame.publishYear}")
                                }
                            })
                    }
                }

                CollectionScreenTabs.EXPANSIONS -> {
                    LazyNullableList(expansions) { item ->
                        ListItemWithAsyncImage(headlineContent = { Text(item.name) }, supportingContent = {
                            Text("Released in ${item.publishYear}")
                        }, model = item.thumbnail, contentDescription = item.name
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun CollectionScreenTabs(content: @Composable ((tab: Int) -> Unit)) {
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
