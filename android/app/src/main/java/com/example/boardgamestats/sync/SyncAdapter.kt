package com.example.boardgamestats.sync

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import com.example.boardgamestats.R
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class SyncAdapter(private val context: Context, autoInitialize: Boolean) : AbstractThreadedSyncAdapter(context, autoInitialize) {
    override fun onPerformSync(p0: Account?, p1: Bundle?, p2: String?, p3: ContentProviderClient?, p4: SyncResult?) {
        Log.d("SyncAdapter", "********** ************* **********")
        Log.d("SyncAdapter", "********** onPerformSync **********")
        Log.d("SyncAdapter", "********** ************* **********")

        val apiUrl = context.getString(R.string.sync_server_address)
//"        GlobalScope.launch {"

            val client = OkHttpClient().newBuilder().build()
            val requestBody = "{\"idToken\": \"test\"}".toRequestBody("application/json".toMediaTypeOrNull())

            val request = Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                for ((name, value) in response.headers) {
                    println("$name: $value")
                }
                println(response.body!!.string())
            }
//"        }"

        Log.d("SyncAdapter", "********** ************* **********")
        Log.d("SyncAdapter", "**********      END     **********")
        Log.d("SyncAdapter", "********** ************* **********")
    }
}