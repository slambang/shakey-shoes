package com.slambang.rcb.service

import com.slambang.rcb.bluetooth.BluetoothConnection
import com.slambang.rcb.bluetooth.BluetoothConnectionState
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class RcbServiceImpl(
    private val bluetoothConnection: BluetoothConnection
) : RcbService {

    override var id = ID.incrementAndGet()

    private lateinit var _config: RcbServiceConfig
    override val config: RcbServiceConfig
        get() = _config

    private var wasStopped = false

    override fun connect(
        macAddress: String,
        serviceUuid: String,
        listener: RcbServiceListener
    ) {
        wasStopped = false
        bluetoothConnection.start(macAddress, serviceUuid) {
            when (it) {
                BluetoothConnectionState.CONNECTING -> listener.onBufferServiceState(
                    this,
                    RcbState.CONNECTING
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

    override fun setConfig(config: RcbServiceConfig) {
        _config = config
        transmitNumRefills(config)
        transmitRefillSize(config)
        transmitWindowSize(config)
        transmitMaxUnderflows(config)
    }

    private fun transmitNumRefills(config: RcbServiceConfig) {
        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(config.numRefills)
    }

    private fun transmitRefillSize(config: RcbServiceConfig) {
        val refillSizeBytes = config.refillSize.toByteArray()
        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(refillSizeBytes[0].toInt())
        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(refillSizeBytes[1].toInt())
        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(refillSizeBytes[2].toInt())
        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(refillSizeBytes[3].toInt())
    }

    private fun transmitWindowSize(config: RcbServiceConfig) {
        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(config.windowSizeMs)
    }

    private fun transmitMaxUnderflows(config: RcbServiceConfig) {
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
            if (data >= SIGNAL_OUT_COMMAND) {
                /*
                 * Buffer data range is 0..254.
                 * 255 is a reserved value used to indicated that the *next*
                 * byte in the stream is a command.
                 * This reduces the buffer payload by 50%, with the trade-off
                 * that a buffer data value of 255 cannot be used.
                 */
                throw IllegalArgumentException("Buffer data value $data too large")
            } else {
                data
            }
        )

    private fun listenForSignals(listener: RcbServiceListener) =
        try {
            while (bluetoothConnection.isConnected) {
                when (val value = bluetoothConnection.inputStream.read()) {
                    SIGNAL_IN_READY -> listener.onBufferServiceState(
                        this,
                        RcbState.READY
                    )
                    SIGNAL_IN_PAUSED -> listener.onBufferServiceState(
                        this,
                        RcbState.PAUSED
                    )
                    SIGNAL_IN_RESUMED -> listener.onBufferServiceState(
                        this,
                        RcbState.RESUMED
                    )
                    SIGNAL_IN_REQUEST_REFILL -> listener.onBufferServiceState(
                        this,
                        RcbState.REFILL
                    )
                    SIGNAL_IN_UNDERFLOW -> listener.onBufferServiceState(
                        this,
                        RcbState.UNDERFLOW
                    )
                    else -> throw IllegalArgumentException("Invalid SIGNAL_IN: $value")
                }
            }
        } catch (error: IOException) {
            if (wasStopped) {
                listener.onBufferServiceState(this,
                    RcbState.DISCONNECTED
                )
            } else {
                stop()
                listener.onBufferServiceError(this, error)
            }
        }

    private fun awaitFreeHeap(listener: RcbServiceListener) {
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
