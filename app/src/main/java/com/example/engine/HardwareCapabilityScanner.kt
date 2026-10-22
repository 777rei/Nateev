package com.example.engine

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.util.Range
import android.util.SizeF

data class CameraHardwareProfile(
    val hardwareLevelName: String,
    val hasOis: Boolean,
    val hasVideoStabilization: Boolean,
    val isoRange: Range<Int>?,
    val exposureTimeRange: Range<Long>?,
    val minFocusDistance: Float,
    val supportsRaw: Boolean,
    val availableApertures: FloatArray?,
    val focalLengths: FloatArray?,
    val sensorSize: SizeF?,
    val hasFlash: Boolean
)

object HardwareCapabilityScanner {

    fun scanCapabilities(context: Context, lensFacing: Int = 0): CameraHardwareProfile {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            ?: return createFallbackProfile()

        try {
            val cameraIds = manager.cameraIdList
            for (id in cameraIds) {
                val chars = manager.getCameraCharacteristics(id)
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                val targetFacing = if (lensFacing == 0) {
                    CameraCharacteristics.LENS_FACING_BACK
                } else {
                    CameraCharacteristics.LENS_FACING_FRONT
                }

                if (facing == targetFacing) {
                    val level = chars.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
                    val levelName = when (level) {
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "LEGACY"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "LIMITED"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "FULL"
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "LEVEL_3 (PRO)"
                        4 -> "EXTERNAL"
                        else -> "UNKNOWN"
                    }

                    val oisModes = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
                    val hasOis = oisModes?.contains(CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_ON) == true

                    val videoStabModes = chars.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES)
                    val hasVideoStab = videoStabModes?.contains(CameraCharacteristics.CONTROL_VIDEO_STABILIZATION_MODE_ON) == true

                    val isoRange = chars.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE)
                    val exposureTimeRange = chars.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE)
                    val minFocusDistance = chars.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE) ?: 0.0f

                    val capabilities = chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                    val supportsRaw = capabilities?.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW) == true

                    val apertures = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)
                    val focalLengths = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                    val sensorSize = chars.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                    val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

                    return CameraHardwareProfile(
                        hardwareLevelName = levelName,
                        hasOis = hasOis,
                        hasVideoStabilization = hasVideoStab,
                        isoRange = isoRange,
                        exposureTimeRange = exposureTimeRange,
                        minFocusDistance = minFocusDistance,
                        supportsRaw = supportsRaw,
                        availableApertures = apertures,
                        focalLengths = focalLengths,
                        sensorSize = sensorSize,
                        hasFlash = hasFlash
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return createFallbackProfile()
    }

    private fun createFallbackProfile(): CameraHardwareProfile {
        return CameraHardwareProfile(
            hardwareLevelName = "COMPATIBLE",
            hasOis = true,
            hasVideoStabilization = true,
            isoRange = Range(50, 6400),
            exposureTimeRange = Range(100_000L, 1_000_000_000L),
            minFocusDistance = 10.0f,
            supportsRaw = false,
            availableApertures = floatArrayOf(1.8f),
            focalLengths = floatArrayOf(24f),
            sensorSize = SizeF(6.4f, 4.8f),
            hasFlash = true
        )
    }
}
