package com.example.domain.models

enum class FlashMode { OFF, ON, AUTO, TORCH }
enum class CaptureMode { PHOTO, VIDEO, BURST, TIMELAPSE, PRO_MANUAL }

data class CameraSettings(
    val isVideoMode: Boolean = false,
    val isRecording: Boolean = false,
    val recordingDurationSec: Long = 0L,
    val captureMode: CaptureMode = CaptureMode.PHOTO,
    val exposureCompensation: Int = 0,
    val isStabilizationEnabled: Boolean = true,
    val activePreset: CameraPreset = CameraPreset.getAllPresets().first(),
    val flashMode: FlashMode = FlashMode.OFF,
    val gridOverlayEnabled: Boolean = true,
    val lensFacing: Int = 0, // 0 = Back, 1 = Front
    val zoomRatio: Float = 1.0f,
    val isFocusPeakingEnabled: Boolean = false,
    val isZebraEnabled: Boolean = false,
    val isPortraitBokehEnabled: Boolean = false,
    val bokehAperture: Float = 4.0f,
    val isVirtualHorizonEnabled: Boolean = true,
    val isLiveHistogramEnabled: Boolean = true,
    val isManualControlsVisible: Boolean = false,
    val manualIso: Int = 0, // 0 = Auto
    val manualShutterNumerator: Long = 0L, // 0 = Auto, e.g. 10000000L for 1/100s
    val manualFocusDistance: Float = 0f, // 0 = Auto AF
    val isManualModeActive: Boolean = false,
    val lastCapturedUri: String? = null,
    val audioDecibelLevel: Float = -60f,
    val pitchAngle: Float = 0f,
    val rollAngle: Float = 0f,
    val isLevel: Boolean = false,
    val isWatermarkEnabled: Boolean = false,
    val isFocusExposureLocked: Boolean = false
)
