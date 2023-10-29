package com.example.dplsearchmyphone

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.FirebaseDatabase

class MapViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_map_view)
        val mapView = findViewById<MapView>(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    //val userLocation = LatLng(latitude, longitude)

                    try {
                        MapsInitializer.initialize(applicationContext)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    //var googleMap: GoogleMap? = null
                    mapView.getMapAsync { googleMap ->
                        val userLocation = LatLng(latitude, longitude)
                        googleMap.addMarker(MarkerOptions().position(userLocation).title("My Location"))
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f)) }
                }
            }
        }

    }
}