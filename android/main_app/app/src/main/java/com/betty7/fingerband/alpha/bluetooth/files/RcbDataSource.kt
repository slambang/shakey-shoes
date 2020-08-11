package com.betty7.fingerband.alpha.bluetooth.files

interface RcbDataSource : Iterator<Int> {

    /**
     * Check if there is an element available.
     *
     * @return True if an element is available, otherwise false.
     */
    override fun hasNext(): Boolean

    /**
     * Removes the next element and returns it.
     * The offset passed to [init] is already added.
     *
     * @return The next element
     */
    override fun next(): Int
}
