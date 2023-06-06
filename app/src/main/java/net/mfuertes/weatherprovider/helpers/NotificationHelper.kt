package net.mfuertes.weatherprovider.helpers

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import net.mfuertes.weatherprovider.R
import org.json.JSONObject
import java.text.DecimalFormat
import java.time.format.DateTimeFormatter

class NotificationHelper {
    companion object {
        private const val CHANNEL_ID = "CHANNEL_ID"
        fun askPermissions(activity: Activity) {
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

        fun calculateDrawable(id: Int): Int {
            if (id in 200..232)
                return R.drawable.owm11d
            else if (id in 300..321)
                return R.drawable.owm09d
            else if (id in 500..504)
                return R.drawable.owm10d
            else if (id in 520..531)
                return R.drawable.owm09d
            else if (id == 511)
                return R.drawable.owm13d
            else if (id in 600..622)
                return R.drawable.owm13d
            else if (id in 701..781)
                return R.drawable.owm50d
            else if (id == 800)
                return R.drawable.owm01d
            else if (id == 801)
                return R.drawable.owm02d
            else if (id == 802)
                return R.drawable.owm03d
            else if (id == 803)
                return R.drawable.owm04d
            else
                return R.drawable.owm01n
        }

        fun createNotification(context: Context, weatherObject: JSONObject): Notification {
            val id = weatherObject.getInt("currentConditionCode")
            val condition = weatherObject.getString("currentCondition")
            val currentTemp = weatherObject.getInt("currentTemp")
            val location = if (weatherObject.has("location"))
                weatherObject.getString("location")
            else
                null

            val df = DecimalFormat("#.##")

            val notificationBuilder =
                NotificationCompat.Builder(context, CHANNEL_ID) //TODO: change!
                    .setSmallIcon(calculateDrawable(id))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            if (location != null) {
                notificationBuilder
                    .setContentTitle(location)
                    .setContentText("${condition.replaceFirstChar { it.titlecase() }} - ${df.format(currentTemp - 273.15)}ºC")
            } else {
                notificationBuilder
                    .setContentTitle(condition.replaceFirstChar { it.titlecase() })
                    .setContentText("${df.format(currentTemp - 273.15)}ºC")
            }

            return notificationBuilder.build()
        }

        fun createNotificationChannel(context: Context) {
            val channel = NotificationChannel(
                "CHANNEL_ID",
                "Weather Notification",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
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