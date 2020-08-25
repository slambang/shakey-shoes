package com.slambang.rcb.bluetooth

interface BluetoothConnection {

    fun open(
        macAddress: String,
        serviceUuid: String,
        stateObserver: (state: BluetoothConnectionState) -> Unit
    )

    fun close()
}
