package com.example.engine

import android.content.ContentValues
import android.content.Context
import android.hardware.camera2.CaptureRequest
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.domain.models.CameraPreset
import com.example.domain.models.FlashMode
import com.example.utils.ExifDetails
import com.example.utils.ExifMetadataWriter
import com.example.utils.LocationProvider
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.runBlocking

class CameraEngine(private val context: Context) {

    var camera: Camera? = null
        private set
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var activeRecording: Recording? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private val locationProvider = LocationProvider(context)
    val audioRecorderPipeline = AudioRecorderPipeline()

    var manualControl: ManualCameraControl? = null
        private set

    suspend fun initializeProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            try {
                cameraProvider = providerFuture.get()
                continuation.resume(cameraProvider!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    @OptIn(ExperimentalCamera2Interop::class)
    fun bindCamera(
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        lensFacing: Int,
        isVideoMode: Boolean,
        preset: CameraPreset,
        enableStabilization: Boolean,
        onHistogramData: (IntArray) -> Unit,
        onCameraBound: (Camera) -> Unit
    ) {
        val provider = cameraProvider ?: return

        try {
            imageCapture = null
            videoCapture = null
            provider.unbindAll()

            val selector = if (lensFacing == 0) CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA

            val previewBuilder = Preview.Builder()
            val previewCamera2Extender = Camera2Interop.Extender(previewBuilder)

            if (enableStabilization) {
                previewCamera2Extender.setCaptureRequestOption(
                    CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                    CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON
                )
                previewCamera2Extender.setCaptureRequestOption(
                    CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                    CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON
                )
            }

            previewCamera2Extender.setCaptureRequestOption(
                CaptureRequest.EDGE_MODE,
                CaptureRequest.EDGE_MODE_HIGH_QUALITY
            )
            previewCamera2Extender.setCaptureRequestOption(
                CaptureRequest.NOISE_REDUCTION_MODE,
                CaptureRequest.NOISE_REDUCTION_MODE_HIGH_QUALITY
            )

            val preview = previewBuilder.build().also {
                it.setSurfaceProvider(surfaceProvider)
            }

            // Real-time Luminance Analysis for Live Histogram across all modes
            val analysisBuilder = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)

            val analysis = analysisBuilder.build().also {
                it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    try {
                        val buffer = imageProxy.planes[0].buffer
                        val histogram = calculateLuminanceHistogram(buffer, imageProxy.width, imageProxy.height)
                        onHistogramData(histogram)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        imageProxy.close()
                    }
                }
            }
            imageAnalysis = analysis

            if (isVideoMode) {
                // Foolproof quality selector: Try FHD, fallback to HD, then SD
                val qualitySelector = QualitySelector.fromOrderedList(
                    listOf(Quality.FHD, Quality.HD, Quality.SD),
                    FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
                )

                val recorder = Recorder.Builder()
                    .setQualitySelector(qualitySelector)
                    .build()

                val videoCaptureBuilder = VideoCapture.Builder(recorder)

                val vCapture = videoCaptureBuilder.build()
                videoCapture = vCapture

                camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    vCapture,
                    analysis
                )
            } else {
                val captureBuilder = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)

                val captureExtender = Camera2Interop.Extender(captureBuilder)
                captureExtender.setCaptureRequestOption(
                    CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
                    CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY
                )
                if (enableStabilization) {
                    captureExtender.setCaptureRequestOption(
                        CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                        CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON
                    )
                }

                val imgCap = captureBuilder.build()
                imageCapture = imgCap

                camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    imgCap,
                    analysis
                )
            }

            camera?.let {
                manualControl = ManualCameraControl(it)
                onCameraBound(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateLuminanceHistogram(buffer: ByteBuffer, width: Int, height: Int): IntArray {
        val histogram = IntArray(256)
        val data = ByteArray(buffer.remaining())
        buffer.get(data)
        val step = 4
        val total = data.size
        var i = 0
        while (i < total) {
            val y = data[i].toInt() and 0xFF
            histogram[y]++
            i += step
        }
        return histogram
    }

    fun setExposureCompensation(index: Int) {
        try {
            camera?.cameraControl?.setExposureCompensationIndex(index)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setZoomRatio(zoomRatio: Float) {
        try {
            camera?.cameraControl?.setZoomRatio(zoomRatio)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setFlashMode(flashMode: FlashMode) {
        val cam = camera ?: return
        try {
            when (flashMode) {
                FlashMode.TORCH -> cam.cameraControl.enableTorch(true)
                FlashMode.OFF -> {
                    cam.cameraControl.enableTorch(false)
                    imageCapture?.flashMode = ImageCapture.FLASH_MODE_OFF
                }
                FlashMode.ON -> {
                    cam.cameraControl.enableTorch(false)
                    imageCapture?.flashMode = ImageCapture.FLASH_MODE_ON
                }
                FlashMode.AUTO -> {
                    cam.cameraControl.enableTorch(false)
                    imageCapture?.flashMode = ImageCapture.FLASH_MODE_AUTO
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val processingExecutor = Executors.newSingleThreadExecutor()

    fun takePhoto(
        preset: CameraPreset,
        enableStabilization: Boolean,
        isWatermarkEnabled: Boolean,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val capture = imageCapture ?: run {
            onError(IllegalStateException("Camera capture not initialized"))
            return
        }

        capture.takePicture(
            processingExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                    try {
                        val rawBitmap = imageProxy.toBitmap()
                        val rotation = imageProxy.imageInfo.rotationDegrees
                        val orientedBitmap = if (rotation != 0) {
                            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                            Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
                        } else {
                            rawBitmap
                        }

                        // Apply exact 3D .cube trilinear interpolation or ColorMatrix LUT
                        val baseGraded = if (preset.cubeLutData != null) {
                            runBlocking { preset.cubeLutData.apply3dLut(orientedBitmap) }
                        } else {
                            val output = Bitmap.createBitmap(
                                orientedBitmap.width,
                                orientedBitmap.height,
                                Bitmap.Config.ARGB_8888
                            )
                            val canvas = Canvas(output)
                            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                            paint.colorFilter = ColorMatrixColorFilter(preset.colorMatrix)
                            canvas.drawBitmap(orientedBitmap, 0f, 0f, paint)
                            output
                        }

                        // Apply advanced software virtual gimbal stabilization if enabled
                        val stabilizedBitmap = if (enableStabilization) {
                            StabilizationEngine.stabilizeBitmapInstant(baseGraded, 0.04f)
                        } else {
                            baseGraded
                        }
                        
                        // Apply Watermark
                        val finalBitmap = if (isWatermarkEnabled) {
                            val output = stabilizedBitmap.copy(Bitmap.Config.ARGB_8888, true)
                            val canvas = Canvas(output)
                            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                            
                            // Draw Transparent Logo (Circle)
                            paint.color = android.graphics.Color.WHITE
                            paint.alpha = 128 // Semi-transparent
                            canvas.drawCircle(output.width - 240f, output.height - 60f, 30f, paint)
                            
                            // Draw Text
                            paint.alpha = 255
                            paint.textSize = 48f
                            paint.textAlign = Paint.Align.RIGHT
                            paint.setShadowLayer(5f, 2f, 2f, android.graphics.Color.BLACK)
                            canvas.drawText("Nateev | 777rei", output.width - 40f, output.height - 40f, paint)
                            output
                        } else {
                            stabilizedBitmap
                        }

                        // Save the graded image into MediaStore
                        val name = SimpleDateFormat("yyyyMMdd_HHmmss_SSS", Locale.US).format(System.currentTimeMillis())
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, "NATEEV_$name.jpg")
                            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/NateevCamera")
                            }
                        }

                        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        if (uri != null) {
                            context.contentResolver.openOutputStream(uri)?.use { stream ->
                                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 98, stream)
                            }

                            val loc = locationProvider.getLastKnownLocation()
                            ExifMetadataWriter.injectMetadata(
                                context = context,
                                imageUri = uri,
                                details = ExifDetails(
                                    software = "Nateev Pro Camera (${preset.displayName})",
                                    latitude = loc?.latitude,
                                    longitude = loc?.longitude
                                )
                            )

                            ContextCompat.getMainExecutor(context).execute {
                                onSuccess(uri.toString())
                            }
                        } else {
                            ContextCompat.getMainExecutor(context).execute {
                                onError(IllegalStateException("Failed to create MediaStore entry"))
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        ContextCompat.getMainExecutor(context).execute {
                            onError(e)
                        }
                    } finally {
                        imageProxy.close()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    ContextCompat.getMainExecutor(context).execute {
                        onError(exception)
                    }
                }
            }
        )
    }

    fun startRecording(
        onVideoSaved: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val vCapture = videoCapture
        if (vCapture == null) {
            onError("Camera recorder not initialized")
            return
        }

        try {
            val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "NATEEV_VIDEO_$name.mp4")
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/NateevCamera")
                }
            }

            val mediaStoreOutput = MediaStoreOutputOptions.Builder(
                context.contentResolver,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            ).setContentValues(contentValues).build()

            val pending = vCapture.output.prepareRecording(context, mediaStoreOutput)

            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                try {
                    pending.withAudioEnabled()
                } catch (e: Exception) {
                    // Fall back to video without audio if mic is busy
                }
            }

            activeRecording = pending.start(ContextCompat.getMainExecutor(context)) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        // Successfully recording
                    }
                    is VideoRecordEvent.Finalize -> {
                        val recordingRef = activeRecording
                        activeRecording = null
                        recordingRef?.close()

                        if (!event.hasError()) {
                            onVideoSaved(event.outputResults.outputUri.toString())
                        } else {
                            val code = event.error
                            val msg = when (code) {
                                VideoRecordEvent.Finalize.ERROR_NO_VALID_DATA -> "Recording stopped before video data was written"
                                VideoRecordEvent.Finalize.ERROR_FILE_SIZE_LIMIT_REACHED -> "File size limit reached"
                                VideoRecordEvent.Finalize.ERROR_INSUFFICIENT_STORAGE -> "Insufficient storage"
                                else -> "Recording error ($code)"
                            }
                            onError(msg)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onError("Failed to start recording: ${e.message}")
        }
    }

    fun stopRecording() {
        try {
            activeRecording?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
