package com.example.data

import android.content.Context
import android.content.SharedPreferences

enum class VideoCodec { HEVC_H265, AVC_H264, AV1 }
enum class GridType { NONE, RULE_OF_THIRDS, GOLDEN_RATIO, FIBONACCI }
enum class ShutterSound { ENABLED, MUTED }

data class AppPreferences(
    val videoCodec: VideoCodec = VideoCodec.HEVC_H265,
    val targetBitrateMbps: Int = 50,
    val gridType: GridType = GridType.RULE_OF_THIRDS,
    val enableNpuAcceleration: Boolean = true,
    val enableRawPlusJpeg: Boolean = false,
    val shutterSound: ShutterSound = ShutterSound.ENABLED,
    val autoGeotagging: Boolean = true,
    val audioStereo: Boolean = true,
    val isWatermarkEnabled: Boolean = false
)

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("nateev_settings", Context.MODE_PRIVATE)

    fun loadPreferences(): AppPreferences {
        return AppPreferences(
            videoCodec = try {
                VideoCodec.valueOf(prefs.getString("codec", VideoCodec.HEVC_H265.name)!!)
            } catch (e: Exception) {
                VideoCodec.HEVC_H265
            },
            targetBitrateMbps = prefs.getInt("bitrate", 50),
            gridType = try {
                GridType.valueOf(prefs.getString("grid", GridType.RULE_OF_THIRDS.name)!!)
            } catch (e: Exception) {
                GridType.RULE_OF_THIRDS
            },
            enableNpuAcceleration = prefs.getBoolean("npu", true),
            enableRawPlusJpeg = prefs.getBoolean("raw_jpeg", false),
            shutterSound = try {
                ShutterSound.valueOf(prefs.getString("shutter_sound", ShutterSound.ENABLED.name)!!)
            } catch (e: Exception) {
                ShutterSound.ENABLED
            },
            autoGeotagging = prefs.getBoolean("geotag", true),
            audioStereo = prefs.getBoolean("audio_stereo", true),
            isWatermarkEnabled = prefs.getBoolean("watermark", false)
        )
    }
    
    // ... (rest of the methods) ...

    fun setWatermarkEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("watermark", enabled).apply()
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

    fun setShutterSound(sound: ShutterSound) {
        prefs.edit().putString("shutter_sound", sound.name).apply()
    }

    fun setAutoGeotagging(enabled: Boolean) {
        prefs.edit().putBoolean("geotag", enabled).apply()
    }

    fun setAudioStereo(enabled: Boolean) {
        prefs.edit().putBoolean("audio_stereo", enabled).apply()
    }
}
