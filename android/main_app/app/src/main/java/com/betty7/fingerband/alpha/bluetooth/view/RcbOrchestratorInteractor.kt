package com.betty7.fingerband.alpha.bluetooth.view

import com.betty7.fingerband.alpha.bluetooth.domain.DeviceAccuracyDomain
import com.betty7.fingerband.alpha.bluetooth.domain.DeviceDomain
import com.betty7.fingerband.alpha.bluetooth.domain.DeviceRepositoryInteractor
import com.betty7.fingerband.alpha.bluetooth.domain.RcbServiceOrchestrator
import java.lang.IllegalArgumentException

// Responsible for mapping rcb service id's to device domains
class RcbOrchestratorInteractor constructor(
    private val rcbServiceOrchestrator: RcbServiceOrchestrator,
    private val deviceRepoInteractor: DeviceRepositoryInteractor
) {
    private val domainMap = mutableMapOf<Int, DeviceDomain>()

    fun subscribe(
        rcbServiceStatusObserver: (DeviceDomain) -> Unit,
        rcbServiceAccuracyObserver: (DeviceAccuracyDomain) -> Unit
    ) {
        rcbServiceOrchestrator.subscribe({ modelId, status ->
            requireDeviceDomain(modelId).let {
                it.status = status
                rcbServiceStatusObserver(it)
            }
        }, { modelId ->
            requireDeviceDomain(modelId).let {
                rcbServiceAccuracyObserver(it.accuracies)
            }
        })
    }

    fun getAvailableDeviceNames() = deviceRepoInteractor.getAvailableDeviceNames()

    fun createRcbService(deviceDomainId: Int): Pair<Int, DeviceDomain> {
        val modelId = rcbServiceOrchestrator.createRcbService()
        val deviceDomain = deviceRepoInteractor.getDeviceDomain(deviceDomainId)
        domainMap[modelId] = deviceDomain
        return modelId to deviceDomain
    }

    fun connectBufferService(modelId: Int) =
        rcbServiceOrchestrator.connectRcbService(
            modelId,
            requireDeviceDomain(modelId)
        )

    fun configureRcbService(
        modelId: Int,
        numberOfRefills: Int,
        refillSize: Int,
        windowSizeMs: Int,
        maxUnderflows: Int
    ) = rcbServiceOrchestrator.configureRcbService(
        modelId,
        numberOfRefills,
        refillSize,
        windowSizeMs,
        maxUnderflows
    )

    fun setVibrateValue(modelId: Int, vibrateValue: Int) =
        rcbServiceOrchestrator.hackBufferValue(modelId, vibrateValue)

    fun toggleRcb(modelId: Int, isResumed: Boolean) =
        if (isResumed) {
            rcbServiceOrchestrator.pauseRcbService(modelId)
        } else {
            rcbServiceOrchestrator.startRcbService(modelId)
        }

    fun resumeRcbService(modelId: Int) =
        rcbServiceOrchestrator.resumeRcbService(modelId)

    fun pauseRcbService(modelId: Int) =
        rcbServiceOrchestrator.pauseRcbService(modelId)

    fun pauseAllRcbServices() = domainMap.keys.forEach {
        pauseRcbService(it)
    }

    fun stopAllRcbServices() = domainMap.keys.forEach {
        rcbServiceOrchestrator.stopRcbService(it)
    }

    fun deleteRcbService(modelId: Int) {
        rcbServiceOrchestrator.deleteRcbService(modelId)
        domainMap.remove(modelId)
    }

    fun deleteAllRcbServices() = domainMap.keys.forEach {
        rcbServiceOrchestrator.deleteRcbService(it)
    }.also {
        domainMap.clear()
    }

    private fun requireDeviceDomain(modelId: Int) =
        domainMap[modelId]
            ?: throw IllegalArgumentException("Invalid modelId")
}
