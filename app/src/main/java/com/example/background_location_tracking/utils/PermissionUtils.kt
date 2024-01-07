package com.example.background_location_tracking.utils


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionUtils {

    fun isAccessFineLocationGranted(context: Context): Boolean {
        val fineLocation =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocation =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fineLocation == PackageManager.PERMISSION_GRANTED && coarseLocation == PackageManager.PERMISSION_GRANTED
    }

}

