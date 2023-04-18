package com.example.boardgamestats.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder

class SyncAdapterService : Service() {
    private var syncAdapter: SyncAdapter? = null
    private val syncAdapterLock = Any()

    override fun onCreate() {
        super.onCreate()

        synchronized(syncAdapterLock) {
            if (syncAdapter == null) {
                syncAdapter = SyncAdapter(applicationContext, true)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return syncAdapter!!.syncAdapterBinder
    }
}