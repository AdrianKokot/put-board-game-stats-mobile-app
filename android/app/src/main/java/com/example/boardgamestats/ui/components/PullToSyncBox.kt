package com.example.boardgamestats.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.boardgamestats.MainActivity
import com.example.boardgamestats.vm.MainViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PullToSyncBox(
    mainViewModel: MainViewModel = viewModel((LocalContext.current as MainActivity)),
    content: @Composable (BoxScope.() -> Unit),
) {
    val syncState = mainViewModel.syncState.collectAsState().value

    val state = rememberPullRefreshState(syncState.isSyncing, onRefresh = {
        mainViewModel.startSync()
    })

    Box(Modifier.pullRefresh(state, enabled = syncState.isSyncEnabled)) {
        content(this)

        PullRefreshIndicator(
            syncState.isSyncing, state,
            Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}
