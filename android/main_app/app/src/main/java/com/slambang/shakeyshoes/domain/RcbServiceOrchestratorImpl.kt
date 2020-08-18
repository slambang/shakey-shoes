package com.slambang.shakeyshoes.domain

import com.slambang.rcb.service.*
import com.slambang.shakeyshoes.data.audio.RcbDataSource
import com.slambang.shakeyshoes.data.audio.SettableRcbDataSource
import com.slambang.shakeyshoes.di.factories.RcbDataFactory
import com.slambang.shakeyshoes.di.factories.RcbServiceFactory
import javax.inject.Inject

// Responsible for orchestrating `n` RcbService's and updating domains
class RcbServiceOrchestratorImpl @Inject constructor(
    private val rcbDataFactory: RcbDataFactory,
    private val rcbServiceFactory: RcbServiceFactory,
    private val rcbDataSources: MutableMap<Int, RcbDataSource>,
    private val rcbServices: MutableMap<Int, RcbService>
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

    private lateinit var rcbServiceStatusObserver: (Int, RcbServiceStatus) -> Unit
    private lateinit var rcbServiceAccuracyObserver: (Int) -> Unit

    override fun subscribe(
        rcbServiceStatusObserver: (Int, RcbServiceStatus) -> Unit,
        rcbServiceAccuracyObserver: (Int) -> Unit
    ) {
        this.rcbServiceStatusObserver = rcbServiceStatusObserver
        this.rcbServiceAccuracyObserver = rcbServiceAccuracyObserver
    }

    override fun createRcbService(): Int {
        val rcbService = rcbServiceFactory.newRcbService()
        rcbServices[rcbService.id] = rcbService
        rcbDataSources[rcbService.id] = rcbDataFactory.newRcbDataSource()
        return rcbService.id
    }

    override fun deleteRcbService(rcbServiceId: Int) {
        rcbServices.remove(rcbServiceId)?.stop()
            ?: throw IllegalStateException("Rcb Service with id $rcbServiceId is not in the orchestrator")

        rcbDataSources.remove(rcbServiceId)
            ?: throw IllegalStateException("Data source with id $rcbServiceId is not in the orchestrator")
    }

    private fun onBufferConnecting(rcbService: RcbService) =
        rcbServiceStatusObserver(rcbService.id, RcbServiceStatus.Connecting)

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

        rcbServiceStatusObserver(rcbService.id, RcbServiceStatus.Ready)
    }

    private fun onBufferDisconnected(rcbService: RcbService) =
        rcbServiceStatusObserver(rcbService.id, RcbServiceStatus.Disconnected)

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
        rcbServiceStatusObserver(rcbService.id, RcbServiceStatus.Resumed)

    override fun pauseRcbService(rcbServiceId: Int) =
        requireBufferService(rcbServiceId).pause()

    override fun stopRcbService(rcbServiceId: Int) =
        requireBufferService(rcbServiceId).stop()

    private fun onBufferPaused(rcbService: RcbService) =
        rcbServiceStatusObserver(rcbService.id, RcbServiceStatus.Paused)

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
        rcbServiceStatusObserver(rcbService.id, RcbServiceStatus.Setup(freeHeapBytes))
    }

    private fun onBufferServiceErrorReceived(
        rcbService: RcbService,
        error: Throwable?
    ) {
        error?.printStackTrace()
        rcbServiceStatusObserver(
            rcbService.id,
            RcbServiceStatus.Error(message = error?.message ?: "Unknown")
        )
    }

    private fun requireDataSource(id: Int) =
        rcbDataSources[id] ?: throw IllegalArgumentException("Required data source: $id")

    private fun requireBufferService(id: Int) =
        rcbServices[id] ?: throw IllegalArgumentException("Required buffer service: $id")
}