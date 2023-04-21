package com.example.boardgamestats.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.utils.toDaysAgo

@Composable
fun PlayedGamesScreen(context: Context = LocalContext.current, navigateToGameplayDetails: (Int) -> Unit) {
    PullToSyncBox {
        LazyNullableList(
            BoardGameDatabase.getDatabase(context)
                .gameplayDao()
                .getGameplayListWithUniqueGames()
                .collectAsState(null)
                .value
        ) { item ->
            ListItemWithAsyncImage(
                headlineContent = { Text(item.boardGame.name) },
                supportingContent = { Text(item.gameplay.date.toDaysAgo(context)) },
                model = item.boardGame.thumbnail,
                contentDescription = item.boardGame.name,
                modifier = Modifier.clickable { navigateToGameplayDetails.invoke(item.gameplay.id) }
            )
        }
    }
}