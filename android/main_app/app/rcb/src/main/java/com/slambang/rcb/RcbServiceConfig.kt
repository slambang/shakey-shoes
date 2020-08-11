package com.slambang.rcb

data class RcbServiceConfig(
    val numRefills: Int = 0,
    val refillSize: Int = 0,
    val windowSizeMs: Int = 0,
    val maxUnderflows: Int = 0
)
