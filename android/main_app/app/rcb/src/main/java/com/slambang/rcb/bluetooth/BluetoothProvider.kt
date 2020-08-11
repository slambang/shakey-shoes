package com.slambang.rcb.bluetooth

import android.bluetooth.BluetoothDevice
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
