package net.mfuertes.weatherprovider

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat


class PermissionsHelper {
    companion object {
        fun askPermissions(activity: Activity) {
            ActivityCompat.requestPermissions(
                activity, arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 0x2
            )
        }


        fun requestBatteryOptimization(context: Context) {
            val packageName: String = context.packageName
            val pm = context.getSystemService(POWER_SERVICE) as PowerManager?
            if (!pm!!.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent().apply {
                    action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    data = Uri.parse("package:$packageName")
                }
                context.startActivity(intent)
            }
        }

        fun isNotificationPermissionGranted(context: Context): Boolean {
            val requestPermissionNotification = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            )

            return (requestPermissionNotification == PackageManager.PERMISSION_GRANTED)
        }
    }
}