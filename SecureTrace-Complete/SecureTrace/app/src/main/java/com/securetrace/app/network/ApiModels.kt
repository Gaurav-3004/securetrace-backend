package com.securetrace.app.network

import com.google.gson.annotations.SerializedName

// ---------- Auth ----------

data class RegisterRequest(val username: String, val email: String, val password: String)
data class RegisterResponse(val message: String, @SerializedName("email_sent") val emailSent: Boolean?)

data class LoginRequest(
    val username: String,
    val password: String,
    @SerializedName("device_name") val deviceName: String,
    @SerializedName("device_model") val deviceModel: String,
    @SerializedName("device_type") val deviceType: String,
    @SerializedName("operating_system") val operatingSystem: String,
    @SerializedName("app_version") val appVersion: String
)
data class LoginResponse(
    val message: String,
    @SerializedName("email_sent") val emailSent: Boolean?,
    @SerializedName("masked_email") val maskedEmail: String?,
    val email: String?
)

data class VerifyOtpRequest(val email: String, val otp: String, val purpose: String)
data class VerifyOtpResponse(
    val message: String,
    val token: String?,
    val user: UserDto?,
    @SerializedName("log_id") val logId: Long?
)
data class UserDto(val id: Int, val username: String, @SerializedName("is_admin") val isAdmin: Boolean)

data class ResendOtpRequest(val email: String, val purpose: String)
data class ResendOtpResponse(val message: String, @SerializedName("email_sent") val emailSent: Boolean?)

data class ForgotPasswordRequest(
    val username: String,
    val email: String,
    @SerializedName("new_password") val newPassword: String
)
data class ChangePasswordRequest(
    @SerializedName("old_password") val oldPassword: String,
    @SerializedName("new_password") val newPassword: String
)

data class MessageResponse(val message: String?)
data class ErrorResponse(val error: String?)

// ---------- User dashboard / activity / alerts ----------

data class ActivityLogDto(
    val id: Long,
    @SerializedName("user_id") val userId: Int,
    val username: String,
    @SerializedName("device_name") val deviceName: String?,
    @SerializedName("device_model") val deviceModel: String?,
    @SerializedName("device_type") val deviceType: String?,
    @SerializedName("operating_system") val operatingSystem: String?,
    @SerializedName("app_version") val appVersion: String?,
    @SerializedName("login_status") val loginStatus: String?,
    @SerializedName("login_time") val loginTime: String?,
    @SerializedName("logout_time") val logoutTime: String?,
    @SerializedName("is_active") val isActive: Int,
    @SerializedName("duration_minutes") val durationMinutes: Long?
)

data class ProfileResponse(val username: String, val email: String, @SerializedName("created_at") val createdAt: String)

data class DashboardResponse(
    @SerializedName("total_logins") val totalLogins: Int,
    @SerializedName("last_login") val lastLogin: String?,
    @SerializedName("active_devices") val activeDevices: Int,
    @SerializedName("unread_alerts") val unreadAlerts: Int,
    @SerializedName("recent_activity") val recentActivity: List<ActivityLogDto>
)

data class ActivityHistoryResponse(val activity: List<ActivityLogDto>)

data class AlertDto(
    val id: Long,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("alert_type") val alertType: String,
    val description: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("is_read") val isRead: Int
)
data class AlertsResponse(val alerts: List<AlertDto>)

data class LogoutRequest(@SerializedName("log_id") val logId: Long)

// ---------- Admin ----------

data class AdminStatsResponse(
    @SerializedName("total_users") val totalUsers: Int,
    @SerializedName("total_activities") val totalActivities: Int,
    @SerializedName("failed_logins") val failedLogins: Int,
    @SerializedName("success_logins") val successLogins: Int,
    @SerializedName("total_alerts") val totalAlerts: Int,
    @SerializedName("active_users") val activeUsers: Int,
    @SerializedName("disabled_users") val disabledUsers: Int
)

data class AdminUserDto(
    val id: Int,
    val username: String,
    val email: String,
    @SerializedName("is_admin") val isAdmin: Int,
    @SerializedName("is_active") val isActive: Int,
    @SerializedName("created_at") val createdAt: String
)
data class AdminUsersResponse(val users: List<AdminUserDto>)

data class ToggleUserResponse(val message: String, @SerializedName("is_active") val isActive: Boolean)

data class AdminActivityResponse(val activity: List<ActivityLogDto>)

data class TrendItemDto(val label: String, val success: Int, val failed: Int)
data class DeviceDistItemDto(val type: String, val count: Int)
data class AnalyticsResponse(
    val trend: List<TrendItemDto>,
    @SerializedName("device_distribution") val deviceDistribution: List<DeviceDistItemDto>
)
