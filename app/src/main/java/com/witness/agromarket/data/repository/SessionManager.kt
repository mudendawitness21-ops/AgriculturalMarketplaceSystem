package com.witness.agromarket.data.repository

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("agromarket_session", Context.MODE_PRIVATE)

    fun saveLoggedInPhone(phone: String) {
        prefs.edit().putString("logged_in_phone", phone.trim()).apply()
    }

    fun getLoggedInPhone(): String {
        return prefs.getString("logged_in_phone", "") ?: ""
    }

    fun clearSession() {
        prefs.edit().remove("logged_in_phone").apply()
    }
}
