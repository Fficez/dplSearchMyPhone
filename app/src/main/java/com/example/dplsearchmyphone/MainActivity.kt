package com.example.dplsearchmyphone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
    }

}