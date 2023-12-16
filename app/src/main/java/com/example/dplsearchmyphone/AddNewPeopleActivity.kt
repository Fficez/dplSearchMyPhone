package com.example.dplsearchmyphone

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.dplsearchmyphone.R.color
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddNewPeopleActivity : AppCompatActivity() {

    private val databaseAllUsers: FirebaseDatabase = FirebaseDatabase.getInstance()

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
        val fieldName = findViewById<EditText>(R.id.addNameField)
        val userInput = refUid.text.toString()
        val nameInput = fieldName.text.toString()
        //Проверка что бы в поле Name было написано хоть что-то
        if (nameInput.isEmpty()) {
            fieldName.backgroundTintList = ContextCompat.getColorStateList(this, color.red)
            Toast.makeText(applicationContext, "Введите имя", Toast.LENGTH_SHORT).show()
            return
        } else {
            fieldName.backgroundTintList = ContextCompat.getColorStateList(this, color.black)

            //Проверка на существование пользователя в бд
            val checkUserInDataBase = databaseAllUsers.getReference("locations")
            checkUserInDataBase.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.hasChild(userInput)) {
                        //добавление UID в бд
                        val uidDataBase =
                            FirebaseDatabase.getInstance().getReference("acceptedUids")
                                .child(userId ?: "")
                                .child("AcceptedUids")
                        uidDataBase.child(userInput).setValue(true)
                        //добавление имени в ветку бд named
                        val nameUidDataBase =
                            FirebaseDatabase.getInstance().getReference("named").child(userId ?: "")
                                .child(userInput).child("name")
                        nameUidDataBase.setValue(nameInput)
                        //После добавления сразу открываем экран с картой
                        val mapIntent =
                            Intent(this@AddNewPeopleActivity, MapViewActivity::class.java)
                        startActivity(mapIntent)
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Ошибка. Такого пользователя не существует, проверьте UID",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
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