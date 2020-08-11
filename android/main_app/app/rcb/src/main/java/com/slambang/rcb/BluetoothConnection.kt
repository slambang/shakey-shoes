package com.slambang.rcb

import java.io.InputStream
import java.io.OutputStream

interface BluetoothConnection {

    val isConnected: Boolean
    val inputStream: InputStream
    val outputStream: OutputStream

    fun start(
        macAddress: String,
        serviceUuid: String,
        stateObserver: (state: BluetoothConnectionState) -> Unit
    )

    fun stop()
}
