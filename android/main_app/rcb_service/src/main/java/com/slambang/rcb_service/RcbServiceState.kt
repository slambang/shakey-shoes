package com.slambang.rcb_service

sealed class RcbServiceState {

    object Connecting: RcbServiceState()

    object Ready: RcbServiceState()

    object Paused: RcbServiceState()

    object Resumed: RcbServiceState()

    object Disconnected: RcbServiceState()

    object Refill: RcbServiceState()

    object Underflow: RcbServiceState()

    object Unknown: RcbServiceState()

    sealed class Error {

        object NotFound : Error()

        object Unavailable : Error()

        object Disabled : Error()

        data class Generic(
            val cause: Throwable? = null
        ) : Error()

        object Unknown : Error()
    }
}
