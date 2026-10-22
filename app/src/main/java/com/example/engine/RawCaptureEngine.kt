package com.example.engine

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.DngCreator
import android.media.Image
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileOutputStream

object RawCaptureEngine {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun writeDng(
        characteristics: CameraCharacteristics,
        captureResult: CaptureResult,
        image: Image,
        destinationFile: File
    ): Boolean {
        return try {
            DngCreator(characteristics, captureResult).use { dngCreator ->
                FileOutputStream(destinationFile).use { fos ->
                    dngCreator.writeImage(fos, image)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
