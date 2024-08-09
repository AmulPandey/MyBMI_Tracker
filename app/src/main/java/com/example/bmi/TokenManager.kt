package com.example.bmi

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    /**
     * Saves the user login state as logged in.
     */
    fun saveUserExistence() {
        prefs.edit().putBoolean("is_logged_in", true).apply()
    }

    /**
     * Clears the user login state, setting it to logged out.
     */
    fun clearUserExistence() {
        prefs.edit().putBoolean("is_logged_in", false).apply()
    }

    /**
     * Checks if the user is currently logged in.
     * @return True if the user is logged in, false otherwise.
     */
    fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }
}
