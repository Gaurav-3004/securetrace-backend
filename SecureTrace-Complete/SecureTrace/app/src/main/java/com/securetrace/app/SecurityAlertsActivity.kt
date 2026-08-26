package com.securetrace.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.securetrace.app.adapter.SecurityAlertAdapter
import com.securetrace.app.databinding.ActivitySecurityAlertsBinding
import com.securetrace.app.network.ApiClient
import com.securetrace.app.util.SessionManager
import kotlinx.coroutines.launch

class SecurityAlertsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecurityAlertsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecurityAlertsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = SessionManager(this)
        binding.btnBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            try {
                val api = ApiClient.create(this@SecurityAlertsActivity)
                val response = api.getAlerts(session.authHeader())
                if (response.isSuccessful) {
                    val alerts = response.body()?.alerts ?: emptyList()
                    if (alerts.isEmpty()) {
                        binding.tvEmptyState.visibility = View.VISIBLE
                        binding.rvAlerts.visibility = View.GONE
                    } else {
                        binding.rvAlerts.layoutManager = LinearLayoutManager(this@SecurityAlertsActivity)
                        binding.rvAlerts.adapter = SecurityAlertAdapter(alerts)
                    }
                } else {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvAlerts.visibility = View.GONE
                }
            } catch (e: Exception) {
                binding.tvEmptyState.text = "⚠️ Couldn't load alerts. Check your connection."
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvAlerts.visibility = View.GONE
            }
        }
    }
}
