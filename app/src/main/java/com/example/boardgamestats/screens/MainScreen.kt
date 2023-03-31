package com.example.boardgamestats.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.rememberNavController
import com.example.boardgamestats.api.BoardGame
import com.example.boardgamestats.api.queryXmlApi
import com.example.boardgamestats.ui.components.BottomNavigationBar
import com.example.boardgamestats.ui.components.BottomNavigationGraph
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    var text by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }
    var searchResults by rememberSaveable { mutableStateOf(emptyList<BoardGame>()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(bottomBar = {
        BottomNavigationBar(navController)
    },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box(Modifier.fillMaxWidth().zIndex(3f).padding(bottom = 8.dp)) {
                SearchBar(
                    modifier = Modifier.align(Alignment.TopCenter).padding(0.dp),
                    query = text,
                    onQueryChange = { text = it },
                    onSearch = {
                        GlobalScope.launch {
                            searchResults =
                                queryXmlApi("https://www.boardgamegeek.com/xmlapi2/search?type=boardgame&query=$text")
                        }
                    },
                    active = active,
                    onActiveChange = {
                        active = it
                        if (!it) {
                            searchResults = emptyList()
                        }
                    },
                    placeholder = { Text("Hinted search text") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = { Icon(Icons.Default.MoreVert, contentDescription = null) },
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (searchResults.isNotEmpty()) {
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
//                                        leadingContent = { Icon(Icons.Filled.Star, contentDescription = null) },
//                                        modifier = Modifier.clickable {
//                                            text = resultText
//                                            active = false
//                                        }
                                    )
                                }
                            }
                        } else {
                            Text("No results found")
                        }
                    }
                }
            }
        }) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            BottomNavigationGraph(navController)
        }
    }

}