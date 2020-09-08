package com.slambang.bluetooth_connection

import android.bluetooth.BluetoothDevice
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.*

class BluetoothConnectionImpl private constructor(
    private val scheduler: Scheduler,
    private val bluetoothProvider: BluetoothProvider,
    private val subscriptions: CompositeDisposable
) : BluetoothConnection {

    /*
     * This could be better: convert to Rx.
     * This removes the `Scheduler` and `CompositeDisposable` dependency.
     * This also allows us to subscribe to `BluetoothProvider.bluetoothState` and emit events.
     * Then the ViewModel can subscribe as usual.
     *
     * Note: This may introduce performance overhead. Do this change later, after more refactoring!
     */
    override fun open(
        macAddress: String,
        serviceUuid: String,
        stateObserver: (state: BluetoothConnectionState) -> Unit
    ) {
        scheduler.scheduleDirect {

            stateObserver(BluetoothConnectionState.Connecting)

            when {
                !bluetoothProvider.isBluetoothAvailable -> BluetoothConnectionState.Unavailable
                !bluetoothProvider.isBluetoothEnabled -> BluetoothConnectionState.Disabled
                else -> connectToDevice(macAddress, serviceUuid)
            }.let {
                stateObserver(it)
            }
        }.also { subscriptions.add(it) }
    }

    override fun close() {
        subscriptions.clear()
    }

    private fun connectToDevice(
        macAddress: String,
        serviceUuid: String
    ): BluetoothConnectionState {

        val device = bluetoothProvider.bondedDevices?.firstOrNull {
            it.address == macAddress
        } ?: return BluetoothConnectionState.NotFound

        return createSocket(serviceUuid, device)
    }

    private fun createSocket(
        serviceUuid: String,
        device: BluetoothDevice
    ): BluetoothConnectionState =
        try {
            val uuid = UUID.fromString(serviceUuid)
            device.createInsecureRfcommSocketToServiceRecord(uuid)?.let {
                it.connect()
                BluetoothConnectionState.Connected(it)
            } ?: throw IllegalStateException("Unable to create Bluetooth socket")
        } catch (error: Throwable) {
            BluetoothConnectionState.Error(error)
        }

    companion object {

        fun newInstance(
            scheduler: Scheduler,
            bluetoothProvider: BluetoothProvider,
            subscriptions: CompositeDisposable
        ) : BluetoothConnection = BluetoothConnectionImpl(
            scheduler,
            bluetoothProvider,
            subscriptions
        )
    }
}
