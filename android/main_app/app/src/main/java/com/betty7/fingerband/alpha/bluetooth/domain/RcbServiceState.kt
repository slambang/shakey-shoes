package com.betty7.fingerband.alpha.bluetooth.domain

enum class RcbServiceState {
    DISCONNECTED,
    CONNECTING,
    SETUP,
    READY,
    PAUSED,
    RESUMED,
    ERROR;

    var message: String? = null

    fun with(message: String): RcbServiceState {
        this.message = message
        return this
    }
}
