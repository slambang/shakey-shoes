package com.betty7.fingerband.alpha.bluetooth.domain

import com.betty7.fingerband.alpha.bluetooth.data.entity.BluetoothDeviceRepository

class DeviceRepositoryInteractor(
    private val deviceRepo: BluetoothDeviceRepository,
    private val entityMapper: BluetoothDeviceEntityMapper
) {

    fun getDeviceDomain(deviceId: Int): DeviceDomain {
        val device = deviceRepo.getDeviceEntity(deviceId)
        return entityMapper.map(device)
    }

    fun getAllDeviceNames(): List<String> {
        val deviceEntities = deviceRepo.getDeviceEntities()
        val deviceDomains =  entityMapper.map(deviceEntities)
        return deviceDomains.map { it.name }
    }
}
