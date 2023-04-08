package com.example.boardgamestats.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.utils.toTimeString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameplayListScreen(gameId: Int, navController: NavController) {
    val plays by BoardGameDatabase.getDatabase(LocalContext.current)
        .gameplayDao()
        .getAllForGame(gameId)
        .collectAsState(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
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
        LazyColumn(modifier = Modifier.padding(scaffoldPadding).fillMaxSize()) {
            if (plays.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
            } else {
                items(plays) { gameplay ->
                    Text("Playtime: ${gameplay.gameplay.playtime?.toTimeString()}")
                }
            }
        }
    }
}