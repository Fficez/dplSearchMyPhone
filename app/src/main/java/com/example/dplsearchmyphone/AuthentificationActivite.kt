package com.example.dplsearchmyphone

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

private val RC_SIGN_IN = 199504
class AuthentificationActivite : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page_authentification)

        FirebaseApp.initializeApp(this)
        val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
        val authIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        val permissionLocation = Manifest.permission.ACCESS_FINE_LOCATION
        val requestCodeLocation = 1992355121 //код запроса разрешения
        if (ContextCompat.checkSelfPermission(this, permissionLocation) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permissionLocation), requestCodeLocation)
        }
        val permissionInternet = Manifest.permission.INTERNET
        val requestCodeInternet = 1992355122 //код запроса разрешения
        if (ContextCompat.checkSelfPermission(this, permissionInternet) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permissionInternet), requestCodeInternet)
        }
        val permissionNetwork = Manifest.permission.ACCESS_NETWORK_STATE
        val requestCodeNetwork = 1992355123 //код запроса разрешения
        if (ContextCompat.checkSelfPermission(this, permissionNetwork) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permissionNetwork), requestCodeNetwork)
        }
        val permissionCoarseLocation = Manifest.permission.ACCESS_COARSE_LOCATION
        val requestCodeCoarseLocation = 1992355124 //код запроса разрешения
        if (ContextCompat.checkSelfPermission(this, permissionCoarseLocation) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permissionCoarseLocation), requestCodeCoarseLocation)
        }
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