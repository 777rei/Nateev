package com.example.ui.components

import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.View
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.GridType
import com.example.domain.models.CameraSettings
import com.example.engine.CameraEngine
import com.example.engine.FocusPeakingShaderEngine
import com.example.engine.NpuGpuComputePipeline

@Composable
fun CameraPreview(
    cameraEngine: CameraEngine,
    settings: CameraSettings,
    gridType: GridType,
    enableNpuAcceleration: Boolean = true,
    onHistogramUpdate: (IntArray) -> Unit,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    // Bind Camera ONLY when lens, mode, or surface changes - NEVER on every recomposition frame!
    LaunchedEffect(
        settings.lensFacing,
        settings.isVideoMode,
        settings.isStabilizationEnabled,
        previewViewRef
    ) {
        val pv = previewViewRef ?: return@LaunchedEffect
        cameraEngine.bindCamera(
            lifecycleOwner = lifecycleOwner,
            surfaceProvider = pv.surfaceProvider,
            lensFacing = settings.lensFacing,
            isVideoMode = settings.isVideoMode,
            preset = settings.activePreset,
            enableStabilization = settings.isStabilizationEnabled,
            onHistogramData = onHistogramUpdate,
            onCameraBound = { camera ->
                camera.cameraControl.setExposureCompensationIndex(settings.exposureCompensation)
                camera.cameraControl.setZoomRatio(settings.zoomRatio)
                cameraEngine.setFlashMode(settings.flashMode)
            }
        )
    }

    // Update dynamic properties smoothly without unbinding camera
    LaunchedEffect(settings.exposureCompensation) {
        cameraEngine.setExposureCompensation(settings.exposureCompensation)
    }

    LaunchedEffect(settings.zoomRatio) {
        cameraEngine.setZoomRatio(settings.zoomRatio)
    }

    LaunchedEffect(settings.flashMode) {
        cameraEngine.setFlashMode(settings.flashMode)
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PreviewView(context).apply {
                    // COMPATIBLE mode uses TextureView so paint filters and AGSL shaders render properly
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    setLayerType(View.LAYER_TYPE_HARDWARE, Paint().apply {
                        colorFilter = ColorMatrixColorFilter(settings.activePreset.colorMatrix)
                    })
                    previewViewRef = this
                }
            },
            update = { previewView ->
                // Fast visual update: only set paint filter on recomposition
                val paint = Paint().apply {
                    colorFilter = ColorMatrixColorFilter(settings.activePreset.colorMatrix)
                }
                previewView.setLayerType(View.LAYER_TYPE_HARDWARE, paint)

                if (settings.isFocusPeakingEnabled || settings.isZebraEnabled) {
                    FocusPeakingShaderEngine.applyFocusAndZebra(
                        view = previewView,
                        width = previewView.width.toFloat(),
                        height = previewView.height.toFloat(),
                        enablePeaking = settings.isFocusPeakingEnabled,
                        enableZebra = settings.isZebraEnabled
                    )
                } else if (enableNpuAcceleration) {
                    NpuGpuComputePipeline.attachGpuHdrComputeShader(
                        view = previewView,
                        width = previewView.width.toFloat(),
                        height = previewView.height.toFloat(),
                        exposureWeight = 1.0f + (settings.exposureCompensation * 0.1f),
                        noiseReductionStrength = 0.85f,
                        clarityBoost = 0.35f
                    )
                } else {
                    FocusPeakingShaderEngine.clearShader(previewView)
                }
            }
        )

        if (settings.gridOverlayEnabled && gridType != GridType.NONE) {
            DynamicGridOverlay(gridType = gridType)
        }
    }
}

@Composable
fun DynamicGridOverlay(gridType: GridType) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val strokeWidth = 1.2f
        val color = Color.White.copy(alpha = 0.35f)

        when (gridType) {
            GridType.RULE_OF_THIRDS -> {
                drawLine(color, Offset(width / 3f, 0f), Offset(width / 3f, height), strokeWidth)
                drawLine(color, Offset(2 * width / 3f, 0f), Offset(2 * width / 3f, height), strokeWidth)
                drawLine(color, Offset(0f, height / 3f), Offset(width, height / 3f), strokeWidth)
                drawLine(color, Offset(0f, 2 * height / 3f), Offset(width, 2 * height / 3f), strokeWidth)
            }
            GridType.GOLDEN_RATIO -> {
                val phi1 = width * 0.382f
                val phi2 = width * 0.618f
                val hPhi1 = height * 0.382f
                val hPhi2 = height * 0.618f
                drawLine(color, Offset(phi1, 0f), Offset(phi1, height), strokeWidth)
                drawLine(color, Offset(phi2, 0f), Offset(phi2, height), strokeWidth)
                drawLine(color, Offset(0f, hPhi1), Offset(width, hPhi1), strokeWidth)
                drawLine(color, Offset(0f, hPhi2), Offset(width, hPhi2), strokeWidth)
            }
            GridType.FIBONACCI -> {
                drawLine(color, Offset(0f, 0f), Offset(width, height), strokeWidth)
                drawLine(color, Offset(width, 0f), Offset(0f, height), strokeWidth)
                drawLine(color, Offset(width / 2f, 0f), Offset(width / 2f, height), strokeWidth * 0.8f)
                drawLine(color, Offset(0f, height / 2f), Offset(width, height / 2f), strokeWidth * 0.8f)
            }
            GridType.NONE -> {}
        }
    }
}
