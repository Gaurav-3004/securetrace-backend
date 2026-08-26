package com.securetrace.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.securetrace.app.R
import com.securetrace.app.databinding.ItemUserManageBinding
import com.securetrace.app.network.AdminUserDto

class UserManagementAdapter(
    private val users: List<AdminUserDto>,
    private val onToggleActive: (AdminUserDto) -> Unit
) : RecyclerView.Adapter<UserManagementAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserManageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserManageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        val ctx = holder.itemView.context

        holder.binding.tvUsername.text = user.username + if (user.isAdmin == 1) "  👑" else ""
        holder.binding.tvEmail.text = user.email
        holder.binding.tvJoined.text = "Joined: ${user.createdAt}"

        if (user.isActive == 1) {
            holder.binding.tvStatus.text = "Active"
            holder.binding.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_safe))
            holder.binding.btnToggle.text = "Disable"
            holder.binding.btnToggle.setBackgroundResource(R.drawable.badge_danger)
            holder.binding.btnToggle.setTextColor(ContextCompat.getColor(ctx, R.color.status_danger))
        } else {
            holder.binding.tvStatus.text = "Disabled"
            holder.binding.tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.status_danger))
            holder.binding.btnToggle.text = "Enable"
            holder.binding.btnToggle.setBackgroundResource(R.drawable.badge_safe)
            holder.binding.btnToggle.setTextColor(ContextCompat.getColor(ctx, R.color.status_safe))
        }

        holder.binding.btnToggle.setOnClickListener { onToggleActive(user) }
    }

    override fun getItemCount(): Int = users.size
}
