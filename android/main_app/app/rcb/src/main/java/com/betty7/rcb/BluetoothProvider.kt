package com.betty7.rcb

import android.bluetooth.BluetoothDevice
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

interface BluetoothProvider {
    val isBluetoothAvailable: Boolean
    val isBluetoothEnabled: Boolean
    val bondedDevices: Set<BluetoothDevice>?
    val bluetoothState: Flowable<Int>

    companion object {
        // http://sviluppomobile.blogspot.com/2012/11/bluetooth-services-uuids.html
        const val SPP_SERVICE_UUID = "00001101-0000-1000-8000-00805F9B34FB"
    }
}

internal class BluetoothProviderImpl(private val rxBluetooth: RxBluetooth) : BluetoothProvider {

    override val isBluetoothAvailable
        get() = rxBluetooth.isBluetoothAvailable

    override val isBluetoothEnabled
        get() = rxBluetooth.isBluetoothEnabled

    override val bondedDevices: Set<BluetoothDevice>?
        get() = rxBluetooth.bondedDevices

    override val bluetoothState: Flowable<Int>
        get() = rxBluetooth.observeConnectionState()
            .map { it.state }
            .toFlowable(BackpressureStrategy.BUFFER)
}
