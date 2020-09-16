package com.slambang.rcb_service

interface RcbServiceListener {
    fun onBufferServiceState(rcbService: RcbService, serviceState: RcbServiceState)
    fun onBufferServiceFreeHeap(rcbService: RcbService, freeHeapBytes: Int)
    fun onBufferServiceError(rcbService: RcbService, error: RcbServiceState.Error)
}
