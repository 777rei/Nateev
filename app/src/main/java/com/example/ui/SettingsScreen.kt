package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppPreferences
import com.example.data.GridType
import com.example.data.ShutterSound
import com.example.data.VideoCodec
import com.example.engine.CameraHardwareProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentPreferences: AppPreferences,
    hardwareProfile: CameraHardwareProfile,
    onPreferenceChanged: (AppPreferences) -> Unit,
    onBackClicked: () -> Unit
) {
    BackHandler { onBackClicked() }
    var prefs by remember { mutableStateOf(currentPreferences) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PRO SETTINGS & PIPELINE",
                        color = Color(0xFFFFD54F),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF101216))
            )
        },
        containerColor = Color(0xFF0C0E12)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section: Hardware Diagnostics
            Text(
                text = "HARDWARE & OPTICS PROFILE",
                color = Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    HardwareRow("HAL Support Level", hardwareProfile.hardwareLevelName)
                    HardwareRow("Optical Stabilization (OIS)", if (hardwareProfile.hasOis) "ACTIVE / SUPPORTED" else "UNAVAILABLE")
                    HardwareRow("Video Stabilization (EIS)", if (hardwareProfile.hasVideoStabilization) "ENABLED" else "UNAVAILABLE")
                    HardwareRow("RAW / DNG Sensor", if (hardwareProfile.supportsRaw) "SUPPORTED" else "EMULATED")
                    HardwareRow("Sensor ISO Limits", "${hardwareProfile.isoRange?.lower ?: 50} - ${hardwareProfile.isoRange?.upper ?: 6400}")
                }
            }

            // Section: Video Encoding & Bitrate
            Text(
                text = "CINEMATIC VIDEO PIPELINE",
                color = Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Video Compression Codec", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(VideoCodec.HEVC_H265, VideoCodec.AVC_H264, VideoCodec.AV1).forEach { codec ->
                            val isSelected = prefs.videoCodec == codec
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    prefs = prefs.copy(videoCodec = codec)
                                    onPreferenceChanged(prefs)
                                },
                                label = { Text(codec.name.replace("_", " ")) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFD54F),
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    Text("Target Recording Bitrate: ${prefs.targetBitrateMbps} Mbps", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = prefs.targetBitrateMbps.toFloat(),
                        onValueChange = {
                            prefs = prefs.copy(targetBitrateMbps = it.toInt())
                            onPreferenceChanged(prefs)
                        },
                        valueRange = 25f..100f,
                        steps = 2,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFD54F),
                            activeTrackColor = Color(0xFFFFD54F)
                        )
                    )
                }
            }

            // Section: Composition Guides
            Text(
                text = "VIEWFINDER & COMPOSITION",
                color = Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Framing Grid Geometry", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        GridType.entries.forEach { grid ->
                            val isSelected = prefs.gridType == grid
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    prefs = prefs.copy(gridType = grid)
                                    onPreferenceChanged(prefs)
                                },
                                label = { Text(grid.name.replace("_", " "), fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFD54F),
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }
                }
            }

            // Section: NPU & Processing Power
            Text(
                text = "OPTIMIZATION & ENGINE BYPASS",
                color = Color.LightGray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("NPU Accelerated Shaders", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Utilizes hardware GPU/NPU runtime shaders for zero-latency preview grading.", color = Color.Gray, fontSize = 10.sp)
                        }
                        Switch(
                            checked = prefs.enableNpuAcceleration,
                            onCheckedChange = {
                                prefs = prefs.copy(enableNpuAcceleration = it)
                                onPreferenceChanged(prefs)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFD54F), checkedTrackColor = Color(0xFF7A651F))
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("RAW + JPEG Capture", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Saves uncompressed DNG alongside processed cinema photo.", color = Color.Gray, fontSize = 10.sp)
                        }
                        Switch(
                            checked = prefs.enableRawPlusJpeg,
                            onCheckedChange = {
                                prefs = prefs.copy(enableRawPlusJpeg = it)
                                onPreferenceChanged(prefs)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFD54F), checkedTrackColor = Color(0xFF7A651F))
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("GPS Geotagging", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Write latitude and longitude directly to EXIF header.", color = Color.Gray, fontSize = 10.sp)
                        }
                        Switch(
                            checked = prefs.autoGeotagging,
                            onCheckedChange = {
                                prefs = prefs.copy(autoGeotagging = it)
                                onPreferenceChanged(prefs)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFD54F), checkedTrackColor = Color(0xFF7A651F))
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Watermark (\"Nateev | 777rei\")", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Apply branding to captured photos.", color = Color.Gray, fontSize = 10.sp)
                        }
                        Switch(
                            checked = prefs.isWatermarkEnabled,
                            onCheckedChange = {
                                prefs = prefs.copy(isWatermarkEnabled = it)
                                onPreferenceChanged(prefs)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFFD54F), checkedTrackColor = Color(0xFF7A651F))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Section: About
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161922)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("ABOUT", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Made by 777rei (reialwaysfreemium)", color = Color.White, fontSize = 12.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HardwareRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.LightGray, fontSize = 11.sp)
        Text(text = value, color = Color(0xFF00E676), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}
