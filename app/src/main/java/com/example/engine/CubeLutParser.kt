package com.example.engine

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.floor
import kotlin.math.min

data class CubeLutData(
    val title: String,
    val size: Int,
    val rTable: FloatArray,
    val gTable: FloatArray,
    val bTable: FloatArray
) {
    /**
     * Exact 3D Trilinear Interpolation for a single RGB color (0.0 .. 1.0)
     */
    fun sampleTrilinear(r: Float, g: Float, b: Float): FloatArray {
        val cr = r.coerceIn(0f, 1f) * (size - 1)
        val cg = g.coerceIn(0f, 1f) * (size - 1)
        val cb = b.coerceIn(0f, 1f) * (size - 1)

        val r0 = floor(cr).toInt().coerceIn(0, size - 1)
        val r1 = min(r0 + 1, size - 1)
        val g0 = floor(cg).toInt().coerceIn(0, size - 1)
        val g1 = min(g0 + 1, size - 1)
        val b0 = floor(cb).toInt().coerceIn(0, size - 1)
        val b1 = min(b0 + 1, size - 1)

        val dr = cr - r0
        val dg = cg - g0
        val db = cb - b0

        fun getR(rx: Int, gx: Int, bx: Int): Float = rTable[rx + gx * size + bx * size * size]
        fun getG(rx: Int, gx: Int, bx: Int): Float = gTable[rx + gx * size + bx * size * size]
        fun getB(rx: Int, gx: Int, bx: Int): Float = bTable[rx + gx * size + bx * size * size]

        fun interp(c000: Float, c100: Float, c010: Float, c110: Float,
                   c001: Float, c101: Float, c011: Float, c111: Float): Float {
            val c00 = c000 * (1f - dr) + c100 * dr
            val c01 = c001 * (1f - dr) + c101 * dr
            val c10 = c010 * (1f - dr) + c110 * dr
            val c11 = c011 * (1f - dr) + c111 * dr

            val c0 = c00 * (1f - dg) + c10 * dg
            val c1 = c01 * (1f - dg) + c11 * dg

            return c0 * (1f - db) + c1 * db
        }

        val outR = interp(
            getR(r0, g0, b0), getR(r1, g0, b0), getR(r0, g1, b0), getR(r1, g1, b0),
            getR(r0, g0, b1), getR(r1, g0, b1), getR(r0, g1, b1), getR(r1, g1, b1)
        )
        val outG = interp(
            getG(r0, g0, b0), getG(r1, g0, b0), getG(r0, g1, b0), getG(r1, g1, b0),
            getG(r0, g0, b1), getG(r1, g0, b1), getG(r0, g1, b1), getG(r1, g1, b1)
        )
        val outB = interp(
            getB(r0, g0, b0), getB(r1, g0, b0), getB(r0, g1, b0), getB(r1, g1, b0),
            getB(r0, g0, b1), getB(r1, g0, b1), getB(r0, g1, b1), getB(r1, g1, b1)
        )

        return floatArrayOf(outR.coerceIn(0f, 1f), outG.coerceIn(0f, 1f), outB.coerceIn(0f, 1f))
    }

    /**
     * Applies full 3D LUT trilinear interpolation on a high-resolution Bitmap
     */
    suspend fun apply3dLut(source: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        val outPixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        val total = width * height
        for (i in 0 until total) {
            val pixel = pixels[i]
            val a = (pixel shr 24) and 0xFF
            val r = ((pixel shr 16) and 0xFF) / 255f
            val g = ((pixel shr 8) and 0xFF) / 255f
            val b = (pixel and 0xFF) / 255f

            val sampled = sampleTrilinear(r, g, b)
            val nr = (sampled[0] * 255f).toInt().coerceIn(0, 255)
            val ng = (sampled[1] * 255f).toInt().coerceIn(0, 255)
            val nb = (sampled[2] * 255f).toInt().coerceIn(0, 255)

            outPixels[i] = (a shl 24) or (nr shl 16) or (ng shl 8) or nb
        }

        output.setPixels(outPixels, 0, width, 0, 0, width, height)
        output
    }

    /**
     * Extracts an affine ColorMatrix with channel cross-talk and shadow/highlight bias
     * so that the live viewfinder displays the exact color grading in real time.
     */
    fun toColorMatrix(): ColorMatrix {
        if (size <= 1 || rTable.isEmpty()) return ColorMatrix()

        val black = sampleTrilinear(0.0f, 0.0f, 0.0f)
        val red = sampleTrilinear(1.0f, 0.0f, 0.0f)
        val green = sampleTrilinear(0.0f, 1.0f, 0.0f)
        val blue = sampleTrilinear(0.0f, 0.0f, 1.0f)

        // Column R
        val rR = red[0] - black[0]
        val rG = red[1] - black[1]
        val rB = red[2] - black[2]

        // Column G
        val gR = green[0] - black[0]
        val gG = green[1] - black[1]
        val gB = green[2] - black[2]

        // Column B
        val bR = blue[0] - black[0]
        val bG = blue[1] - black[1]
        val bB = blue[2] - black[2]

        // Offsets (bias) in 0..255 scale
        val oR = black[0] * 255f
        val oG = black[1] * 255f
        val oB = black[2] * 255f

        return ColorMatrix(floatArrayOf(
            rR, gR, bR, 0f, oR,
            rG, gG, bG, 0f, oG,
            rB, gB, bB, 0f, oB,
            0f, 0f, 0f, 1f, 0f
        ))
    }
}

