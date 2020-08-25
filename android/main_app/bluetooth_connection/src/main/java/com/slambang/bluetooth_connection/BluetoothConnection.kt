package com.slambang.bluetooth_connection

interface BluetoothConnection {

    fun open(
        macAddress: String,
        serviceUuid: String,
        stateObserver: (state: BluetoothConnectionState) -> Unit
    )

    fun close()
}
