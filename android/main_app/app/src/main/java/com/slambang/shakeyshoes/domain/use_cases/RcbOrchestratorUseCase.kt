package com.slambang.shakeyshoes.domain.use_cases

import com.slambang.shakeyshoes.domain.BluetoothDeviceAccuracyDomain
import com.slambang.shakeyshoes.domain.BluetoothDeviceDomain
import com.slambang.shakeyshoes.domain.RcbServiceStatus

interface RcbOrchestratorUseCase {

    val rcbServiceStatusObserver: (BluetoothDeviceDomain) -> Unit
    val rcbServiceAccuracyObserver: (BluetoothDeviceAccuracyDomain) -> Unit

    fun subscribe(
        rcbServiceStatusObserver: (BluetoothDeviceDomain) -> Unit,
        rcbServiceAccuracyObserver: (BluetoothDeviceAccuracyDomain) -> Unit
    )

    fun onRcbStatusUpdate(rcbServiceId: Int, status: RcbServiceStatus)
    fun getAvailableDeviceNames(): List<Pair<Int, String>>
    fun createRcbService(deviceDomainId: Int): BluetoothDeviceDomain
    fun connectBufferService(domainId: Int)
    fun configureRcbService(
        domainId: Int,
        numberOfRefills: Int,
        refillSize: Int,
        windowSizeMs: Int,
        maxUnderflows: Int
    )

    fun setVibrateValue(domainId: Int, vibrateValue: Int)
    fun toggleRcb(domainId: Int, isResumed: Boolean)
    fun resumeRcbService(domainId: Int)
    fun pauseRcbService(domainId: Int)
    fun pauseAllRcbServices()
    fun stopAllRcbServices()
    fun deleteRcbService(deviceDomainId: Int)
    fun deleteAllRcbServices()
}