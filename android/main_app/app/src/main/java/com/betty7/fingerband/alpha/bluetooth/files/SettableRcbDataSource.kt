package com.betty7.fingerband.alpha.bluetooth.files

class SettableRcbDataSource(initialValue: Int) : RcbDataSource {

    var value = initialValue

    override fun hasNext(): Boolean = true

    override fun next() = value
}
