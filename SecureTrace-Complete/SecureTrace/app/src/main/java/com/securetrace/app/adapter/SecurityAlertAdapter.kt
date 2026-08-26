package com.securetrace.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.securetrace.app.R
import com.securetrace.app.databinding.ItemSecurityAlertBinding
import com.securetrace.app.network.AlertDto

class SecurityAlertAdapter(private val alerts: List<AlertDto>) :
    RecyclerView.Adapter<SecurityAlertAdapter.AlertViewHolder>() {

    inner class AlertViewHolder(val binding: ItemSecurityAlertBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemSecurityAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = alerts[position]

        val (icon, title, badgeBg) = when (alert.alertType) {
            "NEW_DEVICE" -> Triple("📱", "New Device Login", R.drawable.badge_safe)
            "MULTIPLE_FAILED_ATTEMPTS" -> Triple("🚫", "Account Locked", R.drawable.badge_danger)
            "FAILED_LOGIN" -> Triple("⚠️", "Failed Login Attempts", R.drawable.badge_warning)
            "PASSWORD_CHANGED" -> Triple("🔑", "Password Changed", R.drawable.badge_safe)
            "UNUSUAL_PATTERN" -> Triple("🔍", "Unusual Login Pattern", R.drawable.badge_warning)
            "ACCOUNT_UPDATE" -> Triple("🛡️", "Account Update", R.drawable.badge_safe)
            else -> Triple("🔔", "Security Alert", R.drawable.badge_safe)
        }

        holder.binding.tvIcon.text = icon
        holder.binding.tvTitle.text = title
        holder.binding.tvDescription.text = alert.description
        holder.binding.tvTime.text = alert.createdAt
        holder.binding.cardAlert.setBackgroundResource(badgeBg)
    }

    override fun getItemCount(): Int = alerts.size
}
