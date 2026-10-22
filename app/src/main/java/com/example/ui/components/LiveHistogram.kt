package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LiveHistogram(
    histogramData: IntArray?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 110.dp, height = 50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.55f))
            .border(0.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        if (histogramData != null && histogramData.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val bins = histogramData.size
                var maxCount = 1
                for (v in histogramData) {
                    if (v > maxCount) maxCount = v
                }

                val path = Path()
                path.moveTo(0f, height)

                val step = width / bins.toFloat()
                for (i in 0 until bins) {
                    val x = i * step
                    val normalized = (histogramData[i].toFloat() / maxCount.toFloat()).coerceIn(0f, 1f)
                    val y = height - (normalized * (height - 4f))
                    path.lineTo(x, y)
                }
                path.lineTo(width, height)
                path.close()

                drawPath(
                    path = path,
                    color = Color(0xFFFFD54F).copy(alpha = 0.45f)
                )
                drawPath(
                    path = path,
                    color = Color(0xFFFFD54F),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }

        Text(
            text = "RGB / LUMA",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 7.sp,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}
