package com.example.android.navigation


import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import com.example.android.navigation.util.Constants
import com.example.android.navigation.util.Globals
import com.google.firebase.messaging.FirebaseMessaging


class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var prefs: SharedPreferences
    private var actualTheme: String = Globals().THEME
    private var actualRole: Boolean = Globals().ROLE
    private var actualNotification: Boolean = Globals().NOTIFICATION
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        actualTheme = prefs.getString(Constants().KEY_THEME, actualTheme)!!
        actualRole = prefs.getBoolean(Constants().KEY_ROLE, actualRole)
        actualNotification = prefs.getBoolean(Constants().KEY_NOTIFICATION, actualNotification)
        prefs.registerOnSharedPreferenceChangeListener(themePreferenceChange)
        if(!prefs.getBoolean(Constants().KEY_LOGGED,Globals().LOGGED)){
            val switch = findPreference(Constants().KEY_ROLE)
            switch.summary = "You must log in first"
            switch.isEnabled = false
            val switch2 = findPreference(Constants().KEY_NOTIFICATION)
            switch2.summary = "You must log in first"
            switch2.isEnabled = false
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferenceScreen(null)
        addPreferencesFromResource(R.xml.main_settings)
    }

    val themePreferenceChange = SharedPreferences.OnSharedPreferenceChangeListener(fun(sharedPref : SharedPreferences, key: String){
        when(key){
            Constants().KEY_THEME -> {
                var newTheme = actualTheme
                newTheme = sharedPref.getString(key, newTheme)!!
                if (!newTheme.equals(actualTheme)) {
                    activity?.recreate()
                }
            }
            Constants().KEY_ROLE -> {
                var newRole = actualRole
                newRole = sharedPref.getBoolean(key, newRole)
                if (newRole != actualRole) {
                    activity?.recreate()
                }
            }
            Constants().KEY_NOTIFICATION -> {
                var status = false
                status = sharedPref.getBoolean(key, status)
                if(status){
                    FirebaseMessaging.getInstance().subscribeToTopic(Constants().NOTIFICATION_CHANNEL)
                        .addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Toast.makeText(activity,"Something went wrong",Toast.LENGTH_SHORT).show()
                                sharedPref.edit().putBoolean(key, actualNotification).apply()
                            }else{
                                actualNotification = status
                            }
                        }
                }else{
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Constants().NOTIFICATION_CHANNEL)
                        .addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Toast.makeText(activity,"Something went wrong",Toast.LENGTH_SHORT).show()
                                sharedPref.edit().putBoolean(key, actualNotification).apply()
                            }else{
                                actualNotification = status
                            }
                        }
                }
            }
        }
    })

}
