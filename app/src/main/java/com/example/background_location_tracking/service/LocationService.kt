package com.example.background_location_tracking.service

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.background_location_tracking.*
import com.example.background_location_tracking.R
import com.example.background_location_tracking.model.LocationEvent
import com.example.background_location_tracking.utils.Constant
import com.google.android.gms.location.*
import org.greenrobot.eventbus.EventBus

class LocationService : Service() {

    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var locationRequest: LocationRequest? = null
    private var notificationManager: NotificationManager? = null
    private var location: Location? = null

    override fun onCreate() {
        super.onCreate()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).setIntervalMillis(500)
                .build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult)
            }
        }
        notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel =
            NotificationChannel(Constant.CHANNEL_ID,
                "locations",
                NotificationManager.IMPORTANCE_DEFAULT)
        notificationChannel.lightColor = Color.BLUE
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        notificationManager?.createNotificationChannel(notificationChannel)
    }

    @Suppress("MissingPermission")
    fun createLocationRequest() {
        try {
            fusedLocationProviderClient?.requestLocationUpdates(
                locationRequest!!, locationCallback!!, null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun removeLocationUpdates() {
        locationCallback?.let {
            fusedLocationProviderClient?.removeLocationUpdates(it)
        }
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun onNewLocation(locationResult: LocationResult) {
        location = locationResult.lastLocation
        EventBus.getDefault().post(LocationEvent(
            latitude = location?.latitude,
            longitude = location?.longitude
        ))
    }

    private fun getNotification(): Notification {
        val intent = Intent(applicationContext, SplashScreenActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, Constant.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_location)
            .setContentTitle("Google Map Location Tracking")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
        notification.setChannelId(Constant.CHANNEL_ID)
        return notification.build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createLocationRequest()
        startForeground(Constant.NOTIFICATION_ID, getNotification())
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        removeLocationUpdates()
    }
}