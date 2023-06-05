package net.mfuertes.weatherprovider

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        PermissionsHelper.askPermissions(this)
        PermissionsHelper.requestBatteryOptimization(this)
        NotificationHelper.createNotificationChannel(this)

        var fab: ExtendedFloatingActionButton = findViewById(R.id.startstop)
        if(WeatherWorker.isScheduled(this)){
            fab.text = getText(R.string.unschedule)
            fab.icon = getDrawable(R.drawable.baseline_stop_24)
        }else{
            fab.text = getText(R.string.schedule)
            fab.icon = getDrawable(R.drawable.baseline_play_arrow_24)
        }
        fab.setOnClickListener {
            if(fab.text == getString(R.string.schedule)){
                WeatherWorker.scheduleFetcher(this)
                fab.text = getText(R.string.unschedule)
                fab.icon = getDrawable(R.drawable.baseline_stop_24)
            }else{
                WeatherWorker.cancelFetcher(this)
                fab.text = getText(R.string.schedule)
                fab.icon = getDrawable(R.drawable.baseline_play_arrow_24)
            }
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val forceUpdate: Preference = preferenceManager.findPreference("force_update")!!
            forceUpdate.setOnPreferenceClickListener {

                WeatherWorker.scheduleFetcherOneTime(requireContext())
                true
            }
        }
    }
}