package com.slambang.shakeyshoes.oboe

import android.util.Log
import java.util.*

class MockArduinoConsumer {

    private var isAlive = false
    private var isPlaying = false

    fun start(bufferList: Queue<List<Int>>) {
        Thread {

            isAlive = true

            while (isAlive) {
                if (isPlaying) {
                    Thread.sleep(50) // 40 = underflow

                    if (bufferList.isNotEmpty()) {
                        val window = bufferList.remove()
                        Log.d("Steve", "window=${window.joinToString()}")
                    } else {
                        Log.e("Steve", "Arduino buffer underflow")
                    }
                }
            }

            Log.d("Steve", "Mock arduino finished")
        }.start()
    }

    fun setIsPlaying(isPlaying: Boolean) {
        this.isPlaying = isPlaying
    }

    fun stop() {
        isAlive = false
    }
}
