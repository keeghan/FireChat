package com.keeghan.firechat.util

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private var sharedPreferences: SharedPreferences =
        context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE)

    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    fun getBoolean(keyIsSignedIn: String): Boolean {
        return sharedPreferences.getBoolean(keyIsSignedIn, false)
    }
}