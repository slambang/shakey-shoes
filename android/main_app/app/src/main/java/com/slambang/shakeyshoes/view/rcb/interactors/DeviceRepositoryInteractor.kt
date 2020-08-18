package com.slambang.shakeyshoes.view.rcb.interactors

import com.slambang.shakeyshoes.domain.BluetoothDeviceEntityMapper
import com.slambang.shakeyshoes.data.entity.BluetoothDeviceRepository
import com.slambang.shakeyshoes.domain.BluetoothDeviceDomain
import java.lang.IllegalArgumentException
import javax.inject.Inject

class DeviceRepositoryInteractor @Inject constructor(
    private val deviceRepo: BluetoothDeviceRepository,
    private val entityMapper: BluetoothDeviceEntityMapper,
    private val reservedDevices: MutableSet<Int>
) {

    fun getDevice(deviceId: Int): BluetoothDeviceDomain {
        val device = deviceRepo.getDeviceEntity(deviceId)
        setDeviceReserved(device.id, false)
        return entityMapper.map(device)
    }

    fun reserveDevice(deviceId: Int): BluetoothDeviceDomain {
        val device = deviceRepo.getDeviceEntity(deviceId)
        setDeviceReserved(device.id, false)
        return entityMapper.map(device)
    }

    fun returnDevice(deviceId: Int) =
        setDeviceReserved(deviceId, true)

    fun getAvailableDeviceNames(): List<Pair<Int, String>> =
        deviceRepo.getDeviceEntities()
            .filter { !reservedDevices.contains(it.id) }
            .map { entityMapper.map(it) }
            .map { Pair(it.id, it.name) }

    private fun setDeviceReserved(deviceId: Int, isReserved: Boolean) {
        val result = if (isReserved) {
            reservedDevices.remove(deviceId)
        } else {
            reservedDevices.add(deviceId)
        }
        if (!result)
            throw IllegalArgumentException("Error setting device reserved $isReserved for device $deviceId")
    }
}
