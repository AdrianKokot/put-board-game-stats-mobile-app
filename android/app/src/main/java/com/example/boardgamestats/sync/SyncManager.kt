package com.example.boardgamestats.sync

import android.accounts.Account
import android.content.ContentResolver
import android.content.Context
import android.os.Bundle

class SyncManager {
    companion object {
        private const val AUTHORITY = "com.example.boardgamestats.sync"
        private const val ACCOUNT_TYPE = "com.example.boardgamestats"
        private const val ACCOUNT = "stubaccount"
        private val account = Account(ACCOUNT, ACCOUNT_TYPE)

        fun setup(context: Context) {
            val accountManager = android.accounts.AccountManager.get(context)
            accountManager.addAccountExplicitly(account, null, null)
        }

        fun forceSync() {
            val bundle = Bundle()
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)

            ContentResolver.requestSync(account, AUTHORITY, bundle)
        }
    }
}