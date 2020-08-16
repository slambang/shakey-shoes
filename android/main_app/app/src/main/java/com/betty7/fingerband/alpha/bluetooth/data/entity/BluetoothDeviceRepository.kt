package com.betty7.fingerband.alpha.bluetooth.data.entity

import com.slambang.rcb.bluetooth.BluetoothProvider

interface BluetoothDeviceRepository {
    fun getDeviceEntities(): List<BluetoothDeviceEntity>
    fun getDeviceEntity(deviceId: Int): BluetoothDeviceEntity
}

class BluetoothDeviceRepositoryImpl : BluetoothDeviceRepository {

    override fun getDeviceEntities() = KNOWN_DEVICES

    override fun getDeviceEntity(deviceId: Int): BluetoothDeviceEntity =
        KNOWN_DEVICES.firstOrNull { it.id == deviceId }
            ?: throw IllegalArgumentException("Invalid device id: $deviceId")

    companion object {
        private val KNOWN_DEVICES = listOf(
            HC05_DSD_MK0,
            HC05_DSD_MK1,
            HC05_WINGONEER
        )
    }
}

val HC05_DSD_MK0 =
    BluetoothDeviceEntity(
        0,
        "00:14:03:05:08:60",
        BluetoothProvider.SPP_SERVICE_UUID,
        9600,
        "1234"
    )

val HC05_DSD_MK1 =
    BluetoothDeviceEntity(
        1,
        "00:14:03:05:FF:88",
        BluetoothProvider.SPP_SERVICE_UUID,
        9600,
        "1234"
    )

val HC05_WINGONEER =
    BluetoothDeviceEntity(
        2,
        "98:D3:B1:FD:49:D0",
        BluetoothProvider.SPP_SERVICE_UUID,
        9600,
        "1234"
    )
