package com.mtnance.phoneblock.helpers

import android.content.Context
import androidx.preference.PreferenceManager

class ScreeningPreferences(context: Context) {
    companion object {
        private const val PREF_SERVICE_ENABLED = "service_enabled"
        private const val PREF_SKIP_NOTIFICATION = "skip_notification"
        private const val PREF_SKIP_CALL_LOG = "skip_call_log"
        // List mode
        private const val PREF_LIST_MODE = "list_mode"
    }

    private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

    var isServiceEnabled: Boolean
        get() = sharedPrefs.getBoolean(PREF_SERVICE_ENABLED, false)
        set(value) = sharedPrefs.edit().putBoolean(PREF_SERVICE_ENABLED, value).apply()
    var skipCallNotification: Boolean
        get() = sharedPrefs.getBoolean(PREF_SKIP_NOTIFICATION, false)
        set(value) = sharedPrefs.edit().putBoolean(PREF_SKIP_NOTIFICATION, value).apply()
    var skipCallLog: Boolean
        get() = sharedPrefs.getBoolean(PREF_SKIP_CALL_LOG, false)
        set(value) = sharedPrefs.edit().putBoolean(PREF_SKIP_CALL_LOG, value).apply()
    // List mode
    var listMode: String?
        get() = sharedPrefs.getString(PREF_LIST_MODE, "whitelist")
        set(value) = sharedPrefs.edit().putString(PREF_LIST_MODE, value).apply()
}