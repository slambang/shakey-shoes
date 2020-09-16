package com.slambang.shakeyshoes.view.rcb.mappers

import com.slambang.shakeyshoes.R
import com.slambang.shakeyshoes.util.StringProvider
import javax.inject.Inject

interface ErrorMapper {

    fun map(error: Throwable): String
}

class ErrorMapperImpl @Inject constructor(
    private val strings: StringProvider
) : ErrorMapper {

    override fun map(error: Throwable): String =
        strings.getString(
            R.string.error_template,
            error.message ?: strings.getString(R.string.unknown_error)
        )
}
