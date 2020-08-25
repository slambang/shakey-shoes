package com.slambang.shakeyshoes.view.rcb.mappers

import com.slambang.shakeyshoes.domain.use_cases.DeviceRepositoryUseCase
import javax.inject.Inject

class ProductUrlMapper @Inject constructor(
    private val deviceRepoUseCase: DeviceRepositoryUseCase
) {

    fun map(deviceId: Int) =
        deviceRepoUseCase.peekDevice(deviceId).productUrl
}
