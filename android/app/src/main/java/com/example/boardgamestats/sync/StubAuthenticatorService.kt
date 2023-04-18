package com.example.boardgamestats.sync

import android.app.Service
import android.content.Intent

import android.os.IBinder

class StubAuthenticatorService : Service() {
    private var authenticator: StubAuthenticator? = null

    override fun onCreate() {
        authenticator = StubAuthenticator(this)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return authenticator!!.iBinder
    }
}