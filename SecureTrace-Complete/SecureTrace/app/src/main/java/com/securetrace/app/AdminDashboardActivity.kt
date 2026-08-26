package com.securetrace.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.securetrace.app.adapter.LoginActivityAdapter
import com.securetrace.app.databinding.ActivityAdminDashboardBinding
import com.securetrace.app.network.ApiClient
import com.securetrace.app.network.LogoutRequest
import com.securetrace.app.util.FileDownloadHelper
import com.securetrace.app.util.SessionManager
import kotlinx.coroutines.launch

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var session: SessionManager

    private val dayOptions = listOf("Last 7 days", "Last 30 days", "Last 90 days", "All time")
    private val dayValues = listOf(7, 30, 90, null)
    private var selectedDays: Int? = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        binding.btnUserManagement.setOnClickListener { startActivity(Intent(this, UserManagementActivity::class.java)) }
        binding.btnAnalytics.setOnClickListener { startActivity(Intent(this, AnalyticsActivity::class.java)) }
        binding.btnLogout.setOnClickListener { performLogout() }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dayOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDays.adapter = adapter
        binding.spinnerDays.setSelection(1) // default: Last 30 days

        binding.spinnerDays.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedDays = dayValues[position]
                loadActivityOnly()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnDownload.setOnClickListener { downloadExcel() }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.create(this@AdminDashboardActivity)

                val statsResponse = api.getAdminStats(session.authHeader())
                if (statsResponse.isSuccessful) {
                    val stats = statsResponse.body()!!
                    binding.tvTotalUsers.text = stats.totalUsers.toString()
                    binding.tvTotalActivities.text = stats.totalActivities.toString()
                    binding.tvFailedLogins.text = stats.failedLogins.toString()
                    binding.tvAlertsCount.text = stats.totalAlerts.toString()
                    binding.tvActiveUsers.text = stats.activeUsers.toString()
                    binding.tvDisabledUsers.text = stats.disabledUsers.toString()
                }

                val activityResponse = api.getAdminActivity(session.authHeader(), selectedDays)
                if (activityResponse.isSuccessful) {
                    val recent = activityResponse.body()?.activity ?: emptyList()
                    binding.rvRecentActivity.layoutManager = LinearLayoutManager(this@AdminDashboardActivity)
                    binding.rvRecentActivity.adapter = LoginActivityAdapter(recent)
                }
            } catch (e: Exception) {
                // Keep showing last-known values on network failure
            }
        }
    }

    private fun loadActivityOnly() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.create(this@AdminDashboardActivity)
                val activityResponse = api.getAdminActivity(session.authHeader(), selectedDays)
                if (activityResponse.isSuccessful) {
                    val recent = activityResponse.body()?.activity ?: emptyList()
                    binding.rvRecentActivity.layoutManager = LinearLayoutManager(this@AdminDashboardActivity)
                    binding.rvRecentActivity.adapter = LoginActivityAdapter(recent)
                }
            } catch (e: Exception) {
                // Ignore — list just stays as-is
            }
        }
    }

    private fun downloadExcel() {
        binding.btnDownload.text = "Downloading..."
        binding.btnDownload.isEnabled = false

        lifecycleScope.launch {
            try {
                val api = ApiClient.create(this@AdminDashboardActivity)
                val response = api.exportAdminActivity(session.authHeader(), selectedDays)

                binding.btnDownload.text = "⬇ Excel (All Users)"
                binding.btnDownload.isEnabled = true

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val result = FileDownloadHelper.saveExcelToDownloads(
                            this@AdminDashboardActivity, body, "securetrace_all_users_activity.xlsx"
                        )
                        when (result) {
                            is FileDownloadHelper.Result.Success ->
                                Toast.makeText(this@AdminDashboardActivity, "Saved to ${result.displayPath}", Toast.LENGTH_LONG).show()
                            is FileDownloadHelper.Result.Failure ->
                                Toast.makeText(this@AdminDashboardActivity, result.message, Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(this@AdminDashboardActivity, "Couldn't download the file", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.btnDownload.text = "⬇ Excel (All Users)"
                binding.btnDownload.isEnabled = true
                Toast.makeText(this@AdminDashboardActivity, "Couldn't reach the server", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun performLogout() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.create(this@AdminDashboardActivity)
                api.logout(session.authHeader(), LogoutRequest(session.getLogId()))
            } catch (e: Exception) {
                // Ignore — clear local session regardless
            }
            session.clearSession()
            startActivity(Intent(this@AdminDashboardActivity, LoginActivity::class.java))
            finish()
        }
    }
}
