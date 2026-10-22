package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.domain.models.CameraPreset
import com.example.ui.components.*

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    onNavigateToGallery: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val preferences by viewModel.appPreferences.collectAsState()
    val histogramData by viewModel.histogramData.collectAsState()

    var showLutPicker by remember { mutableStateOf(false) }
    var selectedInspectorUri by remember { mutableStateOf<String?>(null) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: false
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // Camera Live Viewfinder
            CameraPreview(
                cameraEngine = viewModel.cameraEngine,
                settings = uiState,
                gridType = preferences.gridType,
                enableNpuAcceleration = preferences.enableNpuAcceleration,
                onHistogramUpdate = { viewModel.updateHistogram(it) }
            )

            // Touch to Focus & Metering Gesture overlay
            GestureFocusOverlay(
                isLocked = uiState.isFocusExposureLocked,
                onFocusRequested = { offset -> viewModel.lockFocusExposure(offset.x, offset.y) },
                onUnlockRequested = { viewModel.unlockFocusExposure() }
            )

            // Sensor Driven Virtual Horizon Leveling Overlay
            if (uiState.isVirtualHorizonEnabled) {
                VirtualHorizonOverlay(
                    pitchAngle = uiState.pitchAngle,
                    rollAngle = uiState.rollAngle
                )
            }

            // Top Video Recording HUD (Time, Red flashing beacon, VU meter)
            if (uiState.isRecording) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 56.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    VideoRecordingOverlay(
                        isRecording = uiState.isRecording,
                        durationSec = uiState.recordingDurationSec,
                        decibelLevel = uiState.audioDecibelLevel
                    )
                }
            }

            // Main Viewfinder HUD Controls
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(52.dp))

                // Mid Floating Controls: Lens Selector, AI Director Pill, Preset Selector
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LensSelector(
                            selectedZoomRatio = uiState.zoomRatio,
                            onZoomRatioSelected = { viewModel.setZoom(it) }
                        )

                        AiDirectorOverlay(
                            currentPresetName = uiState.activePreset.displayName,
                            currentEv = uiState.exposureCompensation,
                            isLevel = uiState.isLevel,
                            currentIso = uiState.manualIso,
                            currentShutter = uiState.manualShutterNumerator,
                            onGetFastAdvice = { hint -> viewModel.getFastSceneAdvice(hint) },
                            onGetDeepThinking = { inquiry -> viewModel.getDeepThinkingAnalysis(inquiry) }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    PresetSelector(
                        presets = CameraPreset.getAllPresets(),
                        activePreset = uiState.activePreset,
                        onPresetSelected = { viewModel.selectPreset(it) }
                    )

                    ControlOverlay(
                        settings = uiState,
                        histogramData = histogramData,
                        onEVChange = { viewModel.setExposure(it) },
                        onModeSelected = { viewModel.setCaptureMode(it) },
                        onToggleStabilization = { viewModel.toggleStabilization() },
                        onToggleGrid = { viewModel.toggleGrid() },
                        onToggleFlash = { viewModel.toggleFlash() },
                        onToggleManualControls = { viewModel.toggleManualControls() },
                        onOpenLutPicker = { showLutPicker = true },
                        onOpenSettings = onNavigateToSettings,
                        onSwitchCamera = { viewModel.switchCamera() },
                        onShutterClick = { viewModel.triggerShutterAction() },
                        onOpenGallery = {
                            if (uiState.lastCapturedUri != null) {
                                selectedInspectorUri = uiState.lastCapturedUri
                            } else {
                                onNavigateToGallery()
                            }
                        }
                    )
                }
            }

            // Pro Manual Controls Drawer
            AnimatedVisibility(
                visible = uiState.isManualControlsVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                ProManualDashboard(
                    settings = uiState,
                    onSetIso = { viewModel.setManualIso(it) },
                    onSetShutter = { viewModel.setManualShutter(it) },
                    onSetFocusDistance = { viewModel.setManualFocusDistance(it) },
                    onToggleFocusPeaking = { viewModel.toggleFocusPeaking() },
                    onToggleZebra = { viewModel.toggleZebra() },
                    onResetToAuto = { viewModel.resetManualToAuto() },
                    onClose = { viewModel.toggleManualControls() }
                )
            }

            // 3D LUT File Picker Sheet
            if (showLutPicker) {
                LutPickerBottomSheet(
                    onLutSelected = { uri -> viewModel.loadCustomCubeLut(uri) },
                    onBuiltInSelected = { lutData -> viewModel.applyBuiltInLut(lutData) },
                    activeLutTitle = uiState.activePreset.displayName,
                    onDismiss = { showLutPicker = false }
                )
            }

            // EXIF Media Inspector Sheet
            val inspectorUri = selectedInspectorUri
            if (inspectorUri != null) {
                MediaGallerySheet(
                    imageUri = inspectorUri,
                    colorMatrix = uiState.activePreset.colorMatrix,
                    onDismiss = { selectedInspectorUri = null }
                )
            }
        }
    } else {
        // Permission Request Fallback Screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F1115)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "NATEEV CAMERA",
                    color = Color(0xFFFFD54F),
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Nateev requires Camera and Microphone hardware permissions to unleash pro stabilization, manual diopter control, and AGSL real-time LUT color science.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = {
                        launcher.launch(
                            arrayOf(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F), contentColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Grant Permissions", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                }
            }
        }
    }
}
