package com.securetrace.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.securetrace.app.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("securetrace_prefs", MODE_PRIVATE)
        binding.switchNotifications.isChecked = prefs.getBoolean("notifications_enabled", true)

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply()
        }

        binding.btnBack.setOnClickListener { finish() }
    }
}
