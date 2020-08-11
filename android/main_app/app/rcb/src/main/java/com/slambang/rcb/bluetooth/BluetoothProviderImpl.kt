package com.slambang.rcb.bluetooth

import android.bluetooth.BluetoothDevice
import com.github.ivbaranov.rxbluetooth.RxBluetooth
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

internal class BluetoothProviderImpl(
    private val rxBluetooth: RxBluetooth
) : BluetoothProvider {

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
