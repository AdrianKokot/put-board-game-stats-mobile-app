package com.example.boardgamestats.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.utils.toTimeString
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameplayDetailsScreen(gameplayId: Int, navController: NavController) {
    val gameplayDao = BoardGameDatabase
        .getDatabase(LocalContext.current)
        .gameplayDao()

    val gameplay by gameplayDao.getGameplayWithPlayers(gameplayId)
        .collectAsState(null)

    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gameplay details", maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Localized description")
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    scope.launch {
                                        gameplayDao.delete(gameplay!!.gameplay)
                                    }

                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                })
        }
    ) { scaffoldPadding ->
        Box(modifier = Modifier.padding(scaffoldPadding).fillMaxSize()) {
            if (gameplay == null) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {

                Column {
                    Text("Playtime: ${gameplay?.gameplay?.playtime?.toTimeString()}")
//                Text("Date: ${gameplay.date}")
//                Text("Location: ${gameplay.location}")
//                Text("Duration: ${gameplay.duration}")
//                Text("Notes: ${gameplay.notes}")
//                Text("Players:")
//                gameplay.players.forEach { player ->
//                    Text("  ${player.name}")
//                }
                }
            }

        }
    }
}