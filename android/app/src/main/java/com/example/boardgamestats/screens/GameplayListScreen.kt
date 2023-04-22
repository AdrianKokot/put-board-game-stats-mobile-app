package com.example.boardgamestats.screens

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.navigation.GameNavigation
import com.example.boardgamestats.ui.components.LazyNullableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameplayListScreen(gameId: Int, navController: NavController, context: Context = LocalContext.current) {
    val dao = remember { BoardGameDatabase.getDatabase(context).gameplayDao() }
    val plays by remember { dao.getAllForGame(gameId) }.collectAsState(null)

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val formatter = DateFormat.getDateFormat(context)

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
        LazyNullableList(
            list = plays,
            placeholderHasImage = false,
            contentPadding = scaffoldPadding,
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { gameplay ->
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