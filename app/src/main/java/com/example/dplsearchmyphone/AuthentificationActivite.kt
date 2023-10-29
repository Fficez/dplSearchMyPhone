package com.example.dplsearchmyphone

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

private val RC_SIGN_IN = 199504
class AuthentificationActivite : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun onLoginButtonClick(view: View) {
        val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val mapIntent = Intent(this, MapViewActivity::class.java)
                    startActivity(mapIntent)
            } else {
                //аутентификация не удалась
                if (response == null) {
                    //прервал аутентификацию
                } else if (response.error?.errorCode == ErrorCodes.NO_NETWORK) {
                    // ошибка нет сети
                } else if (response.error?.errorCode == ErrorCodes.UNKNOWN_ERROR) {
                    //неизвестная ошибка
                }
            }
            }

        }
    }

}