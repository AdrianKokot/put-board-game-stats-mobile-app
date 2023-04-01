package com.example.boardgamestats.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.boardgamestats.api.BoardGame
import com.example.boardgamestats.api.queryXmlApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun BoardGamesSearchBar() {
    var text by rememberSaveable { mutableStateOf("") }
    var previousText by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }
    var showOpenedIcons by rememberSaveable { mutableStateOf(false) }
    var searchResults by rememberSaveable { mutableStateOf(emptyList<BoardGame>()) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    var searchJob by rememberSaveable { mutableStateOf<Job?>(null) }
    var wasSearched by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(active) {
        delay(100)
        showOpenedIcons = active
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    Box(Modifier.fillMaxWidth().zIndex(3f).padding(bottom = 8.dp)) {
        SearchBar(modifier = Modifier.align(Alignment.TopCenter).padding(0.dp),
            query = text,
            onQueryChange = { text = it },
            onSearch = {
                if (previousText != text) {
                    keyboardController?.hide()
                    wasSearched = true
                    isLoading = true
                    searchJob?.cancel()
                    searchJob = GlobalScope.launch {
                        searchResults =
                            queryXmlApi("https://www.boardgamegeek.com/xmlapi2/search?type=boardgame&query=$text")
                        isLoading = false
                    }
                }
                previousText = text
            },
            active = active,
            onActiveChange = {
                active = it
                if (!it) {
                    searchResults = emptyList()
                    searchJob?.cancel()
                    isLoading = false
                    searchJob = null
                    wasSearched = false
                    text = ""
                    previousText = ""
                }
            },
            placeholder = { Text("Search games") },
            leadingIcon = {
                if (showOpenedIcons) {
                    IconButton(
                        onClick = {
                            active = false
                            searchResults = emptyList()
                            searchJob?.cancel()
                            isLoading = false
                            searchJob = null
                            wasSearched = false
                            text = ""
                            previousText = ""
                        }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                } else {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            },
            trailingIcon = {
                Crossfade(targetState = showOpenedIcons && text.isNotEmpty()) {
                    if (it) {
                        IconButton(onClick = { text = "" }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                }

            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else if (searchResults.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(searchResults.size) { idx ->
                            val boardGame = searchResults[idx]
                            ListItem(
                                headlineContent = { Text(boardGame.name) },
                                supportingContent = { Text(boardGame.publishYear.toString()) },
                            )
                        }
                    }
                } else if (wasSearched) {
                    Text("No results found", modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }

    DisposableEffect(key1 = searchJob) {
        onDispose {
            searchJob?.cancel()
        }
    }
}