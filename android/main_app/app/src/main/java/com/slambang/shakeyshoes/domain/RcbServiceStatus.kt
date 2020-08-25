package com.slambang.shakeyshoes.domain

sealed class RcbServiceStatus {

    object Connecting : RcbServiceStatus()

    object NotFound : RcbServiceStatus()

    object Unavailable : RcbServiceStatus()

    object Disabled : RcbServiceStatus()

    data class Setup(
        val freeHeapBytes: Int
    ) : RcbServiceStatus()

    object Ready : RcbServiceStatus()

    object Refill : RcbServiceStatus()

    object Underflow : RcbServiceStatus()

    object Paused : RcbServiceStatus()

    object Resumed : RcbServiceStatus()

    object Disconnected : RcbServiceStatus()

    data class Error (
        val cause: Throwable?
    ) : RcbServiceStatus()

    object Unknown : RcbServiceStatus()
}
