package com.securetrace.app.network

import android.content.Context
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val PREFS = "securetrace_server"

    fun saveBaseUrl(context: Context, url: String) {
        var clean = url.trim()
        if (!clean.startsWith("http")) clean = "https://$clean"
        if (!clean.endsWith("/")) clean += "/"
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("base_url", clean)
            .apply()
    }

    fun getBaseUrl(context: Context): String {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString("base_url", "") ?: ""
    }

    fun isConfigured(context: Context): Boolean = getBaseUrl(context).isNotBlank()

    fun create(context: Context): ApiService {
        val baseUrl = getBaseUrl(context).ifBlank { "https://example.invalid/" }
        val client = OkHttpClient.Builder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

/** Extracts a human-readable error message from a failed Retrofit response. */
object ErrorParser {
    fun parse(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                val parsed = Gson().fromJson(errorBody, ErrorResponse::class.java)
                parsed.error ?: "Something went wrong (${response.code()})"
            } else {
                "Something went wrong (${response.code()})"
            }
        } catch (e: Exception) {
            "Something went wrong (${response.code()})"
        }
    }
}
