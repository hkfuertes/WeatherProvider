package net.mfuertes.weatherprovider

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat


class LocationHelper {

    companion object {
        @SuppressLint("MissingPermission")
        fun getLocation(context: Context): Location? {
            if (isLocationPermissionGranted(context)) {
                //Log.d("LocationHelper","I have Permissions!")
                val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
                var lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (lastKnown == null){
                    lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                }
                return lastKnown;
            }
            return null;
        }

        fun isLocationPermissionGranted(context: Context): Boolean {
            val requestPermissionFineLocation = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            )
            val requestPermissionCoarseLocation = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            )

            return (requestPermissionCoarseLocation == PackageManager.PERMISSION_GRANTED
                    && requestPermissionFineLocation == PackageManager.PERMISSION_GRANTED)
        }

    }
}