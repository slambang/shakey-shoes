package com.slambang.rcb_service

class RcbStateMapper {

    fun a(signalIn: Int): RcbState? =
        when (signalIn) {
            SIGNAL_IN_READY -> RcbState.READY
            SIGNAL_IN_PAUSED -> RcbState.PAUSED
            SIGNAL_IN_RESUMED -> RcbState.RESUMED
            SIGNAL_IN_REQUEST_REFILL -> RcbState.REFILL
            SIGNAL_IN_UNDERFLOW -> RcbState.UNDERFLOW
            else -> null
        }

    companion object {
        private const val SIGNAL_IN_READY = 0
        private const val SIGNAL_IN_PAUSED = 1
        private const val SIGNAL_IN_RESUMED = 2
        private const val SIGNAL_IN_REQUEST_REFILL = 3
        private const val SIGNAL_IN_UNDERFLOW = 4
    }
}
