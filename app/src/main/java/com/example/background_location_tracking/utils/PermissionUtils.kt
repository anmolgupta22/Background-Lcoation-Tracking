package com.example.background_location_tracking.utils


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {

    fun isAccessFineLocationGranted(context: Context): Boolean {
        val coarseLocation =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        val fineLocation =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        return coarseLocation == PackageManager.PERMISSION_GRANTED && fineLocation == PackageManager.PERMISSION_GRANTED
    }

    fun isAccessBackgroundLocationGranted(context: Context): Boolean {
        val backgroundLocation =
            ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        return backgroundLocation == PackageManager.PERMISSION_GRANTED
    }

    fun isLocationEnabled(context: Context): Boolean {
        val locationManager: LocationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun isCheckBackgroundLocationPermissionDenied(activity: Activity): Boolean {
        return (ActivityCompat.shouldShowRequestPermissionRationale(activity,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION))
    }

    fun requestLocationPermission(
        permissionsLauncher: ActivityResultLauncher<Array<String>>,
    ) {
        val permissionsToRequest = mutableListOf<String>()
        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        permissionsLauncher.launch(permissionsToRequest.toTypedArray())
    }
}

