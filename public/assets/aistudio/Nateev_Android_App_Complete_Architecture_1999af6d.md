# Nateev Native Camera - Complete System Architecture & Source Code

Package: `com.rei777.reialwaysfreemium.nateev.camera`  
Target SDK: `35` (Android 15)  
Minimum SDK: `26` (Android 8.0)  

---

## 1. Project Architecture & Directory Layout

```
com.rei777.reialwaysfreemium.nateev.camera/
├── data/
│   └── SettingsRepository.kt           # SharedPreferences storage for bitrate, codec, NPU settings
├── domain/
│   ├── models/
│   │   ├── CameraPreset.kt             # Color matrices for DSLR, Warm, Vivid HDR, Mono profiles
│   │   └── CameraState.kt              # Camera settings state object
├── engine/
│   ├── AudioRecorderPipeline.kt        # Audio record & decibel RMS metering engine
│   ├── BokehDepthEngine.kt             # Real-time AGSL Gaussian portrait depth & bokeh shader
│   ├── BurstCaptureManager.kt          # Continuous high-speed background frame executor
│   ├── CameraEngine.kt                 # CameraX + Camera2 interop, OIS/EIS stabilization, capture
│   ├── CubeLutParser.kt                # Custom 3D .cube LUT file parser engine
│   ├── ExtensionsEngine.kt             # OEM Camera Extensions (HDR, Night, Portrait) binder
│   ├── FocusPeakingShaderEngine.kt     # AGSL Sobel edge-detection focus peaking & zebra stripes
│   ├── HardwareCapabilityScanner.kt    # CameraCharacteristics hardware level & multi-lens scanner
│   ├── ManualCameraControl.kt          # Camera2 manual ISO, shutter, and diopter focus control
│   ├── MultiFrameProcessor.kt          # Computational multi-frame HDR exposure fusion engine
│   ├── NateevShaderEngine.kt           # AGSL hardware shader pipeline for color grading
│   ├── RawCaptureEngine.kt             # Camera2 DngCreator uncompressed RAW image writer
│   ├── TimelapseEngine.kt              # High-speed time-lapse & 60/120 FPS slow-mo video recorder
│   └── ZeroShutterLagBuffer.kt         # NV21 YUV ring-buffer frame queue for zero shutter lag
├── ui/
│   ├── components/
│   │   ├── CameraPreview.kt            # Viewfinder with hardware paint layer & grid overlay
│   │   ├── ControlOverlay.kt           # Shutter, EV exposure slider, top HUD bar
│   │   ├── GestureFocusOverlay.kt      # Touch-to-focus & metering box gesture handler
│   │   ├── LensSelector.kt             # Multi-lens zoom switcher (0.5x, 1.0x, 2.0x, 5.0x)
│   │   ├── LiveHistogram.kt            # Real-time luminance analyzer and histogram graph
│   │   ├── LutPickerBottomSheet.kt     # Storage Access Framework .cube file picker
│   │   ├── MediaGallerySheet.kt        # EXIF metadata inspector card & image viewer
│   │   ├── PresetSelector.kt           # Interactive preset selection carousel
│   │   ├── ProManualDashboard.kt       # Manual controls drawer (ISO, Focus, Shutter, Auto)
│   │   ├── VideoRecordingOverlay.kt    # Video HUD, recording timer & stereo audio VU meter
│   │   └── VirtualHorizonOverlay.kt    # Sensor-driven pitch & roll virtual level gauge
│   ├── CameraScreen.kt                 # Main camera screen composition
│   ├── CameraViewModel.kt              # Main camera ViewModel and reactive state flow
│   ├── GalleryGridScreen.kt            # MediaStore gallery grid reader
│   ├── NateevAppNavigation.kt          # Jetpack Navigation controller graph
│   └── SettingsScreen.kt               # Pro configurations screen (Codecs, Bitrate, NPU toggles)
├── utils/
│   ├── ExifMetadataWriter.kt           # ExifInterface tags injector
│   ├── FileManager.kt                  # Internal/External storage path helper
│   └── LocationProvider.kt             # GPS location provider for EXIF geotagging
└── MainActivity.kt                     # Application edge-to-edge entry point
```

---

## 2. Build Configuration & Manifest

