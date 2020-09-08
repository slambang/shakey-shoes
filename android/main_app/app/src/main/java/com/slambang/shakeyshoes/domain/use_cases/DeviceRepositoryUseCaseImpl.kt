package com.slambang.shakeyshoes.domain.use_cases

import com.slambang.shakeyshoes.domain.BluetoothDeviceDomain
import com.slambang.shakeyshoes.entity.BluetoothDeviceEntityMapper
import com.slambang.shakeyshoes.entity.BluetoothDeviceRepository
import javax.inject.Inject

class DeviceRepositoryUseCaseImpl @Inject constructor(
    private val deviceRepo: BluetoothDeviceRepository,
    private val entityMapper: BluetoothDeviceEntityMapper,
    private val reservedDevices: MutableSet<Int>
) : DeviceRepositoryUseCase {

    override fun peekDevice(deviceId: Int): BluetoothDeviceDomain {
        val device = deviceRepo.getDeviceEntity(deviceId)
        return entityMapper.map(device)
    }

    override fun popDevice(deviceId: Int): BluetoothDeviceDomain {
        val device = deviceRepo.getDeviceEntity(deviceId)
        setDeviceReserved(device.id, true)
        return entityMapper.map(device)
    }

    override fun pushDevice(deviceId: Int) =
        setDeviceReserved(deviceId, false)

    override fun getAvailableDeviceNames(): List<Pair<Int, String>> =
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
