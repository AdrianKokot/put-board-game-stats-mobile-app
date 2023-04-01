package com.example.boardgamestats.screens

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.rememberNavController
import com.example.boardgamestats.api.BoardGame
import com.example.boardgamestats.api.queryXmlApi
import com.example.boardgamestats.ui.components.BottomNavigationBar
import com.example.boardgamestats.ui.components.BottomNavigationGraph
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var text by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }
    var showOpenedIcons by rememberSaveable { mutableStateOf(false) }
    var searchResults by rememberSaveable { mutableStateOf(emptyList<BoardGame>()) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    var searchJob by rememberSaveable { mutableStateOf<Job?>(null) }
    var wasSearched by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(active) {
        delay(100)
        showOpenedIcons = active
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(bottomBar = {
        BottomNavigationBar(navController)
    }, snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
        Box(Modifier.fillMaxWidth().zIndex(3f).padding(bottom = 8.dp)) {
            SearchBar(modifier = Modifier.align(Alignment.TopCenter).padding(0.dp),
                query = text,
                onQueryChange = { text = it },
                onSearch = {
                    keyboardController?.hide()
                    wasSearched = true
                    isLoading = true
                    searchJob?.cancel()
                    searchJob = GlobalScope.launch {
                        searchResults =
                            queryXmlApi("https://www.boardgamegeek.com/xmlapi2/search?type=boardgame&query=$text")
                        isLoading = false
                    }
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
                    }
                },
                placeholder = { Text("Search games") },
                leadingIcon = {
                    if (showOpenedIcons) {
                        IconButton(
                            onClick = { active = false }) {
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
    }) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            BottomNavigationGraph(navController)
        }
    }

    DisposableEffect(key1 = searchJob) {
        onDispose {
            searchJob?.cancel()
        }
    }
}