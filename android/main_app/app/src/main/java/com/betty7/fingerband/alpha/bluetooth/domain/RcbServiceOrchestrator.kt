package com.betty7.fingerband.alpha.bluetooth.domain

import android.util.Log
import com.betty7.fingerband.alpha.bluetooth.entity.BluetoothDeviceEntityMapper
import com.betty7.fingerband.alpha.bluetooth.entity.DeviceRepository
import com.betty7.fingerband.alpha.bluetooth.files.BeatMap
import com.betty7.fingerband.alpha.bluetooth.files.SettableBeatMap
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

    var bufferStateObserver: (DeviceDomain) -> Unit
    var bufferAccuracyObserver: (DeviceAccuracyDomain) -> Unit

    fun getDeviceDomains(): List<DeviceDomain>
    fun getDeviceDomain(deviceId: Int): DeviceDomain

    fun createBuffer(): Int
    fun begin(bufferId: Int)
    fun deleteBuffer(bufferId: Int)

    fun connectBuffer(bufferId: Int, deviceId: Int)
    fun configureBuffer(
        bufferId: Int,
        numberOfRefills: Int,
        refillSize: Int,
        windowSizeMs: Int,
        maxUnderflows: Int
    )

    fun start(bufferId: Int)
    fun resume(bufferId: Int)
    fun pause(bufferId: Int)
    fun stop(bufferId: Int)

    fun hackBufferValue(bufferId: Int, value: Int)
}

class RcbServiceInteractorImpl(
    private val deviceRepo: DeviceRepository,
    private val entityMapper: BluetoothDeviceEntityMapper,
    private val newBeatMap: () -> BeatMap,
    private val newBufferService: () -> CircularBufferService
) : RcbServiceOrchestrator, CircularBufferService.Listener {

    override lateinit var bufferStateObserver: (DeviceDomain) -> Unit
    override lateinit var bufferAccuracyObserver: (DeviceAccuracyDomain) -> Unit

    private val beatMaps = mutableMapOf<Int, BeatMap>()
    private val bufferServices =
        mutableMapOf<Int, CircularBufferService>()
    private val deviceDomains = mutableMapOf<Int, DeviceDomain>()

    override fun getDeviceDomains(): List<DeviceDomain> {
        val deviceEntities = deviceRepo.getDeviceEntities()
        return entityMapper.map(deviceEntities)
    }

    override fun getDeviceDomain(deviceId: Int): DeviceDomain {
        val device = deviceRepo.getDeviceEntity(deviceId)
        return entityMapper.map(device)
    }

    override fun createBuffer(): Int {
        val bufferService = newBufferService()
        bufferServices[bufferService.id] = bufferService
        beatMaps[bufferService.id] = newBeatMap()
        deviceDomains[bufferService.id] = DeviceDomain(bufferService.id)
        return bufferService.id
    }

    override fun begin(bufferId: Int) {
        requireBufferDomain(bufferId).also {
            bufferStateObserver(it)
            bufferAccuracyObserver(it.accuracies)
        }
    }

    override fun deleteBuffer(bufferId: Int) {
        deviceDomains.remove(bufferId)
        bufferServices.remove(bufferId)?.stop()
        beatMaps.remove(bufferId)
    }

    override fun onBufferState(buffer: CircularBufferService, state: CircularBufferState) {
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

    private fun onBufferConnecting(buffer: CircularBufferService) =
        requireBufferDomain(buffer.id).also {
            it.status = CircularBufferStatus.CONNECTING
            bufferStateObserver(it)
        }

    override fun connectBuffer(bufferId: Int, deviceId: Int) {

        val deviceEntity = deviceRepo.getDeviceEntity(deviceId)
        val deviceDomain = entityMapper.map(deviceEntity)
        deviceDomains[deviceId] = deviceDomain

        requireBufferService(bufferId).connect(
            deviceEntity.macAddress,
            deviceEntity.serviceUuid,
            this
        )
    }

    override fun configureBuffer(
        bufferId: Int,
        numberOfRefills: Int,
        refillSize: Int,
        windowSizeMs: Int,
        maxUnderflows: Int
    ) = requireBufferService(bufferId).setConfig(
        CircularBufferConfig(
            numberOfRefills,
            refillSize,
            windowSizeMs,
            maxUnderflows
        )
    )

    override fun onBufferFreeHeap(
        buffer: CircularBufferService,
        freeHeapBytes: Int
    ) {
        requireBufferDomain(buffer.id).also {
            it.freeHeapBytes = freeHeapBytes
            it.status = CircularBufferStatus.SETUP
            bufferStateObserver(it)
        }
    }

    var i = 0
    private fun onBufferRefill(buffer: CircularBufferService) {

        Log.d("Steve", "refill ${i++}")

        val beatMap = requireBeatMap(buffer.id)
        for (i in 0 until buffer.config.refillSize) {
            if (beatMap.hasNext()) {
                buffer.sendBufferData(beatMap.next())
            }
        }

        requireBufferDomain(buffer.id).accuracies.also {
            ++it.refillCount
            bufferAccuracyObserver(it)
        }
    }

    private fun onBufferReady(buffer: CircularBufferService) {

        for (i in 0 until buffer.config.numRefills) {
            onBufferRefill(buffer)
        }

        requireBufferDomain(buffer.id).also {
            it.status = CircularBufferStatus.READY
            bufferStateObserver(it)
        }
    }

    private fun onBufferDisconnected(buffer: CircularBufferService) =
        requireBufferDomain(buffer.id).also {
            it.status = CircularBufferStatus.DISCONNECTED
            bufferStateObserver(it)
        }

    override fun onBufferError(
        buffer: CircularBufferService,
        error: Throwable?
    ) {
        error?.printStackTrace()
        requireBufferDomain(buffer.id).also {
            it.status = CircularBufferStatus.ERROR.with(message = error?.message ?: "Unknown")
            bufferStateObserver(it)
        }
    }

    private fun onBufferUnderflow(buffer: CircularBufferService) =
        requireBufferDomain(buffer.id).accuracies.also {
            ++it.underflowCount
            bufferAccuracyObserver(it)
        }

    override fun hackBufferValue(bufferId: Int, value: Int) {
        requireBeatMap(bufferId).also {
            if (it is SettableBeatMap) {
                it.value = value
            }
        }
    }

    override fun start(bufferId: Int) = requireBufferService(bufferId).resume()

    override fun resume(bufferId: Int) = requireBufferService(bufferId).resume()

    private fun onBufferResumed(buffer: CircularBufferService) =
        requireBufferDomain(buffer.id).also {
            it.status = CircularBufferStatus.RESUMED
            bufferStateObserver(it)
        }

    override fun pause(bufferId: Int) = requireBufferService(bufferId).pause()

    private fun onBufferPaused(buffer: CircularBufferService) =
        requireBufferDomain(buffer.id).also {
            it.status = CircularBufferStatus.PAUSED
            bufferStateObserver(it)
        }

    override fun stop(bufferId: Int) = requireBufferService(bufferId).stop()

    private fun requireBeatMap(id: Int) =
        beatMaps[id] ?: throw IllegalArgumentException("Required data source: $id")

    private fun requireBufferService(id: Int) =
        bufferServices[id] ?: throw IllegalArgumentException("Required buffer service: $id")

    private fun requireBufferDomain(id: Int) =
        deviceDomains[id] ?: throw IllegalStateException("Required device domain: $id")
}
