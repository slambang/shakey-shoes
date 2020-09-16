package com.slambang.shakeyshoes.entity

sealed class BluetoothDeviceEntity(
    val id: Int,
    val macAddress: String,
    val serviceUuid: String,
    val baudRateBits: Int,
    val pairingPin: String
) {

    object Hc05DsdMk0 : BluetoothDeviceEntity(
        id = 0,
        macAddress = "00:14:03:05:08:60",
        serviceUuid = SPP_SERVICE_UUID,
        baudRateBits = 9600,
        pairingPin = "1234"
    )

    object Hc05DsdMk1 : BluetoothDeviceEntity(
        id = 1,
        macAddress = "00:14:03:05:FF:88",
        serviceUuid = SPP_SERVICE_UUID,
        baudRateBits = 9600,
        pairingPin = "1234"
    )

    object Hc05Wingoneer : BluetoothDeviceEntity(
        id = 2,
        macAddress = "98:D3:B1:FD:49:D0",
        serviceUuid = SPP_SERVICE_UUID,
        baudRateBits = 9600,
        pairingPin = "1234"
    )

    companion object {
        // http://sviluppomobile.blogspot.com/2012/11/bluetooth-services-uuids.html
        private const val SPP_SERVICE_UUID = "00001101-0000-1000-8000-00805F9B34FB"
    }
}
