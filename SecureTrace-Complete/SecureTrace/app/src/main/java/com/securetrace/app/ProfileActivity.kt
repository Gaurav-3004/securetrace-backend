package com.securetrace.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.securetrace.app.databinding.ActivityProfileBinding
import com.securetrace.app.network.ApiClient
import com.securetrace.app.network.ChangePasswordRequest
import com.securetrace.app.network.ErrorParser
import com.securetrace.app.util.SessionManager
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        binding.tvUsername.text = session.getUsername()
        binding.btnBack.setOnClickListener { finish() }

        lifecycleScope.launch {
            try {
                val api = ApiClient.create(this@ProfileActivity)
                val response = api.getMe(session.authHeader())
                if (response.isSuccessful) {
                    val profile = response.body()
                    binding.tvEmail.text = profile?.email ?: ""
                    binding.tvJoined.text = "Member since: ${profile?.createdAt ?: "—"}"
                }
            } catch (e: Exception) {
                binding.tvJoined.text = "Member since: —"
            }
        }

        binding.btnChangePassword.setOnClickListener { changePassword() }
    }

    private fun changePassword() {
        val oldPassword = binding.etOldPassword.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (oldPassword.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPassword.length < 6) {
            Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }
        if (newPassword != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnChangePassword.isEnabled = false
        lifecycleScope.launch {
            try {
                val api = ApiClient.create(this@ProfileActivity)
                val response = api.changePassword(
                    session.authHeader(),
                    ChangePasswordRequest(oldPassword, newPassword)
                )
                binding.btnChangePassword.isEnabled = true

                if (response.isSuccessful) {
                    Toast.makeText(this@ProfileActivity, "Password updated successfully", Toast.LENGTH_SHORT).show()
                    binding.etOldPassword.text.clear()
                    binding.etNewPassword.text.clear()
                    binding.etConfirmPassword.text.clear()
                } else {
                    Toast.makeText(this@ProfileActivity, ErrorParser.parse(response), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.btnChangePassword.isEnabled = true
                Toast.makeText(this@ProfileActivity, "Couldn't reach the server", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
