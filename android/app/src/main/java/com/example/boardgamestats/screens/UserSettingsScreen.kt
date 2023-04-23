package com.example.boardgamestats.screens

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.boardgamestats.database.BoardGameDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSettingsScreen(
    context: Context = LocalContext.current,
    navController: NavController
) {
    val dao = remember { BoardGameDatabase.getDatabase(context).settingsDao() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val settings by dao.getUserSettings().collectAsState(null)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text("Profile")
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
                Crossfade(settings != null) {
                    if (!it) {
                        return@Crossfade
                    }

                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Enable sync", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = settings!!.isSyncEnabled,
                            onCheckedChange = {
                                GlobalScope.launch {
                                    dao.updateUserSettings(it)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
