package com.slambang.shakeyshoes.entity

import javax.inject.Inject

interface BluetoothDeviceRepository {
    fun getDeviceEntities(): List<BluetoothDeviceEntity>
    fun getDeviceEntity(deviceId: Int): BluetoothDeviceEntity
}

/*
 * A cheap repo.
 * The project does not yet support automatic discovery of Bluetooth devices.
 * For now we hardcode each known device and its properties.
 */
class BluetoothDeviceRepositoryImpl @Inject constructor() : BluetoothDeviceRepository {

    override fun getDeviceEntities() = ALL_KNOWN_DEVICES

    override fun getDeviceEntity(deviceId: Int): BluetoothDeviceEntity =
        ALL_KNOWN_DEVICES.firstOrNull { it.id == deviceId }
            ?: throw IllegalArgumentException("Invalid Bluetooth entity id: $deviceId")

    companion object {
        private val ALL_KNOWN_DEVICES = listOf(
            BluetoothDeviceEntity.Hc05DsdMk0,
            BluetoothDeviceEntity.Hc05DsdMk1,
            BluetoothDeviceEntity.Hc05Wingoneer
        )
    }
}
