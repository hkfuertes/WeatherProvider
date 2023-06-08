package net.mfuertes.weatherprovider.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mfuertes.weatherprovider.WeatherFetcher
import net.mfuertes.weatherprovider.helpers.LocationHelper
import net.mfuertes.weatherprovider.helpers.NotificationHelper
import org.json.JSONObject
import java.util.Calendar

class WeatherAlarm : BroadcastReceiver() {

    // For Testing
    private val APIKEY = "966f9979caf0bf53ff0706a981c17d49"
    override fun onReceive(context: Context, intent: Intent) {
        val key = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("owm_key", APIKEY)
        val showNotification = PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean("show_notification", true)

        val location = LocationHelper.getLocation(context)
        Log.d("WeatherAlarm:Location", location.toString())

        if (location != null && key != null) {
            CoroutineScope(Dispatchers.Main).launch {
                withContext(Dispatchers.IO) {
                    val weather =
                        WeatherFetcher.fetchWeather(key, location.latitude, location.longitude)

                    Log.d("WeatherAlarm:Weather", weather.toString())

                    if (weather != null) {
                        if (showNotification)
                            createAndShowNotification(context, weatherObject = weather)
                        WeatherFetcher.sendToGadgetBridge(context, weather)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun createAndShowNotification(context: Context, weatherObject: JSONObject) {
        with(NotificationManagerCompat.from(context)) {
            notify(0X01, NotificationHelper.createNotification(context, weatherObject))
        }
    }

    companion object {
        private fun now(): Long {
            return Calendar.getInstance().timeInMillis
        }

        fun setAlarm(
            context: Context,
            millis: Long = AlarmManager.INTERVAL_FIFTEEN_MINUTES
        ) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, WeatherAlarm::class.java)
            //val intent = Intent("net.mfuertes.weatherprovider.weatheralarm")
            val pendingIntent = PendingIntent
                .getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT)
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, now(), millis, pendingIntent)
        }

        fun trigger(context: Context) {
            //val intent = Intent("net.mfuertes.weatherprovider.weatheralarm")
            val intent = Intent(context, WeatherAlarm::class.java)
            context.sendBroadcast(intent)
        }

        fun isAlarmSet(context: Context): Boolean {
            return PendingIntent.getBroadcast(
                context, 0,
                Intent(context, WeatherAlarm::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
            ) != null
        }

        fun cancelAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, WeatherAlarm::class.java)
            val pendingIntent = PendingIntent
                .getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            alarmManager.cancel(pendingIntent)
        }
    }
}