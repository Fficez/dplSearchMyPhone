package com.example.dplsearchmyphone

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dplsearchmyphone.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ControlLocation : AppCompatActivity() {

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control_location_view)
        supportActionBar?.title = "Настройки"
        val mySwitch: Switch = findViewById(R.id.switch_control_location)
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val isChecked: Boolean = mySwitch.isChecked

        val acceptedUidsRef: DatabaseReference = database.getReference("acceptedUids")

        mySwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val database: FirebaseDatabase = FirebaseDatabase.getInstance()
                val acceptedUidsRef: DatabaseReference = database.getReference("acceptedUids")

                acceptedUidsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // Перебираем узлы AcceptedUids
                        for (userSnapshot in dataSnapshot.children) {
                            val uid = userSnapshot.key
                            val acceptedUidsNode = userSnapshot.child("AcceptedUids")

                            // Проверяем наличие пользователя в узле AcceptedUids
                            if (acceptedUidsNode.child(userId).exists()) {
                                // Меняем значение true на false и наоборот
                                val newValue = true

                                // Обновляем значение в базе данных
                                acceptedUidsRef.child(uid ?: "").child("AcceptedUids").child(userId).setValue(newValue)
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(
                            applicationContext,
                            "Ошибка проверки пользователя, повторите позже",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })

                //lflf
            } else {
                val database: FirebaseDatabase = FirebaseDatabase.getInstance()
                val acceptedUidsRef: DatabaseReference = database.getReference("acceptedUids")

                acceptedUidsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // Перебираем узлы AcceptedUids
                        for (userSnapshot in dataSnapshot.children) {
                            val uid = userSnapshot.key
                            val acceptedUidsNode = userSnapshot.child("AcceptedUids")

                            // Проверяем наличие пользователя в узле AcceptedUids
                            if (acceptedUidsNode.child(userId).exists()) {
                                // Меняем значение true на false и наоборот
                                val newValue = false

                                // Обновляем значение в базе данных
                                acceptedUidsRef.child(uid ?: "").child("AcceptedUids").child(userId).setValue(newValue)
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(
                            applicationContext,
                            "Ошибка проверки пользователя, повторите позже",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
            }
        }
    }

    private fun change() {}

}