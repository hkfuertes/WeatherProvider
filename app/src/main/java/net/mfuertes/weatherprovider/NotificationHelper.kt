package net.mfuertes.weatherprovider

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class NotificationHelper {
    companion object{
        fun askPermissions(activity: Activity){
            ActivityCompat.requestPermissions(
                activity, arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS,
                ), 0x2
            )
        }

        fun isNotificationPermissionGranted(context: Context): Boolean {
            val requestPermissionNotification = ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            )

            return (requestPermissionNotification == PackageManager.PERMISSION_GRANTED)
        }

        fun createNotificationChannel(context: Context) {
            val channel = NotificationChannel(
                "CHANNEL_ID",
                "Weather Notification",
                NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Shows timestamp for latest Weather Notification"
                setSound(null, null)
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}