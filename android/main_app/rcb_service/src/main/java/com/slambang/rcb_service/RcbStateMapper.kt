package com.slambang.rcb_service

class RcbStateMapper {

    fun map(signalIn: Int): RcbServiceState =
        when (signalIn) {
            SIGNAL_IN_READY -> RcbServiceState.Ready
            SIGNAL_IN_PAUSED -> RcbServiceState.Paused
            SIGNAL_IN_RESUMED -> RcbServiceState.Resumed
            SIGNAL_IN_REQUEST_REFILL -> RcbServiceState.Refill
            SIGNAL_IN_UNDERFLOW -> RcbServiceState.Underflow
            else -> RcbServiceState.Unknown
        }

    companion object {
        private const val SIGNAL_IN_READY = 0
        private const val SIGNAL_IN_PAUSED = 1
        private const val SIGNAL_IN_RESUMED = 2
        private const val SIGNAL_IN_REQUEST_REFILL = 3
        private const val SIGNAL_IN_UNDERFLOW = 4
    }
}
