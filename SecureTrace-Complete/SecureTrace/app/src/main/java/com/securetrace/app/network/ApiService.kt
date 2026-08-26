package com.securetrace.app.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ApiService {

    @POST("api/register")
    suspend fun register(@Body body: RegisterRequest): Response<RegisterResponse>

    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("api/verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest): Response<VerifyOtpResponse>

    @POST("api/resend-otp")
    suspend fun resendOtp(@Body body: ResendOtpRequest): Response<ResendOtpResponse>

    @POST("api/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<MessageResponse>

    @POST("api/change-password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body body: ChangePasswordRequest
    ): Response<MessageResponse>

    @GET("api/me")
    suspend fun getMe(@Header("Authorization") token: String): Response<ProfileResponse>

    @GET("api/dashboard")
    suspend fun getDashboard(@Header("Authorization") token: String): Response<DashboardResponse>

    @GET("api/activity")
    suspend fun getActivity(
        @Header("Authorization") token: String,
        @Query("days") days: Int? = null
    ): Response<ActivityHistoryResponse>

    @Streaming
    @GET("api/activity/export")
    suspend fun exportMyActivity(
        @Header("Authorization") token: String,
        @Query("days") days: Int? = null
    ): Response<ResponseBody>

    @GET("api/alerts")
    suspend fun getAlerts(@Header("Authorization") token: String): Response<AlertsResponse>

    @POST("api/logout")
    suspend fun logout(
        @Header("Authorization") token: String,
        @Body body: LogoutRequest
    ): Response<MessageResponse>

    @GET("api/admin/stats")
    suspend fun getAdminStats(@Header("Authorization") token: String): Response<AdminStatsResponse>

    @GET("api/admin/users")
    suspend fun getAdminUsers(
        @Header("Authorization") token: String,
        @Query("search") search: String
    ): Response<AdminUsersResponse>

    @POST("api/admin/users/{id}/toggle")
    suspend fun toggleUser(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<ToggleUserResponse>

    @GET("api/admin/activity")
    suspend fun getAdminActivity(
        @Header("Authorization") token: String,
        @Query("days") days: Int? = null
    ): Response<AdminActivityResponse>

    @Streaming
    @GET("api/admin/activity/export")
    suspend fun exportAdminActivity(
        @Header("Authorization") token: String,
        @Query("days") days: Int? = null
    ): Response<ResponseBody>

    @GET("api/admin/analytics")
    suspend fun getAnalytics(@Header("Authorization") token: String): Response<AnalyticsResponse>
}
