package com.slambang.shakeyshoes.audio

class SettableDataSource(
    initialValue: Int
) : DataSource {

    var value = initialValue

    override fun hasNext(): Boolean = true

    override fun next() = value
}