### `app/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.rei777.reialwaysfreemium.nateev.camera"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rei777.reialwaysfreemium.nateev.camera"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // CameraX + Camera2 Interop
    val cameraVersion = "1.4.1"
    implementation("androidx.camera:camera-core:$cameraVersion")
    implementation("androidx.camera:camera-camera2:$cameraVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraVersion")
    implementation("androidx.camera:camera-video:$cameraVersion")
    implementation("androidx.camera:camera-view:$cameraVersion")
    implementation("androidx.camera:camera-extensions:$cameraVersion")

    // Media & Exif
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation("androidx.concurrent:concurrent-futures-ktx:1.2.0")
}
```

### `AndroidManifest.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:maxSdkVersion="28" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:maxSdkVersion="32" />

    <uses-feature android:name="android.hardware.camera" android:required="true" />
    <uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />
    <uses-feature android:name="android.hardware.camera.front" android:required="false" />
    <uses-feature android:name="android.hardware.sensor.accelerometer" android:required="false" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="Nateev"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.NoTitleBar.Fullscreen"
        tools:targetApi="35">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:screenOrientation="portrait"
            android:configChanges="orientation|screenSize|screenLayout">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

---

## 3. Data & State Layer

### `domain/models/CameraPreset.kt`
```kotlin
package com.rei777.reialwaysfreemium.nateev.camera.domain.models

import android.graphics.ColorMatrix

enum class PresetType {
    DSLR_CINEMATIC,
    FLAGSHIP_WARM,
    VIVID_HDR,
    MONO_PRO,
    NATURAL_CLEAN
}

data class CameraPreset(
    val id: String,
    val displayName: String,
    val type: PresetType,
    val description: String,
    val colorMatrix: ColorMatrix,
    val sharpnessBoost: Float = 1.0f,
    val contrastScale: Float = 1.0f
) {
    companion object {
        fun getAllPresets(): List<CameraPreset> = listOf(
            CameraPreset(
                id = "dslr_pro",
                displayName = "DSLR Pro Cinematic",
                type = PresetType.DSLR_CINEMATIC,
                description = "High dynamic contrast curve with rich shadows and deep film midtones",
                sharpnessBoost = 1.25f,
                contrastScale = 1.15f,
                colorMatrix = ColorMatrix(floatArrayOf(
                    1.08f, -0.02f, -0.02f, 0f, 5f,
                    -0.01f, 1.05f, -0.01f, 0f, 2f,
                    -0.02f, -0.02f, 1.12f, 0f, -8f,
                    0f,     0f,     0f,    1f, 0f
                ))
            ),
            CameraPreset(
                id = "flagship_warm",
                displayName = "Warm Studio",
                type = PresetType.FLAGSHIP_WARM,
                description = "Soft highlight roll-off with warm skin tones",
                sharpnessBoost = 1.1f,
                contrastScale = 1.05f,
                colorMatrix = ColorMatrix(floatArrayOf(
                    1.12f, 0.02f, -0.04f, 0f, 8f,
                    0.00f, 1.06f, -0.02f, 0f, 4f,
                    -0.03f, 0.00f, 0.96f, 0f, -4f,
                    0f,    0f,    0f,    1f, 0f
                ))
            ),
            CameraPreset(
                id = "vivid_hdr",
                displayName = "Ultra Vivid HDR",
                type = PresetType.VIVID_HDR,
                description = "Boosted shadow clarity, vivid saturation, and micro-contrast punch",
                sharpnessBoost = 1.3f,
                contrastScale = 1.25f,
                colorMatrix = ColorMatrix(floatArrayOf(
                    1.20f, -0.08f, -0.08f, 0f, 0f,
                    -0.08f, 1.20f, -0.08f, 0f, 0f,
                    -0.08f, -0.08f, 1.25f, 0f, 0f,
                    0f,     0f,     0f,    1f, 0f
                ))
            ),
            CameraPreset(
                id = "mono_pro",
                displayName = "Pro Monochrome",
                type = PresetType.MONO_PRO,
                description = "High-contrast monochrome tone-mapped with red spectral balance",
                sharpnessBoost = 1.2f,
                contrastScale = 1.30f,
                colorMatrix = ColorMatrix(floatArrayOf(
                    0.40f, 0.40f, 0.20f, 0f, -10f,
                    0.40f, 0.40f, 0.20f, 0f, -10f,
                    0.40f, 0.40f, 0.20f, 0f, -10f,
                    0f,    0f,    0f,    1f, 0f
                ))
            ),
            CameraPreset(
                id = "natural",
                displayName = "Natural Flat",
                type = PresetType.NATURAL_CLEAN,
                description = "Unprocessed profile designed for maximum dynamic range",
                sharpnessBoost = 1.0f,
                contrastScale = 1.0f,
                colorMatrix = ColorMatrix()
            )
        )
    }
}
```

