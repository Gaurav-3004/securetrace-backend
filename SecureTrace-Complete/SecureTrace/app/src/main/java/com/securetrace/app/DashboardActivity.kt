package com.securetrace.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.securetrace.app.adapter.LoginActivityAdapter
import com.securetrace.app.databinding.ActivityDashboardBinding
import com.securetrace.app.network.ApiClient
import com.securetrace.app.network.LogoutRequest
import com.securetrace.app.util.SessionManager
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        binding.tvWelcome.text = "Hi, ${session.getUsername()} 👋"

        binding.btnAlerts.setOnClickListener { startActivity(Intent(this, SecurityAlertsActivity::class.java)) }
        binding.btnProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        binding.tvViewAll.setOnClickListener { startActivity(Intent(this, ActivityHistoryActivity::class.java)) }
        binding.btnSettings.setOnClickListener { startActivity(Intent(this, SettingsActivity::class.java)) }
        binding.btnLogout.setOnClickListener { performLogout() }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.create(this@DashboardActivity)
                val response = api.getDashboard(session.authHeader())
                if (!response.isSuccessful) return@launch
                val data = response.body() ?: return@launch

                binding.tvTotalLogins.text = data.totalLogins.toString()
                binding.tvActiveDevices.text = data.activeDevices.toString()
                binding.tvLastLogin.text = data.lastLogin ?: "—"
                binding.tvAlertCount.text = data.unreadAlerts.toString()

                when {
                    data.unreadAlerts >= 3 -> {
                        binding.cardSecurityStatus.setBackgroundResource(R.drawable.badge_danger)
                        binding.tvStatusIcon.text = "🔴"
                        binding.tvStatusTitle.text = "Action Recommended"
                        binding.tvStatusSubtitle.text = "Unusual activity detected — review your alerts"
                    }
                    data.unreadAlerts > 0 -> {
                        binding.cardSecurityStatus.setBackgroundResource(R.drawable.badge_warning)
                        binding.tvStatusIcon.text = "🟡"
                        binding.tvStatusTitle.text = "New Security Activity"
                        binding.tvStatusSubtitle.text = "${data.unreadAlerts} new alert(s) to review"
                    }
                    else -> {
                        binding.cardSecurityStatus.setBackgroundResource(R.drawable.badge_safe)
                        binding.tvStatusIcon.text = "🟢"
                        binding.tvStatusTitle.text = "Account Protected"
                        binding.tvStatusSubtitle.text = "No suspicious activity detected"
                    }
                }

                binding.rvRecentActivity.layoutManager = LinearLayoutManager(this@DashboardActivity)
                binding.rvRecentActivity.adapter = LoginActivityAdapter(data.recentActivity)
            } catch (e: Exception) {
                // Dashboard just keeps showing last-known values until the next successful load
            }
        }
    }

    private fun performLogout() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.create(this@DashboardActivity)
                api.logout(session.authHeader(), LogoutRequest(session.getLogId()))
            } catch (e: Exception) {
                // Clear local session below regardless
            }
            session.clearSession()
            startActivity(Intent(this@DashboardActivity, LoginActivity::class.java))
            finish()
        }
    }
}
