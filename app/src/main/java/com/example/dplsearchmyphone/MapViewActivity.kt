package com.example.dplsearchmyphone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MapViewActivity : AppCompatActivity() {

    private val locationUpdateHandler = Handler()
    private val locationUpdateInterval = 5000L
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val userCoordinatesMap = mutableMapOf<String, MutableList<LatLng>>()
    private val userMarkersMap = mutableMapOf<String, Marker?>()
    private val userRoutesMap = mutableMapOf<String, Polyline>()
    private val MY_PERMISSIONS_REQUEST_FOREGROUND_SERVICE = 1

    private val updateLocationRunnable = object : Runnable {
        override fun run() {
            updateMyLocation()
            locationUpdateHandler.postDelayed(this, locationUpdateInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

       val serviceIntent = Intent(this, LocationForegroundService::class.java)
       ContextCompat.startForegroundService(this, serviceIntent)

        setContentView(R.layout.activity_map_view)
        val mapView = findViewById<MapView>(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()
        checkAndAddMyDataInDB()
        //Добавить кнопки зума
        mapView.getMapAsync { googleMap ->
            //Приближение карты минска
            val cityBounds = LatLngBounds(
                LatLng(53.825495, 27.340865),
                LatLng(53.986034, 27.738183)
            )
            val padding = 10
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(cityBounds, padding))
            googleMap.uiSettings.isZoomControlsEnabled = true
        }

        setupLocationUpdates()
        try {
            MapsInitializer.initialize(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        startLocationUpdates()
    }

    private fun setupLocationUpdates() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userLocationRef: DatabaseReference =
            database.getReference("locations").child(userId ?: "")

        val acceptedUidsRef: DatabaseReference =
            database.getReference("acceptedUids").child(userId ?: "").child("AcceptedUids")

        // Добавляем слушатель изменений для AcceptedUids
        acceptedUidsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val acceptedUid = childSnapshot.key

                    val value = childSnapshot.getValue(Boolean::class.java)
                    // Проверка, что у пользователя разрешен доступ
                    if (value == true) {
                        Log.d("Firebaseeee", "+++ $acceptedUid")
                        // Запрос координат из узла "locations" для данного acceptedUid
                        val otherUserLocationRef: DatabaseReference =
                            database.getReference("locations").child(acceptedUid ?: "")
                        otherUserLocationRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(otherUserLocationDataSnapshot: DataSnapshot) {
                                val otherLatitude = otherUserLocationDataSnapshot.child("latitude")
                                    .getValue(Double::class.java)
                                val otherLongitude =
                                    otherUserLocationDataSnapshot.child("longitude")
                                        .getValue(Double::class.java)

                                if (otherLatitude != null && otherLongitude != null) {
                                    val otherUserLocation = LatLng(otherLatitude, otherLongitude)
                                    // Вызываем функцию для обновления карты для другого пользователя
                                    updateRouteForUser(
                                        acceptedUid ?: "",
                                        otherUserLocation,
                                        userId ?: ""
                                    )
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Обработка ошибки
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Обработка ошибки
            }
        })

        // Добавляем слушатель изменений для своих координат
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            fusedLocationClient.requestLocationUpdates(
                LocationRequest.create(),
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        val location = locationResult.lastLocation
                        if (location != null) {
                            val latitude = location.latitude
                            val longitude = location.longitude
                            val userLocation = LatLng(latitude, longitude)

                            // Обновляем координаты в базе данных
                            userLocationRef.setValue(
                                LocationData(
                                    userId ?: "",
                                    latitude,
                                    longitude
                                )
                            )
                                .addOnSuccessListener {
                                    Log.d("Firebase", "hi my name is")
                                }
                                .addOnFailureListener {
                                    Log.e("Firebase", "Failed to update user location")
                                }

                            // Вызываем функцию для обновления карты для себя
                            updateRouteForUser(userId ?: "", userLocation, userId ?: "")
                        }
                    }
                },
                null
            )
        }
    }

    private fun updateRouteForUser(acceptedUid: String, userLocation: LatLng, userUid: String) {
        val mapView = findViewById<MapView>(R.id.mapView)
        //получение Name
        val databaseReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("named").child(userUid).child(acceptedUid)
                .child("name")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name: String? = dataSnapshot.getValue(String::class.java)

                mapView.getMapAsync { googleMap ->
                    val lastCoordinates = userCoordinatesMap[acceptedUid]?.lastOrNull()

                    // Добавление новой координаты в маршрут
                    val routeCoordinates =
                        userCoordinatesMap.getOrPut(acceptedUid) { mutableListOf() }
                    routeCoordinates.add(userLocation)

                    // Добавление новой полилинии на карту
                    if (lastCoordinates != null) {
                        val polyline = googleMap.addPolyline(
                            PolylineOptions().add(lastCoordinates, userLocation)
                                .color(Color.BLUE)
                        )
                        userRoutesMap[acceptedUid] = polyline
                    }

                    // Обновление маркера на карте
                    if (!userMarkersMap.containsKey(acceptedUid)) {
                        val marker = googleMap.addMarker(
                            MarkerOptions().position(userLocation)
                                .title(name)
                        )
                        userMarkersMap[acceptedUid] = marker
                    } else {
                        val userMarker = userMarkersMap[acceptedUid]
                        userMarker?.position = userLocation
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                println("Ошибка получения из бд")
            }
        })


    }

    private fun startLocationUpdates() {
        locationUpdateHandler.postDelayed(updateLocationRunnable, locationUpdateInterval)
    }

    private fun stopLocationUpdates() {
        locationUpdateHandler.removeCallbacks(updateLocationRunnable)
    }

    private fun updateMyLocation() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userLocationRef: DatabaseReference =
            database.getReference("locations").child(userId ?: "")
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    userLocationRef.setValue(LocationData(userId ?: "", latitude, longitude))
                        .addOnSuccessListener {
                            //updateRouteForUser(userId ?: "", userLocation, userId ?: "")
                            Log.d("Firebase", "User location updated successfully THIS")
                        }
                        .addOnFailureListener {
                            Log.e("Firebase", "Failed to update user location")
                        }
                }
            }
        }
    }

    fun onAddPeopleClick(view: View) {
        val mapIntent = Intent(this, AddNewPeopleActivity::class.java)
        startActivity(mapIntent)
    }

    override fun onDestroy() {
        val serviceIntent = Intent(this, LocationForegroundService::class.java)
        stopService(serviceIntent)
        finishAffinity()
        super.onDestroy()
    }

    fun controlLocationClickButton(view: View) {
        val mapIntent = Intent(this, ControlLocation::class.java)
        startActivity(mapIntent)
    }

    private fun checkAndAddMyDataInDB() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val acceptedUidsRef: DatabaseReference =
            database.getReference("acceptedUids").child(userId ?: "").child("AcceptedUids")
        val name = "user"
                    //добавление UID в бд
                    val uidDataBase =
                        FirebaseDatabase.getInstance().getReference("acceptedUids")
                            .child(userId ?: "")
                            .child("AcceptedUids")
                    uidDataBase.child(userId!!).setValue(true)
                    //добавление имени в ветку бд named
                    val nameUidDataBase =
                        FirebaseDatabase.getInstance().getReference("named").child(userId ?: "")
                            .child(userId).child("name")
                    nameUidDataBase.setValue(name)
    }
}