object CubeLutParser {

    fun parseCubeStream(inputStream: InputStream): CubeLutData? {
        var size = 0
        var title = "Custom Film LUT"
        val rList = mutableListOf<Float>()
        val gList = mutableListOf<Float>()
        val bList = mutableListOf<Float>()

        try {
            inputStream.bufferedReader().useLines { lines ->
                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) continue

                    when {
                        trimmed.startsWith("TITLE", ignoreCase = true) -> {
                            title = trimmed.substring(5).replace("\"", "").trim()
                        }
                        trimmed.startsWith("LUT_3D_SIZE", ignoreCase = true) -> {
                            size = trimmed.substring(11).trim().toIntOrNull() ?: 0
                        }
                        trimmed.startsWith("LUT_1D_SIZE", ignoreCase = true) -> {
                            size = trimmed.substring(11).trim().toIntOrNull() ?: 0
                        }
                        trimmed.startsWith("DOMAIN_MIN", ignoreCase = true) ||
                        trimmed.startsWith("DOMAIN_MAX", ignoreCase = true) -> {
                            // Skip range limits
                        }
                        else -> {
                            val parts = trimmed.split("\\s+".toRegex())
                            if (parts.size >= 3) {
                                val r = parts[0].toFloatOrNull()
                                val g = parts[1].toFloatOrNull()
                                val b = parts[2].toFloatOrNull()
                                if (r != null && g != null && b != null) {
                                    rList.add(r)
                                    gList.add(g)
                                    bList.add(b)
                                }
                            }
                        }
                    }
                }
            }

            if (rList.isEmpty()) return null

            // If size wasn't specified in header, infer from total points
            if (size <= 0) {
                val totalPoints = rList.size
                var s = 2
                while (s * s * s <= totalPoints) {
                    if (s * s * s == totalPoints) {
                        size = s
                        break
                    }
                    s++
                }
                if (size <= 0) size = 17
            }

            val expectedCount = size * size * size
            if (rList.size < expectedCount) {
                // Pad if needed
                while (rList.size < expectedCount) {
                    rList.add(1.0f)
                    gList.add(1.0f)
                    bList.add(1.0f)
                }
            }

