package com.betty7.fingerband.alpha.bluetooth.domain

import com.betty7.fingerband.alpha.bluetooth.files.RcbDataSource
import com.betty7.fingerband.alpha.bluetooth.files.SettableRcbDataSource
import com.betty7.rcb.CircularBufferConfig
import com.betty7.rcb.CircularBufferService
import com.betty7.rcb.CircularBufferState

enum class CircularBufferStatus {
    DISCONNECTED,
    CONNECTING,
    SETUP,
    READY,
    PAUSED,
    RESUMED,
    ERROR;

    var message: String? = null

    fun with(message: String): CircularBufferStatus {
        this.message = message
        return this
    }
}

interface RcbServiceOrchestrator {

    fun subscribe(
        bufferServiceStatusObserver: (DeviceDomain) -> Unit,
        bufferServiceAccuracyObserver: (DeviceAccuracyDomain) -> Unit
    )

    fun createBufferService(): Int
    fun beginBufferService(bufferServiceId: Int)
    fun deleteBufferService(bufferServiceId: Int)

    fun connectBufferService(bufferServiceId: Int, deviceDomain: DeviceDomain)
    fun configureBufferService(
        bufferServiceId: Int,
        numberOfRefills: Int,
        refillSize: Int,
        windowSizeMs: Int,
        maxUnderflows: Int
    )

    fun startBufferService(bufferServiceId: Int)
    fun resumeBufferService(bufferServiceId: Int)
    fun pauseBufferService(bufferServiceId: Int)
    fun stopBufferService(bufferServiceId: Int)

    fun hackBufferValue(bufferServiceId: Int, value: Int)
}

