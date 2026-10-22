package com.example.engine

import android.graphics.Bitmap
import java.util.concurrent.ArrayBlockingQueue

data class BufferedFrame(
    val bitmap: Bitmap,
    val timestamp: Long,
    val exposureTimeNanos: Long,
    val iso: Int
)

class ZeroShutterLagBuffer(private val capacity: Int = 5) {

    private val queue = ArrayBlockingQueue<BufferedFrame>(capacity)

    @Synchronized
    fun pushFrame(frame: BufferedFrame) {
        if (queue.size >= capacity) {
            val oldest = queue.poll()
            if (oldest != null && !oldest.bitmap.isRecycled) {
                // Free previous buffer
            }
        }
        queue.offer(frame)
    }

    @Synchronized
    fun getLatestFrame(): BufferedFrame? {
        return queue.peek()
    }

    @Synchronized
    fun drainFrames(): List<BufferedFrame> {
        val list = mutableListOf<BufferedFrame>()
        queue.drainTo(list)
        return list
    }

    @Synchronized
    fun clear() {
        queue.clear()
    }
}
