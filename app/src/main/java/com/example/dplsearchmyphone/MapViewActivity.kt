package com.example.dplsearchmyphone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.MarkerOptions

class MapViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_view)
        val mapView = findViewById<MapView>(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()

        try {
            MapsInitializer.initialize(applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mapView.getMapAsync(googleMap -> val userLocation = LatLng(latitude, longitude)
        googleMap.addMarker(MarkerOptions().position(userLocation).title("My Location"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f)))

    }
}