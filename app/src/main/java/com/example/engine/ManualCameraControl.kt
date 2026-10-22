package com.example.engine

import android.hardware.camera2.CaptureRequest
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera

class ManualCameraControl(private val camera: Camera) {

    @OptIn(ExperimentalCamera2Interop::class)
    private val camera2Control = Camera2CameraControl.from(camera.cameraControl)

    @OptIn(ExperimentalCamera2Interop::class)
    fun setManualExposure(iso: Int, shutterTimeNanos: Long) {
        val builder = CaptureRequestOptions.Builder()
        builder.setCaptureRequestOption(
            CaptureRequest.CONTROL_AE_MODE,
            CaptureRequest.CONTROL_AE_MODE_OFF
        )
        if (iso > 0) {
            builder.setCaptureRequestOption(
                CaptureRequest.SENSOR_SENSITIVITY,
                iso
            )
        }
        if (shutterTimeNanos > 0L) {
            builder.setCaptureRequestOption(
                CaptureRequest.SENSOR_EXPOSURE_TIME,
                shutterTimeNanos
            )
        }
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
    fun setAwbMode(awbMode: Int) {
        val builder = CaptureRequestOptions.Builder()
        builder.setCaptureRequestOption(
            CaptureRequest.CONTROL_AWB_MODE,
            awbMode
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
        builder.setCaptureRequestOption(
            CaptureRequest.CONTROL_AWB_MODE,
            CaptureRequest.CONTROL_AWB_MODE_AUTO
        )
        camera2Control.setCaptureRequestOptions(builder.build())
    }
}
