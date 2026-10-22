package com.example.engine

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi

object FocusPeakingShaderEngine {

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

    fun applyFocusAndZebra(
        view: View,
        width: Float,
        height: Float,
        enablePeaking: Boolean,
        enableZebra: Boolean,
        zebraThreshold: Float = 0.92f
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                if (!enablePeaking && !enableZebra) {
                    clearShader(view)
                    return
                }
                applyShaderInternal(view, width, height, enablePeaking, enableZebra, zebraThreshold)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun applyShaderInternal(
        view: View,
        width: Float,
        height: Float,
        enablePeaking: Boolean,
        enableZebra: Boolean,
        zebraThreshold: Float
    ) {
        val shader = RuntimeShader(FOCUS_ZEBRA_AGSL)
        val validWidth = if (width > 0f) width else 1080f
        val validHeight = if (height > 0f) height else 1920f
        shader.setFloatUniform("resolution", validWidth, validHeight)
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