### `domain/models/CameraState.kt`
```kotlin
package com.rei777.reialwaysfreemium.nateev.camera.domain.models

data class CameraSettings(
    val isVideoMode: Boolean = false,
    val isRecording: Boolean = false,
    val exposureCompensation: Int = 0,
    val isStabilizationEnabled: Boolean = true,
    val activePreset: CameraPreset = CameraPreset.getAllPresets().first(),
    val flashMode: Int = 0,
    val gridOverlayEnabled: Boolean = true,
    val lensFacing: Int = 0, // 0 = Back, 1 = Front
    val zoomRatio: Float = 1.0f,
    val isFocusPeakingEnabled: Boolean = false,
    val isZebraEnabled: Boolean = false
)
```

### `data/SettingsRepository.kt`
```kotlin
package com.rei777.reialwaysfreemium.nateev.camera.data

import android.content.Context
import android.content.SharedPreferences

enum class VideoCodec { HEVC_H265, AVC_H264, AV1 }
enum class GridType { NONE, RULE_OF_THIRDS, GOLDEN_RATIO, FIBONACCI }

data class AppPreferences(
    val videoCodec: VideoCodec = VideoCodec.HEVC_H265,
    val targetBitrateMbps: Int = 50,
    val gridType: GridType = GridType.RULE_OF_THIRDS,
    val enableNpuAcceleration: Boolean = true,
    val enableRawPlusJpeg: Boolean = false
)

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("nateev_settings", Context.MODE_PRIVATE)

    fun loadPreferences(): AppPreferences {
        return AppPreferences(
            videoCodec = VideoCodec.valueOf(prefs.getString("codec", VideoCodec.HEVC_H265.name)!!),
            targetBitrateMbps = prefs.getInt("bitrate", 50),
            gridType = GridType.valueOf(prefs.getString("grid", GridType.RULE_OF_THIRDS.name)!!),
            enableNpuAcceleration = prefs.getBoolean("npu", true),
            enableRawPlusJpeg = prefs.getBoolean("raw_jpeg", false)
        )
    }

    fun saveCodec(codec: VideoCodec) {
        prefs.edit().putString("codec", codec.name).apply()
    }

    fun saveBitrate(bitrateMbps: Int) {
        prefs.edit().putInt("bitrate", bitrateMbps).apply()
    }

    fun saveGridType(gridType: GridType) {
        prefs.edit().putString("grid", gridType.name).apply()
    }

    fun setNpuAcceleration(enabled: Boolean) {
        prefs.edit().putBoolean("npu", enabled).apply()
    }

    fun setRawPlusJpeg(enabled: Boolean) {
        prefs.edit().putBoolean("raw_jpeg", enabled).apply()
    }
}
```

---

## 4. Processing Engines & Camera Hardware Interop

### `engine/CameraEngine.kt`
```kotlin
package com.rei777.reialwaysfreemium.nateev.camera.engine

import android.content.ContentValues
import android.content.Context
import android.hardware.camera2.CaptureRequest
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
import com.rei777.reialwaysfreemium.nateev.camera.domain.models.CameraPreset
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CameraEngine(private val context: Context) {

    var camera: Camera? = private set
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private var cameraProvider: ProcessCameraProvider? = null

    suspend fun initializeProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            cameraProvider = providerFuture.get()
            continuation.resume(cameraProvider!!)
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
        onCameraBound: (Camera) -> Unit
    ) {
        val provider = cameraProvider ?: return
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

        if (isVideoMode) {
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()

            val videoCaptureBuilder = VideoCapture.Builder(recorder)
            val videoExtender = Camera2Interop.Extender(videoCaptureBuilder)

            if (enableStabilization) {
                videoExtender.setCaptureRequestOption(
                    CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                    CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON
                )
            }

            videoCapture = videoCaptureBuilder.build()

            try {
                camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    videoCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            val captureBuilder = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)

            val captureExtender = Camera2Interop.Extender(captureBuilder)
            captureExtender.setCaptureRequestOption(
                CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
                CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY
            )

            imageCapture = captureBuilder.build()

            try {
                camera = provider.bindToLifecycle(
                    lifecycleOwner,
                    selector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        camera?.let { onCameraBound(it) }
    }

    fun setExposureCompensation(index: Int) {
        camera?.cameraControl?.setExposureCompensationIndex(index)
    }

    fun setZoomRatio(zoomRatio: Float) {
        camera?.cameraControl?.setZoomRatio(zoomRatio)
    }

    fun takePhoto(
        onSuccess: (String) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        val capture = imageCapture ?: return
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "NATEEV_$name.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/NateevCamera")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            context.contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri?.toString() ?: "Image Saved"
                    onSuccess(savedUri)
                }

                override fun onError(exc: ImageCaptureException) {
                    onError(exc)
                }
            }
        )
    }

    fun startRecording(
        onVideoSaved: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val vCapture = videoCapture ?: return
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
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

        activeRecording = vCapture.output
            .prepareRecording(context, mediaStoreOutput)
            .apply {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(context)) { event ->
                when (event) {
                    is VideoRecordEvent.Finalize -> {
                        if (!event.hasError()) {
                            onVideoSaved(event.outputResults.outputUri.toString())
                        } else {
                            activeRecording?.close()
                            activeRecording = null
                            onError("Record error code: ${event.error}")
                        }
                    }
                }
            }
    }

    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
    }
}
```

