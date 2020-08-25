package com.slambang.shakeyshoes.domain

import android.bluetooth.BluetoothSocket
import com.slambang.bluetooth_connection.BluetoothConnection
import com.slambang.bluetooth_connection.BluetoothConnectionState
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

/**
 * WIP
 * The idea here is to have mocked input/output streams to act as an Arduino device.
 * This class will vibrate the user's device instead.
 */
class MockBluetoothConnection(
    private val scheduler: Scheduler,
    private val subscriptions: CompositeDisposable
) : BluetoothConnection {

    private var isMockConnected = false

    private lateinit var bluetoothSocket: BluetoothSocket

    override fun open(
        macAddress: String,
        serviceUuid: String,
        stateObserver: (state: BluetoothConnectionState) -> Unit
    ) {
        scheduler.scheduleDirect {
            stateObserver(BluetoothConnectionState.Connecting)
            Thread.sleep(2000)
            stateObserver(BluetoothConnectionState.Connected(bluetoothSocket))
        }.also { subscriptions.add(it) }
    }

    override fun close() {
        isMockConnected = false
    }
}
