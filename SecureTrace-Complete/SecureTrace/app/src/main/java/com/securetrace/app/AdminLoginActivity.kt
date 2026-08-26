package com.securetrace.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.securetrace.app.databinding.ActivityAdminLoginBinding
import com.securetrace.app.network.ApiClient
import com.securetrace.app.network.ErrorParser
import com.securetrace.app.network.LoginRequest
import com.securetrace.app.util.DeviceUtil
import com.securetrace.app.util.PendingOtp
import kotlinx.coroutines.launch

class AdminLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAdminLogin.setOnClickListener { performLogin() }

        binding.tvBackToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun performLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val api = ApiClient.create(this@AdminLoginActivity)
                val response = api.login(
                    LoginRequest(
                        username = username,
                        password = password,
                        deviceName = DeviceUtil.getDeviceName(),
                        deviceModel = DeviceUtil.getDeviceModel(),
                        deviceType = DeviceUtil.getDeviceType(),
                        operatingSystem = DeviceUtil.getOperatingSystem(),
                        appVersion = DeviceUtil.getAppVersion(this@AdminLoginActivity)
                    )
                )
                setLoading(false)

                if (response.isSuccessful) {
                    val email = response.body()?.email
                    if (email.isNullOrBlank()) {
                        Toast.makeText(this@AdminLoginActivity, "Unexpected server response", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    PendingOtp.email = email
                    PendingOtp.purpose = "login"
                    PendingOtp.requireAdmin = true
                    startActivity(Intent(this@AdminLoginActivity, OtpVerifyActivity::class.java))
                } else {
                    Toast.makeText(this@AdminLoginActivity, ErrorParser.parse(response), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(
                    this@AdminLoginActivity,
                    "Couldn't reach the server. Check your internet or server settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnAdminLogin.isEnabled = !loading
        binding.btnAdminLogin.text = if (loading) "Sending code..." else "Access Admin Panel"
    }
}
