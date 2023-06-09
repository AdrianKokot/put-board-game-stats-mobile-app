package com.example.boardgamestats.screens

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.models.Gameplay
import com.example.boardgamestats.models.PlayerWithScoreDto
import com.example.boardgamestats.ui.components.TextFieldWithDropdown
import com.example.boardgamestats.ui.components.TimePickerDialog
import com.example.boardgamestats.utils.toTimeString
import kotlinx.coroutines.launch
import java.time.Instant

data class GameplayPlayer(var name: String, var score: String, val id: Int = 0)

class NewGamePlayViewModel : ViewModel() {
    private var _id = 0
    private val _players = mutableStateListOf(GameplayPlayer(id = _id++, name = "", score = ""))
    val players: List<GameplayPlayer> = _players

    fun addPlayer(player: GameplayPlayer) {
        _players.add(player.copy(id = _id++))
    }

    fun removePlayer(player: GameplayPlayer) {
        _players.remove(player)
    }

    fun updatePlayer(index: Int, newName: String? = null, newScore: String? = null) {
        _players[index] = _players[index].copy(
            name = newName ?: _players[index].name,
            score = newScore ?: _players[index].score
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NewGameplayScreen(context: Context = LocalContext.current,
    popBackStack: () -> Unit, gameId: Int, newGamePlayViewModel: NewGamePlayViewModel = viewModel()
) {
    val database = remember { BoardGameDatabase.getDatabase(context) }
    val dao = remember { database.playerDao() }
    val gameplayDao = remember { database.gameplayDao() }

    var openDialog by rememberSaveable { mutableStateOf(false) }
    var dateInstant by rememberSaveable { mutableStateOf(Instant.now().toEpochMilli()) }
    val datePickerState = rememberDatePickerState(dateInstant)

    val players = newGamePlayViewModel.players

    val allPlayerNames = dao.getAll().collectAsState(emptyList()).value.map { it.name }.toList()
    val pickedPlayers by remember { derivedStateOf { players.map { it.name }.filter { it.isNotEmpty() }.toSet() } }

    val alreadyExistingPlayerNames = allPlayerNames.filter { !pickedPlayers.contains(it) }

    val scope = rememberCoroutineScope()

    val timePickerState = rememberTimePickerState(is24Hour = true)
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var duration by rememberSaveable { mutableStateOf(0L) }
    val textFieldColors = TextFieldDefaults.colors(
        disabledTextColor = MaterialTheme.colorScheme.onSurface,
        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
    var notes by rememberSaveable { mutableStateOf("") }

    var submitted by rememberSaveable { mutableStateOf(false) }

    val valid by remember { derivedStateOf { (players.isNotEmpty() && players.all { player -> player.score.toIntOrNull() != null && player.name.isNotEmpty() }) } }

    if (openDialog) {
        val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }
        DatePickerDialog(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            onDismissRequest = {
                openDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                        datePickerState.selectedDateMillis?.let {
                            dateInstant = it
                        }
                    },
                    enabled = confirmEnabled
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    openDialog = false
                }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState, dateValidator = {
                !Instant.ofEpochMilli(it).isAfter(Instant.now())
            })
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = {
                showTimePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        duration = timePickerState.hour * 60L + timePickerState.minute
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showTimePicker = false
                }) {
                    Text("Cancel")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New play") },
                navigationIcon = {
                    IconButton(onClick = { popBackStack() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    FilledTonalButton(onClick = {
                        if (valid) {
                            scope.launch {
                                gameplayDao.insert(
                                    Gameplay(
                                        boardGameId = gameId,
                                        date = dateInstant,
                                        playtime = duration,
                                        notes = notes
                                    ),
                                    players.map {
                                        PlayerWithScoreDto(
                                            playerName = it.name, score = it.score.toIntOrNull() ?: 0
                                        )
                                    },
                                )
                            }
                            popBackStack()
                        }
                        submitted = true
                    }, modifier = Modifier.padding(end = 16.dp)) {
                        Text("Save")
                    }
                })
        },
        content = {
            LazyColumn(
                modifier = Modifier.padding(it)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = DateFormat.getDateFormat(context).format(dateInstant),
                            readOnly = true,
                            enabled = false,
                            colors = textFieldColors,
                            onValueChange = {},
                            label = { Text("Play date") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = null,
                                )
                            },
                            modifier = Modifier.weight(1f).clickable {
                                openDialog = true
                            }
                        )

                        TextField(
                            value = duration.toTimeString(),
                            readOnly = true,
                            enabled = false,
                            colors = textFieldColors,
                            onValueChange = {},
                            label = { Text("Play time") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = null,
                                )
                            },
                            modifier = Modifier.weight(1f).clickable {
                                showTimePicker = true
                            }
                        )
                    }

                    TextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                stickyHeader {
                    Row(
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface).fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Players",
                            style = MaterialTheme.typography.titleLarge
                        )

                        TextButton(
                            onClick = {
                                newGamePlayViewModel.addPlayer(GameplayPlayer("", ""))
                            }
                        ) {
                            Text("Add player")
                        }
                    }
                }

                itemsIndexed(players, key = { _, player -> player.id }) { index, player ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val invalidScore = submitted && !valid && player.score.toIntOrNull() == null
                        val invalidName = submitted && !valid && player.name.isEmpty()

                        TextField(
                            label = { Text("Score") },
                            value = player.score,
                            onValueChange = {
                                newGamePlayViewModel.updatePlayer(
                                    index, newScore = (it.toIntOrNull() ?: "").toString()
                                )
                            },
                            modifier = Modifier.weight(3f),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            isError = invalidScore,
                            singleLine = true,
                            supportingText = if (invalidScore) {
                                { Text("Score is required") }
                            } else null
                        )

                        TextFieldWithDropdown(
                            label = { Text("Name") },
                            onValueChange = {
                                newGamePlayViewModel.updatePlayer(index, newName = it)
                            },
                            modifier = Modifier.weight(4f),
                            options = alreadyExistingPlayerNames,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Text,
                                autoCorrect = false,
                                imeAction = if (players.size - 1 == index) ImeAction.Done else ImeAction.Next
                            ),
                            isError = invalidName,
                            supportingText = if (invalidName) {
                                { Text("Name is required") }
                            } else null
                        )

                        IconButton(
                            onClick = {
                                newGamePlayViewModel.removePlayer(player)
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        }
    )
}