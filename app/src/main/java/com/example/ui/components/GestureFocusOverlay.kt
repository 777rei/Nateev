package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun GestureFocusOverlay(
    isLocked: Boolean,
    onFocusRequested: (Offset) -> Unit,
    onUnlockRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    val scale = remember { Animatable(1.5f) }
    val alpha = remember { Animatable(1.0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        if (isLocked) {
                            onUnlockRequested()
                        }
                        focusPoint = offset
                        onFocusRequested(offset)
                        scope.launch {
                            scale.snapTo(1.4f)
                            alpha.snapTo(1.0f)
                            scale.animateTo(1.0f, tween(250))
                            if (!isLocked) {
                                alpha.animateTo(0.0f, tween(800, delayMillis = 1000))
                                focusPoint = null
                            }
                        }
                    },
                    onLongPress = { offset ->
                        focusPoint = offset
                        onFocusRequested(offset)
                        scope.launch {
                            scale.snapTo(1.4f)
                            alpha.snapTo(1.0f)
                            scale.animateTo(1.0f, tween(250))
                        }
                    }
                )
            }
    ) {
        val point = focusPoint
        if (point != null && (alpha.value > 0.01f || isLocked)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = 32.dp.toPx() * scale.value
                val color = if (isLocked) Color.Red else Color(0xFFFFD54F).copy(alpha = alpha.value)

                // Outer focus ring
                drawCircle(
                    color = color,
                    radius = radius,
                    center = point,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Center metering dot
                drawCircle(
                    color = color,
                    radius = 3.dp.toPx(),
                    center = point
                )
                
                if (isLocked) {
                     // Add "LOCKED" text or indicator
                     // For simplicity, just use a thicker stroke/different color for now
                }
            }
        }
    }
}
