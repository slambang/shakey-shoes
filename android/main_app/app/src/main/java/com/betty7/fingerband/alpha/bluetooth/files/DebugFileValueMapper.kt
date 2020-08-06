package com.betty7.fingerband.alpha.bluetooth.files

import kotlin.math.roundToInt

class DebugFileValueMapper {

    private var maxRange = 0

    private var actualRange = 0
    private var rangeOffset = 0

    fun init(range: Int, offset: Int) {
        maxRange = range
        rangeOffset = offset
        actualRange = maxRange - rangeOffset
    }

    fun mapValue(input: Float): Int {
        val scaledInput = actualRange * input
        return (rangeOffset + scaledInput).roundToInt()
    }
}
