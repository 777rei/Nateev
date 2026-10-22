package com.example.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager

class LocationProvider(private val context: Context) {

    private val locationManager: LocationManager? =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(): Location? {
        return try {
            val gpsLocation = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val networkLocation = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val passiveLocation = locationManager?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)

            gpsLocation ?: networkLocation ?: passiveLocation
        } catch (e: Exception) {
            null
        }
    }
}
