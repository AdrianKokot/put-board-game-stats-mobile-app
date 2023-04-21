package com.example.boardgamestats.sync

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import android.util.Log
import com.example.boardgamestats.R
import com.example.boardgamestats.database.BoardGameDatabase
import com.example.boardgamestats.database.daos.*
import com.example.boardgamestats.models.BoardGame
import com.google.android.gms.auth.api.signin.GoogleSignIn
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalSerializationApi::class)
class SyncAdapter(private val context: Context, autoInitialize: Boolean) :
    AbstractThreadedSyncAdapter(context, autoInitialize) {
    override fun onPerformSync(p0: Account?, p1: Bundle?, p2: String?, p3: ContentProviderClient?, p4: SyncResult?) {
        val syncDao = BoardGameDatabase.getDatabase(context).syncDao()

        val token = GoogleSignIn.getLastSignedInAccount(context)?.idToken ?: "test";

        val json = buildJsonObject {
            put("idToken", token)
            put("syncData", Json.encodeToJsonElement(syncDao.getSyncData()))
        }

        val apiUrl = context.getString(R.string.sync_server_address)

        val client = OkHttpClient().newBuilder()
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()

        Log.d("SyncAdapter", json.toString())
        val requestBody = json.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(apiUrl)
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            Log.d("SyncAdapter", "got response ${response.code} ${response.body!!.toString()}")
            val responseSyncData = Json.decodeFromStream<SyncData>(response.body!!.byteStream())
            processSyncData(responseSyncData)
        }
    }

    private fun processSyncData(responseSyncData: SyncData) {
        val database = BoardGameDatabase.getDatabase(context)
        val syncDao = database.syncDao()

        syncDao.insertBoardGame(
            *responseSyncData.plays.boardGames
                .map {
                    BoardGame(
                        id = it.id,
                        name = it.name,
                        thumbnail = it.thumbnail, publishYear = it.publishYear
                    )
                }.toTypedArray()
        )

        responseSyncData.boardGames.addedToCollection.forEach {
            syncDao.syncCollectionItem(it)
        }

        responseSyncData.boardGames.removedFromCollection.forEach {
            syncDao.syncCollectionItem(it)
        }

        responseSyncData.plays.deleted.forEach {
            syncDao.syncGameplay(it)
        }

        responseSyncData.plays.added.forEach {
            syncDao.syncGameplay(it)
        }

        syncDao.setLastSync(responseSyncData.currentSync)
    }
}