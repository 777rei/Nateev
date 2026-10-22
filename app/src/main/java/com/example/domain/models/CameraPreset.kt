package com.example.domain.models

import android.graphics.ColorMatrix
import com.example.engine.CubeLutParser

enum class PresetCategory {
    FLAGSHIP_PHONES,
    DSLR_CINEMA,
    CREATIVE_FILM,
    MONOCHROME
}

data class CameraPreset(
    val id: String,
    val displayName: String,
    val category: PresetCategory,
    val description: String,
    val colorMatrix: ColorMatrix,
    val sharpnessBoost: Float = 1.0f,
    val contrastScale: Float = 1.0f,
    val saturationBoost: Float = 1.0f,
    val cubeLutData: com.example.engine.CubeLutData? = null
) {
    companion object {
        fun getAllPresets(): List<CameraPreset> {
            val luts = CubeLutParser.BUILT_IN_LUTS
            return listOf(
                // --- Flagship Phone Emulations (No trademark brands) ---
                CameraPreset(
                    id = "bionic_13",
                    displayName = "Cupertino 13 Pro",
                    category = PresetCategory.FLAGSHIP_PHONES,
                    description = "Iconic warm tone profile with smooth highlight roll-off and balanced skin tones",
                    sharpnessBoost = 1.20f,
                    contrastScale = 1.12f,
                    colorMatrix = ColorMatrix(floatArrayOf(
                        1.10f, 0.02f, -0.03f, 0f, 6f,
                        0.00f, 1.06f, -0.02f, 0f, 4f,
                        -0.04f, 0.00f, 0.95f, 0f, -4f,
                        0f,    0f,    0f,    1f, 0f
                    ))
                ),
                CameraPreset(
                    id = "redmi_matrix",
                    displayName = "Matrix HyperOS",
                    category = PresetCategory.FLAGSHIP_PHONES,
                    description = "High micro-contrast, punchy foliage greens, and luminous sky saturation",
                    sharpnessBoost = 1.35f,
                    contrastScale = 1.25f,
                    colorMatrix = ColorMatrix(floatArrayOf(
                        1.16f, -0.06f, -0.04f, 0f, 2f,
                        -0.05f, 1.22f, -0.05f, 0f, 4f,
                        -0.05f, -0.05f, 1.28f, 0f, 0f,
                        0f,     0f,     0f,    1f, 0f
                    ))
                ),
                CameraPreset(
                    id = "pixel_hdr",
                    displayName = "Computational HDR+",
                    category = PresetCategory.FLAGSHIP_PHONES,
                    description = "Deep computational shadows, lifted dynamic range, and crisp edge clarity",
                    sharpnessBoost = 1.40f,
                    contrastScale = 1.20f,
                    colorMatrix = ColorMatrix(floatArrayOf(
                        1.14f, -0.04f, -0.02f, 0f, 3f,
                        -0.03f, 1.15f, -0.03f, 0f, 3f,
                        -0.04f, -0.04f, 1.22f, 0f, 2f,
                        0f,     0f,     0f,    1f, 0f
                    ))
                ),
                CameraPreset(
                    id = "galaxy_vivid",
                    displayName = "Ultra AMOLED Glow",
                    category = PresetCategory.FLAGSHIP_PHONES,
                    description = "Vibrant pop colors with rich blues and luminous sunset warm tones",
                    sharpnessBoost = 1.28f,
                    contrastScale = 1.18f,
                    colorMatrix = ColorMatrix(floatArrayOf(
                        1.18f, -0.05f, -0.05f, 0f, 8f,
                        -0.04f, 1.16f, -0.04f, 0f, 6f,
                        -0.06f, -0.04f, 1.25f, 0f, 8f,
                        0f,     0f,     0f,    1f, 0f
                    ))
                ),

                // --- Pro DSLR & Medium Format Configurations ---
                CameraPreset(
                    id = "dslr_pro_50",
                    displayName = "DSLR Cine Prime 50mm",
                    category = PresetCategory.DSLR_CINEMA,
                    description = "Organic cinematic contrast curve, rich shadow depth, and filmic midtones",
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
                    id = "hassel_natural",
                    displayName = "Medium Format Studio",
                    category = PresetCategory.DSLR_CINEMA,
                    description = "Ultra linear skin tone graduation with soft highlight rolloff and neutral balance",
                    sharpnessBoost = 1.15f,
                    contrastScale = 1.05f,
                    colorMatrix = ColorMatrix(floatArrayOf(
                        1.05f, 0.01f, -0.01f, 0f, 4f,
                        0.00f, 1.03f, 0.00f,  0f, 2f,
                        -0.01f, 0.00f, 1.02f, 0f, -2f,
                        0f,    0f,    0f,    1f, 0f
                    ))
                ),
                CameraPreset(
                    id = "alpha_realism",
                    displayName = "Alpha Realism Pro",
                    category = PresetCategory.DSLR_CINEMA,
                    description = "Clinical high precision color mapping with ultra-sharp edge definition",
                    sharpnessBoost = 1.30f,
                    contrastScale = 1.10f,
                    colorMatrix = ColorMatrix(floatArrayOf(
                        1.04f, 0.00f, 0.00f, 0f, 0f,
                        0.00f, 1.04f, 0.00f, 0f, 0f,
                        0.00f, 0.00f, 1.06f, 0f, 0f,
                        0f,    0f,    0f,    1f, 0f
                    ))
                ),

                // --- Creative Film & Street Profiles (Backed by authentic 3D .cube LUTs) ---
                CameraPreset(
                    id = "kodak_portra",
                    displayName = luts.getOrNull(0)?.title?.replace(".cube", "")?.trim() ?: "Kodak Portra 400",
                    category = PresetCategory.CREATIVE_FILM,
                    description = "Authentic 3D .cube Kodak Portra film emulation with warm skin tones",
                    sharpnessBoost = 1.20f,
                    contrastScale = 1.15f,
                    colorMatrix = luts.getOrNull(0)?.toColorMatrix() ?: ColorMatrix(),
                    cubeLutData = luts.getOrNull(0)
                ),
                CameraPreset(
                    id = "teal_orange",
                    displayName = luts.getOrNull(1)?.title?.replace(".cube", "")?.trim() ?: "Teal & Orange Cinema",
                    category = PresetCategory.CREATIVE_FILM,
                    description = "Authentic 3D .cube Hollywood block-buster teal shadows and orange skin highlights",
                    sharpnessBoost = 1.25f,
                    contrastScale = 1.25f,
                    colorMatrix = luts.getOrNull(1)?.toColorMatrix() ?: ColorMatrix(),
                    cubeLutData = luts.getOrNull(1)
                ),
                CameraPreset(
                    id = "fuji_velvia",
                    displayName = luts.getOrNull(2)?.title?.replace(".cube", "")?.trim() ?: "Fuji Velvia 50",
                    category = PresetCategory.CREATIVE_FILM,
                    description = "Authentic 3D .cube slide film emulation with vibrant saturated punch",
                    sharpnessBoost = 1.28f,
                    contrastScale = 1.28f,
                    colorMatrix = luts.getOrNull(2)?.toColorMatrix() ?: ColorMatrix(),
                    cubeLutData = luts.getOrNull(2)
                ),
                CameraPreset(
                    id = "bleach_bypass",
                    displayName = luts.getOrNull(3)?.title?.replace(".cube", "")?.trim() ?: "Bleach Bypass Film",
                    category = PresetCategory.CREATIVE_FILM,
                    description = "Authentic 3D .cube gritty low-saturation high contrast gritty film look",
                    sharpnessBoost = 1.35f,
                    contrastScale = 1.35f,
                    colorMatrix = luts.getOrNull(3)?.toColorMatrix() ?: ColorMatrix(),
                    cubeLutData = luts.getOrNull(3)
                ),
                CameraPreset(
                    id = "kodachrome",
                    displayName = luts.getOrNull(4)?.title?.replace(".cube", "")?.trim() ?: "Kodachrome 1964",
                    category = PresetCategory.CREATIVE_FILM,
                    description = "Authentic 3D .cube vintage legendary color reproduction with deep primaries",
                    sharpnessBoost = 1.22f,
                    contrastScale = 1.22f,
                    colorMatrix = luts.getOrNull(4)?.toColorMatrix() ?: ColorMatrix(),
                    cubeLutData = luts.getOrNull(4)
                ),

                // --- Monochrome Pro ---
                CameraPreset(
                    id = "leica_noir",
                    displayName = "Leica Noir Monochrom",
                    category = PresetCategory.MONOCHROME,
                    description = "Legendary German high-contrast black & white with red spectral filtration",
                    sharpnessBoost = 1.35f,
                    contrastScale = 1.35f,
                    colorMatrix = ColorMatrix(floatArrayOf(
                        0.45f, 0.35f, 0.20f, 0f, -12f,
                        0.45f, 0.35f, 0.20f, 0f, -12f,
                        0.45f, 0.35f, 0.20f, 0f, -12f,
                        0f,    0f,    0f,    1f, 0f
                    ))
                ),
                CameraPreset(
                    id = "silver_gelatin",
                    displayName = "Silver Gelatin Fine Art",
                    category = PresetCategory.MONOCHROME,
                    description = "Smooth 16-zone grayscale roll-off reminiscent of classic darkroom prints",
                    sharpnessBoost = 1.15f,
                    contrastScale = 1.08f,
                    colorMatrix = ColorMatrix(floatArrayOf(
                        0.30f, 0.59f, 0.11f, 0f, 0f,
                        0.30f, 0.59f, 0.11f, 0f, 0f,
                        0.30f, 0.59f, 0.11f, 0f, 0f,
                        0f,    0f,    0f,    1f, 0f
                    ))
                ),

                // --- Natural Baseline ---
                CameraPreset(
                    id = "natural_raw",
                    displayName = "Natural Pure (Flat)",
                    category = PresetCategory.DSLR_CINEMA,
                    description = "Zero alteration neutral profile preserving 100% sensor dynamic range for grading",
                    sharpnessBoost = 1.0f,
                    contrastScale = 1.0f,
                    colorMatrix = ColorMatrix()
                )
            )
        }
    }
}
