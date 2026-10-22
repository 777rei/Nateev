package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun VirtualHorizonOverlay(
    pitchAngle: Float,
    rollAngle: Float,
    modifier: Modifier = Modifier
) {
    val isLevel = abs(rollAngle) < 1.5f && abs(pitchAngle) < 2.0f

    val lineColor by animateColorAsState(
        targetValue = if (isLevel) Color(0xFF00E676) else Color.White.copy(alpha = 0.55f),
        label = "horizonColor"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val lineLength = 140.dp.toPx()

            // Center crosshair reference dots
            drawCircle(
                color = Color.White.copy(alpha = 0.4f),
                radius = 3.dp.toPx(),
                center = center
            )
            drawCircle(
                color = lineColor,
                radius = 1.5.dp.toPx(),
                center = center
            )

            // Dynamic rolling horizon line
            rotate(degrees = -rollAngle, pivot = center) {
                // Left wing
                drawLine(
                    color = lineColor,
                    start = Offset(center.x - lineLength / 2f, center.y),
                    end = Offset(center.x - 24.dp.toPx(), center.y),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Right wing
                drawLine(
                    color = lineColor,
                    start = Offset(center.x + 24.dp.toPx(), center.y),
                    end = Offset(center.x + lineLength / 2f, center.y),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        if (isLevel) {
            Text(
                text = "LEVEL 0°",
                color = Color(0xFF00E676),
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 40.dp)
            )
        }
    }
}
