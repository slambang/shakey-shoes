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
    private val rcbServices: MutableMap<Int, RcbService> = mutableMapOf()
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

    private lateinit var rcbServiceStatusObserver: (Int, RcbServiceState) -> Unit
    private lateinit var rcbServiceAccuracyObserver: (Int) -> Unit

    override fun subscribe(
        rcbServiceStatusObserver: (Int, RcbServiceState) -> Unit,
        rcbServiceAccuracyObserver: (Int) -> Unit
    ) {
        this.rcbServiceStatusObserver = rcbServiceStatusObserver
        this.rcbServiceAccuracyObserver = rcbServiceAccuracyObserver
    }

    override fun createRcbService(): Int {
        val rcbService = newRcbService()
        rcbServices[rcbService.id] = rcbService
        rcbDataSources[rcbService.id] = newRcbDataSource()
        return rcbService.id
    }

    override fun deleteRcbService(rcbServiceId: Int) {
        rcbServices.remove(rcbServiceId)?.stop()
            ?: throw IllegalStateException("Rcb Service with id $rcbServiceId is not in the orchestrator")

        rcbDataSources.remove(rcbServiceId)
            ?: throw IllegalStateException("Data source with id $rcbServiceId is not in the orchestrator")
    }

    private fun onBufferConnecting(rcbService: RcbService) =
        rcbServiceStatusObserver(rcbService.id, RcbServiceState.CONNECTING)

    override fun connectRcbService(rcbServiceId: Int, macAddress: String, serviceUuid: String) =
        requireBufferService(rcbServiceId).connect(
            macAddress,
            serviceUuid,
            rcbServiceListener
        )

    override fun configureRcbService(
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

        rcbServiceAccuracyObserver(rcbService.id)
    }

    private fun onBufferReady(rcbService: RcbService) {

        for (i in 0 until rcbService.config.numRefills) {
            onBufferRefill(rcbService)
        }

        rcbServiceStatusObserver(rcbService.id, RcbServiceState.READY)
    }

    private fun onBufferDisconnected(rcbService: RcbService) =
        rcbServiceStatusObserver(rcbService.id, RcbServiceState.DISCONNECTED)

    private fun onBufferUnderflow(rcbService: RcbService) =
        rcbServiceAccuracyObserver(rcbService.id)

    override fun hackBufferValue(rcbServiceId: Int, value: Int) {
        requireDataSource(rcbServiceId).also {
            if (it is SettableRcbDataSource) {
                it.value = value
            }
        }
    }

    override fun startRcbService(rcbServiceId: Int) =
        requireBufferService(rcbServiceId).resume()

    override fun resumeRcbService(rcbServiceId: Int) =
        requireBufferService(rcbServiceId).resume()

    private fun onBufferResumed(rcbService: RcbService) =
        rcbServiceStatusObserver(rcbService.id, RcbServiceState.RESUMED)

    override fun pauseRcbService(rcbServiceId: Int) =
        requireBufferService(rcbServiceId).pause()

    override fun stopRcbService(rcbServiceId: Int) =
        requireBufferService(rcbServiceId).stop()

    private fun onBufferPaused(rcbService: RcbService) =
        rcbServiceStatusObserver(rcbService.id, RcbServiceState.PAUSED)

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
//            it.freeHeapBytes = freeHeapBytes
            rcbServiceStatusObserver(rcbService.id, RcbServiceState.SETUP)
    }

    private fun onBufferServiceErrorReceived(
        rcbService: RcbService,
        error: Throwable?
    ) {
        error?.printStackTrace()
        rcbServiceStatusObserver(rcbService.id, RcbServiceState.ERROR.with(message = error?.message ?: "Unknown"))
    }

    private fun requireDataSource(id: Int) =
        rcbDataSources[id] ?: throw IllegalArgumentException("Required data source: $id")

    private fun requireBufferService(id: Int) =
        rcbServices[id] ?: throw IllegalArgumentException("Required buffer service: $id")
}
