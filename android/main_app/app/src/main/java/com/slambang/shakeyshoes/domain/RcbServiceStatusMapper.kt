package com.slambang.shakeyshoes.domain

import com.slambang.rcb_service.RcbServiceState
import javax.inject.Inject

class RcbServiceStatusMapper @Inject constructor() {

    fun map(error: RcbServiceState.Error): RcbServiceStatus =
        when (error) {
            is RcbServiceState.Error.NotFound -> RcbServiceStatus.NotFound
            is RcbServiceState.Error.Unavailable -> RcbServiceStatus.Unavailable
            is RcbServiceState.Error.Disabled -> RcbServiceStatus.Disabled
            is RcbServiceState.Error.Generic -> RcbServiceStatus.Error(error.cause)
            else -> RcbServiceStatus.Unknown
        }
}