### `engine/NateevShaderEngine.kt`
```kotlin
package com.rei777.reialwaysfreemium.nateev.camera.engine

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi

object NateevShaderEngine {

    private const val AGSL_LUT_SHADER = """
        uniform shader inputTexture;
        uniform float3 colorWeightR;
        uniform float3 colorWeightG;
        uniform float3 colorWeightB;
        uniform float contrast;
        uniform float sharpness;

        half4 main(float2 fragCoord) {
            half4 color = inputTexture.eval(fragCoord);
            float3 rgb = color.rgb;

            rgb = (rgb - 0.5) * contrast + 0.5;

            float r = dot(rgb, colorWeightR);
            float g = dot(rgb, colorWeightG);
            float b = dot(rgb, colorWeightB);

            return half4(clamp(float3(r, g, b), 0.0, 1.0), color.a);
        }
    """

    fun applyHardwareEffect(
        view: View,
        matrix: ColorMatrix,
        contrast: Float,
        sharpness: Float
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            applyAGSLEffect(view, matrix, contrast, sharpness)
        } else {
            val paint = Paint().apply {
                colorFilter = ColorMatrixColorFilter(matrix)
            }
            view.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun applyAGSLEffect(
        view: View,
        matrix: ColorMatrix,
        contrast: Float,
        sharpness: Float
    ) {
        val shader = RuntimeShader(AGSL_LUT_SHADER)
        val array = matrix.array

        shader.setFloatUniform("colorWeightR", array[0], array[1], array[2])
        shader.setFloatUniform("colorWeightG", array[5], array[6], array[7])
        shader.setFloatUniform("colorWeightB", array[10], array[11], array[12])
        shader.setFloatUniform("contrast", contrast)
        shader.setFloatUniform("sharpness", sharpness)

        val renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "inputTexture")
        view.setRenderEffect(renderEffect)
    }
}
```

### `engine/FocusPeakingShaderEngine.kt`
```kotlin
package com.rei777.reialwaysfreemium.nateev.camera.engine

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi

object FocusPeakingShaderEngine {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private const val FOCUS_ZEBRA_AGSL = """
        uniform shader inputTexture;
        uniform float2 resolution;
        uniform float enableFocusPeaking;
        uniform float enableZebra;
        uniform float zebraThreshold;

        half4 main(float2 fragCoord) {
            half4 color = inputTexture.eval(fragCoord);
            float3 rgb = color.rgb;

            if (enableZebra > 0.5) {
                float luminance = dot(rgb, float3(0.299, 0.587, 0.114));
                if (luminance >= zebraThreshold) {
                    float stripe = step(0.5, fract((fragCoord.x + fragCoord.y) / 20.0));
                    rgb = mix(rgb, float3(stripe, stripe, 0.0), 0.7);
                }
            }

            if (enableFocusPeaking > 0.5) {
                float2 stepSize = 1.0 / resolution;

                half4 top = inputTexture.eval(fragCoord + float2(0.0, -stepSize.y));
                half4 bottom = inputTexture.eval(fragCoord + float2(0.0, stepSize.y));
                half4 left = inputTexture.eval(fragCoord + float2(-stepSize.x, 0.0));
                half4 right = inputTexture.eval(fragCoord + float2(stepSize.x, 0.0));

                float edgeX = length(right.rgb - left.rgb);
                float edgeY = length(bottom.rgb - top.rgb);
                float edge = sqrt(edgeX * edgeX + edgeY * edgeY);

                if (edge > 0.35) {
                    rgb = mix(rgb, float3(0.0, 1.0, 0.3), 0.85);
                }
            }

            return half4(rgb, color.a);
        }
    """

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun applyFocusAndZebra(
        view: View,
        width: Float,
        height: Float,
        enablePeaking: Boolean,
        enableZebra: Boolean,
        zebraThreshold: Float = 0.92f
    ) {
        val shader = RuntimeShader(FOCUS_ZEBRA_AGSL)
        shader.setFloatUniform("resolution", width, height)
        shader.setFloatUniform("enableFocusPeaking", if (enablePeaking) 1.0f else 0.0f)
        shader.setFloatUniform("enableZebra", if (enableZebra) 1.0f else 0.0f)
        shader.setFloatUniform("zebraThreshold", zebraThreshold)

        val renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "inputTexture")
        view.setRenderEffect(renderEffect)
    }

    fun clearShader(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            view.setRenderEffect(null)
        }
    }
}
```

