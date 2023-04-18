package com.example.boardgamestats

import android.accounts.Account
import android.content.ContentResolver
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.boardgamestats.navigation.RootNavigationGraph
import com.example.boardgamestats.ui.theme.BoardGameStatsTheme


const val AUTHORITY = "com.example.boardgamestats.sync"
const val ACCOUNT_TYPE = "com.example.boardgamestats"
const val ACCOUNT = "stubaccount"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val newAccount = Account(ACCOUNT, ACCOUNT_TYPE)
        val accountManager = android.accounts.AccountManager.get(this)
        accountManager.addAccountExplicitly(newAccount, null, null)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            BoardGameStatsTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    RootNavigationGraph(rememberNavController())
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Button(modifier = Modifier.align(Alignment.Center), onClick = {
                        val bundle = Bundle()
                        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
                        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)

                        ContentResolver.requestSync(newAccount, AUTHORITY, bundle)
                    }) {
                        Text("Sync")
                    }
                }
            }
        }
    }
}