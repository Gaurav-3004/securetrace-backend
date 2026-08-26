package com.securetrace.app

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.securetrace.app.adapter.LoginActivityAdapter
import com.securetrace.app.databinding.ActivityActivityHistoryBinding
import com.securetrace.app.network.ApiClient
import com.securetrace.app.util.FileDownloadHelper
import com.securetrace.app.util.SessionManager
import kotlinx.coroutines.launch

class ActivityHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActivityHistoryBinding
    private lateinit var session: SessionManager

    private val dayOptions = listOf("Last 7 days", "Last 30 days", "Last 90 days", "All time")
    private val dayValues = listOf(7, 30, 90, null)
    private var selectedDays: Int? = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        binding.btnBack.setOnClickListener { finish() }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dayOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDays.adapter = adapter
        binding.spinnerDays.setSelection(1) // default: Last 30 days

        binding.spinnerDays.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedDays = dayValues[position]
                loadActivity()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnDownload.setOnClickListener { downloadExcel() }

        loadActivity()
    }

    private fun loadActivity() {
        binding.rvHistory.adapter = null
        lifecycleScope.launch {
            try {
                val api = ApiClient.create(this@ActivityHistoryActivity)
                val response = api.getActivity(session.authHeader(), selectedDays)
                if (response.isSuccessful) {
                    val logs = response.body()?.activity ?: emptyList()
                    binding.rvHistory.layoutManager = LinearLayoutManager(this@ActivityHistoryActivity)
                    binding.rvHistory.adapter = LoginActivityAdapter(logs)
                }
            } catch (e: Exception) {
                Toast.makeText(this@ActivityHistoryActivity, "Couldn't load activity", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun downloadExcel() {
        binding.btnDownload.text = "Downloading..."
        binding.btnDownload.isEnabled = false

        lifecycleScope.launch {
            try {
                val api = ApiClient.create(this@ActivityHistoryActivity)
                val response = api.exportMyActivity(session.authHeader(), selectedDays)

                binding.btnDownload.text = "⬇ Excel"
                binding.btnDownload.isEnabled = true

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        val fileName = "securetrace_${session.getUsername()}_activity.xlsx"
                        when (val result = FileDownloadHelper.saveExcelToDownloads(this@ActivityHistoryActivity, body, fileName)) {
                            is FileDownloadHelper.Result.Success ->
                                Toast.makeText(this@ActivityHistoryActivity, "Saved to ${result.displayPath}", Toast.LENGTH_LONG).show()
                            is FileDownloadHelper.Result.Failure ->
                                Toast.makeText(this@ActivityHistoryActivity, result.message, Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(this@ActivityHistoryActivity, "Couldn't download the file", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                binding.btnDownload.text = "⬇ Excel"
                binding.btnDownload.isEnabled = true
                Toast.makeText(this@ActivityHistoryActivity, "Couldn't reach the server", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