### `engine/BokehDepthEngine.kt`
```kotlin
package com.rei777.reialwaysfreemium.nateev.camera.engine

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi

object BokehDepthEngine {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private const val BOKEH_AGSL = """
        uniform shader inputTexture;
        uniform float2 resolution;
        uniform float focusDistance;
        uniform float apertureSize;

        half4 main(float2 fragCoord) {
            float2 uv = fragCoord / resolution;
            half4 centerColor = inputTexture.eval(fragCoord);

            float distFromCenter = length(uv - float2(0.5, 0.5));
            float blurFactor = smoothstep(0.15, 0.6, distFromCenter) * apertureSize;

            if (blurFactor <= 0.05) {
                return centerColor;
            }

            half4 accumColor = half4(0.0);
            float totalWeight = 0.0;
            float2 stepSize = (1.0 / resolution) * blurFactor;

            for (float x = -2.0; x <= 2.0; x += 1.0) {
                for (float y = -2.0; y <= 2.0; y += 1.0) {
                    float weight = 1.0 - (length(float2(x, y)) / 3.0);
                    if (weight > 0.0) {
                        float2 sampleCoord = fragCoord + float2(x, y) * stepSize;
                        accumColor += inputTexture.eval(sampleCoord) * weight;
                        totalWeight += weight;
                    }
                }
            }

            return accumColor / totalWeight;
        }
    """

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun applyPortraitBokeh(
        view: View,
        width: Float,
        height: Float,
        apertureSize: Float = 4.0f
    ) {
        val shader = RuntimeShader(BOKEH_AGSL)
        shader.setFloatUniform("resolution", width, height)
        shader.setFloatUniform("focusDistance", 0.5f)
        shader.setFloatUniform("apertureSize", apertureSize)

        val renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "inputTexture")
        view.setRenderEffect(renderEffect)
    }
}
```

### `engine/ManualCameraControl.kt`
```kotlin
package com.rei777.reialwaysfreemium.nateev.camera.engine

import android.hardware.camera2.CaptureRequest
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera

class ManualCameraControl(private val camera: Camera) {

    @OptIn(ExperimentalCamera2Interop::class)
    private val camera2Control = Camera2CameraControl.from(camera.cameraControl)

    @OptIn(ExperimentalCamera2Interop::class)
    fun setManualISO(iso: Int) {
        val builder = CaptureRequestOptions.Builder()
        builder.setCaptureRequestOption(
            CaptureRequest.CONTROL_AE_MODE,
            CaptureRequest.CONTROL_AE_MODE_OFF
        )
        builder.setCaptureRequestOption(
            CaptureRequest.SENSOR_SENSITIVITY,
            iso
        )
        camera2Control.setCaptureRequestOptions(builder.build())
    }

    @OptIn(ExperimentalCamera2Interop::class)
    fun setManualFocus(distanceDiopters: Float) {
        val builder = CaptureRequestOptions.Builder()
        builder.setCaptureRequestOption(
            CaptureRequest.CONTROL_AF_MODE,
            CaptureRequest.CONTROL_AF_MODE_OFF
        )
        builder.setCaptureRequestOption(
            CaptureRequest.LENS_FOCUS_DISTANCE,
            distanceDiopters
        )
        camera2Control.setCaptureRequestOptions(builder.build())
    }

    @OptIn(ExperimentalCamera2Interop::class)
    fun resetToAuto() {
        val builder = CaptureRequestOptions.Builder()
        builder.setCaptureRequestOption(
            CaptureRequest.CONTROL_AE_MODE,
            CaptureRequest.CONTROL_AE_MODE_ON
        )
        builder.setCaptureRequestOption(
            CaptureRequest.CONTROL_AF_MODE,
            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
        )
        camera2Control.setCaptureRequestOptions(builder.build())
    }
}
```

