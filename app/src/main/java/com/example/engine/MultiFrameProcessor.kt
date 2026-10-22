package com.example.engine

import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MultiFrameProcessor {

    /**
     * Blends multiple exposures using NPU/GPU compute pipeline.
     * Executes HDR+ multi-frame exposure fusion and bilateral noise reduction.
     */
    suspend fun fuseExposures(bitmaps: List<Bitmap>): Bitmap? = withContext(Dispatchers.Default) {
        if (bitmaps.isEmpty()) return@withContext null
        NpuGpuComputePipeline.processHdrPlusStack(bitmaps, applyDenoise = true)
    }

    /**
     * Denoises and enhances micro-contrast on single or merged frames.
     */
    suspend fun applyClarityBoost(source: Bitmap, boostFactor: Float = 1.15f): Bitmap = withContext(Dispatchers.Default) {
        NpuGpuComputePipeline.denoiseBilateral(source)
    }
}