class RcbServiceInteractorImpl(
    private val newRcbDataSource: () -> RcbDataSource,
    private val newRcbService: () -> CircularBufferService,
    private val rcbDataSources: MutableMap<Int, RcbDataSource> = mutableMapOf(),
    private val bufferServices: MutableMap<Int, CircularBufferService> = mutableMapOf(),
    private val deviceDomains: MutableMap<Int, DeviceDomain> = mutableMapOf()
) : RcbServiceOrchestrator {

    private val rcbServiceListener = object : CircularBufferService.Listener {
        override fun onBufferServiceState(
            bufferService: CircularBufferService,
            state: CircularBufferState
        ) {
            onBufferServiceStateReceived(bufferService, state)
        }

        override fun onBufferServiceFreeHeap(
            bufferService: CircularBufferService,
            freeHeapBytes: Int
        ) {
            onBufferServiceHeapReceived(bufferService, freeHeapBytes)
        }

        override fun onBufferServiceError(
            bufferService: CircularBufferService,
            error: Throwable?
        ) {
            onBufferServiceErrorReceived(bufferService, error)
        }
    }

    private lateinit var bufferServiceStatusObserver: (DeviceDomain) -> Unit
    private lateinit var bufferServiceAccuracyObserver: (DeviceAccuracyDomain) -> Unit

    override fun subscribe(
        bufferServiceStatusObserver: (DeviceDomain) -> Unit,
        bufferServiceAccuracyObserver: (DeviceAccuracyDomain) -> Unit
    ) {
        this.bufferServiceStatusObserver = bufferServiceStatusObserver
        this.bufferServiceAccuracyObserver = bufferServiceAccuracyObserver
    }

    override fun createBufferService(): Int {
        val bufferService = newRcbService()
        bufferServices[bufferService.id] = bufferService
        rcbDataSources[bufferService.id] = newRcbDataSource()
        deviceDomains[bufferService.id] = DeviceDomain(bufferService.id)
        return bufferService.id
    }

    override fun beginBufferService(bufferServiceId: Int) {
        requireDeviceDomain(bufferServiceId).also {
            bufferServiceStatusObserver(it)
            bufferServiceAccuracyObserver(it.accuracies)
        }
    }

    override fun deleteBufferService(bufferServiceId: Int) {
        deviceDomains.remove(bufferServiceId)
        bufferServices.remove(bufferServiceId)?.stop()
        rcbDataSources.remove(bufferServiceId)
    }

    private fun onBufferConnecting(buffer: CircularBufferService) =
        requireDeviceDomain(buffer.id).also {
            it.status = CircularBufferStatus.CONNECTING
            bufferServiceStatusObserver(it)
        }

    override fun connectBufferService(bufferServiceId: Int, deviceDomain: DeviceDomain) {

        deviceDomains[deviceDomain.id] = deviceDomain

        requireBufferService(bufferServiceId).connect(
            deviceDomain.macAddress,
            deviceDomain.serviceUuid,
            rcbServiceListener
        )
    }

    override fun configureBufferService(
        bufferServiceId: Int,
        numberOfRefills: Int,
        refillSize: Int,
        windowSizeMs: Int,
        maxUnderflows: Int
    ) = requireBufferService(bufferServiceId).setConfig(
        CircularBufferConfig(
            numberOfRefills,
            refillSize,
            windowSizeMs,
            maxUnderflows
        )
    )

    private fun onBufferRefill(buffer: CircularBufferService) {

        val beatMap = requireBeatMap(buffer.id)
        for (i in 0 until buffer.config.refillSize) {
            if (beatMap.hasNext()) {
                buffer.sendBufferData(beatMap.next())
            }
        }

        requireDeviceDomain(buffer.id).accuracies.also {
            ++it.refillCount
            bufferServiceAccuracyObserver(it)
        }
    }

    private fun onBufferReady(buffer: CircularBufferService) {

        for (i in 0 until buffer.config.numRefills) {
            onBufferRefill(buffer)
        }

        requireDeviceDomain(buffer.id).also {
            it.status = CircularBufferStatus.READY
            bufferServiceStatusObserver(it)
        }
    }

    private fun onBufferDisconnected(buffer: CircularBufferService) =
        requireDeviceDomain(buffer.id).also {
            it.status = CircularBufferStatus.DISCONNECTED
            bufferServiceStatusObserver(it)
        }

    private fun onBufferUnderflow(buffer: CircularBufferService) =
        requireDeviceDomain(buffer.id).accuracies.also {
            ++it.underflowCount
            bufferServiceAccuracyObserver(it)
        }

    override fun hackBufferValue(bufferServiceId: Int, value: Int) {
        requireBeatMap(bufferServiceId).also {
            if (it is SettableRcbDataSource) {
                it.value = value
            }
        }
    }

    override fun startBufferService(bufferServiceId: Int) =
        requireBufferService(bufferServiceId).resume()

    override fun resumeBufferService(bufferServiceId: Int) =
        requireBufferService(bufferServiceId).resume()

    private fun onBufferResumed(buffer: CircularBufferService) =
        requireDeviceDomain(buffer.id).also {
            it.status = CircularBufferStatus.RESUMED
            bufferServiceStatusObserver(it)
        }

    override fun pauseBufferService(bufferServiceId: Int) =
        requireBufferService(bufferServiceId).pause()

    override fun stopBufferService(bufferServiceId: Int) =
        requireBufferService(bufferServiceId).stop()

    private fun onBufferPaused(buffer: CircularBufferService) =
        requireDeviceDomain(buffer.id).also {
            it.status = CircularBufferStatus.PAUSED
            bufferServiceStatusObserver(it)
        }

    private fun onBufferServiceStateReceived(
        buffer: CircularBufferService,
        state: CircularBufferState
    ) {
        when (state) {
            CircularBufferState.CONNECTING -> onBufferConnecting(buffer)
            CircularBufferState.READY -> onBufferReady(buffer)
            CircularBufferState.PAUSED -> onBufferPaused(buffer)
            CircularBufferState.RESUMED -> onBufferResumed(buffer)
            CircularBufferState.DISCONNECTED -> onBufferDisconnected(buffer)
            CircularBufferState.REFILL -> onBufferRefill(buffer)
            CircularBufferState.UNDERFLOW -> onBufferUnderflow(buffer)
        }
    }

    private fun onBufferServiceHeapReceived(
        bufferService: CircularBufferService,
        freeHeapBytes: Int
    ) {
        requireDeviceDomain(bufferService.id).also {
            it.freeHeapBytes = freeHeapBytes
            it.status = CircularBufferStatus.SETUP
            bufferServiceStatusObserver(it)
        }
    }

    private fun onBufferServiceErrorReceived(
        bufferService: CircularBufferService,
        error: Throwable?
    ) {
        error?.printStackTrace()
        requireDeviceDomain(bufferService.id).also {
            it.status = CircularBufferStatus.ERROR.with(message = error?.message ?: "Unknown")
            bufferServiceStatusObserver(it)
        }
    }

    private fun requireBeatMap(id: Int) =
        rcbDataSources[id] ?: throw IllegalArgumentException("Required data source: $id")

    private fun requireBufferService(id: Int) =
        bufferServices[id] ?: throw IllegalArgumentException("Required buffer service: $id")

    private fun requireDeviceDomain(id: Int) =
        deviceDomains[id] ?: throw IllegalStateException("Required device domain: $id")
}
