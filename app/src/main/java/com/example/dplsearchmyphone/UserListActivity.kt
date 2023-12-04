package com.example.dplsearchmyphone

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserListActivity : AppCompatActivity() {

    private lateinit var usersListView: ListView
    private lateinit var usersAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_list_activity)

        usersListView = findViewById(R.id.usersListView)
        usersAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        usersListView.adapter = usersAdapter

        // Подобно тому, как вы делали в MapViewActivity, добавьте код для отображения списка пользователей
        startUserListTracking()
    }

    private fun startUserListTracking() {
        val usersReference = FirebaseDatabase.getInstance().getReference("AcceptedUids")

        usersReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                usersAdapter.clear()

                for (userSnapshot in dataSnapshot.children) {
                    val uid = userSnapshot.key
                    val name = userSnapshot.child("name").getValue(String::class.java)

                    if (uid != null && name != null) {
                        val userInfo = "$name ($uid)"
                        usersAdapter.add(userInfo)
                    }
                }
                usersAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обработка ошибки
            }
        })

        usersListView.setOnItemClickListener { _, _, position, _ ->
            // Обработка выбора пользователя из списка, если нужно
            // Например, можно отобразить его на карте или выполнять другие действия
            // ...
        }
    }

}