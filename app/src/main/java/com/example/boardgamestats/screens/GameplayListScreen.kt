package com.example.boardgamestats.screens

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.navigation.GameNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameplayListScreen(gameId: Int, navController: NavController) {
    val plays by BoardGameDatabase.getDatabase(LocalContext.current)
        .gameplayDao()
        .getAllForGame(gameId)
        .collectAsState(emptyList())

    val lazyListScrollState = rememberLazyListState()
    val isScrollable by remember { derivedStateOf { lazyListScrollState.canScrollBackward || lazyListScrollState.canScrollForward } }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(canScroll = { isScrollable })
    val formatter = DateFormat.getDateFormat(LocalContext.current)

    Scaffold(
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        "Gameplay list", maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { scaffoldPadding ->
        LazyColumn(
            state = lazyListScrollState,
            modifier = Modifier.padding(scaffoldPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .fillMaxSize()
        ) {
            if (plays.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
            } else {
                items(plays) { gameplay ->
                    ListItem(
                        headlineContent = { Text(formatter.format(gameplay.gameplay.date)) },
                        supportingContent = {
                            Text(
                                gameplay.playerResults.sortedByDescending { it.score }
                                    .joinToString(", ") { it.playerName + " (" + it.score + ")" },
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.clickable {
                            navController.navigate(GameNavigation.gameplayDetailsScreen(gameplay.gameplay.id))
                        }
                    )
                }
            }
        }
    }
}