package com.example.boardgamestats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.boardgamestats.navigation.RootNavigationGraph
import com.example.boardgamestats.sync.SyncManager
import com.example.boardgamestats.ui.theme.BoardGameStatsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        SyncManager.setup(this)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            BoardGameStatsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    RootNavigationGraph(rememberNavController())
                }
            }
        }
    }
}