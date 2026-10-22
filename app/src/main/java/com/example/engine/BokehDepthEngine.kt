package com.example.engine

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi

object BokehDepthEngine {

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

    fun applyPortraitBokeh(
        view: View,
        width: Float,
        height: Float,
        apertureSize: Float = 4.0f
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                applyPortraitBokehInternal(view, width, height, apertureSize)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun applyPortraitBokehInternal(
        view: View,
        width: Float,
        height: Float,
        apertureSize: Float
    ) {
        val shader = RuntimeShader(BOKEH_AGSL)
        val validWidth = if (width > 0f) width else 1080f
        val validHeight = if (height > 0f) height else 1920f
        shader.setFloatUniform("resolution", validWidth, validHeight)
        shader.setFloatUniform("focusDistance", 0.5f)
        shader.setFloatUniform("apertureSize", apertureSize)

        val renderEffect = RenderEffect.createRuntimeShaderEffect(shader, "inputTexture")
        view.setRenderEffect(renderEffect)
    }

    fun clearBokeh(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            view.setRenderEffect(null)
        }
    }
}
