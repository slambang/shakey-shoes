package com.betty7.fingerband.alpha.bluetooth.entity

import com.betty7.rcb.BluetoothProvider

interface DeviceRepository {
    fun getDeviceEntities(): List<BluetoothDeviceEntity>
    fun getDeviceEntity(deviceId: Int): BluetoothDeviceEntity
}

class DeviceRepositoryImpl :
    DeviceRepository {

    override fun getDeviceEntities() =
        DEVICES

    override fun getDeviceEntity(deviceId: Int): BluetoothDeviceEntity =
        DEVICES.firstOrNull { it.id == deviceId }
            ?: throw IllegalArgumentException("Invalid device id: $deviceId")

    companion object {
        private val DEVICES = listOf(
            HC05_DSD_MK1,
            HC05_WINGONEER,
            HC05_DSD
        )
    }
}

val HC05_DSD_MK1 =
    BluetoothDeviceEntity(
        0,
        "00:14:03:05:FF:88",
        BluetoothProvider.SPP_SERVICE_UUID,
        9600,
        "1234"
    )

val HC05_WINGONEER =
    BluetoothDeviceEntity(
        1,
        "98:D3:B1:FD:49:D0",
        BluetoothProvider.SPP_SERVICE_UUID,
        9600,
        "1234"
    )

val HC05_DSD =
    BluetoothDeviceEntity(
        2,
        "00:14:03:05:08:60",
        BluetoothProvider.SPP_SERVICE_UUID,
        9600,
        "1234"
    )
