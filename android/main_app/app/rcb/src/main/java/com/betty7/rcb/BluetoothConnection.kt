package com.betty7.rcb

import java.io.InputStream
import java.io.OutputStream

enum class BluetoothConnectionState {
    UNAVAILABLE,
    DISABLED,
    CONNECTION_ERROR,
    CONNECTING,
    CONNECTED,
    GENERIC_ERROR
}

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
