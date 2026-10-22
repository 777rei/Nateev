package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.models.CameraSettings

@Composable
fun ProManualDashboard(
    settings: CameraSettings,
    onSetIso: (Int) -> Unit,
    onSetShutter: (Long) -> Unit,
    onSetFocusDistance: (Float) -> Unit,
    onToggleFocusPeaking: () -> Unit,
    onToggleZebra: () -> Unit,
    onResetToAuto: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: ISO, 1: SHUTTER, 2: FOCUS, 3: ASSIST

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(Color(0xFF14161C).copy(alpha = 0.95f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(16.dp)
    ) {
        // Top Pro Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MANUAL PRO CONTROLS",
                color = Color(0xFFFFD54F),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onResetToAuto,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = "Reset to Auto",
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Manual Controls",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs: ISO | SHUTTER | FOCUS | ASSIST
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("ISO", "SHUTTER", "FOCUS", "ASSIST").forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFFFFD54F) else Color.Transparent)
                        .clickable { selectedTab = index }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedTab) {
            0 -> {
                // ISO control
                val isoList = listOf(0, 50, 100, 200, 400, 800, 1600, 3200, 6400)
                Text(
                    text = if (settings.manualIso == 0) "ISO: AUTO" else "ISO: ${settings.manualIso}",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    isoList.forEach { iso ->
                        val isSelected = settings.manualIso == iso
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFFFFD54F) else Color.DarkGray)
                                .clickable { onSetIso(iso) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (iso == 0) "AUTO" else iso.toString(),
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            1 -> {
                // Shutter speed control
                val shutters = listOf(
                    0L to "AUTO",
                    125_000L to "1/8000s",
                    1_000_000L to "1/1000s",
                    4_000_000L to "1/250s",
                    16_666_666L to "1/60s",
                    33_333_333L to "1/30s",
                    100_000_000L to "1/10s",
                    500_000_000L to "0.5s"
                )
                Text(
                    text = if (settings.manualShutterNumerator == 0L) "SHUTTER: AUTO" else "SHUTTER: MANUAL",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    shutters.forEach { (timeNanos, label) ->
                        val isSelected = settings.manualShutterNumerator == timeNanos
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFFFFD54F) else Color.DarkGray)
                                .clickable { onSetShutter(timeNanos) }
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            2 -> {
                // Manual focus diopter slider
                Text(
                    text = if (settings.manualFocusDistance == 0f) "FOCUS: CONTINUOUS AF" else "FOCUS DISTANCE: ${String.format("%.1f", settings.manualFocusDistance)} D (MACRO -> INF)",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        value = settings.manualFocusDistance,
                        onValueChange = { onSetFocusDistance(it) },
                        valueRange = 0f..10f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFD54F),
                            activeTrackColor = Color(0xFFFFD54F)
                        )
                    )
                    TextButton(onClick = { onSetFocusDistance(0f) }) {
                        Text("AF", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                    }
                }
            }
            3 -> {
                // Assist tools: Focus Peaking, Zebra
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = settings.isFocusPeakingEnabled,
                        onClick = onToggleFocusPeaking,
                        label = { Text("FOCUS PEAKING (GREEN)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00E676),
                            selectedLabelColor = Color.Black
                        )
                    )
                    FilterChip(
                        selected = settings.isZebraEnabled,
                        onClick = onToggleZebra,
                        label = { Text("ZEBRA STRIPES") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFFD54F),
                            selectedLabelColor = Color.Black
                        )
                    )
                }
            }
        }
    }
}
