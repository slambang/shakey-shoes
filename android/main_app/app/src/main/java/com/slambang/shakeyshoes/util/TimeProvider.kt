package com.slambang.shakeyshoes.util

import javax.inject.Inject

interface TimeProvider {

    fun now(): Long
}

class TimeProviderImpl @Inject constructor() : TimeProvider {

    override fun now() = System.currentTimeMillis()
}
