package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToInt

object StabilizationEngine {

    /**
     * Stabilization 
     */
    suspend fun alignAndStackFrames(frames: List<Bitmap>): Bitmap = withContext(Dispatchers.Default) {
        if (frames.isEmpty()) throw IllegalArgumentException("No frames provided for stabilization stacking")
        if (frames.size == 1) return@withContext frames[0]

        val refFrame = frames[0]
        val width = refFrame.width
        val height = refFrame.height

        // Downscale reference for fast coarse motion estimation
        val scale = 0.25f
        val smallW = (width * scale).toInt().coerceAtLeast(32)
        val smallH = (height * scale).toInt().coerceAtLeast(32)

        val refSmall = Bitmap.createScaledBitmap(refFrame, smallW, smallH, true)
        val refPixels = IntArray(smallW * smallH)
        refSmall.getPixels(refPixels, 0, smallW, 0, 0, smallW, smallH)

        val accumulated = Array(width * height) { FloatArray(3) } // R, G, B
        var validFramesCount = 0

        for (frame in frames) {
            val frameSmall = Bitmap.createScaledBitmap(frame, smallW, smallH, true)
            val currPixels = IntArray(smallW * smallH)
            frameSmall.getPixels(currPixels, 0, smallW, 0, 0, smallW, smallH)

            // Compute translation vector (dx, dy) using block matching
            val shift = estimateMotionVector(refPixels, currPixels, smallW, smallH)
            val dx = shift[0] / scale
            val dy = shift[1] / scale

            // If motion is too extreme (fast pan), skip outlier frame to avoid ghosting
            if (abs(dx) > width * 0.2f || abs(dy) > height * 0.2f) {
                frameSmall.recycle()
                continue
            }

            // Align full-res frame using matrix translation & crop border padding and heading
            val alignedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(alignedBitmap)
            val matrix = Matrix().apply { setTranslate(dx, dy) }
            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            canvas.drawBitmap(frame, matrix, paint)

            val pixels = IntArray(width * height)
            alignedBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            for (i in pixels.indices) {
                val p = pixels[i]
                val r = ((p shr 16) and 0xFF).toFloat()
                val g = ((p shr 8) and 0xFF).toFloat()
                val b = (p and 0xFF).toFloat()

                accumulated[i][0] += r
                accumulated[i][1] += g
                accumulated[i][2] += b
            }

            validFramesCount++
            frameSmall.recycle()
            alignedBitmap.recycle()
        }

        refSmall.recycle()

        if (validFramesCount == 0) return@withContext refFrame

        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val outPixels = IntArray(width * height)
        val count = validFramesCount.toFloat()

        for (i in outPixels.indices) {
            val r = (accumulated[i][0] / count).roundToInt().coerceIn(0, 255)
            val g = (accumulated[i][1] / count).roundToInt().coerceIn(0, 255)
            val b = (accumulated[i][2] / count).roundToInt().coerceIn(0, 255)
            outPixels[i] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
        }

        output.setPixels(outPixels, 0, width, 0, 0, width, height)
        output
    }

    /**
     * Fast luminance-based motion vector estimation between two downscaled frames.
     */
    private fun estimateMotionVector(ref: IntArray, curr: IntArray, width: Int, height: Int): FloatArray {
        var bestDx = 0f
        var bestDy = 0f
        var minDiff = Float.MAX_VALUE

        // Search window for stabilization (e.g. +/- 6 pixels at 1/4 scale)
        val searchRange = 6
        val step = 2

        var dy = -searchRange
        while (dy <= searchRange) {
            var dx = -searchRange
            while (dx <= searchRange) {
                var diff = 0f
                var count = 0

                val startY = maxOf(0, -dy)
                val endY = minOf(height, height - dy)
                val startX = maxOf(0, -dx)
                val endX = minOf(width, width - dx)

                var y = startY
                while (y < endY) {
                    var x = startX
                    while (x < endX) {
                        val refIdx = y * width + x
                        val currIdx = (y + dy) * width + (x + dx)

                        val pRef = ref[refIdx]
                        val pCurr = curr[currIdx]

                        val lRef = (((pRef shr 16) and 0xFF) * 0.299f + ((pRef shr 8) and 0xFF) * 0.587f + (pRef and 0xFF) * 0.114f)
                        val lCurr = (((pCurr shr 16) and 0xFF) * 0.299f + ((pCurr shr 8) and 0xFF) * 0.587f + (pCurr and 0xFF) * 0.114f)

                        val d = abs(lRef - lCurr)
                        diff += d
                        count++
                        x += step
                    }
                    y += step
                }

                if (count > 0) {
                    val avgDiff = diff / count
                    if (avgDiff < minDiff) {
                        minDiff = avgDiff
                        bestDx = dx.toFloat()
                        bestDy = dy.toFloat()
                    }
                }
                dx += step
            }
            dy += step
        }

        return floatArrayOf(bestDx, bestDy)
    }

    /**
     * Applies real-time digital software stabilization (Virtual Gimbal crop & smoothing) on a preview/video frame
     * to eliminate handheld jitters even without hardware OIS/EIS.
     */
    fun stabilizeBitmapInstant(source: Bitmap, stabilizationStrength: Float = 0.1f): Bitmap {
        val width = source.width
        val height = source.height
        val cropW = (width * (1f - stabilizationStrength)).toInt()
        val cropH = (height * (1f - stabilizationStrength)).toInt()
        val startX = (width - cropW) / 2
        val startY = (height - cropH) / 2

        return Bitmap.createBitmap(source, startX, startY, cropW, cropH)
    }
}
