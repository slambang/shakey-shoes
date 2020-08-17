package com.betty7.fingerband.alpha.bluetooth.domain

import com.betty7.fingerband.alpha.bluetooth.data.entity.BluetoothDeviceRepository
import java.lang.IllegalArgumentException

class DeviceRepositoryInteractor(
    private val deviceRepo: BluetoothDeviceRepository,
    private val entityMapper: BluetoothDeviceEntityMapper,
    private val unavailableDevices: MutableSet<Int> = mutableSetOf()
) {

    fun getDevice(deviceId: Int): DeviceDomain {
        val device = deviceRepo.getDeviceEntity(deviceId)
        setDeviceAvailability(device.id, false)
        return entityMapper.map(device)
    }

    // Hmm... naming
    fun takeDevice(deviceId: Int): DeviceDomain {
        val device = deviceRepo.getDeviceEntity(deviceId)
        setDeviceAvailability(device.id, false)
        return entityMapper.map(device)
    }

    fun returnDevice(deviceId: Int) =
        setDeviceAvailability(deviceId, true)

    fun getAvailableDeviceNames(): List<Pair<Int, String>> =
        deviceRepo.getDeviceEntities()
            .filter { !unavailableDevices.contains(it.id) }
            .map { entityMapper.map(it) }
            .map { Pair(it.id, it.name) }

    private fun setDeviceAvailability(deviceId: Int, isAvailable: Boolean) {
        val result = if (isAvailable) {
            unavailableDevices.remove(deviceId)
        } else {
            unavailableDevices.add(deviceId)
        }
        if (!result)
            throw IllegalArgumentException("Error setting device availability $isAvailable for device $deviceId")
    }
}
