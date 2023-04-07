package com.example.boardgamestats.screens

import android.text.format.DateFormat
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.models.Gameplay
import com.example.boardgamestats.models.PlayerWithScoreDto
import com.example.boardgamestats.ui.components.TextFieldWithDropdown
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Instant

data class GameplayPlayer(var name: String, var score: String)

class NewGamePlayViewModel : ViewModel() {
    private val _players = mutableStateListOf<GameplayPlayer>()
    val players: List<GameplayPlayer> = _players

    fun addPlayer(player: GameplayPlayer) {
        _players.add(player)
    }

    fun removePlayer(player: GameplayPlayer) {
        _players.remove(player)
    }

    fun updatePlayer(player: GameplayPlayer, newName: String? = null, newScore: String? = null) {
        val index = _players.indexOf(player)
        _players[index] = _players[index].copy(name = newName ?: player.name, score = newScore ?: player.score)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGameplayScreen(
    popBackStack: () -> Unit, gameId: Int, newGamePlayViewModel: NewGamePlayViewModel = viewModel()
) {
    val dao = BoardGameDatabase.getDatabase(LocalContext.current).playerDao()

    val gameplayDao = BoardGameDatabase.getDatabase(LocalContext.current).gameplayDao()

    val openDialog = remember { mutableStateOf(false) }

    var dateInstant by remember { mutableStateOf(Instant.now()) }
    val datePickerState = rememberDatePickerState(dateInstant.toEpochMilli())

    val players = newGamePlayViewModel.players

    val alreadyExistingPlayerNames = dao.getAll().collectAsState(emptyList()).value.map { it.name }.toList()

    if (openDialog.value) {
        val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }
        DatePickerDialog(onDismissRequest = {
            openDialog.value = false
        }, confirmButton = {
            TextButton(
                onClick = {
                    openDialog.value = false
                    dateInstant = Instant.ofEpochMilli(datePickerState.selectedDateMillis!!)
                }, enabled = confirmEnabled.value
            ) {
                Text("Save")
            }
        }, dismissButton = {
            TextButton(onClick = {
                openDialog.value = false
            }) {
                Text("Cancel")
            }
        }) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("New play") }, navigationIcon = {
            IconButton(onClick = { popBackStack() }) {
                Icon(Icons.Filled.Close, contentDescription = "Close")
            }
        }, actions = {
            Button(onClick = {

                GlobalScope.launch {
                    gameplayDao.insert(
                        Gameplay(
                            boardGameId = gameId, date = dateInstant.toEpochMilli()
                        ),
                        players.map {
                            PlayerWithScoreDto(
                                playerName = it.name, score = it.score.toIntOrNull() ?: 0
                            )
                        },
                    )
                }
                popBackStack()
            }, modifier = Modifier.padding(end = 16.dp)) {
                Text("Save")
            }
        })
    }, content = {
        Box(modifier = Modifier.padding(it)) {


            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize()
            ) {
                TextField(interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect {
                            if (it is PressInteraction.Release) {
                                openDialog.value = true
                            }
                        }
                    }
                },
                    value = DateFormat.getDateFormat(LocalContext.current).format(dateInstant.toEpochMilli()),
                    readOnly = true,
                    onValueChange = {},
                    label = { Text("Play date") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Players"
                    )
                    Button(onClick = {
                        newGamePlayViewModel.addPlayer(GameplayPlayer("", ""))
                    }) {
                        Text("Add")
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(players) { player ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextField(
                                label = { Text("Score") },
                                value = player.score,
                                onValueChange = {
                                    newGamePlayViewModel.updatePlayer(
                                        player, newScore = (it.toIntOrNull() ?: "").toString()
                                    )
                                },
                                modifier = Modifier.width(96.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            TextFieldWithDropdown(label = { Text("Name") }, value = player.name, onValueChange = {
                                newGamePlayViewModel.updatePlayer(player, newName = it)
                            }, modifier = Modifier.weight(1f), options = alreadyExistingPlayerNames
                            )

                            IconButton(
                                onClick = {
                                    newGamePlayViewModel.removePlayer(player)
                                }, modifier = Modifier.width(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                }
            }
        }
    })
}