package com.securetrace.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.securetrace.app.databinding.ActivityServerSetupBinding
import com.securetrace.app.network.ApiClient
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class ServerSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityServerSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ApiClient.isConfigured(this)) {
            binding.etServerUrl.setText(ApiClient.getBaseUrl(this))
            binding.btnBack.visibility = android.view.View.VISIBLE
        }

        binding.btnBack.setOnClickListener { finish() }
        binding.btnConnect.setOnClickListener { testAndSave() }
    }

    private fun testAndSave() {
        val url = binding.etServerUrl.text.toString().trim()
        if (url.isEmpty()) {
            binding.tvStatus.text = "Please enter your server URL"
            return
        }

        ApiClient.saveBaseUrl(this, url)
        val fullUrl = ApiClient.getBaseUrl(this)

        binding.btnConnect.isEnabled = false
        binding.btnConnect.text = "Connecting..."
        binding.tvStatus.text = "Checking connection to $fullUrl"

        Thread {
            var success = false
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()
                val request = Request.Builder().url(fullUrl).get().build()
                val response = client.newCall(request).execute()
                success = response.isSuccessful
                response.close()
            } catch (e: Exception) {
                success = false
            }

            runOnUiThread {
                binding.btnConnect.isEnabled = true
                binding.btnConnect.text = "Connect"
                if (success) {
                    binding.tvStatus.text = "✅ Connected successfully!"
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    binding.tvStatus.text = "⚠️ Couldn't reach the server. Check the URL and your internet connection, or make sure the backend is deployed and awake."
                }
            }
        }.start()
    }
}
