package com.slambang.rcb_service

import java.io.InputStream
import java.nio.ByteBuffer

fun InputStream.readByte() = read()

fun InputStream.readInt(): Int {
    val buffer = IntArray(Int.SIZE_BYTES)
    for (i in (Int.SIZE_BYTES - 1) downTo 0) {
        buffer[i] = this.read()
    }

    // TODO Try IntBuffer.order() here instead
    return toLittleEndian(buffer)
}

// TODO This allocates on each call
fun Int.toByteArray(): ByteArray =
    ByteBuffer
        .allocate(Int.SIZE_BYTES)
        .putInt(this)
        .array()

private fun toLittleEndian(bytes: IntArray): Int {
    var result = 0
    for (i in bytes.indices) {
        result = result xor (bytes[i] shl 8 * i)
    }
    return result
}
