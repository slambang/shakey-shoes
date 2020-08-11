package com.betty7.rcb

import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

data class CircularBufferConfig(
    val numRefills: Int = 0,
    val refillSize: Int = 0,
    val windowSizeMs: Int = 0,
    val maxUnderflows: Int = 0
)

enum class CircularBufferState {
    CONNECTING,
    READY,
    PAUSED,
    RESUMED,
    DISCONNECTED,
    REFILL,
    UNDERFLOW
}

interface CircularBufferService {

    interface Listener {
        fun onBufferServiceState(bufferService: CircularBufferService, state: CircularBufferState)
        fun onBufferServiceFreeHeap(bufferService: CircularBufferService, freeHeapBytes: Int)
        fun onBufferServiceError(bufferService: CircularBufferService, error: Throwable? = null)
    }

    val id: Int
    val config: CircularBufferConfig

    fun connect(
        macAddress: String,
        serviceUuid: String, listener: Listener
    )

    fun setConfig(config: CircularBufferConfig)
    fun reset()
    fun resume()
    fun pause()
    fun stop()
    fun sendBufferData(data: Int)
}

class CircularBufferServiceImpl(
    private val bluetoothConnection: BluetoothConnection
) : CircularBufferService {

    override var id = ID.incrementAndGet()

    private lateinit var _config: CircularBufferConfig
    override val config: CircularBufferConfig
        get() = _config

    private var wasStopped = false

    override fun connect(
        macAddress: String,
        serviceUuid: String,
        listener: CircularBufferService.Listener
    ) {
        wasStopped = false
        bluetoothConnection.start(macAddress, serviceUuid) {
            when (it) {
                BluetoothConnectionState.CONNECTING -> listener.onBufferServiceState(
                    this,
                    CircularBufferState.CONNECTING
                )
                BluetoothConnectionState.CONNECTED -> {
                    awaitFreeHeap(listener)
                    listenForSignals(listener)
                }
                BluetoothConnectionState.GENERIC_ERROR -> listener.onBufferServiceError(
                    this,
                    java.lang.IllegalStateException("Not found")
                )
                BluetoothConnectionState.UNAVAILABLE -> listener.onBufferServiceError(
                    this,
                    IllegalStateException("Connection unavailable")
                )
                BluetoothConnectionState.DISABLED -> listener.onBufferServiceError(
                    this,
                    IllegalStateException("Connection disabled")
                )
                BluetoothConnectionState.CONNECTION_ERROR -> listener.onBufferServiceError(
                    this,
                    IllegalStateException("Connection error")
                )
            }
        }
    }

    override fun setConfig(config: CircularBufferConfig) {
        _config = config

        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(config.numRefills)

        val refillSizeBytes = config.refillSize.toByteArray()
        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(refillSizeBytes[0].toInt())
        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(refillSizeBytes[1].toInt())
        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(refillSizeBytes[2].toInt())
        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(refillSizeBytes[3].toInt())

        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(config.windowSizeMs)

        val maxUnderflowsBytes = config.maxUnderflows.toByteArray()
        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(maxUnderflowsBytes[0].toInt())
        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(maxUnderflowsBytes[1].toInt())
        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(maxUnderflowsBytes[2].toInt())
        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(maxUnderflowsBytes[3].toInt())
    }

    override fun reset() = transmitCommand(SIGNAL_OUT_COMMAND_RESET)

    override fun resume() = transmitCommand(SIGNAL_OUT_COMMAND_RESUME)

    override fun pause() = transmitCommand(SIGNAL_OUT_COMMAND_PAUSE)

    override fun stop() {
        wasStopped = true
        bluetoothConnection.stop()
    }

    override fun sendBufferData(data: Int) =
        transmit(
            if (data == SIGNAL_OUT_COMMAND) {
                data - 1
            } else {
                data
            }
        )

    private fun listenForSignals(listener: CircularBufferService.Listener) =
        try {
            while (bluetoothConnection.isConnected) {
                when (val value = bluetoothConnection.inputStream.read()) {
                    SIGNAL_IN_READY -> listener.onBufferServiceState(
                        this,
                        CircularBufferState.READY
                    )
                    SIGNAL_IN_PAUSED -> listener.onBufferServiceState(
                        this,
                        CircularBufferState.PAUSED
                    )
                    SIGNAL_IN_RESUMED -> listener.onBufferServiceState(
                        this,
                        CircularBufferState.RESUMED
                    )
                    SIGNAL_IN_REQUEST_REFILL -> listener.onBufferServiceState(
                        this,
                        CircularBufferState.REFILL
                    )
                    SIGNAL_IN_UNDERFLOW -> listener.onBufferServiceState(
                        this,
                        CircularBufferState.UNDERFLOW
                    )
                    else -> throw IllegalArgumentException("Invalid SIGNAL_IN: $value")
                }
            }
        } catch (error: IOException) {
            if (wasStopped) {
                listener.onBufferServiceState(this, CircularBufferState.DISCONNECTED)
            } else {
                stop()
                listener.onBufferServiceError(this, error)
            }
        }

    private fun awaitFreeHeap(listener: CircularBufferService.Listener) {
        transmitCommand(SIGNAL_OUT_COMMAND_CONNECT)
        val freeRamBytes = bluetoothConnection.inputStream.readInt()
        listener.onBufferServiceFreeHeap(this, freeRamBytes)
    }

    private fun transmit(data: Int) {
        if (bluetoothConnection.isConnected) {
            bluetoothConnection.outputStream.write(data)
        }
    }

    private fun transmitCommand(command: Int) {
        transmit(SIGNAL_OUT_COMMAND)
        transmit(command)
    }

    companion object {
        private const val SIGNAL_IN_READY = 0
        private const val SIGNAL_IN_PAUSED = 1
        private const val SIGNAL_IN_RESUMED = 2
        private const val SIGNAL_IN_REQUEST_REFILL = 3
        private const val SIGNAL_IN_UNDERFLOW = 4

        private const val SIGNAL_OUT_COMMAND = 255
        private const val SIGNAL_OUT_COMMAND_CONNECT = 2
        private const val SIGNAL_OUT_COMMAND_RESET = 3
        private const val SIGNAL_OUT_COMMAND_CONFIG = 4
        private const val SIGNAL_OUT_COMMAND_RESUME = 5
        private const val SIGNAL_OUT_COMMAND_PAUSE = 6

        private val ID = AtomicInteger(-1)
    }
}
