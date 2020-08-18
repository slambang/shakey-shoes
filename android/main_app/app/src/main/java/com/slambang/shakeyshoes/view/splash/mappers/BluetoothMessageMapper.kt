package com.slambang.shakeyshoes.view.splash.mappers

import com.slambang.shakeyshoes.R
import com.slambang.shakeyshoes.util.StringProvider
import com.slambang.shakeyshoes.view.splash.SplashViewState
import java.lang.IllegalStateException
import javax.inject.Inject

interface BluetoothMessageMapper {
    fun map(isBluetoothAvailable: Boolean, isBluetoothEnabled: Boolean): SplashViewState
}

class BluetoothMessageMapperImpl @Inject constructor(
    private val strings: StringProvider
) : BluetoothMessageMapper {

    @Throws(IllegalStateException::class)
    override fun map(
        isBluetoothAvailable: Boolean,
        isBluetoothEnabled: Boolean
    ) = SplashViewState(
        mapMessage(isBluetoothAvailable, isBluetoothEnabled),
        showPermissionButton = false,
        permissionButtonText = null
    )

    private fun mapMessage(
        isBluetoothAvailable: Boolean,
        isBluetoothEnabled: Boolean
    ) = if (isBluetoothAvailable && isBluetoothEnabled) {
        throw IllegalStateException("Bluetooth is available and enabled!")
    } else if (!isBluetoothAvailable) {
        strings.getString(R.string.bluetooth_not_available)
    } else {
        strings.getString(R.string.bluetooth_not_enabled)
    }
}
