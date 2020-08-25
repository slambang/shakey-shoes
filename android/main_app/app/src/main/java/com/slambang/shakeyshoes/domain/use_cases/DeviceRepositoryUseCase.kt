package com.slambang.shakeyshoes.domain.use_cases

import com.slambang.shakeyshoes.entity.BluetoothDeviceEntityMapper
import com.slambang.shakeyshoes.domain.BluetoothDeviceDomain
import com.slambang.shakeyshoes.entity.BluetoothDeviceRepository
import java.lang.IllegalArgumentException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepositoryUseCase @Inject constructor(
    private val deviceRepo: BluetoothDeviceRepository,
    private val entityMapper: BluetoothDeviceEntityMapper,
    private val reservedDevices: MutableSet<Int>
) { // Add interface

    fun peekDevice(deviceId: Int): BluetoothDeviceDomain {
        val device = deviceRepo.getDeviceEntity(deviceId)
        return entityMapper.map(device)
    }

    fun popDevice(deviceId: Int): BluetoothDeviceDomain {
        val device = deviceRepo.getDeviceEntity(deviceId)
        setDeviceReserved(device.id, true)
        return entityMapper.map(device)
    }

    fun pushDevice(deviceId: Int) =
        setDeviceReserved(deviceId, false)

    fun getAvailableDeviceNames(): List<Pair<Int, String>> =
        deviceRepo.getDeviceEntities()
            .filter { !reservedDevices.contains(it.id) }
            .map { entityMapper.map(it) }
            .map { Pair(it.id, it.name) }

    private fun setDeviceReserved(deviceId: Int, isReserved: Boolean) {
        val result = if (isReserved) {
            reservedDevices.add(deviceId)
        } else {
            reservedDevices.remove(deviceId)
        }
        if (!result)
            throw IllegalArgumentException("Error setting device reserved $isReserved for device $deviceId")
    }
}
