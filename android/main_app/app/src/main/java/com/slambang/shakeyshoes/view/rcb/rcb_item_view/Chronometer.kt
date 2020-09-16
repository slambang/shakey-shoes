package com.slambang.shakeyshoes.view.rcb.rcb_item_view

import android.content.Context
import android.os.SystemClock
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class Chronometer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    init {
        updateText(0)
    }

    private var baseTime = 0L
    private var pauseTime = 0L
    private var pauseTotal = 0L

    @Volatile
    private var isResumed = false

    private val runnable = Runnable {
        updateText(now())
    }

    fun resume() {
        if (isResumed) return

        if (baseTime == 0L) {
            baseTime = now()
        }

        pauseTotal += if (pauseTime != 0L) {
            now() - pauseTime
        } else {
            0
        }

        isResumed = true
        postDelayed(runnable, DELAY_MS)
    }

    fun pause() {
        if (!isResumed) return

        pauseTime = now()
        isResumed = false
        removeCallbacks(runnable)
    }

    fun reset() {
        baseTime = 0L
        pauseTime = 0L
        pauseTotal = 0L
        removeCallbacks(runnable)
        updateText(baseTime)
    }

    @Synchronized
    private fun updateText(now: Long) {

        val timeElapsed = (now - baseTime) - pauseTotal
        val millis: Long = timeElapsed % 1000
        val second: Long = timeElapsed / 1000 % 60
        val minute: Long = timeElapsed / (1000 * 60) % 60
        val hour: Long = timeElapsed / (1000 * 60 * 60) % 24
        text = String.format("%02d:%02d:%02d.%03d", hour, minute, second, millis)

        if (isResumed) postDelayed(runnable, DELAY_MS)
    }

    private fun now() = SystemClock.elapsedRealtime()

    companion object {
        private const val DELAY_MS = 75L
    }
}
