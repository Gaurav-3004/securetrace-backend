package com.securetrace.app.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Collects only non-sensitive device metadata needed for security monitoring.
 * Deliberately does NOT collect IP address, GPS coordinates, or precise location.
 */
object DeviceUtil {

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }
    }

    fun getDeviceModel(): String = Build.MODEL

    fun getDeviceType(): String {
        // Simple heuristic: screen size isn't queried here to avoid needing a Context
        // at call sites that don't have one; default to Phone which covers the vast
        // majority of installs. Tablet detection can be refined later if needed.
        return "Phone"
    }

    fun getOperatingSystem(): String = "Android ${Build.VERSION.RELEASE}"

    fun getAppVersion(context: Context): String {
        return try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0"
        }
    }
}
