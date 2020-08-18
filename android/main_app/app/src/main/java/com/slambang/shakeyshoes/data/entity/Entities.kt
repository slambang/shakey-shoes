package com.slambang.shakeyshoes.data.entity

sealed class BluetoothDeviceEntity(
    val id: Int,
    val macAddress: String,
    val serviceUuid: String,
    val baudRateBits: Int,
    val pairingPin: String
) {

    object Hc05DsdMk0 : BluetoothDeviceEntity(
        0,
        "00:14:03:05:08:60",
        SPP_SERVICE_UUID,
        9600,
        "1234"
    )

    object Hc05DsdMk1 : BluetoothDeviceEntity(
        1,
        "00:14:03:05:FF:88",
        SPP_SERVICE_UUID,
        9600,
        "1234"
    )

    object Hc05Wingoneer : BluetoothDeviceEntity(
        2,
        "98:D3:B1:FD:49:D0",
        SPP_SERVICE_UUID,
        9600,
        "1234"
    )

    companion object {
        // http://sviluppomobile.blogspot.com/2012/11/bluetooth-services-uuids.html
        private const val SPP_SERVICE_UUID = "00001101-0000-1000-8000-00805F9B34FB"
    }
}
