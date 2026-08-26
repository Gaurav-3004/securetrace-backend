package com.securetrace.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.securetrace.app.databinding.ActivityLoginBinding
import com.securetrace.app.network.ApiClient
import com.securetrace.app.network.ErrorParser
import com.securetrace.app.network.LoginRequest
import com.securetrace.app.util.DeviceUtil
import com.securetrace.app.util.PendingOtp
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermission()

        binding.btnLogin.setOnClickListener { performLogin() }

        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
        binding.tvAdminLogin.setOnClickListener {
            startActivity(Intent(this, AdminLoginActivity::class.java))
        }
        binding.tvServerSetup.setOnClickListener {
            startActivity(Intent(this, ServerSetupActivity::class.java))
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
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
                val api = ApiClient.create(this@LoginActivity)
                val response = api.login(
                    LoginRequest(
                        username = username,
                        password = password,
                        deviceName = DeviceUtil.getDeviceName(),
                        deviceModel = DeviceUtil.getDeviceModel(),
                        deviceType = DeviceUtil.getDeviceType(),
                        operatingSystem = DeviceUtil.getOperatingSystem(),
                        appVersion = DeviceUtil.getAppVersion(this@LoginActivity)
                    )
                )
                setLoading(false)

                if (response.isSuccessful) {
                    val body = response.body()
                    val email = body?.email
                    if (email.isNullOrBlank()) {
                        Toast.makeText(this@LoginActivity, "Unexpected server response", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                    PendingOtp.email = email
                    PendingOtp.purpose = "login"
                    PendingOtp.requireAdmin = false
                    startActivity(Intent(this@LoginActivity, OtpVerifyActivity::class.java))
                } else {
                    Toast.makeText(this@LoginActivity, ErrorParser.parse(response), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(
                    this@LoginActivity,
                    "Couldn't reach the server. Check your internet or server settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
        binding.btnLogin.text = if (loading) "Sending code..." else "Sign In"
    }
}
