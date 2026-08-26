package com.securetrace.app

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.securetrace.app.adapter.UserManagementAdapter
import com.securetrace.app.databinding.ActivityUserManagementBinding
import com.securetrace.app.network.ApiClient
import com.securetrace.app.util.SessionManager
import kotlinx.coroutines.launch

class UserManagementActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserManagementBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserManagementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        binding.btnBack.setOnClickListener { finish() }

        loadUsers("")

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                loadUsers(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadUsers(query: String) {
        lifecycleScope.launch {
            try {
                val api = ApiClient.create(this@UserManagementActivity)
                val response = api.getAdminUsers(session.authHeader(), query)
                if (response.isSuccessful) {
                    val users = response.body()?.users ?: emptyList()
                    binding.rvUsers.layoutManager = LinearLayoutManager(this@UserManagementActivity)
                    binding.rvUsers.adapter = UserManagementAdapter(users) { user ->
                        toggleUser(user.id, query)
                    }
                }
            } catch (e: Exception) {
                // Leave the list as-is on network failure
            }
        }
    }

    private fun toggleUser(userId: Int, currentQuery: String) {
        lifecycleScope.launch {
            try {
                val api = ApiClient.create(this@UserManagementActivity)
                api.toggleUser(session.authHeader(), userId)
                loadUsers(currentQuery)
            } catch (e: Exception) {
                // Ignore — list will just stay unchanged until next refresh
            }
        }
    }
}
