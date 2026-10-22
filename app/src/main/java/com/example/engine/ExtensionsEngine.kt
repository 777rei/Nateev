package com.example.engine

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.extensions.ExtensionMode
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

data class SupportedExtensions(
    val hasHdr: Boolean,
    val hasNight: Boolean,
    val hasBokeh: Boolean,
    val hasFaceRetouch: Boolean,
    val hasAuto: Boolean
)

class ExtensionsEngine(private val context: Context) {

    private var extensionsManager: ExtensionsManager? = null

    suspend fun initialize(cameraProvider: ProcessCameraProvider): ExtensionsManager =
        suspendCoroutine { cont ->
            val future = ExtensionsManager.getInstanceAsync(context, cameraProvider)
            future.addListener({
                val manager = future.get()
                extensionsManager = manager
                cont.resume(manager)
            }, ContextCompat.getMainExecutor(context))
        }

    fun checkSupportedExtensions(selector: CameraSelector): SupportedExtensions {
        val manager = extensionsManager ?: return SupportedExtensions(
            hasHdr = false,
            hasNight = false,
            hasBokeh = false,
            hasFaceRetouch = false,
            hasAuto = false
        )

        return SupportedExtensions(
            hasHdr = manager.isExtensionAvailable(selector, ExtensionMode.HDR),
            hasNight = manager.isExtensionAvailable(selector, ExtensionMode.NIGHT),
            hasBokeh = manager.isExtensionAvailable(selector, ExtensionMode.BOKEH),
            hasFaceRetouch = manager.isExtensionAvailable(selector, ExtensionMode.FACE_RETOUCH),
            hasAuto = manager.isExtensionAvailable(selector, ExtensionMode.AUTO)
        )
    }

    fun getExtensionCameraSelector(
        baseSelector: CameraSelector,
        extensionMode: Int
    ): CameraSelector {
        val manager = extensionsManager ?: return baseSelector
        return if (manager.isExtensionAvailable(baseSelector, extensionMode)) {
            manager.getExtensionEnabledCameraSelector(baseSelector, extensionMode)
        } else {
            baseSelector
        }
    }
}
