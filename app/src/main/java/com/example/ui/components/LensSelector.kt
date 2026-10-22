package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LensSelector(
    selectedZoomRatio: Float,
    availableZoomRatios: List<Float> = listOf(0.5f, 1.0f, 2.0f, 5.0f),
    onZoomRatioSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.55f))
            .border(0.8.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        availableZoomRatios.forEach { ratio ->
            val isSelected = selectedZoomRatio == ratio
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.1f else 1.0f,
                label = "lensScale"
            )

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(if (isSelected) Color(0xFFFFD54F) else Color.Transparent)
                    .clickable { onZoomRatioSelected(ratio) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (ratio % 1.0f == 0.0f) "${ratio.toInt()}x" else "${ratio}x",
                    color = if (isSelected) Color.Black else Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
