package net.mfuertes.weatherprovider

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import net.mfuertes.weatherprovider.helpers.NotificationHelper
import net.mfuertes.weatherprovider.helpers.PermissionsHelper

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

        WeatherWorker.scheduleFetcher(this)
    }

    class SettingsFragment : PreferenceFragmentCompat(){

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