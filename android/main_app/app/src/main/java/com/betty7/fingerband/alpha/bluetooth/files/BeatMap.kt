package com.betty7.fingerband.alpha.bluetooth.files

interface BeatMap : Iterator<Int> {
    /**
     * Initialises an instance.
     *
     * @param maxRange The maximum value of any element. Minimum is always zero.
     * @param offset An additional amount to automatically add to every element.
     */
    fun init(maxRange: Int, offset: Int)

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
