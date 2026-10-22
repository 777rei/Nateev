package com.example.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.InputStream
import java.io.OutputStream

data class ExifDetails(
    val iso: String = "Auto",
    val exposureTime: String = "Auto",
    val fNumber: String = "f/1.8",
    val focalLength: String = "24mm",
    val model: String = "Nateev Pro Camera",
    val software: String = "Nateev Native Camera 1.0",
    val latitude: Double? = null,
    val longitude: Double? = null
)

object ExifMetadataWriter {

    fun injectMetadata(
        context: Context,
        imageUri: Uri,
        details: ExifDetails
    ) {
        try {
            context.contentResolver.openFileDescriptor(imageUri, "rw")?.use { pfd ->
                val exif = ExifInterface(pfd.fileDescriptor)
                exif.setAttribute(ExifInterface.TAG_MAKE, "Nateev")
                exif.setAttribute(ExifInterface.TAG_MODEL, "${Build.MANUFACTURER.uppercase()} ${Build.MODEL} (Nateev)")
                exif.setAttribute(ExifInterface.TAG_SOFTWARE, details.software)
                exif.setAttribute(ExifInterface.TAG_DATETIME, java.text.SimpleDateFormat("yyyy:MM:dd HH:mm:ss", java.util.Locale.US).format(java.util.Date()))

                if (details.latitude != null && details.longitude != null) {
                    exif.setLatLong(details.latitude, details.longitude)
                }

                exif.saveAttributes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun readExif(context: Context, imageUri: Uri): ExifDetails {
        return try {
            context.contentResolver.openInputStream(imageUri)?.use { input ->
                val exif = ExifInterface(input)
                val iso = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY) ?: "Auto"
                val exp = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)?.let { "$it s" } ?: "Auto"
                val fNum = exif.getAttribute(ExifInterface.TAG_F_NUMBER)?.let { "f/$it" } ?: "f/1.8"
                val focal = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)?.let { "${it}mm" } ?: "24mm"
                val model = exif.getAttribute(ExifInterface.TAG_MODEL) ?: "Nateev Pro"
                val software = exif.getAttribute(ExifInterface.TAG_SOFTWARE) ?: "Nateev"

                val latLong = exif.latLong
                ExifDetails(
                    iso = iso,
                    exposureTime = exp,
                    fNumber = fNum,
                    focalLength = focal,
                    model = model,
                    software = software,
                    latitude = latLong?.getOrNull(0),
                    longitude = latLong?.getOrNull(1)
                )
            } ?: ExifDetails()
        } catch (e: Exception) {
            ExifDetails()
        }
    }
}
