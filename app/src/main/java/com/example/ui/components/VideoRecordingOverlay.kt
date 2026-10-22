package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun VideoRecordingOverlay(
    isRecording: Boolean,
    durationSec: Long,
    decibelLevel: Float,
    modifier: Modifier = Modifier
) {
    if (!isRecording) return

    val infiniteTransition = rememberInfiniteTransition(label = "recBlink")
    val beaconAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beaconAlpha"
    )

    val minutes = durationSec / 60
    val seconds = durationSec % 60
    val hours = minutes / 60
    val timeFormatted = if (hours > 0) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes % 60, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    // Convert -60dB..0dB to a 0f..1f level
    val normalizedAudio = ((decibelLevel + 60f) / 60f).coerceIn(0.05f, 1f)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black.copy(alpha = 0.7f))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Red flashing recording dot
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Color.Red.copy(alpha = beaconAlpha))
        )

        Text(
            text = "REC",
            color = Color.Red,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = timeFormatted,
            color = Color.White,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )

        // Audio VU meter bar
        Row(
            modifier = Modifier
                .width(36.dp)
                .height(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.DarkGray),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(normalizedAudio)
                    .background(
                        if (decibelLevel > -6f) Color.Red
                        else if (decibelLevel > -18f) Color(0xFFFFD54F)
                        else Color(0xFF00E676)
                    )
            )
        }
    }
}
