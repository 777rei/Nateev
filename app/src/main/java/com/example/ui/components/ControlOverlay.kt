package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.models.CameraSettings
import com.example.domain.models.CaptureMode
import com.example.domain.models.FlashMode

@Composable
fun ControlOverlay(
    settings: CameraSettings,
    histogramData: IntArray?,
    onEVChange: (Int) -> Unit,
    onModeSelected: (CaptureMode) -> Unit,
    onToggleStabilization: () -> Unit,
    onToggleGrid: () -> Unit,
    onToggleFlash: () -> Unit,
    onToggleManualControls: () -> Unit,
    onOpenLutPicker: () -> Unit,
    onOpenSettings: () -> Unit,
    onSwitchCamera: () -> Unit,
    onShutterClick: () -> Unit,
    onOpenGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP HUD BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash Mode button
            IconButton(onClick = onToggleFlash, modifier = Modifier.size(36.dp)) {
                val icon = when (settings.flashMode) {
                    FlashMode.OFF -> Icons.Default.FlashOff
                    FlashMode.ON -> Icons.Default.FlashOn
                    FlashMode.AUTO -> Icons.Default.FlashAuto
                    FlashMode.TORCH -> Icons.Default.Highlight
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Flash Mode",
                    tint = if (settings.flashMode != FlashMode.OFF) Color(0xFFFFD54F) else Color.White
                )
            }

            // Stabilization OIS/EIS toggle
            IconButton(onClick = onToggleStabilization, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.VideoStable,
                    contentDescription = "Stabilization",
                    tint = if (settings.isStabilizationEnabled) Color(0xFF00E676) else Color.White
                )
            }

            // Grid toggle
            IconButton(onClick = onToggleGrid, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.GridOn,
                    contentDescription = "Grid Overlay",
                    tint = if (settings.gridOverlayEnabled) Color(0xFFFFD54F) else Color.White
                )
            }

            // 3D LUT Picker
            IconButton(onClick = onOpenLutPicker, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "3D LUT Filter",
                    tint = Color(0xFF81D4FA)
                )
            }

            // Pro Manual Controls button
            IconButton(onClick = onToggleManualControls, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Pro Manual Controls",
                    tint = if (settings.isManualControlsVisible) Color(0xFFFFD54F) else Color.White
                )
            }

            // Camera Switcher (Front/Back)
            IconButton(onClick = onSwitchCamera, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Switch Camera",
                    tint = Color.White
                )
            }

            // Settings Screen
            IconButton(onClick = onOpenSettings, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }

        // Live Histogram overlay on the right
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            if (settings.isLiveHistogramEnabled) {
                LiveHistogram(histogramData = histogramData)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // EV EXPOSURE SLIDER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Exposure,
                contentDescription = "EV Exposure",
                tint = Color.White,
                modifier = Modifier.padding(end = 6.dp).size(20.dp)
            )
            Slider(
                value = settings.exposureCompensation.toFloat(),
                onValueChange = { onEVChange(it.toInt()) },
                valueRange = -4f..4f,
                steps = 7,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFFD54F),
                    activeTrackColor = Color(0xFFFFD54F)
                )
            )
            Text(
                text = "${if (settings.exposureCompensation > 0) "+" else ""}${settings.exposureCompensation} EV",
                color = Color.White,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 6.dp)
            )
        }

        // MODE SELECTOR (PHOTO | VIDEO | BURST | PRO)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                    .padding(2.dp)
            ) {
                listOf(
                    CaptureMode.PHOTO to "PHOTO",
                    CaptureMode.VIDEO to "VIDEO",
                    CaptureMode.BURST to "BURST",
                    CaptureMode.PRO_MANUAL to "PRO"
                ).forEach { (mode, title) ->
                    val isSelected = settings.captureMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) Color(0xFFFFD54F) else Color.Transparent)
                            .clickable { onModeSelected(mode) }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
                        )
                    }
                }
            }
        }

        // BOTTOM ACTION ROW: [GALLERY THUMB] | [LARGE SHUTTER] | [EFFECTS/BURST]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Recent Capture Gallery Button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
                    .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                    .clickable { onOpenGallery() },
                contentAlignment = Alignment.Center
            ) {
                if (settings.lastCapturedUri != null) {
                    val isVideo = settings.lastCapturedUri.contains(".mp4", ignoreCase = true) || settings.lastCapturedUri.contains("video", ignoreCase = true)
                    if (isVideo) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = "Video Thumbnail",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else {
                        AsyncImage(
                            model = settings.lastCapturedUri,
                            contentDescription = "Last Capture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery",
                        tint = Color.White
                    )
                }
            }

            // Central Shutter Button
            val shutterColor = animateColorAsState(
                targetValue = if (settings.isVideoMode) {
                    if (settings.isRecording) Color.Red else Color(0xFFFF5252)
                } else if (settings.captureMode == CaptureMode.BURST) {
                    Color(0xFF00E676)
                } else Color.White,
                label = "shutterColor"
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(4.dp, Color.White, CircleShape)
                    .background(Color.Transparent)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(if (settings.isRecording) RoundedCornerShape(12.dp) else CircleShape)
                        .background(shutterColor.value)
                        .clickable { onShutterClick() }
                )
            }

            // Quick Pro Toggle / Reset
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    .clickable { onToggleManualControls() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Pro Toggle",
                    tint = if (settings.isManualControlsVisible) Color(0xFFFFD54F) else Color.White
                )
            }
        }
    }
}
