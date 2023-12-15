package com.example.dplsearchmyphone

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

private const val CHANNEL_ID = "LocationUpdateServiceChannel"
private const val NOTIFICATION_ID = 123551541

class LocationForegroundService : Service() {

    private val locationUpdateHandler = Handler()
    private val locationUpdateInterval = 5000L  // Интервал обновления координат (в миллисекундах)
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private val updateLocationRunnable = object : Runnable {
        override fun run() {
            updateMyLocation()
            locationUpdateHandler.postDelayed(this, locationUpdateInterval)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("LocationUpdateService", "Service Created")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        createNotificationChannel()

        startLocationUpdates()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun createNotificationChannel() {
        Log.d("Notification create", "notification chanel create")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Update Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun stopService(name: Intent?): Boolean {
        stopForeground(true)
        stopSelf()
        return super.stopService(name)
    }

    private fun createNotification(): Notification {
        Log.d("notification create", "notification create")
        val notificationIntent = Intent(this, MapViewActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Update Service")
            .setContentText("Service is running...")
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LocationUpdateService", "Service Started")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("LocationUpdateService", "Service Destroyed")
        stopLocationUpdates()
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Метод onBind() для нас сейчас они не требуются
        return null
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
            FirebaseDatabase.getInstance().getReference("locations").child(userId ?: "")

        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val userLocation = LatLng(latitude, longitude)

                    // Обновляем координаты в базе данных
                    userLocationRef.setValue(LocationData(userId ?: "", latitude, longitude))
                        .addOnSuccessListener {
                            Log.d("LocationUpdateService", "User location service updated")
                        }
                        .addOnFailureListener {
                            Log.e("LocationUpdateService", "Failed to update user location")
                        }
                }
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        //т.к. разрешения требуются при входе в приложение, то дальше не требуется проверять разрещения
        return true
    }

}