package com.example.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileManager {

    private const val PHOTO_FOLDER = "Pictures/NateevCamera"
    private const val VIDEO_FOLDER = "Movies/NateevCamera"

    fun createPhotoUri(context: Context, extension: String = "jpg"): Uri? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(Date())
        val displayName = "NATEEV_$timestamp.$extension"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, if (extension == "dng") "image/x-adobe-dng" else "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, PHOTO_FOLDER)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    fun markPhotoComplete(context: Context, uri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            context.contentResolver.update(uri, values, null, null)
        }
    }

    fun getTempMediaFile(context: Context, suffix: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val cacheDir = context.cacheDir
        return File.createTempFile("NATEEV_${timestamp}_", suffix, cacheDir)
    }
}
