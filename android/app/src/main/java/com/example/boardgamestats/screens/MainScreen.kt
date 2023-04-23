package com.example.boardgamestats.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.boardgamestats.navigation.BottomNavigationGraph
import com.example.boardgamestats.navigation.GameNavigation
import com.example.boardgamestats.navigation.MainNavigation
import com.example.boardgamestats.ui.components.BoardGamesSearchBar
import com.example.boardgamestats.ui.components.BottomNavigationBar

@Composable
fun MainScreen(rootNavController: NavController) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        },
        topBar = {
            BoardGamesSearchBar(
                navigateToDetails = {
                    rootNavController.navigate(GameNavigation.detailsScreen(it))
                },
                navigateToUserSettings = {
                    rootNavController.navigate(MainNavigation.UserSettingsScreen)
                }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            BottomNavigationGraph(navController, rootNavController)
        }
    }
}


