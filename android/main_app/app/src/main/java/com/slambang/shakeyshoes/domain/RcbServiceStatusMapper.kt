package com.slambang.shakeyshoes.domain

import com.slambang.rcb.service.RcbServiceError
import javax.inject.Inject

class RcbServiceStatusMapper @Inject constructor() {

    fun map(error: RcbServiceError): RcbServiceStatus =
        when (error) {
            is RcbServiceError.NotFound -> RcbServiceStatus.NotFound
            is RcbServiceError.Unavailable -> RcbServiceStatus.Unavailable
            is RcbServiceError.Disabled -> RcbServiceStatus.Disabled
            is RcbServiceError.Critical -> RcbServiceStatus.Error(error.cause)
            else -> RcbServiceStatus.Unknown
        }
}
