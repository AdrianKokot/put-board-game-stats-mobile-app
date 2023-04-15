package com.example.boardgamestats.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.ui.animations.SkeletonAnimatedColor
import com.example.boardgamestats.utils.toDaysAgo

@Composable
fun PlayedGamesScreen(navigateToGameplayDetails: (Int) -> Unit) {
    val list = BoardGameDatabase.getDatabase(LocalContext.current)
        .gameplayDao()
        .getGameplayListWithUniqueGames()
        .collectAsState(initial = emptyList()).value

    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        items(list) { item ->
            ListItem(
                headlineContent = { Text(item.boardGame.name) },
                supportingContent = { Text(item.gameplay.date.toDaysAgo()) },
                leadingContent = {
                    SubcomposeAsyncImage(
                        modifier = Modifier.width(64.dp).height(64.dp).clip(MaterialTheme.shapes.extraSmall),
                        model = item.boardGame.thumbnail,
                        contentDescription = item.boardGame.name,
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(Modifier.matchParentSize().background(SkeletonAnimatedColor()))
                        }
                    )
                },
                modifier = Modifier.clickable { navigateToGameplayDetails.invoke(item.gameplay.id) }
            )
        }
    }
}