package com.securetrace.app.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream

/**
 * Saves a downloaded .xlsx file to the device's public Downloads folder.
 * Handles both the modern MediaStore API (Android 10+) and the legacy
 * direct-file-write approach (Android 9 and below).
 */
object FileDownloadHelper {

    sealed class Result {
        data class Success(val displayPath: String) : Result()
        data class Failure(val message: String) : Result()
    }

    fun saveExcelToDownloads(context: Context, body: ResponseBody, fileName: String): Result {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveViaMediaStore(context, body, fileName)
            } else {
                saveLegacy(body, fileName)
            }
        } catch (e: Exception) {
            Result.Failure(e.message ?: "Couldn't save the file")
        }
    }

    private fun saveViaMediaStore(context: Context, body: ResponseBody, fileName: String): Result {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: return Result.Failure("Couldn't create the file")

        resolver.openOutputStream(uri)?.use { out ->
            body.byteStream().use { input -> input.copyTo(out) }
        } ?: return Result.Failure("Couldn't open the file for writing")

        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)

        return Result.Success("Downloads/$fileName")
    }

    private fun saveLegacy(body: ResponseBody, fileName: String): Result {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()

        val file = File(downloadsDir, fileName)
        FileOutputStream(file).use { out ->
            body.byteStream().use { input -> input.copyTo(out) }
        }
        return Result.Success(file.absolutePath)
    }
}
