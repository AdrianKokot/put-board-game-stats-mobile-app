package com.example.boardgamestats.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
fun RootNavigationGraph(navHostController: NavHostController) {
    NavHost(navHostController, startDestination = MainNavigation.RootRoute) {
        GameNavigationGraph(navHostController)
        MainNavigationGraph(navHostController)
    }
}