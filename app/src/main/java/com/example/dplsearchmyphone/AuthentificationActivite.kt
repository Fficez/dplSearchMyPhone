package com.example.dplsearchmyphone

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

private val RC_SIGN_IN = 199504

class AuthentificationActivite : AppCompatActivity() {

    private val REQUEST_LOCATION_PERMISSION = 123
    private val REQUEST_NOTIFICATION_PERMISSION = 12315

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page_authentification)

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION ),
                REQUEST_LOCATION_PERMISSION
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS ),
                REQUEST_NOTIFICATION_PERMISSION
            )
        }
        checkBackgroundLocationPermission()
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
                    // неизвестная ошибка
                }
            }
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                ) {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        Toast.makeText(applicationContext, "Authentication success", Toast.LENGTH_SHORT).show()
                        //val mapIntent = Intent(this, MapViewActivity::class.java)
                        //startActivity(mapIntent)
                    } else {
                        //аутентификация не удалась
                        Toast.makeText(applicationContext, "warning", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun checkBackgroundLocationPermission() {
        val permissionStatus = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            showBackgroundLocationRationale()
            return
        }
        when (permissionStatus) {
            PackageManager.PERMISSION_GRANTED -> {
                // Разрешение уже предоставлено
                // Ваш код для работы с местоположением в фоне
            }
            else -> {
                // Разрешение не предоставлено, направляем пользователя в настройки
                showBackgroundLocationRationale()
            }
        }
    }

    private fun showBackgroundLocationRationale() {
        if (!isFinishing) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Background Location Permission")
            builder.setMessage("This app requires background location permission. Please enable it in the app settings.")

            builder.setPositiveButton("Go to Settings") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "please update the app for background location settings",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            builder.show()
        }
    }
}