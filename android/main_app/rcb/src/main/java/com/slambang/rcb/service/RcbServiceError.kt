package com.slambang.rcb.service

sealed class RcbServiceError {

    object NotFound : RcbServiceError()

    object Unavailable : RcbServiceError()

    object Disabled : RcbServiceError()

    data class Critical(
        val cause: Throwable? = null
    ) : RcbServiceError()

    object Unknown : RcbServiceError()
}
