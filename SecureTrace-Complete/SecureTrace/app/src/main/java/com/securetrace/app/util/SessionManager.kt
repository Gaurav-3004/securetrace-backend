package com.securetrace.app.util

import android.content.Context

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("securetrace_session", Context.MODE_PRIVATE)

    fun saveSession(userId: Int, username: String, isAdmin: Boolean, logId: Long, token: String) {
        prefs.edit()
            .putInt("user_id", userId)
            .putString("username", username)
            .putBoolean("is_admin", isAdmin)
            .putLong("log_id", logId)
            .putString("token", token)
            .putBoolean("is_logged_in", true)
            .apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)
    fun getUserId(): Int = prefs.getInt("user_id", -1)
    fun getUsername(): String = prefs.getString("username", "") ?: ""
    fun isAdmin(): Boolean = prefs.getBoolean("is_admin", false)
    fun getLogId(): Long = prefs.getLong("log_id", -1)
    fun getToken(): String = prefs.getString("token", "") ?: ""
    fun authHeader(): String = "Bearer ${getToken()}"
}
