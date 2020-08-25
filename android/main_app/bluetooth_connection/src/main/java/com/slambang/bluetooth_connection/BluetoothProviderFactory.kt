package com.slambang.bluetooth_connection

import android.content.Context
import com.github.ivbaranov.rxbluetooth.RxBluetooth

// Hides the RxBluetooth dependency from apps
class BluetoothProviderFactory {

    companion object {
        fun newInstance(context: Context): BluetoothProvider =
            BluetoothProviderImpl(RxBluetooth((context)))
    }
}
