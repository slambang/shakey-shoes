package com.slambang.shakeyshoes.data.audio

class SettableRcbDataSource(
    initialValue: Int
) : RcbDataSource {

    var value = initialValue

    override fun hasNext(): Boolean = true

    override fun next() = value
}
