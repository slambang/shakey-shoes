package com.slambang.rcb_service

import com.slambang.bluetooth_connection.BluetoothConnectionState

interface RcbServiceErrorMapper {

    fun map(bluetoothConnectionState: BluetoothConnectionState): RcbServiceState.Error
}

class RcbServiceErrorMapperImpl : RcbServiceErrorMapper {

    override fun map(bluetoothConnectionState: BluetoothConnectionState): RcbServiceState.Error =
        when (bluetoothConnectionState) {
            is BluetoothConnectionState.NotFound -> RcbServiceState.Error.NotFound
            is BluetoothConnectionState.Unavailable -> RcbServiceState.Error.Unavailable
            is BluetoothConnectionState.Disabled -> RcbServiceState.Error.Disabled
            is BluetoothConnectionState.Error -> RcbServiceState.Error.Generic(bluetoothConnectionState.cause)
            else -> RcbServiceState.Error.Unknown
        }
}
