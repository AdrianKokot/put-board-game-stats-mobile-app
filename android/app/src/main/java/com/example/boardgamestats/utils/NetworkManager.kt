package com.example.boardgamestats.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

class NetworkManager {
    companion object {
        suspend fun isInternetAvailable(): Boolean {
            return try {
                val ipAddr: InetAddress = withContext(Dispatchers.IO) {
                    InetAddress.getByName("google.com")
                }

                !ipAddr.equals("")
            } catch (e: Exception) {
                false
            }
        }
    }
}