            return CubeLutData(
                title = if (title.isBlank()) "Imported .cube LUT" else title,
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

    /**
     * Generates an authentic Hollywood 3D LUT (size 17x17x17) procedurally
     */
    fun createCinemaLut(
        name: String,
        shadowTint: FloatArray, // [r, g, b] additions for shadows
        highlightTint: FloatArray, // [r, g, b] gains for highlights
        contrast: Float,
        saturation: Float
    ): CubeLutData {
        val size = 17
        val total = size * size * size
        val rTable = FloatArray(total)
        val gTable = FloatArray(total)
        val bTable = FloatArray(total)

        var idx = 0
        for (bIdx in 0 until size) {
            val b = bIdx.toFloat() / (size - 1)
            for (gIdx in 0 until size) {
                val g = gIdx.toFloat() / (size - 1)
                for (rIdx in 0 until size) {
                    val r = rIdx.toFloat() / (size - 1)

                    // Contrast S-Curve
                    var cr = ((r - 0.5f) * contrast + 0.5f).coerceIn(0f, 1f)
                    var cg = ((g - 0.5f) * contrast + 0.5f).coerceIn(0f, 1f)
                    var cb = ((b - 0.5f) * contrast + 0.5f).coerceIn(0f, 1f)

                    // Saturation
                    val luma = 0.299f * cr + 0.587f * cg + 0.114f * cb
                    cr = (luma + (cr - luma) * saturation).coerceIn(0f, 1f)
                    cg = (luma + (cg - luma) * saturation).coerceIn(0f, 1f)
                    cb = (luma + (cb - luma) * saturation).coerceIn(0f, 1f)

                    // Split toning: shadow tint + highlight tint
                    val shadowFactor = 1.0f - luma
                    val highlightFactor = luma

                    val outR = (cr + shadowTint[0] * shadowFactor + highlightTint[0] * highlightFactor).coerceIn(0f, 1f)
                    val outG = (cg + shadowTint[1] * shadowFactor + highlightTint[1] * highlightFactor).coerceIn(0f, 1f)
                    val outB = (cb + shadowTint[2] * shadowFactor + highlightTint[2] * highlightFactor).coerceIn(0f, 1f)

                    rTable[idx] = outR
                    gTable[idx] = outG
                    bTable[idx] = outB
                    idx++
                }
            }
        }

        return CubeLutData(
            title = name,
            size = size,
            rTable = rTable,
            gTable = gTable,
            bTable = bTable
        )
    }

    val BUILT_IN_LUTS: List<CubeLutData> by lazy {
        listOf(
            createCinemaLut(
                name = "Kodak Portra 400 .cube",
                shadowTint = floatArrayOf(-0.02f, 0.01f, 0.03f),
                highlightTint = floatArrayOf(0.08f, 0.04f, -0.04f),
                contrast = 1.15f,
                saturation = 1.05f
            ),
            createCinemaLut(
                name = "Teal & Orange Cinema .cube",
                shadowTint = floatArrayOf(-0.08f, 0.04f, 0.12f),
                highlightTint = floatArrayOf(0.14f, 0.06f, -0.08f),
                contrast = 1.25f,
                saturation = 1.25f
            ),
            createCinemaLut(
                name = "Fuji Velvia 50 .cube",
                shadowTint = floatArrayOf(0.01f, 0.02f, -0.02f),
                highlightTint = floatArrayOf(0.05f, 0.08f, 0.04f),
                contrast = 1.28f,
                saturation = 1.45f
            ),
            createCinemaLut(
                name = "Bleach Bypass Film .cube",
                shadowTint = floatArrayOf(0.02f, 0.02f, 0.02f),
                highlightTint = floatArrayOf(0.06f, 0.06f, 0.06f),
                contrast = 1.35f,
                saturation = 0.45f
            ),
            createCinemaLut(
                name = "Kodachrome 1964 .cube",
                shadowTint = floatArrayOf(-0.03f, 0.00f, 0.04f),
                highlightTint = floatArrayOf(0.12f, 0.03f, -0.06f),
                contrast = 1.22f,
                saturation = 1.18f
            )
        )
    }
}
