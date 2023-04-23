package com.example.boardgamestats.utils

import android.content.Context
import android.net.ConnectivityManager

class NetworkManager {
    companion object {
        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return connectivityManager.activeNetworkInfo?.isConnected == true
        }
    }
}