package com.betty7.fingerband.alpha.bluetooth.domain

interface RcbServiceOrchestrator {

    fun subscribe(
        rcbServiceStatusObserver: (DeviceDomain) -> Unit,
        rcbServiceAccuracyObserver: (DeviceAccuracyDomain) -> Unit
    )

    fun createBufferService(): Int
    fun beginBufferService(rcbServiceId: Int)
    fun deleteBufferService(rcbServiceId: Int)

    fun connectBufferService(rcbServiceId: Int, deviceDomain: DeviceDomain)
    fun configureBufferService(
        rcbServiceId: Int,
        numberOfRefills: Int,
        refillSize: Int,
        windowSizeMs: Int,
        maxUnderflows: Int
    )

    fun startBufferService(rcbServiceId: Int)
    fun resumeBufferService(rcbServiceId: Int)
    fun pauseBufferService(rcbServiceId: Int)
    fun stopBufferService(rcbServiceId: Int)

    fun hackBufferValue(rcbServiceId: Int, value: Int)
}
