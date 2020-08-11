package com.betty7.fingerband.alpha.bluetooth.data.audio

interface RcbDataSource : Iterator<Int> {

    /**
     * Check if there is an element available.
     *
     * @return True if an element is available, otherwise false.
     */
    override fun hasNext(): Boolean

    /**
     * Removes the next element and returns it.
     *
     * @return The next element
     */
    override fun next(): Int
}
