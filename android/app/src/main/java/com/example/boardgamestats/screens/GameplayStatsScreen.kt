package com.example.boardgamestats.screens

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.ui.components.GameplayStatisticsOverview
import com.example.boardgamestats.ui.components.PlayerStatisticsOverview
import com.example.boardgamestats.ui.components.SectionTitle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GameplayStatsScreen(context: Context = LocalContext.current, gameId: Int, navController: NavController) {
    val database = BoardGameDatabase.getDatabase(context)
    val boardGameDao = remember { database.boardGameDao() }

    val stats = boardGameDao.getBoardGamePlaysStats(gameId).collectAsState(null).value
    val playerStats = boardGameDao.getBoardGamePlayerStats(gameId).collectAsState(null).value
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        "Game plays stats", maxLines = 1, overflow = TextOverflow.Ellipsis
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
            item {

                Card(modifier = Modifier.padding(horizontal = 16.dp)) {
                    GameplayStatisticsOverview(stats = stats)
                }

                Spacer(modifier = Modifier.height(32.dp))

                SectionTitle(
                    title = "Player stats",
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface).padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (playerStats != null) {
                items(playerStats) { playerStat ->
                    Spacer(modifier = Modifier.height(16.dp))
                    PlayerStatisticsOverview(playerStat)
                    Divider(modifier = Modifier.padding(bottom = 16.dp))
                }
            }
        }
    }
}
