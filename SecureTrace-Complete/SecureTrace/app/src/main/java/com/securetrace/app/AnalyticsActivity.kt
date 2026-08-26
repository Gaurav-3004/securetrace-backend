package com.securetrace.app

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.securetrace.app.databinding.ActivityAnalyticsBinding
import com.securetrace.app.network.ApiClient
import com.securetrace.app.network.DeviceDistItemDto
import com.securetrace.app.network.TrendItemDto
import com.securetrace.app.util.SessionManager
import kotlinx.coroutines.launch

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalyticsBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        binding.btnBack.setOnClickListener { finish() }

        loadData()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun loadData() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.create(this@AnalyticsActivity)

                val analyticsResponse = api.getAnalytics(session.authHeader())
                if (analyticsResponse.isSuccessful) {
                    val data = analyticsResponse.body()!!
                    buildTrendChart(data.trend)
                    buildDeviceChart(data.deviceDistribution)
                }

                val statsResponse = api.getAdminStats(session.authHeader())
                if (statsResponse.isSuccessful) {
                    val stats = statsResponse.body()!!
                    val total = stats.successLogins + stats.failedLogins
                    val successRate = if (total == 0) 0 else (stats.successLogins * 100 / total)
                    binding.tvSuccessRate.text = "$successRate%"
                    binding.tvSecurityEvents.text = stats.totalAlerts.toString()
                }
            } catch (e: Exception) {
                // Charts just stay empty on network failure
            }
        }
    }

    private fun buildTrendChart(trend: List<TrendItemDto>) {
        val maxCount = trend.flatMap { listOf(it.success, it.failed) }.maxOrNull()?.coerceAtLeast(1) ?: 1
        val maxBarHeight = dp(110)

        binding.chartTrend.removeAllViews()

        for (item in trend) {
            val column = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            }

            val barsRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.BOTTOM
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val successHeight = ((item.success.toFloat() / maxCount) * maxBarHeight).toInt().coerceAtLeast(dp(3))
            val failedHeight = ((item.failed.toFloat() / maxCount) * maxBarHeight).toInt().coerceAtLeast(dp(3))

            val successBar = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(10), successHeight).apply { marginEnd = dp(3) }
                setBackgroundColor(ContextCompat.getColor(this@AnalyticsActivity, R.color.accent_cyan))
            }
            val failedBar = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(10), failedHeight)
                setBackgroundColor(ContextCompat.getColor(this@AnalyticsActivity, R.color.status_danger))
            }

            barsRow.addView(successBar)
            barsRow.addView(failedBar)

            val labelView = TextView(this).apply {
                text = item.label
                textSize = 9f
                setTextColor(ContextCompat.getColor(this@AnalyticsActivity, R.color.text_muted))
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(6) }
            }

            column.addView(barsRow)
            column.addView(labelView)
            binding.chartTrend.addView(column)
        }
    }

    private fun buildDeviceChart(distribution: List<DeviceDistItemDto>) {
        binding.chartDevices.removeAllViews()

        if (distribution.isEmpty()) {
            val empty = TextView(this).apply {
                text = "No login activity yet"
                setTextColor(ContextCompat.getColor(this@AnalyticsActivity, R.color.text_muted))
                textSize = 12f
            }
            binding.chartDevices.addView(empty)
            return
        }

        val maxCount = distribution.maxOf { it.count }.coerceAtLeast(1)

        for (item in distribution) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(12) }
            }

            val label = TextView(this).apply {
                text = item.type
                setTextColor(ContextCompat.getColor(this@AnalyticsActivity, R.color.text_secondary))
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(dp(70), LinearLayout.LayoutParams.WRAP_CONTENT)
            }

            val barContainer = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, dp(18), 1f).apply {
                    marginStart = dp(8)
                    marginEnd = dp(8)
                }
                setBackgroundColor(Color.parseColor("#0F1B2E"))
            }

            val bar = View(this).apply {
                val widthFraction = item.count.toFloat() / maxCount
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, widthFraction)
                setBackgroundResource(R.drawable.bar_chart_segment)
            }
            barContainer.addView(bar)

            val countLabel = TextView(this).apply {
                text = item.count.toString()
                setTextColor(ContextCompat.getColor(this@AnalyticsActivity, R.color.text_primary))
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(dp(28), LinearLayout.LayoutParams.WRAP_CONTENT)
                gravity = Gravity.END
            }

            row.addView(label)
            row.addView(barContainer)
            row.addView(countLabel)
            binding.chartDevices.addView(row)
        }
    }
}
