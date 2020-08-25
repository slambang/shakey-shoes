package com.slambang.rcb.service

interface RcbService {

    val id: Int

    fun connect(
        macAddress: String,
        serviceUuid: String, listener: RcbServiceListener
    )

    fun transmitConfig(config: RcbServiceConfig)
    fun reset()
    fun resume()
    fun pause()
    fun stop()
    fun sendBufferData(data: Int)
}
