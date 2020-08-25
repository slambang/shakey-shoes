package com.slambang.rcb_service

import com.slambang.bluetooth_connection.BluetoothConnectionState

class RcbServiceErrorMapper {

    fun map(bluetoothConnectionState: BluetoothConnectionState): RcbServiceError =
        when (bluetoothConnectionState) {
            is BluetoothConnectionState.NotFound -> RcbServiceError.NotFound
            is BluetoothConnectionState.Unavailable -> RcbServiceError.Unavailable
            is BluetoothConnectionState.Disabled -> RcbServiceError.Disabled
            is BluetoothConnectionState.Error -> RcbServiceError.Critical(bluetoothConnectionState.cause)
            else -> RcbServiceError.Unknown
        }
}
