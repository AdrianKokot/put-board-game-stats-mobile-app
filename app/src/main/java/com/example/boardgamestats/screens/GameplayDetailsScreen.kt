package com.example.boardgamestats.screens

import android.text.format.DateFormat
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.navigation.GameNavigation
import com.example.boardgamestats.utils.toTimeString
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GameplayDetailsScreen(gameplayId: Int, navController: NavController) {
    val database = BoardGameDatabase.getDatabase(LocalContext.current)
    val gameplayDao = database.gameplayDao()

    val lazyListState = rememberLazyListState()
    val isScrollable by remember { derivedStateOf { lazyListState.canScrollBackward || lazyListState.canScrollForward } }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(canScroll = { isScrollable })

    val gameplay = gameplayDao.getGameplayDetails(gameplayId).collectAsState(null).value

    val formatter = DateFormat.getDateFormat(LocalContext.current)

    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(scrollBehavior = scrollBehavior,
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
                                text = { Text("Delete gameplay") },
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
                val details = listOfNotNull(
                    if (gameplay.gameplay.playtime != null) Icons.Outlined.Timer to gameplay.gameplay.playtime.toTimeString() else null,
                    Icons.Outlined.Today to formatter.format(gameplay.gameplay.date),
                    if (gameplay.gameplay.notes.isNotEmpty()) Icons.Outlined.Description to gameplay.gameplay.notes else null
                )

                LazyColumn(state = lazyListState) {
                    item {
                        Card(modifier = Modifier.padding(horizontal = 16.dp)) {
                            GameplayDetailsCardRow(
                                icon = Icons.Outlined.Casino,
                                text = gameplay.boardGame.name,
                                onClick = {
                                    navController.navigate(GameNavigation.detailsScreen(gameplay.boardGame.id))
                                }
                            )

                            details.forEach { (icon, text) ->
                                GameplayDetailsCardRow(icon = icon, text = text)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    stickyHeader {
                        Crossfade(lazyListState.firstVisibleItemIndex == 0, animationSpec = spring()) {
                            val backgroundColor =
                                if (it) MaterialTheme.colorScheme.surface
                                else MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)

                            SectionTitle(
                                title = "Players",
                                modifier = Modifier.background(backgroundColor)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    items(gameplay.playerResults.sortedByDescending { it.score }) { player ->
                        ListItem(
                            headlineContent = { Text(player.playerName) },
                            trailingContent = { Text(player.score.toString()) },
                            leadingContent = {
                                Icon(Icons.Outlined.Person, contentDescription = null)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameplayDetailsCardRow(onClick: (() -> Unit)? = null, icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(end = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.padding(16.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
                .padding(top = 16.dp, bottom = 16.dp)
        )
    }
}