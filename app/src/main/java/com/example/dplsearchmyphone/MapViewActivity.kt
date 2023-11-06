package com.example.dplsearchmyphone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationRequest
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

private val handler = Handler()
private lateinit var locationUpdateRunnable : Runnable
private const val REQUEST_LOC_PERMISSION = 5213451
private val database = FirebaseDatabase.getInstance()
class MapViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContentView(R.layout.activity_map_view)
        val mapView = findViewById<MapView>(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()

        setupLocationUpdates()

        try {
            MapsInitializer.initialize(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
//        {
//            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
//                if (location != null) {
//                    val latitude = location.latitude
//                    val longitude = location.longitude
//                    mapView.getMapAsync { googleMap ->
//                        val userLocation = LatLng(latitude, longitude)
//                        googleMap.addMarker(
//                            MarkerOptions().position(userLocation).title("My Location")
//                        )
//                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
//                    }
//                }
//            }
//        }
    }

    private fun setupLocationUpdates() {
        locationUpdateRunnable = object : Runnable {
            override fun run() {
                updateLocationToFirebase()
                iSeeDeadPeople()
                handler.postDelayed(this, 7000)
            }
        }
        locationUpdateRunnable.run()
    }

    private fun updateLocationToFirebase() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    //val database = FirebaseDatabase.getInstance()
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    val locationRef = FirebaseDatabase.getInstance().getReference("locations").child(userId ?: "")
                    val userLocation = userId?.let { LocationData(it, latitude, longitude) }
                    locationRef.setValue(userLocation)
                        .addOnSuccessListener { Log.d("Firebase", "Data written successfully, $latitude, $longitude") }
                        .addOnFailureListener { Log.d("Firebase", "Data written failure") }

//                    val mapView = findViewById<MapView>(R.id.mapView)
//                    mapView.getMapAsync { googleMap ->
//                        val userLocatin = LatLng(latitude, longitude)
//                        googleMap.addMarker(
//                            MarkerOptions().position(userLocatin).title("My Location")
//                        )
//                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocatin, 15f))
//                    }
                }
            }
        }
    }

    fun onAddPeopleClick(view: View) {
        val mapIntent = Intent(this, AddNewPeopleActivity::class.java)
        startActivity(mapIntent)
    }

    private fun iSeeDeadPeople() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val locationRef = FirebaseDatabase.getInstance().reference.child("locations")
        val acceptedUidsRef = userId?.let { locationRef.child(it).child("acceptedUids") }

        acceptedUidsRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    if (childSnapshot.getValue(Boolean::class.java) == true) {
                        // Этот пользователь предоставил доступ
                        val userUid = childSnapshot.key
                        Toast.makeText(applicationContext, userUid, Toast.LENGTH_LONG).show()
                        // Получите данные о местоположении этого пользователя
                        val userLocationRef = FirebaseDatabase.getInstance().reference
                            .child("locations")
                            .child(userUid ?: "")

                        userLocationRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(userLocationDataSnapshot: DataSnapshot) {

                                    val latitude = userLocationDataSnapshot.child("latitude").value as Double
                                    val longitude = userLocationDataSnapshot.child("longitude").value as Double

                                    // Создайте маркер на карте
                                    val userLocation = LatLng(latitude, longitude)
                                    val marker = MarkerOptions()
                                        .position(userLocation)
                                        .title(userUid)
                                val mapView = findViewById<MapView>(R.id.mapView)
                                mapView.getMapAsync { googleMap ->
                                    googleMap.addMarker(marker)
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Обработка ошибки
                            }
                        })
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Обработка ошибки
            }
        })
    }

}