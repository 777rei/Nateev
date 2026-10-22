package com.example.engine

import kotlinx.coroutines.*

class BurstCaptureManager {

    private var burstJob: Job? = null

    fun startBurst(
        frameCount: Int = 10,
        intervalMs: Long = 120L,
        onFrameTrigger: (index: Int) -> Unit,
        onComplete: (totalTaken: Int) -> Unit,
        scope: CoroutineScope
    ) {
        cancelBurst()
        burstJob = scope.launch(Dispatchers.Default) {
            var taken = 0
            for (i in 0 until frameCount) {
                if (!isActive) break
                withContext(Dispatchers.Main) {
                    onFrameTrigger(i)
                }
                taken++
                delay(intervalMs)
            }
            withContext(Dispatchers.Main) {
                onComplete(taken)
            }
        }
    }

    fun cancelBurst() {
        burstJob?.cancel()
        burstJob = null
    }

    val isBursting: Boolean
        get() = burstJob?.isActive == true
}
