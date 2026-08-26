package com.securetrace.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.securetrace.app.databinding.ActivitySplashBinding
import com.securetrace.app.network.ApiClient
import com.securetrace.app.util.NotificationHelper
import com.securetrace.app.util.SessionManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationHelper.createChannel(this)

        val session = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = when {
                !ApiClient.isConfigured(this) -> Intent(this, ServerSetupActivity::class.java)
                session.isLoggedIn() && session.isAdmin() -> Intent(this, AdminDashboardActivity::class.java)
                session.isLoggedIn() -> Intent(this, DashboardActivity::class.java)
                else -> Intent(this, LoginActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 1400)
    }
}