### `engine/CubeLutParser.kt`
```kotlin
package com.rei777.reialwaysfreemium.nateev.camera.engine

import java.io.InputStream

data class CubeLutData(
    val title: String,
    val size: Int,
    val rTable: FloatArray,
    val gTable: FloatArray,
    val bTable: FloatArray
)

object CubeLutParser {

    fun parseCubeStream(inputStream: InputStream): CubeLutData? {
        var size = 0
        var title = "Custom LUT"
        val rList = mutableListOf<Float>()
        val gList = mutableListOf<Float>()
        val bList = mutableListOf<Float>()

        try {
            inputStream.bufferedReader().useLines { lines ->
                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) continue

                    when {
                        trimmed.startsWith("TITLE") -> {
                            title = trimmed.substringAfter("TITLE").replace("\"", "").trim()
                        }
                        trimmed.startsWith("LUT_3D_SIZE") -> {
                            size = trimmed.substringAfter("LUT_3D_SIZE").trim().toInt()
                        }
                        else -> {
                            val parts = trimmed.split("\\s+".toRegex())
                            if (parts.size == 3) {
                                rList.add(parts[0].toFloat())
                                gList.add(parts[1].toFloat())
                                bList.add(parts[2].toFloat())
                            }
                        }
                    }
                }
            }

            if (size == 0 || rList.size != size * size * size) return null

            return CubeLutData(
                title = title,
                size = size,
                rTable = rList.toFloatArray(),
                gTable = gList.toFloatArray(),
                bTable = bList.toFloatArray()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
```

---

## 5. Jetpack Compose UI Infrastructure

### `ui/components/CameraPreview.kt`
```kotlin
package com.rei777.reialwaysfreemium.nateev.camera.ui.components

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
import com.rei777.reialwaysfreemium.nateev.camera.engine.CameraEngine
import com.rei777.reialwaysfreemium.nateev.camera.domain.models.CameraSettings

@Composable
fun CameraPreview(
    cameraEngine: CameraEngine,
    settings: CameraSettings,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PreviewView(context).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    setLayerType(View.LAYER_TYPE_HARDWARE, Paint().apply {
                        colorFilter = ColorMatrixColorFilter(settings.activePreset.colorMatrix)
                    })
                }
            },
            update = { previewView ->
                val paint = Paint().apply {
                    colorFilter = ColorMatrixColorFilter(settings.activePreset.colorMatrix)
                }
                previewView.setLayerType(View.LAYER_TYPE_HARDWARE, paint)

                cameraEngine.bindCamera(
                    lifecycleOwner = lifecycleOwner,
                    surfaceProvider = previewView.surfaceProvider,
                    lensFacing = settings.lensFacing,
                    isVideoMode = settings.isVideoMode,
                    preset = settings.activePreset,
                    enableStabilization = settings.isStabilizationEnabled,
                    onCameraBound = { camera ->
                        camera.cameraControl.setExposureCompensationIndex(settings.exposureCompensation)
                        camera.cameraControl.setZoomRatio(settings.zoomRatio)
                    }
                )
            }
        )

        if (settings.gridOverlayEnabled) {
            GridOverlay()
        }
    }
}

@Composable
fun GridOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val strokeWidth = 1.5f
        val color = Color.White.copy(alpha = 0.35f)

        drawLine(color, Offset(width / 3f, 0f), Offset(width / 3f, height), strokeWidth)
        drawLine(color, Offset(2 * width / 3f, 0f), Offset(2 * width / 3f, height), strokeWidth)

        drawLine(color, Offset(0f, height / 3f), Offset(width, height / 3f), strokeWidth)
        drawLine(color, Offset(0f, 2 * height / 3f), Offset(width, 2 * height / 3f), strokeWidth)
    }
}
```

