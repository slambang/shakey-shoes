package com.slambang.shakeyshoes.domain

data class BluetoothDeviceDomain(
    val id: Int,
    val name: String = "",
    val macAddress: String = "",
    val pairingPin: String = "",
    val serviceUuid: String = "",
    val productUrl: String = "",
    val baudRateBytes: Int = 0,
    var freeHeapBytes: Int = 0,
    var status: RcbServiceStatus = RcbServiceStatus.Disconnected,
    val accuracies: BluetoothDeviceAccuracyDomain = BluetoothDeviceAccuracyDomain(id)
)

data class BluetoothDeviceAccuracyDomain(
    val id: Int,
    var refillCount: Int = 0,
    var underflowCount: Int = 0
)
