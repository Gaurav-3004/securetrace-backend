package com.securetrace.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.securetrace.app.databinding.ActivityForgotPasswordBinding
import com.securetrace.app.network.ApiClient
import com.securetrace.app.network.ErrorParser
import com.securetrace.app.network.ForgotPasswordRequest
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnReset.setOnClickListener { resetPassword() }
        binding.tvBackToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun resetPassword() {
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (username.isEmpty() || email.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPassword.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPassword != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnReset.isEnabled = false
        lifecycleScope.launch {
            try {
                val api = ApiClient.create(this@ForgotPasswordActivity)
                val response = api.forgotPassword(ForgotPasswordRequest(username, email, newPassword))
                binding.btnReset.isEnabled = true

                if (response.isSuccessful) {
                    Toast.makeText(this@ForgotPasswordActivity, "Password reset successful. Please sign in.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@ForgotPasswordActivity, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@ForgotPasswordActivity, ErrorParser.parse(response), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                binding.btnReset.isEnabled = true
                Toast.makeText(this@ForgotPasswordActivity, "Couldn't reach the server. Check your internet connection.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
