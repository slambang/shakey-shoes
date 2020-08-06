package com.betty7.rcb

import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

/**
 * This is the start of a mock bluetooth device for testing.
 *
 * Right now all it does is accepts the data coming from the app and
 * echo's it back to the app on the same thread.
 *
 * We do not need to mock the full MCU here, just the essential things the app
 * needs to work.
 *  On connect = send back max heap
 *  On resume = send back "RESUME" value
 *  Need a clean way of mocking the config.
 *      Use a file on disk.
 *      Hard-code the response we want/need. Easy.
 *      Data must be BIG-ENDIAN!
 *
 * Threading:
 *  Mock the MCU "consuming" data from the app every Xms.
 *  Mock The MCU requesting refills.
 */
private class DeviceDataStream : InputStream() {

    internal val queue: Queue<Int> = LinkedList()

    init {
        val array = ByteBuffer
            .allocate(Int.SIZE_BYTES)
            .order(ByteOrder.BIG_ENDIAN)
            .putInt(1234)
            .array()
            .also { it.reverse() }

//        val a: Int = 1
//        a.rev
//        BitSet.valueOf(array).rev

        array.forEach {
            queue.offer(it.toInt())
        }
    }

    override fun read(): Int = queue.remove()

    override fun available(): Int = queue.size
}

private class AppDataStream(private val mockInputStream: DeviceDataStream) : OutputStream() {

    override fun write(b: Int) {
//        mockInputStream.queue.offer(b)
    }
}

class MockBluetoothDevice(private val freeHeap: Int) : BluetoothConnection {

    private val deviceDataStream = DeviceDataStream()
    private val appDataStream = AppDataStream(deviceDataStream)

    private var _isConnected = false
    override val isConnected: Boolean
        get() = _isConnected

    override val inputStream: InputStream
        get() = deviceDataStream

    override val outputStream: OutputStream
        get() = appDataStream

    override fun start(
        macAddress: String,
        serviceUuid: String,
        stateObserver: (state: BluetoothConnectionState) -> Unit
    ) {
        _isConnected = true
        stateObserver(BluetoothConnectionState.CONNECTED)
    }

    override fun stop() {
        _isConnected = false
    }
}
