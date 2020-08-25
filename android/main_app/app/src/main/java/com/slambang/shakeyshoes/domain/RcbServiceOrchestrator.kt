package com.slambang.shakeyshoes.domain

interface RcbServiceOrchestrator {

    fun subscribe(rcbServiceStatusObserver: (Int, RcbServiceStatus) -> Unit)

    fun createRcbService(): Int
    fun removeRcbService(rcbServiceId: Int)

    fun connectRcbService(rcbServiceId: Int, macAddress: String, serviceUuid: String)
    fun configureRcbService(
        rcbServiceId: Int,
        numberOfRefills: Int,
        refillSize: Int,
        windowSizeMs: Int,
        maxUnderflows: Int
    )

    fun startRcbService(rcbServiceId: Int)
    fun resumeRcbService(rcbServiceId: Int)
    fun pauseRcbService(rcbServiceId: Int)
    fun stopRcbService(rcbServiceId: Int)

    fun hackBufferValue(rcbServiceId: Int, value: Int)
}
