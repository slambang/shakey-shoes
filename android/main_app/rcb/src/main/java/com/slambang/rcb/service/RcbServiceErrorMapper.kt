package com.slambang.rcb.service

import com.slambang.rcb.bluetooth.BluetoothConnectionState

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
