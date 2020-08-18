package com.slambang.shakeyshoes.view.rcb.mappers

import com.slambang.shakeyshoes.view.rcb.interactors.DeviceRepositoryInteractor
import javax.inject.Inject

class ProductUrlMapper @Inject constructor(
    private val deviceRepoInteractor: DeviceRepositoryInteractor
) {

    fun map(deviceId: Int) =
        deviceRepoInteractor.getDevice(deviceId).productUrl
}
