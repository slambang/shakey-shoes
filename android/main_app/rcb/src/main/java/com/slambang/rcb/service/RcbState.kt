package com.slambang.rcb.service

// Convert to sealed class?
enum class RcbState {
    CONNECTING,
    READY,
    PAUSED,
    RESUMED,
    DISCONNECTED,
    REFILL,
    UNDERFLOW
}