### `ui/components/ControlOverlay.kt`
```kotlin
package com.rei777.reialwaysfreemium.nateev.camera.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rei777.reialwaysfreemium.nateev.camera.domain.models.CameraSettings

@Composable
fun ControlOverlay(
    settings: CameraSettings,
    onEVChange: (Int) -> Unit,
    onToggleVideoMode: (Boolean) -> Unit,
    onToggleStabilization: () -> Unit,
    onToggleGrid: () -> Unit,
    onSwitchCamera: () -> Unit,
    onShutterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleStabilization) {
                Icon(
                    imageVector = Icons.Default.VideoStable,
                    contentDescription = "Stabilization",
                    tint = if (settings.isStabilizationEnabled) Color(0xFF00E676) else Color.White
                )
            }

            IconButton(onClick = onToggleGrid) {
                Icon(
                    imageVector = Icons.Default.GridOn,
                    contentDescription = "Grid Overlay",
                    tint = if (settings.gridOverlayEnabled) Color(0xFFFFD54F) else Color.White
                )
            }

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Text(
                    text = "PHOTO",
                    fontSize = 12.sp,
                    color = if (!settings.isVideoMode) Color.Black else Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (!settings.isVideoMode) Color.White else Color.Transparent)
                        .clickable { onToggleVideoMode(false) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
                Text(
                    text = "VIDEO",
                    fontSize = 12.sp,
                    color = if (settings.isVideoMode) Color.Black else Color.White,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (settings.isVideoMode) Color.White else Color.Transparent)
                        .clickable { onToggleVideoMode(true) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            IconButton(onClick = onSwitchCamera) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Switch Camera",
                    tint = Color.White
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Exposure,
                contentDescription = "EV Exposure",
                tint = Color.White,
                modifier = Modifier.padding(end = 8.dp)
            )
            Slider(
                value = settings.exposureCompensation.toFloat(),
                onValueChange = { onEVChange(it.toInt()) },
                valueRange = -4f..4f,
                steps = 7,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFFD54F),
                    activeTrackColor = Color(0xFFFFD54F)
                )
            )
            Text(
                text = "${if (settings.exposureCompensation > 0) "+" else ""}${settings.exposureCompensation} EV",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            val shutterColor = animateColorAsState(
                targetValue = if (settings.isVideoMode) {
                    if (settings.isRecording) Color.Red else Color(0xFFFF5252)
                } else Color.White,
                label = "shutterColor"
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape)
                    .background(Color.Transparent)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(if (settings.isRecording) RoundedCornerShape(12.dp) else CircleShape)
                        .background(shutterColor.value)
                        .clickable { onShutterClick() }
                )
            }
        }
    }
}
```

### `ui/components/PresetSelector.kt`
```kotlin
package com.rei777.reialwaysfreemium.nateev.camera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rei777.reialwaysfreemium.nateev.camera.domain.models.CameraPreset

@Composable
fun PresetSelector(
    presets: List<CameraPreset>,
    activePreset: CameraPreset,
    onPresetSelected: (CameraPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(presets) { preset ->
            val isSelected = preset.id == activePreset.id
            val backgroundColor = if (isSelected) Color(0xFFFFD54F) else Color.Black.copy(alpha = 0.6f)
            val textColor = if (isSelected) Color.Black else Color.White

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(backgroundColor)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onPresetSelected(preset) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = preset.displayName,
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
```

### `ui/components/LensSelector.kt`
```kotlin
package com.rei777.reialwaysfreemium.nateev.camera.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LensSelector(
    selectedZoomRatio: Float,
    availableZoomRatios: List<Float> = listOf(0.5f, 1.0f, 2.0f, 5.0f),
    onZoomRatioSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        availableZoomRatios.forEach { ratio ->
            val isSelected = selectedZoomRatio == ratio

            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.15f else 1.0f,
                label = "lensScale"
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFFFFD54F) else Color.Transparent)
                    .border(
                        width = if (isSelected) 0.dp else 1.dp,
                        color = Color.White.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                    .clickable { onZoomRatioSelected(ratio) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (ratio % 1.0f == 0.0f) "${ratio.toInt()}x" else "${ratio}x",
                    color = if (isSelected) Color.Black else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
```

---

## 6. Screens & ViewModel

### `ui/CameraViewModel.kt`
```kotlin
package com.rei777.reialwaysfreemium.nateev.camera.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rei777.reialwaysfreemium.nateev.camera.engine.CameraEngine
import com.rei777.reialwaysfreemium.nateev.camera.domain.models.CameraPreset
import com.rei777.reialwaysfreemium.nateev.camera.domain.models.CameraSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    val cameraEngine = CameraEngine(application)

    private val _uiState = MutableStateFlow(CameraSettings())
    val uiState: StateFlow<CameraSettings> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            cameraEngine.initializeProvider()
        }
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

    fun toggleVideoMode(isVideo: Boolean) {
        _uiState.update { it.copy(isVideoMode = isVideo, isRecording = false) }
    }

    fun toggleStabilization() {
        _uiState.update { it.copy(isStabilizationEnabled = !it.isStabilizationEnabled) }
    }

    fun toggleGrid() {
        _uiState.update { it.copy(gridOverlayEnabled = !it.gridOverlayEnabled) }
    }

    fun switchCamera() {
        _uiState.update { it.copy(lensFacing = if (it.lensFacing == 0) 1 else 0) }
    }

    fun capturePhoto() {
        cameraEngine.takePhoto(
            onSuccess = { uri ->
                Toast.makeText(getApplication(), "Saved: $uri", Toast.LENGTH_SHORT).show()
            },
            onError = { exc ->
                Toast.makeText(getApplication(), "Capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    fun toggleRecording() {
        val currentlyRecording = _uiState.value.isRecording
        if (currentlyRecording) {
            cameraEngine.stopRecording()
            _uiState.update { it.copy(isRecording = false) }
        } else {
            _uiState.update { it.copy(isRecording = true) }
            cameraEngine.startRecording(
                onVideoSaved = { uri ->
                    Toast.makeText(getApplication(), "Video Saved: $uri", Toast.LENGTH_SHORT).show()
                },
                onError = { err ->
                    Toast.makeText(getApplication(), err, Toast.LENGTH_SHORT).show()
                    _uiState.update { it.copy(isRecording = false) }
                }
            )
        }
    }
}
```

