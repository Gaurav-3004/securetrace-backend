package com.securetrace.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.securetrace.app.databinding.ActivityOtpVerifyBinding
import com.securetrace.app.network.ApiClient
import com.securetrace.app.network.ErrorParser
import com.securetrace.app.network.LogoutRequest
import com.securetrace.app.network.ResendOtpRequest
import com.securetrace.app.network.VerifyOtpRequest
import com.securetrace.app.util.PendingOtp
import com.securetrace.app.util.SessionManager
import kotlinx.coroutines.launch

class OtpVerifyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpVerifyBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerifyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        if (PendingOtp.purpose.isBlank() || PendingOtp.email.isBlank()) {
            finish()
            return
        }

        binding.tvSentTo.text = "We sent a 6-digit code to ${maskEmail(PendingOtp.email)}"

        binding.btnVerify.setOnClickListener { verifyCode() }
        binding.tvResend.setOnClickListener { resendCode() }
        binding.tvCancel.setOnClickListener {
            PendingOtp.clear()
            finish()
        }
    }

    override fun onBackPressed() {
        PendingOtp.clear()
        finish()
    }

    private fun verifyCode() {
        val input = binding.etOtp.text.toString().trim()
        if (input.length != 6) {
            Toast.makeText(this, "Enter the 6-digit code", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        lifecycleScope.launch {
            try {
                val api = ApiClient.create(this@OtpVerifyActivity)
                val response = api.verifyOtp(VerifyOtpRequest(PendingOtp.email, input, PendingOtp.purpose))
                setLoading(false)

                if (!response.isSuccessful) {
                    Toast.makeText(this@OtpVerifyActivity, ErrorParser.parse(response), Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val body = response.body()

                if (PendingOtp.purpose == "register") {
                    PendingOtp.clear()
                    Toast.makeText(this@OtpVerifyActivity, "Account verified and created! Please sign in.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@OtpVerifyActivity, LoginActivity::class.java))
                    finish()
                    return@launch
                }

                // purpose == "login"
                val token = body?.token
                val user = body?.user
                val logId = body?.logId ?: -1L

                if (token == null || user == null) {
                    Toast.makeText(this@OtpVerifyActivity, "Unexpected server response", Toast.LENGTH_SHORT).show()
                    PendingOtp.clear()
                    return@launch
                }

                if (PendingOtp.requireAdmin && !user.isAdmin) {
                    // Close out the session that was just created server-side, since this login
                    // was made through the admin-only entry point.
                    runCatching { api.logout("Bearer $token", LogoutRequest(logId)) }
                    Toast.makeText(this@OtpVerifyActivity, "This account does not have admin access", Toast.LENGTH_LONG).show()
                    PendingOtp.clear()
                    startActivity(Intent(this@OtpVerifyActivity, AdminLoginActivity::class.java))
                    finish()
                    return@launch
                }

                session.saveSession(user.id, user.username, user.isAdmin, logId, token)
                PendingOtp.clear()
                Toast.makeText(this@OtpVerifyActivity, "Welcome back, ${user.username}!", Toast.LENGTH_SHORT).show()

                val intent = if (user.isAdmin) Intent(this@OtpVerifyActivity, AdminDashboardActivity::class.java)
                else Intent(this@OtpVerifyActivity, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(this@OtpVerifyActivity, "Couldn't reach the server. Check your internet connection.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun resendCode() {
        binding.tvResend.isEnabled = false
        binding.tvResend.text = "Sending..."

        lifecycleScope.launch {
            try {
                val api = ApiClient.create(this@OtpVerifyActivity)
                val response = api.resendOtp(ResendOtpRequest(PendingOtp.email, PendingOtp.purpose))
                binding.tvResend.isEnabled = true
                binding.tvResend.text = "Didn't get it? Resend code"
                if (response.isSuccessful) {
                    Toast.makeText(this@OtpVerifyActivity, "A new code was sent", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@OtpVerifyActivity, ErrorParser.parse(response), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.tvResend.isEnabled = true
                binding.tvResend.text = "Didn't get it? Resend code"
                Toast.makeText(this@OtpVerifyActivity, "Couldn't reach the server", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.btnVerify.isEnabled = !loading
        binding.btnVerify.text = if (loading) "Verifying..." else "Verify Code"
    }

    private fun maskEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size != 2) return email
        val name = parts[0]
        val visible = name.take(2)
        return "$visible${"*".repeat((name.length - 2).coerceAtLeast(1))}@${parts[1]}"
    }
}
