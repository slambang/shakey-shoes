package com.slambang.rcb_service

interface RcbServiceListener {
    fun onBufferServiceState(rcbService: RcbService, state: RcbState)
    fun onBufferServiceFreeHeap(rcbService: RcbService, freeHeapBytes: Int)
    fun onBufferServiceError(rcbService: RcbService, error: RcbServiceError)
}
