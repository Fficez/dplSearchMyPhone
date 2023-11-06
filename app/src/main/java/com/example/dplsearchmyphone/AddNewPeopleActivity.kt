package com.example.dplsearchmyphone

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

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

    fun buttonClickAddUids(view: View) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val refUid = findViewById<EditText>(R.id.refsUidText)
        val userInput = refUid.text.toString()
        val uidDataBase = FirebaseDatabase.getInstance().getReference("acceptedUids").child(userId ?: "").child("AcceptedUids")
        uidDataBase.child(userInput).setValue(true)
    }
}