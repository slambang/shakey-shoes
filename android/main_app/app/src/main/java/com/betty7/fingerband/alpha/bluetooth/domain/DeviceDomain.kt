package com.betty7.fingerband.alpha.bluetooth.domain

data class DeviceDomain(
    val id: Int,
    val name: String = "",
    val macAddress: String = "",
    val pairingPin: String = "",
    val serviceUuid: String = "",
    val productUrl: String = "",
    val baudRateBytes: Int = 0,
    var freeHeapBytes: Int = 0,
    var status: RcbServiceStatus = RcbServiceStatus.Disconnected,
    val accuracies: DeviceAccuracyDomain = DeviceAccuracyDomain(id)
)

data class DeviceAccuracyDomain(
    val id: Int,
    var refillCount: Int = 0,
    var underflowCount: Int = 0
)
