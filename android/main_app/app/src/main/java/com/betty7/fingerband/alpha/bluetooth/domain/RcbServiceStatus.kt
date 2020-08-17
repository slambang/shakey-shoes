package com.betty7.fingerband.alpha.bluetooth.domain

sealed class RcbServiceStatus {

    object Disconnected : RcbServiceStatus()

    object Connecting : RcbServiceStatus()

    data class Setup(
        val freeHeapBytes: Int
    ) : RcbServiceStatus()

    object Ready : RcbServiceStatus()

    object Paused : RcbServiceStatus()

    object Resumed : RcbServiceStatus()

    data class Error (
        val message: String
    ) : RcbServiceStatus()
}
