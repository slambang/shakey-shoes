package com.slambang.shakeyshoes.oboe

import android.content.Context
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import java.util.*

// TODO rename "refills" to "windows"!!!

private const val WINDOW_COUNT = 3
private const val WINDOW_SIZE = 4
private const val WINDOW_INTERVAL_MS = 10

object NativeApp {

    init {
        System.loadLibrary("hello-oboe")
    }

    private val fileManager = FileManager()

    private var mAppHandle: Long = 0

    private lateinit var mDspBuffer: IntBuffer

    private val mockArduinoConsumer = MockArduinoConsumer()

    @JvmStatic
    fun create(context: Context): Boolean {

        if (mAppHandle == 0L) {

            fileManager.copySamples(context)

            val totalBytesCount = WINDOW_SIZE * (Integer.SIZE / java.lang.Byte.SIZE)
            mDspBuffer = ByteBuffer.allocateDirect(totalBytesCount)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .asIntBuffer()

            if (!mDspBuffer.isDirect) throw IllegalStateException("mDspBuffer is not direct!")

            val path = fileManager.resolve(FileManager.TERMINUS_SIGNED_16BIT_PCM_MONO.second, context)
            mAppHandle = nativeInitApp(path, WINDOW_INTERVAL_MS, WINDOW_COUNT, WINDOW_SIZE, mDspBuffer)

            /*
             * This will prime audio and dsp data.
             * Audio data will remain in the native buffer, ready for Oboe later.
             * DSP data will be delivered to 'onDspBufferReady()' ready for Adruino to prime later.
             *
             * After this call we should have a fully primed native audio buffer and a depleted
             * native dsp buffer.
             * Oboe will receive the primed audio data immediately after starting.
             * Dsp data will eventually be refilled as the audio data is consumed, and will be delivered
             * to 'onDspBufferReady()' as/when it is ready.
             *
             * Arduino will eventually read the data delivered to 'onDspBufferReady()' when it requires
             * refills.
             */
            val maxRefills = WINDOW_COUNT * WINDOW_SIZE
            nativePrime(maxRefills, mAppHandle)

            val refillSizeFrames = nativeGetRefillSizeFrames(mAppHandle)
            val arduinoInterval = 1000 / (nativeGetSampleRate(mAppHandle) / refillSizeFrames)
            Log.d("Steve", "arduinoInterval=$arduinoInterval") // TODO check this!

            nativeStart(mAppHandle)
            mockArduinoConsumer.start(sharedBufferList)
        }

        return mAppHandle != 0L
    }

    // This will be shard with the RCB.
    private val sharedBufferList: Queue<List<Int>> = LinkedList(listOf())

    @JvmStatic
    @Suppress("UNUSED") // Called from JNI
    fun onDspBufferReady() {

        val window = mutableListOf<Int>()
        mDspBuffer.position(0)

        while (mDspBuffer.hasRemaining()) {
            window.add(mDspBuffer.get())
        }

        sharedBufferList.add(window)
        Log.d("Steve", "onDspBufferReady: ${window.joinToString()}")
    }

    @JvmStatic
    fun delete() {
        mockArduinoConsumer.stop()
        if (mAppHandle != 0L) {
            nativeDeleteApp(mAppHandle)
            mAppHandle = 0
        }
    }

    private var mIsPlaying = false

    @JvmStatic
    fun setToneOn(isPlaying: Boolean) {
        mIsPlaying = isPlaying
        mockArduinoConsumer.setIsPlaying(mIsPlaying)
        if (mAppHandle != 0L) nativeSetPlaying(mAppHandle, isPlaying)
    }

    @JvmStatic
    val currentOutputLatencyMillis: Double
        get() = if (mAppHandle == 0L) 0.toDouble() else nativeGetCurrentOutputLatencyMillis(mAppHandle)

    @JvmStatic
    val isLatencyDetectionSupported: Boolean
        get() = mAppHandle != 0L && nativeIsLatencyDetectionSupported(mAppHandle)

    private external fun nativeInitApp(path: String, intervalMs: Int, maxRefills: Int, refillSize: Int, dspBuffer: IntBuffer): Long
    private external fun nativeGetRefillSizeFrames(storeHandle: Long): Int
    private external fun nativeGetSampleRate(storeHandle: Long): Int
    private external fun nativeDeleteApp(storeHandle: Long)
    private external fun nativeSetPlaying(storeHandle: Long, isToneOn: Boolean)
    private external fun nativeGetCurrentOutputLatencyMillis(storeHandle: Long): Double
    private external fun nativeIsLatencyDetectionSupported(storeHandle: Long): Boolean
    private external fun nativePrime(refills: Int, storeHandle: Long)
    private external fun nativeStart(storeHandle: Long)
}