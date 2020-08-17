package com.betty7.fingerband.alpha.bluetooth.view

import com.betty7.fingerband.alpha.bluetooth.domain.DeviceRepositoryInteractor

class ProductUrlMapper(
    private val deviceRepoInteractor: DeviceRepositoryInteractor
) {

    fun map(deviceId: Int) =
        deviceRepoInteractor.getDevice(deviceId).productUrl
}
