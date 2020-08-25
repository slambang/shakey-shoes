package com.slambang.bluetooth_connection

import android.bluetooth.BluetoothSocket

sealed class BluetoothConnectionState {

    object Connecting : BluetoothConnectionState()

    data class  Connected(
        val bluetoothSocket: BluetoothSocket
    ) : BluetoothConnectionState()

    object NotFound : BluetoothConnectionState()

    object Unavailable : BluetoothConnectionState()

    object Disabled : BluetoothConnectionState()

    data class Error(
        val cause: Throwable? = null
    ) : BluetoothConnectionState()
}
