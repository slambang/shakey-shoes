package com.betty7.fingerband.alpha.bluetooth.files

class SettableBeatMap(initialValue: Int) : BeatMap {

    var value = initialValue

    override fun init(maxRange: Int, offset: Int) {}

    override fun hasNext(): Boolean = true

    override fun next() = value
}
