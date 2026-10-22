package com.example.ui

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiCameraAssistant
import com.example.data.AppPreferences
import com.example.data.SettingsRepository
import com.example.domain.models.*
import com.example.engine.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class CameraViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    val cameraEngine = CameraEngine(application)
    val settingsRepo = SettingsRepository(application)
    val burstManager = BurstCaptureManager()
    val timelapseEngine = TimelapseEngine()

    private val _uiState = MutableStateFlow(CameraSettings())
    val uiState: StateFlow<CameraSettings> = _uiState.asStateFlow()

    private val _appPreferences = MutableStateFlow(settingsRepo.loadPreferences())
    val appPreferences: StateFlow<AppPreferences> = _appPreferences.asStateFlow()

    private val _histogramData = MutableStateFlow<IntArray?>(null)
    val histogramData: StateFlow<IntArray?> = _histogramData.asStateFlow()

    private val _hardwareProfile = MutableStateFlow(
        HardwareCapabilityScanner.scanCapabilities(application, 0)
    )
    val hardwareProfile: StateFlow<CameraHardwareProfile> = _hardwareProfile.asStateFlow()

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var recordingTimerJob: Job? = null

    init {
        viewModelScope.launch {
            cameraEngine.initializeProvider()
        }
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager?.unregisterListener(this)
        cameraEngine.audioRecorderPipeline.stopMetering()
        burstManager.cancelBurst()
        timelapseEngine.stopTimelapse()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val ax = event.values[0]
            val ay = event.values[1]
            val az = event.values[2]

            // Calculate pitch and roll in degrees
            val pitch = atan2(ay.toDouble(), sqrt((ax * ax + az * az).toDouble())) * 180.0 / Math.PI
            val roll = atan2(-ax.toDouble(), az.toDouble()) * 180.0 / Math.PI

            val isLevel = abs(roll) < 1.5 && abs(pitch) < 2.0

            _uiState.update {
                it.copy(
                    pitchAngle = pitch.toFloat(),
                    rollAngle = roll.toFloat(),
                    isLevel = isLevel
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun updateHistogram(data: IntArray) {
        _histogramData.value = data
    }

    fun selectPreset(preset: CameraPreset) {
        _uiState.update { it.copy(activePreset = preset) }
    }

    fun setExposure(evIndex: Int) {
        _uiState.update { it.copy(exposureCompensation = evIndex) }
        cameraEngine.setExposureCompensation(evIndex)
    }

    fun setZoom(zoomRatio: Float) {
        _uiState.update { it.copy(zoomRatio = zoomRatio) }
        cameraEngine.setZoomRatio(zoomRatio)
    }

    fun setCaptureMode(mode: CaptureMode) {
        _uiState.update {
            it.copy(
                captureMode = mode,
                isVideoMode = (mode == CaptureMode.VIDEO),
                isManualControlsVisible = (mode == CaptureMode.PRO_MANUAL) || it.isManualControlsVisible
            )
        }
    }

    fun toggleStabilization() {
        _uiState.update { it.copy(isStabilizationEnabled = !it.isStabilizationEnabled) }
    }

    fun toggleGrid() {
        _uiState.update { it.copy(gridOverlayEnabled = !it.gridOverlayEnabled) }
    }

    fun toggleFlash() {
        val nextFlash = when (_uiState.value.flashMode) {
            FlashMode.OFF -> FlashMode.ON
            FlashMode.ON -> FlashMode.AUTO
            FlashMode.AUTO -> FlashMode.TORCH
            FlashMode.TORCH -> FlashMode.OFF
        }
        _uiState.update { it.copy(flashMode = nextFlash) }
        cameraEngine.setFlashMode(nextFlash)
    }

    fun toggleManualControls() {
        _uiState.update { it.copy(isManualControlsVisible = !it.isManualControlsVisible) }
    }

    fun setManualIso(iso: Int) {
        _uiState.update { it.copy(manualIso = iso, isManualModeActive = true) }
        cameraEngine.manualControl?.setManualExposure(iso, _uiState.value.manualShutterNumerator)
    }

    fun setManualShutter(timeNanos: Long) {
        _uiState.update { it.copy(manualShutterNumerator = timeNanos, isManualModeActive = true) }
        cameraEngine.manualControl?.setManualExposure(_uiState.value.manualIso, timeNanos)
    }

    fun setManualFocusDistance(diopters: Float) {
        _uiState.update { it.copy(manualFocusDistance = diopters, isManualModeActive = true) }
        if (diopters == 0f) {
            cameraEngine.manualControl?.resetToAuto()
        } else {
            cameraEngine.manualControl?.setManualFocus(diopters)
        }
    }

    fun toggleFocusPeaking() {
        _uiState.update { it.copy(isFocusPeakingEnabled = !it.isFocusPeakingEnabled) }
    }

    fun toggleZebra() {
        _uiState.update { it.copy(isZebraEnabled = !it.isZebraEnabled) }
    }

    fun resetManualToAuto() {
        _uiState.update {
            it.copy(
                manualIso = 0,
                manualShutterNumerator = 0L,
                manualFocusDistance = 0f,
                isManualModeActive = false
            )
        }
        cameraEngine.manualControl?.resetToAuto()
        Toast.makeText(getApplication(), "Auto Exposure & Focus Reset", Toast.LENGTH_SHORT).show()
    }

    fun switchCamera() {
        val nextFacing = if (_uiState.value.lensFacing == 0) 1 else 0
        _uiState.update { it.copy(lensFacing = nextFacing) }
        _hardwareProfile.value = HardwareCapabilityScanner.scanCapabilities(getApplication(), nextFacing)
    }

    fun capturePhoto() {
        val state = _uiState.value
        val preset = state.activePreset
        cameraEngine.takePhoto(
            preset = preset,
            enableStabilization = state.isStabilizationEnabled,
            isWatermarkEnabled = _appPreferences.value.isWatermarkEnabled,
            onSuccess = { uri ->
                _uiState.update { it.copy(lastCapturedUri = uri) }
                Toast.makeText(getApplication(), "Saved with ${preset.displayName} (Stabilized)", Toast.LENGTH_SHORT).show()
            },
            onError = { exc ->
                Toast.makeText(getApplication(), "Capture error: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun triggerShutterAction() {
        when (_uiState.value.captureMode) {
            CaptureMode.PHOTO, CaptureMode.PRO_MANUAL -> capturePhoto()
            CaptureMode.VIDEO -> toggleRecording()
            CaptureMode.BURST -> triggerBurstCapture()
            CaptureMode.TIMELAPSE -> toggleTimelapse()
        }
    }

    private fun triggerBurstCapture() {
        if (burstManager.isBursting) {
            burstManager.cancelBurst()
            Toast.makeText(getApplication(), "Burst stopped", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(getApplication(), "Burst 10 frames started...", Toast.LENGTH_SHORT).show()
            burstManager.startBurst(
                frameCount = 10,
                intervalMs = 150L,
                onFrameTrigger = { capturePhoto() },
                onComplete = { count ->
                    Toast.makeText(getApplication(), "Burst completed: $count frames", Toast.LENGTH_SHORT).show()
                },
                scope = viewModelScope
            )
        }
    }

    private fun toggleTimelapse() {
        if (timelapseEngine.isRunning) {
            timelapseEngine.stopTimelapse()
            Toast.makeText(getApplication(), "Timelapse stopped", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(getApplication(), "Timelapse started", Toast.LENGTH_SHORT).show()
            timelapseEngine.startTimelapse(
                interval = TimelapseInterval.WALKING_STREET,
                onCaptureFrame = { capturePhoto() },
                scope = viewModelScope
            )
        }
    }

    fun toggleRecording() {
        val currentlyRecording = _uiState.value.isRecording
        if (currentlyRecording) {
            cameraEngine.stopRecording()
            cameraEngine.audioRecorderPipeline.stopMetering()
            recordingTimerJob?.cancel()
            _uiState.update { it.copy(isRecording = false, recordingDurationSec = 0L) }
        } else {
            _uiState.update { it.copy(isRecording = true, recordingDurationSec = 0L) }

            cameraEngine.audioRecorderPipeline.startMetering(
                onDecibelUpdate = { db ->
                    _uiState.update { it.copy(audioDecibelLevel = db) }
                },
                scope = viewModelScope
            )

            recordingTimerJob = viewModelScope.launch {
                while (isActive && _uiState.value.isRecording) {
                    delay(1000L)
                    _uiState.update { it.copy(recordingDurationSec = it.recordingDurationSec + 1L) }
                }
            }

            cameraEngine.startRecording(
                onVideoSaved = { uri ->
                    _uiState.update { it.copy(lastCapturedUri = uri, isRecording = false) }
                    cameraEngine.audioRecorderPipeline.stopMetering()
                    recordingTimerJob?.cancel()
                    Toast.makeText(getApplication(), "Video Saved: Nateev", Toast.LENGTH_SHORT).show()
                },
                onError = { err ->
                    Toast.makeText(getApplication(), err, Toast.LENGTH_SHORT).show()
                    cameraEngine.audioRecorderPipeline.stopMetering()
                    recordingTimerJob?.cancel()
                    _uiState.update { it.copy(isRecording = false) }
                }
            )
        }
    }

    fun loadCustomCubeLut(uri: Uri) {
        viewModelScope.launch {
            try {
                getApplication<Application>().contentResolver.openInputStream(uri)?.use { stream ->
                    val lutData = CubeLutParser.parseCubeStream(stream)
                    if (lutData != null) {
                        val customPreset = CameraPreset(
                            id = "custom_lut_${System.currentTimeMillis()}",
                            displayName = lutData.title.take(16),
                            category = PresetCategory.CREATIVE_FILM,
                            description = "User custom 3D .cube Look-Up Table (Size: ${lutData.size}x${lutData.size})",
                            colorMatrix = lutData.toColorMatrix(),
                            cubeLutData = lutData
                        )
                        _uiState.update { it.copy(activePreset = customPreset) }
                        Toast.makeText(getApplication(), "Applied LUT: ${lutData.title}", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(getApplication(), "Failed to parse .cube LUT", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(getApplication(), "Error loading LUT: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun applyBuiltInLut(lutData: CubeLutData) {
        val customPreset = CameraPreset(
            id = "builtin_lut_${lutData.title.hashCode()}",
            displayName = lutData.title.replace(".cube", "").trim(),
            category = PresetCategory.CREATIVE_FILM,
            description = "Cinema 3D .cube LUT (${lutData.size}x${lutData.size}x${lutData.size})",
            colorMatrix = lutData.toColorMatrix(),
            cubeLutData = lutData
        )
        _uiState.update { it.copy(activePreset = customPreset) }
        Toast.makeText(getApplication(), "Applied: ${customPreset.displayName}", Toast.LENGTH_SHORT).show()
    }

    // AI Director (gemini-3.1-flash-lite)
    suspend fun getFastSceneAdvice(hint: String): String {
        return GeminiCameraAssistant.getQuickSceneAdvice(
            sceneDescription = hint,
            currentPreset = _uiState.value.activePreset.displayName,
            currentEv = _uiState.value.exposureCompensation,
            isLevel = _uiState.value.isLevel
        )
    }

    // Deep Optical Analysis (gemini-4-argon with thinkingLevel: HIGH)
    suspend fun getDeepThinkingAnalysis(inquiry: String): String {
        val hw = _hardwareProfile.value
        val hwSummary = "Level: ${hw.hardwareLevelName}, OIS: ${hw.hasOis}, RAW: ${hw.supportsRaw}, ISO: ${hw.isoRange}"
        return GeminiCameraAssistant.getDeepThinkingProAnalysis(
            query = inquiry,
            hardwareProfile = hwSummary,
            currentIso = _uiState.value.manualIso,
            currentShutter = _uiState.value.manualShutterNumerator,
            activePreset = _uiState.value.activePreset.displayName
        )
    }

    fun updatePreferences(newPrefs: AppPreferences) {
        _appPreferences.value = newPrefs
        settingsRepo.saveCodec(newPrefs.videoCodec)
        settingsRepo.saveBitrate(newPrefs.targetBitrateMbps)
        settingsRepo.saveGridType(newPrefs.gridType)
        settingsRepo.setNpuAcceleration(newPrefs.enableNpuAcceleration)
        settingsRepo.setRawPlusJpeg(newPrefs.enableRawPlusJpeg)
        settingsRepo.setShutterSound(newPrefs.shutterSound)
        settingsRepo.setAutoGeotagging(newPrefs.autoGeotagging)
        settingsRepo.setAudioStereo(newPrefs.audioStereo)
        settingsRepo.setWatermarkEnabled(newPrefs.isWatermarkEnabled)
    }

    fun toggleWatermark() {
        _uiState.update { it.copy(isWatermarkEnabled = !it.isWatermarkEnabled) }
    }

    fun lockFocusExposure(x: Float, y: Float) {
        val metrics = getApplication<Application>().resources.displayMetrics
        val normalizedX = x / metrics.widthPixels
        val normalizedY = y / metrics.heightPixels
        
        cameraEngine.camera?.cameraControl?.startFocusAndMetering(
            androidx.camera.core.FocusMeteringAction.Builder(
                androidx.camera.core.SurfaceOrientedMeteringPointFactory(1.0f, 1.0f).createPoint(normalizedX, normalizedY),
                androidx.camera.core.FocusMeteringAction.FLAG_AF or androidx.camera.core.FocusMeteringAction.FLAG_AE
            ).build()
        )
        _uiState.update { it.copy(isFocusExposureLocked = true) }
        Toast.makeText(getApplication(), "Focus & Exposure Locked", Toast.LENGTH_SHORT).show()
    }

    fun unlockFocusExposure() {
        cameraEngine.camera?.cameraControl?.cancelFocusAndMetering()
        _uiState.update { it.copy(isFocusExposureLocked = false) }
        Toast.makeText(getApplication(), "Focus & Exposure Unlocked", Toast.LENGTH_SHORT).show()
    }
}
