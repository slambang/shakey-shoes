package com.betty7.fingerband.alpha.bluetooth.domain

import com.betty7.fingerband.alpha.bluetooth.data.audio.RcbDataSource
import com.betty7.fingerband.alpha.bluetooth.data.audio.SettableRcbDataSource
import com.slambang.rcb.RcbService
import com.slambang.rcb.RcbServiceConfig
import com.slambang.rcb.RcbServiceListener
import com.slambang.rcb.RcbState

class RcbServiceInteractorImpl(
    private val newRcbDataSource: () -> RcbDataSource,
    private val newRcbService: () -> RcbService,
    private val rcbDataSources: MutableMap<Int, RcbDataSource> = mutableMapOf(),
    private val rcbServices: MutableMap<Int, RcbService> = mutableMapOf(),
    private val deviceDomains: MutableMap<Int, DeviceDomain> = mutableMapOf()
) : RcbServiceOrchestrator {

    private val rcbServiceListener = object : RcbServiceListener {
        override fun onBufferServiceState(
            rcbService: RcbService,
            state: RcbState
        ) {
            onBufferServiceStateReceived(rcbService, state)
        }

        override fun onBufferServiceFreeHeap(
            rcbService: RcbService,
            freeHeapBytes: Int
        ) {
            onBufferServiceHeapReceived(rcbService, freeHeapBytes)
        }

        override fun onBufferServiceError(
            rcbService: RcbService,
            error: Throwable?
        ) {
            onBufferServiceErrorReceived(rcbService, error)
        }
    }

    private lateinit var rcbServiceStatusObserver: (DeviceDomain) -> Unit
    private lateinit var rcbServiceAccuracyObserver: (DeviceAccuracyDomain) -> Unit

    override fun subscribe(
        rcbServiceStatusObserver: (DeviceDomain) -> Unit,
        rcbServiceAccuracyObserver: (DeviceAccuracyDomain) -> Unit
    ) {
        this.rcbServiceStatusObserver = rcbServiceStatusObserver
        this.rcbServiceAccuracyObserver = rcbServiceAccuracyObserver
    }

    override fun createBufferService(): Int {
        val rcbService = newRcbService()
        rcbServices[rcbService.id] = rcbService
        rcbDataSources[rcbService.id] = newRcbDataSource()
        deviceDomains[rcbService.id] = DeviceDomain(rcbService.id)
        return rcbService.id
    }

    override fun beginBufferService(rcbServiceId: Int) {
        requireDeviceDomain(rcbServiceId).also {
            rcbServiceStatusObserver(it)
            rcbServiceAccuracyObserver(it.accuracies)
        }
    }

    override fun deleteBufferService(rcbServiceId: Int) {
        deviceDomains.remove(rcbServiceId)
        rcbServices.remove(rcbServiceId)?.stop()
        rcbDataSources.remove(rcbServiceId)
    }

    private fun onBufferConnecting(buffer: RcbService) =
        requireDeviceDomain(buffer.id).also {
            it.status = RcbServiceState.CONNECTING
            rcbServiceStatusObserver(it)
        }

    override fun connectBufferService(rcbServiceId: Int, deviceDomain: DeviceDomain) {

        deviceDomains[deviceDomain.id] = deviceDomain

        requireBufferService(rcbServiceId).connect(
            deviceDomain.macAddress,
            deviceDomain.serviceUuid,
            rcbServiceListener
        )
    }

    override fun configureBufferService(
        rcbServiceId: Int,
        numberOfRefills: Int,
        refillSize: Int,
        windowSizeMs: Int,
        maxUnderflows: Int
    ) = requireBufferService(rcbServiceId).setConfig(
        RcbServiceConfig(
            numberOfRefills,
            refillSize,
            windowSizeMs,
            maxUnderflows
        )
    )

    private fun onBufferRefill(buffer: RcbService) {

        val beatMap = requireBeatMap(buffer.id)
        for (i in 0 until buffer.config.refillSize) {
            if (beatMap.hasNext()) {
                buffer.sendBufferData(beatMap.next())
            }
        }

        requireDeviceDomain(buffer.id).accuracies.also {
            ++it.refillCount
            rcbServiceAccuracyObserver(it)
        }
    }

    private fun onBufferReady(buffer: RcbService) {

        for (i in 0 until buffer.config.numRefills) {
            onBufferRefill(buffer)
        }

        requireDeviceDomain(buffer.id).also {
            it.status = RcbServiceState.READY
            rcbServiceStatusObserver(it)
        }
    }

    private fun onBufferDisconnected(buffer: RcbService) =
        requireDeviceDomain(buffer.id).also {
            it.status = RcbServiceState.DISCONNECTED
            rcbServiceStatusObserver(it)
        }

    private fun onBufferUnderflow(buffer: RcbService) =
        requireDeviceDomain(buffer.id).accuracies.also {
            ++it.underflowCount
            rcbServiceAccuracyObserver(it)
        }

    override fun hackBufferValue(rcbServiceId: Int, value: Int) {
        requireBeatMap(rcbServiceId).also {
            if (it is SettableRcbDataSource) {
                it.value = value
            }
        }
    }

    override fun startBufferService(rcbServiceId: Int) =
        requireBufferService(rcbServiceId).resume()

    override fun resumeBufferService(rcbServiceId: Int) =
        requireBufferService(rcbServiceId).resume()

    private fun onBufferResumed(buffer: RcbService) =
        requireDeviceDomain(buffer.id).also {
            it.status = RcbServiceState.RESUMED
            rcbServiceStatusObserver(it)
        }

    override fun pauseBufferService(rcbServiceId: Int) =
        requireBufferService(rcbServiceId).pause()

    override fun stopBufferService(rcbServiceId: Int) =
        requireBufferService(rcbServiceId).stop()

    private fun onBufferPaused(buffer: RcbService) =
        requireDeviceDomain(buffer.id).also {
            it.status = RcbServiceState.PAUSED
            rcbServiceStatusObserver(it)
        }

    private fun onBufferServiceStateReceived(
        buffer: RcbService,
        state: RcbState
    ) {
        when (state) {
            RcbState.CONNECTING -> onBufferConnecting(buffer)
            RcbState.READY -> onBufferReady(buffer)
            RcbState.PAUSED -> onBufferPaused(buffer)
            RcbState.RESUMED -> onBufferResumed(buffer)
            RcbState.DISCONNECTED -> onBufferDisconnected(buffer)
            RcbState.REFILL -> onBufferRefill(buffer)
            RcbState.UNDERFLOW -> onBufferUnderflow(buffer)
        }
    }

    private fun onBufferServiceHeapReceived(
        rcbService: RcbService,
        freeHeapBytes: Int
    ) {
        requireDeviceDomain(rcbService.id).also {
            it.freeHeapBytes = freeHeapBytes
            it.status = RcbServiceState.SETUP
            rcbServiceStatusObserver(it)
        }
    }

    private fun onBufferServiceErrorReceived(
        rcbService: RcbService,
        error: Throwable?
    ) {
        error?.printStackTrace()
        requireDeviceDomain(rcbService.id).also {
            it.status = RcbServiceState.ERROR.with(message = error?.message ?: "Unknown")
            rcbServiceStatusObserver(it)
        }
    }

    private fun requireBeatMap(id: Int) =
        rcbDataSources[id] ?: throw IllegalArgumentException("Required data source: $id")

    private fun requireBufferService(id: Int) =
        rcbServices[id] ?: throw IllegalArgumentException("Required buffer service: $id")

    private fun requireDeviceDomain(id: Int) =
        deviceDomains[id] ?: throw IllegalStateException("Required device domain: $id")
}
