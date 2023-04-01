package com.example.boardgamestats.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.boardgamestats.ui.components.BoardGamesSearchBar
import com.example.boardgamestats.ui.components.BottomNavigationBar
import com.example.boardgamestats.ui.components.BottomNavigationGraph

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        },
        topBar = {
            BoardGamesSearchBar()
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            BottomNavigationGraph(navController)
        }
    }
}