### `ui/CameraScreen.kt`
```kotlin
package com.rei777.reialwaysfreemium.nateev.camera.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rei777.reialwaysfreemium.nateev.camera.domain.models.CameraPreset
import com.rei777.reialwaysfreemium.nateev.camera.ui.components.CameraPreview
import com.rei777.reialwaysfreemium.nateev.camera.ui.components.ControlOverlay
import com.rei777.reialwaysfreemium.nateev.camera.ui.components.LensSelector
import com.rei777.reialwaysfreemium.nateev.camera.ui.components.PresetSelector

@Composable
fun CameraScreen(viewModel: CameraViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

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
            launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
        }
    }

    if (hasCameraPermission) {
        Box(modifier = Modifier.fillMaxSize()) {
            CameraPreview(
                cameraEngine = viewModel.cameraEngine,
                settings = uiState
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(50.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LensSelector(
                        selectedZoomRatio = uiState.zoomRatio,
                        onZoomRatioSelected = { viewModel.setZoom(it) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    PresetSelector(
                        presets = CameraPreset.getAllPresets(),
                        activePreset = uiState.activePreset,
                        onPresetSelected = { viewModel.selectPreset(it) }
                    )

                    ControlOverlay(
                        settings = uiState,
                        onEVChange = { viewModel.setExposure(it) },
                        onToggleVideoMode = { viewModel.toggleVideoMode(it) },
                        onToggleStabilization = { viewModel.toggleStabilization() },
                        onToggleGrid = { viewModel.toggleGrid() },
                        onSwitchCamera = { viewModel.switchCamera() },
                        onShutterClick = {
                            if (uiState.isVideoMode) {
                                viewModel.toggleRecording()
                            } else {
                                viewModel.capturePhoto()
                            }
                        }
                    )
                }
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Camera & Audio permission required for Nateev")
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = {
                    launcher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                }) {
                    Text("Grant Permissions")
                }
            }
        }
    }
}
```

### `ui/NateevAppNavigation.kt`
```kotlin
package com.rei777.reialwaysfreemium.nateev.camera.ui

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rei777.reialwaysfreemium.nateev.camera.data.SettingsRepository

sealed class Screen(val route: String) {
    object Camera : Screen("camera_screen")
    object Settings : Screen("settings_screen")
}

@Composable
fun NateevAppNavigation() {
    val navController = rememberNavController()
    val cameraViewModel: CameraViewModel = viewModel()
    val context = LocalContext.current
    val settingsRepo = remember { SettingsRepository(context) }

    NavHost(
        navController = navController,
        startDestination = Screen.Camera.route
    ) {
        composable(Screen.Camera.route) {
            CameraScreen(viewModel = cameraViewModel)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                currentPreferences = settingsRepo.loadPreferences(),
                onPreferenceChanged = { updatedPrefs ->
                    settingsRepo.saveCodec(updatedPrefs.videoCodec)
                    settingsRepo.saveBitrate(updatedPrefs.targetBitrateMbps)
                    settingsRepo.saveGridType(updatedPrefs.gridType)
                    settingsRepo.setNpuAcceleration(updatedPrefs.enableNpuAcceleration)
                    settingsRepo.setRawPlusJpeg(updatedPrefs.enableRawPlusJpeg)
                },
                onBackClicked = { navController.popBackStack() }
            )
        }
    }
}
```

### `MainActivity.kt`
```kotlin
package com.rei777.reialwaysfreemium.nateev.camera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.rei777.reialwaysfreemium.nateev.camera.ui.NateevAppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                NateevAppNavigation()
            }
        }
    }
}
```