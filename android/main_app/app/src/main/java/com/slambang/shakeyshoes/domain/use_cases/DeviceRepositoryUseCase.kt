package com.slambang.shakeyshoes.domain.use_cases

import com.slambang.shakeyshoes.domain.BluetoothDeviceDomain

interface DeviceRepositoryUseCase {
    fun peekDevice(deviceId: Int): BluetoothDeviceDomain
    fun popDevice(deviceId: Int): BluetoothDeviceDomain
    fun pushDevice(deviceId: Int)
    fun getAvailableDeviceNames(): List<Pair<Int, String>>
}
