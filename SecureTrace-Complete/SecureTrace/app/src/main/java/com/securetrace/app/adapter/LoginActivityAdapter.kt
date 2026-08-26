package com.securetrace.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.securetrace.app.R
import com.securetrace.app.databinding.ItemActivityLogBinding
import com.securetrace.app.network.ActivityLogDto

class LoginActivityAdapter(private val logs: List<ActivityLogDto>) :
    RecyclerView.Adapter<LoginActivityAdapter.LogViewHolder>() {

    inner class LogViewHolder(val binding: ItemActivityLogBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemActivityLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logs[position]
        val ctx = holder.itemView.context

        holder.binding.tvDeviceName.text = log.deviceName ?: "Unknown device"
        holder.binding.tvUsername.text = log.username
        holder.binding.tvOsVersion.text = "${log.operatingSystem ?: ""} • v${log.appVersion ?: "1.0"}"
        holder.binding.tvLoginTime.text = log.loginTime ?: ""

        val minutes = log.durationMinutes
        if (minutes != null) {
            val h = minutes / 60
            val m = minutes % 60
            val durationText = if (h > 0) "⏱ ${h}h ${m}m" else "⏱ ${m}m"
            holder.binding.tvDuration.text = if (log.isActive == 1) "$durationText so far" else durationText
            holder.binding.tvDuration.visibility = android.view.View.VISIBLE
        } else {
            holder.binding.tvDuration.visibility = android.view.View.GONE
        }

        if (log.loginStatus == "SUCCESS") {
            holder.binding.badgeStatus.text = if (log.isActive == 1) "🟢 Active" else "Success"
            holder.binding.badgeStatus.background = ContextCompat.getDrawable(ctx, R.drawable.badge_safe)
            holder.binding.badgeStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_safe))
        } else {
            holder.binding.badgeStatus.text = "Failed"
            holder.binding.badgeStatus.background = ContextCompat.getDrawable(ctx, R.drawable.badge_danger)
            holder.binding.badgeStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_danger))
        }
    }

    override fun getItemCount(): Int = logs.size
}
