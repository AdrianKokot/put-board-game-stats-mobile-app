package com.example.boardgamestats.api

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.boardgamestats.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch


class GoogleApiContract : ActivityResultContract<Int?, Task<GoogleSignInAccount>?>() {

    override fun createIntent(context: Context, input: Int?): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.gcp_id))
            .requestId()
            .requestEmail()
            .build()

        val intent = GoogleSignIn.getClient(context, gso)
        return intent.signInIntent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Task<GoogleSignInAccount>? {
        return when (resultCode) {
            Activity.RESULT_OK -> {
                GoogleSignIn.getSignedInAccountFromIntent(intent)
            }

            else -> null
        }
    }
}

data class GoogleUserModel(val id: String?, val name: String?, val email: String?, val photoUrl: String?)

class SignInGoogleViewModel(application: Application) : AndroidViewModel(application) {
    private var _userState = MutableLiveData<GoogleUserModel>()
    val googleUser: LiveData<GoogleUserModel> = _userState
    private var _loadingState = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loadingState
    fun fetchSignInUser(id: String?, email: String?, name: String?, photoUrl: String?) {
        _loadingState.value = true
        viewModelScope.launch {
            _userState.value =
                GoogleUserModel(
                    id = id,
                    email = email,
                    name = name,
                    photoUrl = photoUrl
                )
        }
        _loadingState.value = false
    }

    fun hideLoading() {
        _loadingState.value = false
    }

    fun showLoading() {
        _loadingState.value = true
    }

    fun loadAlreadySignedUser() {
        GoogleSignIn.getLastSignedInAccount(getApplication())?.let {
            fetchSignInUser(it.id, it.email, it.displayName, it.photoUrl.toString())
        }
    }
}