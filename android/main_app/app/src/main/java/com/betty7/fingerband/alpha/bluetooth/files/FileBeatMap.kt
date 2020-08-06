package com.betty7.fingerband.alpha.bluetooth.files

class FileBeatMap(
    private val path: String,
    private val fileReader: DebugFileReader,
    private val valueMapper: DebugFileValueMapper
) : BeatMap {

    override fun init(maxRange: Int, offset: Int) {
        fileReader.init(path)
        valueMapper.init(maxRange, offset)
    }

    override fun hasNext(): Boolean = fileReader.remainingLines > 0

    override fun next(): Int {
        val line = fileReader.nextLine()
        val value = line.toFloat()
        return valueMapper.mapValue(value)
    }
}
