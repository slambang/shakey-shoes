package com.slambang.rcb_service

import android.bluetooth.BluetoothSocket
import com.slambang.bluetooth_connection.BluetoothConnection
import com.slambang.bluetooth_connection.BluetoothConnectionState
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class RcbServiceImpl(
    private val bluetoothConnection: BluetoothConnection,
    private val errorMapper: RcbServiceErrorMapper,
    private val stateMapper: RcbStateMapper
) : RcbService {

    override var id = ID.incrementAndGet() // Hmm...

    private var wasStopped = false

    private var bluetoothSocket: BluetoothSocket? = null

    override fun connect(
        macAddress: String,
        serviceUuid: String,
        listener: RcbServiceListener
    ) {
        wasStopped = false
        bluetoothConnection.open(macAddress, serviceUuid) {
            when (it) {
                is BluetoothConnectionState.Connecting -> listener.onBufferServiceState(
                    this,
                    RcbState.CONNECTING
                )
                is BluetoothConnectionState.Connected -> {
                    bluetoothSocket = it.bluetoothSocket
                    awaitFreeHeap(listener)
                    listenForSignals(listener)
                }
                else -> listener.onBufferServiceError(this, errorMapper.map(it))
            }
        }
    }

    override fun transmitConfig(config: RcbServiceConfig) {
        transmitByte(config.numRefills)
        transmitInt(config.refillSize)
        transmitByte(config.windowSizeMs)
        transmitInt(config.maxUnderflows)
    }

    private fun transmitByte(value: Int) {
        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(value)
    }

    private fun transmitInt(value: Int) {
        val byteArray = value.toByteArray()
        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(byteArray[0].toInt())
        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(byteArray[1].toInt())
        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(byteArray[2].toInt())
        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(byteArray[3].toInt())
    }

    override fun reset() = transmitCommand(SIGNAL_OUT_COMMAND_RESET)

    override fun resume() = transmitCommand(SIGNAL_OUT_COMMAND_RESUME)

    override fun pause() = transmitCommand(SIGNAL_OUT_COMMAND_PAUSE)

    override fun stop() {
        wasStopped = true
        bluetoothSocket?.close()
        bluetoothSocket = null
        bluetoothConnection.close()
    }

    override fun sendBufferData(data: Int) =
        transmit(
            if (data == SIGNAL_OUT_COMMAND) {
                data - 1
            } else {
                data
            }
        )

    private fun listenForSignals(listener: RcbServiceListener) =
        try {
            while (requireSocket().isConnected) {
                waitForSignal(listener)
            }
        } catch (error: IOException) {
            if (wasStopped) {
                listener.onBufferServiceState(this, RcbState.DISCONNECTED)
            } else {
                stop()
                listener.onBufferServiceError(this, RcbServiceError.Critical(error))
            }
        }

    private fun waitForSignal(listener: RcbServiceListener) {

        val value = requireSocket().inputStream.read()

        stateMapper.a(value)?.let {
            listener.onBufferServiceState(this, it)
        } ?: throw IllegalArgumentException("Invalid SIGNAL_IN: $value")
    }

    private fun awaitFreeHeap(listener: RcbServiceListener) {
        transmitCommand(SIGNAL_OUT_COMMAND_CONNECT)
        val freeRamBytes = requireSocket().inputStream.readInt()
        listener.onBufferServiceFreeHeap(this, freeRamBytes)
    }

    private fun transmit(data: Int) {
        if (requireSocket().isConnected) {
            requireSocket().outputStream.write(data)
        }
    }

    private fun transmitCommand(command: Int) {
        transmit(SIGNAL_OUT_COMMAND)
        transmit(command)
    }

    private fun requireSocket() =
        bluetoothSocket ?: throw IllegalStateException("Required Bluetooth socket")

    companion object {
        private val ID = AtomicInteger(-1)

        private const val SIGNAL_OUT_COMMAND = 255
        private const val SIGNAL_OUT_COMMAND_CONNECT = 2
        private const val SIGNAL_OUT_COMMAND_RESET = 3
        private const val SIGNAL_OUT_COMMAND_CONFIG = 4
        private const val SIGNAL_OUT_COMMAND_RESUME = 5
        private const val SIGNAL_OUT_COMMAND_PAUSE = 6
    }
}
