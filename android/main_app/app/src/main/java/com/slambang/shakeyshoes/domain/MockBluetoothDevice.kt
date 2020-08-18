package com.slambang.shakeyshoes.domain

import com.slambang.rcb.bluetooth.BluetoothConnection
import com.slambang.rcb.bluetooth.BluetoothConnectionState
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.io.InputStream
import java.io.OutputStream

/**
 * WIP
 * The idea here is to have mocked input/output streams to act as an Arduino device.
 * This class will vibrate the user's device instead.
 */
class MockBluetoothDevice(
    private val scheduler: Scheduler,
    private val subscriptions: CompositeDisposable
) : BluetoothConnection {

    private var isMockConnected = false

    override val isConnected: Boolean
        get() = isMockConnected

    override val inputStream: InputStream
        get() = TODO("Not yet implemented")

    override val outputStream: OutputStream
        get() = TODO("Not yet implemented")

    override fun start(
        macAddress: String,
        serviceUuid: String,
        stateObserver: (state: BluetoothConnectionState) -> Unit
    ) {
        scheduler.scheduleDirect {
            stateObserver(BluetoothConnectionState.CONNECTING)
            Thread.sleep(2000)
            stateObserver(BluetoothConnectionState.CONNECTED)
        }.also { subscriptions.add(it) }
    }

    override fun stop() {
        isMockConnected = false
    }
}
