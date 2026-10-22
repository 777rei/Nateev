package com.example.engine

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
            try {
                applyAGSLEffect(view, matrix, contrast, sharpness)
            } catch (e: Exception) {
                applyFallbackColorMatrix(view, matrix)
            }
        } else {
            applyFallbackColorMatrix(view, matrix)
        }
    }

    private fun applyFallbackColorMatrix(view: View, matrix: ColorMatrix) {
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(matrix)
        }
        view.setLayerType(View.LAYER_TYPE_HARDWARE, paint)
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

    fun clearEffect(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            view.setRenderEffect(null)
        }
        view.setLayerType(View.LAYER_TYPE_NONE, null)
    }
}
