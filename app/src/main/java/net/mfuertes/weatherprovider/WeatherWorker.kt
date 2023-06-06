package net.mfuertes.weatherprovider

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import net.mfuertes.weatherprovider.helpers.LocationHelper
import net.mfuertes.weatherprovider.helpers.NotificationHelper
import org.json.JSONObject
import java.text.DecimalFormat
import java.time.Duration
import java.time.format.DateTimeFormatter

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
        Log.d("WeatherWorker", location.toString())
        Log.d("WeatherWorker", key.toString())
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

    @SuppressLint("MissingPermission")
    private fun createAndShowNotification(weatherObject: JSONObject) {
        with(NotificationManagerCompat.from(applicationContext)) {
            notify(0X01, NotificationHelper.createNotification(applicationContext, weatherObject))
        }
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
            return  WorkManager.getInstance(context).getWorkInfosByTag(tag).get().all{
                it.state == WorkInfo.State.ENQUEUED
            }
        }
    }
}