package com.betty7.fingerband.alpha.bluetooth.domain

import com.betty7.fingerband.alpha.bluetooth.data.audio.RcbDataSource
import com.betty7.fingerband.alpha.bluetooth.data.audio.SettableRcbDataSource
import com.slambang.rcb.service.RcbService
import com.slambang.rcb.service.RcbServiceConfig
import com.slambang.rcb.service.RcbServiceListener
import com.slambang.rcb.service.RcbState

/**
 * Responsibilities
 * ----------------
 * 1) Orchestrates n RcbService's and keeps RcbDomains updated
 * 3) Connects RcbServices to RcbDataSources - TODO: Move this somewhere else! (OrchestratorInteractor?)
 */
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
        ) = onBufferServiceStateReceived(rcbService, state)

        override fun onBufferServiceFreeHeap(
            rcbService: RcbService,
            freeHeapBytes: Int
        ) = onBufferServiceHeapReceived(rcbService, freeHeapBytes)

        override fun onBufferServiceError(
            rcbService: RcbService,
            error: Throwable?
        ) = onBufferServiceErrorReceived(rcbService, error)
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
        return rcbService.id
    }

    override fun deleteBufferService(rcbServiceId: Int) {
        deviceDomains.remove(rcbServiceId)
            ?: throw IllegalStateException("Device domain with id $rcbServiceId is not in the orchestrator")

        rcbServices.remove(rcbServiceId)?.stop()
            ?: throw IllegalStateException("Rcb Service with id $rcbServiceId is not in the orchestrator")

        rcbDataSources.remove(rcbServiceId)
            ?: throw IllegalStateException("Data source with id $rcbServiceId is not in the orchestrator")
    }

    private fun onBufferConnecting(rcbService: RcbService) =
        requireDeviceDomain(rcbService.id).also {
            it.status = RcbServiceState.CONNECTING
            rcbServiceStatusObserver(it)
        }

    override fun connectBufferService(rcbServiceId: Int, deviceDomain: DeviceDomain) {

        deviceDomains[rcbServiceId] = deviceDomain

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

    private fun onBufferRefill(rcbService: RcbService) {

        val beatMap = requireDataSource(rcbService.id)
        for (i in 0 until rcbService.config.refillSize) {
            if (beatMap.hasNext()) {
                rcbService.sendBufferData(beatMap.next())
            }
        }

        requireDeviceDomain(rcbService.id).accuracies.also {
            ++it.refillCount
            rcbServiceAccuracyObserver(it)
        }
    }

    private fun onBufferReady(rcbService: RcbService) {

        for (i in 0 until rcbService.config.numRefills) {
            onBufferRefill(rcbService)
        }

        requireDeviceDomain(rcbService.id).also {
            it.status = RcbServiceState.READY
            rcbServiceStatusObserver(it)
        }
    }

    private fun onBufferDisconnected(rcbService: RcbService) =
        requireDeviceDomain(rcbService.id).also {
            it.status = RcbServiceState.DISCONNECTED
            rcbServiceStatusObserver(it)
        }

    private fun onBufferUnderflow(rcbService: RcbService) =
        requireDeviceDomain(rcbService.id).accuracies.also {
            ++it.underflowCount
            rcbServiceAccuracyObserver(it)
        }

    override fun hackBufferValue(rcbServiceId: Int, value: Int) {
        requireDataSource(rcbServiceId).also {
            if (it is SettableRcbDataSource) {
                it.value = value
            }
        }
    }

    override fun startBufferService(rcbServiceId: Int) =
        requireBufferService(rcbServiceId).resume()

    override fun resumeBufferService(rcbServiceId: Int) =
        requireBufferService(rcbServiceId).resume()

    private fun onBufferResumed(rcbService: RcbService) =
        requireDeviceDomain(rcbService.id).also {
            it.status = RcbServiceState.RESUMED
            rcbServiceStatusObserver(it)
        }

    override fun pauseBufferService(rcbServiceId: Int) =
        requireBufferService(rcbServiceId).pause()

    override fun stopBufferService(rcbServiceId: Int) =
        requireBufferService(rcbServiceId).stop()

    private fun onBufferPaused(rcbService: RcbService) =
        requireDeviceDomain(rcbService.id).also {
            it.status = RcbServiceState.PAUSED
            rcbServiceStatusObserver(it)
        }

    private fun onBufferServiceStateReceived(
        rcbService: RcbService,
        state: RcbState
    ) {
        when (state) {
            RcbState.CONNECTING -> onBufferConnecting(rcbService)
            RcbState.READY -> onBufferReady(rcbService)
            RcbState.PAUSED -> onBufferPaused(rcbService)
            RcbState.RESUMED -> onBufferResumed(rcbService)
            RcbState.DISCONNECTED -> onBufferDisconnected(rcbService)
            RcbState.REFILL -> onBufferRefill(rcbService)
            RcbState.UNDERFLOW -> onBufferUnderflow(rcbService)
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

    private fun requireDataSource(id: Int) =
        rcbDataSources[id] ?: throw IllegalArgumentException("Required data source: $id")

    private fun requireBufferService(id: Int) =
        rcbServices[id] ?: throw IllegalArgumentException("Required buffer service: $id")

    private fun requireDeviceDomain(id: Int) =
        deviceDomains[id] ?: throw IllegalStateException("Required device domain: $id")
}
