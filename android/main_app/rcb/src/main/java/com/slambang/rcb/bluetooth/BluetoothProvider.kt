package com.slambang.rcb.bluetooth

import android.bluetooth.BluetoothDevice
import io.reactivex.Flowable

interface BluetoothProvider {

    val isBluetoothAvailable: Boolean
    val isBluetoothEnabled: Boolean
    val bondedDevices: Set<BluetoothDevice>?
    val bluetoothState: Flowable<Int>

    fun enableBluetooth(): Boolean
}
