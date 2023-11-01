package com.example.dplsearchmyphone

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class AddNewPeopleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_new_people)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            val myUID = auth.currentUser!!.uid

            val textViewUID = findViewById<TextView>(R.id.textViewMyUID)
            textViewUID.text = "Your UID: $myUID"
        }
    }
}