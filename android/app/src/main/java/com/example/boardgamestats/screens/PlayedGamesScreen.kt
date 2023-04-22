package com.example.boardgamestats.screens

import android.app.Application
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.boardgamestats.MainActivity
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.models.GameplayWithGame
import com.example.boardgamestats.ui.components.LazyNullableList
import com.example.boardgamestats.ui.components.ListItemWithAsyncImage
import com.example.boardgamestats.ui.components.PullToSyncBox
import com.example.boardgamestats.utils.toDaysAgo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayedGamesScreenViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = BoardGameDatabase.getDatabase(application).gameplayDao()

    private val _plays = MutableStateFlow<List<GameplayWithGame>?>(null)
    val plays = _plays.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getGameplayListWithUniqueGames().collect(_plays)
        }
    }
}


@Composable
fun PlayedGamesScreen(
    context: Context = LocalContext.current,
    viewModel: PlayedGamesScreenViewModel = viewModel(context as MainActivity),
    navigateToGameplayDetails: (Int) -> Unit
) {
    PullToSyncBox {
        LazyNullableList(viewModel.plays.collectAsState().value) { item ->
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