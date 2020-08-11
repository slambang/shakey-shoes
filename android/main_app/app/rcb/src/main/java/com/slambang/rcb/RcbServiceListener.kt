package com.slambang.rcb

interface RcbServiceListener {
    fun onBufferServiceState(rcbService: RcbService, state: RcbState)
    fun onBufferServiceFreeHeap(rcbService: RcbService, freeHeapBytes: Int)
    fun onBufferServiceError(rcbService: RcbService, error: Throwable? = null)
}
