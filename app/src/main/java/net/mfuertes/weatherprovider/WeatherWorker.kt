package net.mfuertes.weatherprovider

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.json.JSONObject
import java.math.RoundingMode
import java.text.DecimalFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class WeatherWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    // For Testing
    private val APIKEY = "966f9979caf0bf53ff0706a981c17d49"

    override fun doWork(): Result {
        val key = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .getString("owm_key", null)
        val showNotification = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            .getBoolean("show_notification", true)

        val location = LocationHelper.getLocation(applicationContext)
        if (location != null && key != null) {
            val weather = WeatherFetcher.fetchWeather(
                apiKey = key,
                latitude = location.latitude,
                longitude = location.longitude
            )
            Log.d("WeatherWorker", weather.toString())

            if (weather != null) {
                if (showNotification)
                    createAndShowNotification(weatherObject = weather)
                WeatherFetcher.sendToGadgetBridge(applicationContext, weather)
            }
        }

        return Result.success()
    }

    private fun createAndShowNotification(weatherObject: JSONObject) {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val id = weatherObject.getInt("currentConditionCode")
        val condition = weatherObject.getString("currentCondition")
        val currentTemp = weatherObject.getInt("currentTemp")

        val df = DecimalFormat("#.##")

        val notificationBuilder =
            NotificationCompat.Builder(applicationContext, "CHANNEL_ID") //TODO: change!
                .setSmallIcon(calculateDrawable(id))
                .setContentTitle(condition.replaceFirstChar { it.titlecase() })
                .setContentText("${df.format(currentTemp - 273.15)}ºC")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(applicationContext)) {
            notify(0X01, notificationBuilder.build())
        }
    }

    private fun calculateDrawable(id: Int): Int {
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

    companion object {
        const val WORKER_TAG = "worker_tag"


        fun scheduleFetcherOneTime(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = OneTimeWorkRequestBuilder<WeatherWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }

        fun scheduleFetcher(context: Context, tag: String = WORKER_TAG, minutes: Long = 15) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest = PeriodicWorkRequestBuilder<WeatherWorker>(Duration.ofMinutes(minutes))
                .setConstraints(constraints)
                .addTag(tag)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                tag,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                workRequest
            )
        }

        fun cancelFetcher(context: Context, tag: String = WORKER_TAG) {
            WorkManager.getInstance(context).cancelAllWorkByTag(tag)
        }

        fun isScheduled(context: Context, tag: String = WORKER_TAG): Boolean {
            return WorkManager.getInstance(context).getWorkInfosByTag(tag).get().size > 0
        }
    }
}