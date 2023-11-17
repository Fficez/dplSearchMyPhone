package com.example.dplsearchmyphone

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LocationService : Service() {

    private val TAG = "LocationService"

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 7000
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                // Обработка нового местоположения
                Log.d(TAG, "New Location: ${location?.latitude}, ${location?.longitude}")
                //обновление местоположения в базе данных Firebase
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val locationRef = FirebaseDatabase.getInstance().getReference("locations").child(userId ?: "")
                val userLocation = userId?.let {LocationData(it, location!!.latitude, location.longitude)}
                locationRef.setValue(userLocation)
                    .addOnSuccessListener { Log.d("Firebase", "DataWrittenSuccessfully ${location?.latitude}, ${location?.longitude} ") }
                    .addOnFailureListener {Log.d ("Firebase", "data written failure")}
            }
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }
}