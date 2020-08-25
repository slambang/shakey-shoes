package com.slambang.bluetooth_connection

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.*

class BluetoothConnectionImpl private constructor(
    private val scheduler: Scheduler,
    private val bluetoothProvider: BluetoothProvider,
    private val subscriptions: CompositeDisposable
) : BluetoothConnection {

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
            // Bug: If we close the stream while still creating the socket, we land here
            // Which calls all the way back to the observer. Observer has already removed the RCB service
            BluetoothConnectionState.Error(error)
        }

    companion object {
        fun newInstance(
            context: Context,
            scheduler: Scheduler
        ): BluetoothConnection {

            val subscriptions = CompositeDisposable()
            val rxBluetooth = RxBluetooth(context)
            val provider = BluetoothProviderImpl(rxBluetooth)

            return BluetoothConnectionImpl(
                scheduler,
                provider,
                subscriptions
            )
        }
    }
}
