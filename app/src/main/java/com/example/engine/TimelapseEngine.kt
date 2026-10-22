package com.example.engine

import kotlinx.coroutines.*

enum class TimelapseInterval(val displayName: String, val intervalMs: Long) {
    FAST_CLOUDS("0.5s (Clouds/Traffic)", 500L),
    WALKING_STREET("1s (City Walk)", 1000L),
    SUNSET("2s (Sunset)", 2000L),
    SLOW_NIGHT("5s (Night Sky)", 5000L),
    STARS("10s (Astrophotography)", 10000L)
}

class TimelapseEngine {

    private var timelapseJob: Job? = null
    var isRunning: Boolean = false
        private set

    fun startTimelapse(
        interval: TimelapseInterval,
        onCaptureFrame: (frameIndex: Int) -> Unit,
        scope: CoroutineScope
    ) {
        stopTimelapse()
        isRunning = true
        timelapseJob = scope.launch(Dispatchers.Default) {
            var count = 0
            while (isActive && isRunning) {
                withContext(Dispatchers.Main) {
                    onCaptureFrame(count)
                }
                count++
                delay(interval.intervalMs)
            }
        }
    }

    fun stopTimelapse() {
        isRunning = false
        timelapseJob?.cancel()
        timelapseJob = null
    }
}
