package com.slambang.rcb.service

interface RcbService {

    val id: Int
    val config: RcbServiceConfig

    fun connect(
        macAddress: String,
        serviceUuid: String, listener: RcbServiceListener
    )

    fun setConfig(config: RcbServiceConfig)
    fun reset()
    fun resume()
    fun pause()
    fun stop()
    fun sendBufferData(data: Int)
}
