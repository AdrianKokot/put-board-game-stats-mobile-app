package com.example.boardgamestats.api

import android.app.Activity
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log.d
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.boardgamestats.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


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

data class GoogleUserModel(val id: String?, val name: String?, val email: String?)

class SignInGoogleViewModel(application: Application) : AndroidViewModel(application) {
    private var _userState = MutableLiveData<GoogleUserModel>()
    val googleUser: LiveData<GoogleUserModel> = _userState
    private var _loadingState = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loadingState
    fun fetchSignInUser(id: String?, email: String?, name: String?) {
        _loadingState.value = true
        viewModelScope.launch {
            _userState.value =
                GoogleUserModel(
                    id = id,
                    email = email,
                    name = name,
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
}

@Composable
fun AuthScreen() {
    val mSignInViewModel: SignInGoogleViewModel = viewModel()
    val state = mSignInViewModel.googleUser.observeAsState()
    val user = state.value

    var idToken: String? = null
    GoogleSignIn.getLastSignedInAccount(LocalContext.current)?.let {
        mSignInViewModel.fetchSignInUser(it.id, it.email, it.displayName)
        idToken = it.idToken
    }

    val isError = remember { mutableStateOf(false) }

    val authResultLauncher =
        rememberLauncherForActivityResult(contract = GoogleApiContract()) { task ->
            try {
                val gsa = task?.getResult(ApiException::class.java)
                if (gsa != null) {

                    mSignInViewModel.fetchSignInUser(gsa.id, gsa.email, gsa.displayName)
                } else {
                    isError.value = true
                }
            } catch (e: ApiException) {
                d("Error in AuthScreen%s", e.toString())
            }
        }

    val clipboard = LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager


    Column {

        AuthView(
            onClick = { authResultLauncher.launch(0) },
            isError = isError.value,
            mSignInViewModel
        )

        Text(text = idToken ?: "", modifier = Modifier.selectable(selected = true, onClick = {
            val clip = ClipData.newPlainText("label", idToken ?: "")
            clipboard.setPrimaryClip(clip)
        }))

        Button(onClick = {

            GlobalScope.launch {

                val client = OkHttpClient().newBuilder().build()
                val requestBody = "{\"idToken\": \"$idToken\"}".toRequestBody("application/json".toMediaTypeOrNull())

                val request = Request.Builder()
                    .url("https://ambitious-bush-06681451a3e04facaa59cf7ee7abfb1f.azurewebsites.net")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    for ((name, value) in response.headers) {
                        println("$name: $value")
                    }
                    println(response.body!!.string())
                }
            }

        }) {
            Text("API call")
        }
    }
}

@Composable
fun AuthView(
    onClick: () -> Unit,
    isError: Boolean = false,
    mSignInViewModel: SignInGoogleViewModel
) {
    Button(
        onClick = {
            mSignInViewModel.showLoading()
            onClick()
        }
    ) {
        Text(text = "Sign in with Google")
    }
}