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
                    RcbServiceState.Connecting
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
        transmitConfigByte(config.numRefills)
        transmitConfigInt(config.refillSize)
        transmitConfigByte(config.windowSizeMs)
        transmitConfigInt(config.maxUnderflows)
    }

    private fun transmitConfigInt(value: Int) {
        value.toByteArray().forEach {
            transmitConfigByte(it.toInt())
        }
    }

    private fun transmitConfigByte(value: Int) {
        transmitCommand(SIGNAL_OUT_COMMAND_CONFIG)
        transmit(value)
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

    /*
     * `data` must be in the range 0..254. 255 is reserved as a command prefix.
     *
     * When streaming buffer (not command) data, we need the transfer to be as fast
     * as possible. In Android we do not have the ability to make efficient use of
     * packets/sizes, so the most we can do is reduce the payload as much as possible.
     *
     * If we did not have this reserved value then we would need to prefix all buffer & command
     * values with some other value so the service knows what to do with the next data value
     * (command or buffer). Or, we only prefix command data (with the value 255), which the
     * service checks for each time it receives any data. If it receives 255, it knows
     * a command value is inbound. Otherwise it will treat what it receives as buffer data.
     *
     * This trick reduces the buffer data payload by 50% at the cost of reserving 1 value.
     */
    override fun sendBufferData(data: Int) =
        transmit(
            validateData(data)
        )

    private fun validateData(data: Int): Int =
        if (data < DATA_MINIMUM_VALUE || data >= SIGNAL_OUT_COMMAND_PREFIX) {
            throw IllegalArgumentException("Illegal data value $data")
        } else {
            data
        }

    private fun listenForSignals(listener: RcbServiceListener) =
        try {
            while (requireSocket().isConnected) {
                waitForSignal(listener)
            }
        } catch (error: IOException) {
            if (wasStopped) {
                listener.onBufferServiceState(this, RcbServiceState.Disconnected)
            } else {
                stop()
                listener.onBufferServiceError(this, RcbServiceState.Error.Generic(error))
            }
        }

    private fun waitForSignal(listener: RcbServiceListener) {

        val signal = requireSocket().inputStream.readByte()

        when (val mapped = stateMapper.map(signal)) {
            RcbServiceState.Unknown -> throw IllegalArgumentException("Unknown signal value $signal")
            else -> listener.onBufferServiceState(this, mapped)
        }
    }

    private fun awaitFreeHeap(listener: RcbServiceListener) {
        transmitCommand(SIGNAL_OUT_COMMAND_CONNECT)
        val freeRamBytes = requireSocket().inputStream.readInt()
        listener.onBufferServiceFreeHeap(this, freeRamBytes)
    }

    private fun transmit(data: Int) =
        requireSocket().outputStream.write(data)

    private fun transmitCommand(command: Int) {
        transmit(SIGNAL_OUT_COMMAND_PREFIX)
        transmit(command)
    }

    private fun requireSocket() =
        bluetoothSocket ?: throw IllegalStateException("Required Bluetooth socket")

    companion object {
        private val ID = AtomicInteger(-1)

        private const val DATA_MINIMUM_VALUE = 0
        private const val SIGNAL_OUT_COMMAND_PREFIX = 255
        private const val SIGNAL_OUT_COMMAND_CONNECT = 2
        private const val SIGNAL_OUT_COMMAND_RESET = 3
        private const val SIGNAL_OUT_COMMAND_CONFIG = 4
        private const val SIGNAL_OUT_COMMAND_RESUME = 5
        private const val SIGNAL_OUT_COMMAND_PAUSE = 6
    }
}
