package com.slambang.shakeyshoes.view.rcb.mappers

import com.slambang.shakeyshoes.R
import com.slambang.shakeyshoes.domain.BluetoothStatus
import com.slambang.shakeyshoes.util.StringProvider
import javax.inject.Inject

interface BluetoothMessageMapper {
    fun map(bluetoothStatus: BluetoothStatus): String
}

class BluetoothMessageMapperImpl @Inject constructor(
    private val strings: StringProvider
) : BluetoothMessageMapper {

    override fun map(bluetoothStatus: BluetoothStatus) =
        when (bluetoothStatus) {
            BluetoothStatus.ON -> strings.getString(R.string.bluetooth_not_enabled)
            BluetoothStatus.OFF -> strings.getString(R.string.bluetooth_not_disabled)
            BluetoothStatus.UNAVAILABLE -> strings.getString(R.string.bluetooth_not_available)
        }
}
