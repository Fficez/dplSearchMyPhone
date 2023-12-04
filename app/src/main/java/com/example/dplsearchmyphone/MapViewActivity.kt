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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private val handler = Handler()
private lateinit var locationUpdateRunnable: Runnable
private const val REQUEST_LOC_PERMISSION = 5213451
private val database = FirebaseDatabase.getInstance()
private lateinit var polyline: Polyline
private var previousMarker: Marker? = null
private var routeCoordinates = mutableListOf<LatLng>()
private val userCoordinatesMap = mutableMapOf<String, MutableList<LatLng>>()
private val userMarkersMap = mutableMapOf<String, Marker?>()
private val userRoutesMap = mutableMapOf<String, Polyline>()
private val userLastCoordinatesMap = mutableMapOf<String, LatLng>()
private val googleMap: GoogleMap? = null
private val userRouteMap: MutableMap<String, Polyline> = HashMap()
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

        val serviceIntent = Intent(this, LocationServices::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    mapView.getMapAsync { googleMap ->
                        val userLocation = LatLng(latitude, longitude)
                        googleMap.addMarker(
                            MarkerOptions().position(userLocation).title("My Location")
                        )
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                    }
                }
            }
        }
    }

    private fun setupLocationUpdates() {
        // val locationRequest = com.google.android.gms.location.LocationRequest.create().setPriority(LocationRequest.QUALITY_HIGH_ACCURACY)

        locationUpdateRunnable = object : Runnable {
            override fun run() {
                updateLocationToFirebase()
                iSeeDeadPeople()
                handler.postDelayed(this, 1000)
            }
        }
        locationUpdateRunnable.run()
    }

    private fun updateLocationToFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userCoordinatesMap[userId ?: ""]?.clear()
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
                    val coordinates = LatLng(latitude, longitude)

                    //val database = FirebaseDatabase.getInstance()
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    val locationRef =
                        FirebaseDatabase.getInstance().getReference("locations").child(userId ?: "")
                    val userLocation = userId?.let { LocationData(it, latitude, longitude) }
                    locationRef.setValue(userLocation)
                        .addOnSuccessListener {
                            Log.d(
                                "Firebase",
                                "Data written successfully, $latitude, $longitude"
                            )
                        }
                        .addOnFailureListener { Log.d("Firebase", "Data written failure") }

                    userCoordinatesMap.getOrPut(userId ?: "") { mutableListOf() }.add(coordinates)
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

        //routeCoordinates.clear()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("Firebase", "User provided access: $userId")
        val locationRef = FirebaseDatabase.getInstance().reference
        Log.d("Firebase", "User provided access: $locationRef")
        val acceptedUidsRef =
            userId?.let { locationRef.child("acceptedUids").child(it).child("AcceptedUids") }
        Log.d("Firebase", "User provided access: $acceptedUidsRef")

        acceptedUidsRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (userMarker in userMarkersMap.values) {
                    userMarker?.remove()
                }
                userMarkersMap.clear()

                for (childSnapshot in dataSnapshot.children) {
                    val acceptedUid = childSnapshot.key
                    val acceptedValue = childSnapshot.value

                    val coordinatesList = userCoordinatesMap[acceptedUid ?: ""]

                    if (acceptedValue is Boolean && acceptedValue) {
                        // Этот пользователь предоставил доступ
                        Log.d("Firebase", "User provided access: $acceptedUid")

                        // Получите данные о местоположении этого пользователя
                        val userLocationRef =
                            locationRef.child("locations").child(acceptedUid ?: "")

                        userLocationRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(userLocationDataSnapshot: DataSnapshot) {
                                val latitude = userLocationDataSnapshot.child("latitude")
                                    .getValue(Double::class.java)
                                val longitude = userLocationDataSnapshot.child("longitude")
                                    .getValue(Double::class.java)

                                routeCoordinates.add(LatLng(latitude!!, longitude!!))
                                val userLocation = LatLng(latitude, longitude)
                                userRoutesMap[acceptedUid]?.remove()
                                val acceptedUid = userLocationDataSnapshot.key ?: ""

                                updateRouteForUser(acceptedUid, userLocation)

                                /*val routeCoordinates = coordinatesList ?: mutableListOf()
                                routeCoordinates.add(userLocation)
                                Log.d("ROUTECOORDINATES", "ALL COORDINATES: $routeCoordinates")
                                //if (latitude != null && longitude != null) {
                                // Создайте маркер на карте
                                Log.d("Firebase", "User Location: $latitude, $longitude")
                                val mapView = findViewById<MapView>(R.id.mapView)
                                mapView.getMapAsync { googleMap ->
                                    //создать линию на карте
                                    val lastCoordinates = userLastCoordinatesMap[acceptedUid]
                                    if (lastCoordinates != null) {
                                        userRoutesMap[acceptedUid]?.remove()
                                    }
                                        //Линии для каждого пчелика
                                    if (userLocation != null && lastCoordinates != null) {
                                        val polyline = googleMap.addPolyline(
                                            PolylineOptions().add(lastCoordinates, userLocation)
                                                .color(Color.BLUE)
                                        )
                                        userRoutesMap[acceptedUid ?: ""] = polyline
                                        //Для каждого
                                        userRoutesMap[acceptedUid ?: ""] = polyline
                                    }
                                    // Создайте маркер на карте
                                    if (!userMarkersMap.containsKey(acceptedUid)) {
                                        val userLocation = LatLng(latitude, longitude)
                                        val marker = googleMap.addMarker(
                                            MarkerOptions().position(userLocation)
                                                .title(acceptedUid)
                                        )
                                        userMarkersMap[acceptedUid ?: ""] = marker
                                    } else {
                                        val userMarker = userMarkersMap[acceptedUid]
                                        userMarker?.position = userLocation

                                    }
                                }*/
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

    private fun updateRouteForUser(acceptedUid: String, userLocation: LatLng) {
        val mapView = findViewById<MapView>(R.id.mapView)
        mapView.getMapAsync { googleMap ->
            val lastCoordinates = userLastCoordinatesMap[acceptedUid]
            val coordinatesList = userCoordinatesMap[acceptedUid ?: ""]
            // Удаление старой полилинии
            userRoutesMap[acceptedUid]?.remove()

            // Добавление новой координаты в маршрут
            val routeCoordinates = coordinatesList ?: mutableListOf()
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
                        .title(acceptedUid)
                )
                userMarkersMap[acceptedUid] = marker
            } else {
                val userMarker = userMarkersMap[acceptedUid]
                userMarker?.position = userLocation
            }
        }
    }

    fun onClickShowAllPeople(view: View) {
        val mapIntent = Intent(this, UserListActivity::class.java)
        startActivity(mapIntent)
    }

}