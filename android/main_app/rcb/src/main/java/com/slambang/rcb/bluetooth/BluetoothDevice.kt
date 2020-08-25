package com.slambang.rcb.bluetooth

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class BluetoothDevice private constructor(
    private val scheduler: Scheduler,
    private val bluetoothProvider: BluetoothProvider,
    private val subscriptions: CompositeDisposable
) : BluetoothConnection {

    private var closeLatch: CountDownLatch? = null

    override fun open(
        macAddress: String,
        serviceUuid: String,
        stateObserver: (state: BluetoothConnectionState) -> Unit
    ) {
        closeLatch = CountDownLatch(1)

        scheduler.scheduleDirect {

            stateObserver(BluetoothConnectionState.Connecting)

            when {
                !bluetoothProvider.isBluetoothAvailable -> BluetoothConnectionState.Unavailable
                !bluetoothProvider.isBluetoothEnabled -> BluetoothConnectionState.Disabled
                else -> connectToDevice(macAddress, serviceUuid)
            }.let {
                stateObserver(it)
            }

            closeLatch?.countDown()
        }.also { subscriptions.add(it) }
    }

    override fun close() {
        subscriptions.clear()
        closeLatch?.await(500, TimeUnit.MILLISECONDS)
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
            context: Context,
            scheduler: Scheduler
        ): com.slambang.rcb.bluetooth.BluetoothDevice {

            val subscriptions = CompositeDisposable()
            val rxBluetooth = RxBluetooth(context)
            val provider = BluetoothProviderImpl(rxBluetooth)

            return BluetoothDevice(
                scheduler,
                provider,
                subscriptions
            )
        }
    }
}
