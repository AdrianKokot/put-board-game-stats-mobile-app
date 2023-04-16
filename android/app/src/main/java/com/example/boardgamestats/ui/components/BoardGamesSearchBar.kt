package com.example.boardgamestats.ui.components

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.boardgamestats.api.GoogleApiContract
import com.example.boardgamestats.api.SignInGoogleViewModel
import com.example.boardgamestats.api.queryXmlApi
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.models.BoardGame
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun BoardGamesSearchBar(navigateToDetails: (Int) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    var previousText by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }
    var showOpenedIcons by rememberSaveable { mutableStateOf(false) }
    var searchResults by rememberSaveable { mutableStateOf(emptyList<BoardGame>()) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val googleViewModel: SignInGoogleViewModel = viewModel()
    googleViewModel.loadAlreadySignedUser()
    val user = googleViewModel.googleUser.observeAsState().value
    val isUserLoading = googleViewModel.loading.observeAsState(true).value

    val authLauncher = rememberLauncherForActivityResult(contract = GoogleApiContract()) { task ->
        try {
            val gsa = task?.getResult(ApiException::class.java)
            if (gsa != null) {
                googleViewModel.fetchSignInUser(gsa.id, gsa.email, gsa.displayName, gsa.photoUrl.toString())
            }
        } catch (e: ApiException) {
            Log.d("Error in getAuthLauncher%s", e.toString())
        }
    }

    var searchJob by remember { mutableStateOf<Job?>(null) }
    var wasSearched by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(active) {
        delay(100)
        showOpenedIcons = active
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    val dao = BoardGameDatabase.getDatabase(LocalContext.current)
        .boardGameDao()

    val userIconLoading = @Composable() {
        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 3.dp, color = MaterialTheme.colorScheme.surface)
    }

    Box(Modifier.fillMaxWidth().zIndex(3f).padding(bottom = 8.dp)) {
        SearchBar(modifier = Modifier.align(Alignment.TopCenter).padding(0.dp),
            query = text,
            onQueryChange = { text = it },
            onSearch = {
                if (previousText != text) {
                    keyboardController?.hide()
                    wasSearched = true
                    isLoading = true
                    searchJob?.cancel()
                    searchJob = GlobalScope.launch {
                        searchResults =
                            queryXmlApi("https://www.boardgamegeek.com/xmlapi2/search?type=boardgame&query=$text")
                                .sortedByDescending { it.publishYear }

                        if (searchResults.isNotEmpty()) {
                            dao
                                .insertAll(*searchResults.toTypedArray())
                        }

                        isLoading = false
                    }
                }
                previousText = text
            },
            active = active,
            onActiveChange = {
                active = it
                if (!it) {
                    searchResults = emptyList()
                    searchJob?.cancel()
                    isLoading = false
                    searchJob = null
                    wasSearched = false
                    text = ""
                    previousText = ""
                }
            },
            placeholder = { Text("Search games") },
            leadingIcon = {
                if (showOpenedIcons) {
                    IconButton(
                        onClick = {
                            active = false
                            searchResults = emptyList()
                            searchJob?.cancel()
                            isLoading = false
                            searchJob = null
                            wasSearched = false
                            text = ""
                            previousText = ""
                        }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                } else {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            },
            trailingIcon = {
                Crossfade(targetState = !active) {
                    if (it) {
                        if (isUserLoading) {
                            userIconLoading()
                        } else if (user == null) {
                            IconButton(
                                onClick = {
                                    authLauncher.launch(0)
                                }
                            ) {
                                Icon(
                                    Icons.Outlined.AccountCircle,
                                    contentDescription = "Account"
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {

                                }
                            ) {
                                SubcomposeAsyncImage(
                                    modifier = Modifier.size(32.dp).clip(MaterialTheme.shapes.extraLarge),
                                    model = user.photoUrl,
                                    contentDescription = "Profile picture",
                                    contentScale = ContentScale.Crop,
                                    loading = {
                                        userIconLoading()
                                    }
                                )
                            }
                        }
                    }
                }

                Crossfade(targetState = showOpenedIcons && text.isNotEmpty()) {
                    if (it) {
                        IconButton(onClick = { text = "" }) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                }

            }
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(bottom = 40.dp)) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else if (searchResults.isNotEmpty()) {
                    LazyColumn {
                        items(searchResults.size) { idx ->
                            val boardGame = searchResults[idx]
                            ListItem(
                                headlineContent = { Text(boardGame.name) },
                                supportingContent = { Text(boardGame.publishYear.toString()) },
                                modifier = Modifier.clickable(onClick = {
                                    navigateToDetails(boardGame.id)
                                })
                            )
                        }
                    }
                } else if (wasSearched) {
                    Text("No results found", modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }

    DisposableEffect(key1 = searchJob) {
        onDispose {
            searchJob?.cancel()
        }
    }
}