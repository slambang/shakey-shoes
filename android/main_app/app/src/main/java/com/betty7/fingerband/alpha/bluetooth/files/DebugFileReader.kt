package com.betty7.fingerband.alpha.bluetooth.files

import java.io.File

class DebugFileReader {

    private var lineOffset = 0
    private lateinit var lines: List<String>

    val remainingLines: Int
        get() = lines.size - lineOffset

    fun init(path: String) {
        lines = File(path).readLines()
        lineOffset++ // skip window size value
    }

    fun nextLine() = lines[lineOffset++]
}
