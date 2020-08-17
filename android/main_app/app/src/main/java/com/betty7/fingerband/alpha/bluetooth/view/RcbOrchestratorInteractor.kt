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
        rcbServiceOrchestrator.subscribe({ rcbServiceId, status ->
            requireDeviceDomain(rcbServiceId).let {
                it.status = status
                rcbServiceStatusObserver(it)
            }
        }, { rcbServiceId ->
            requireDeviceDomain(rcbServiceId).let {
                rcbServiceAccuracyObserver(it.accuracies)
            }
        })
    }

    fun getAvailableDeviceNames() = deviceRepoInteractor.getAvailableDeviceNames()

    fun createRcbService(deviceDomainId: Int): Pair<Int, DeviceDomain> {
        val rcbServiceId = rcbServiceOrchestrator.createRcbService()
        val deviceDomain = deviceRepoInteractor.getDeviceDomain(deviceDomainId)
        domainMap[rcbServiceId] = deviceDomain
        return rcbServiceId to deviceDomain
    }

    fun connectBufferService(rcbServiceId: Int) =
        rcbServiceOrchestrator.connectRcbService(
            rcbServiceId,
            requireDeviceDomain(rcbServiceId)
        )

    fun configureRcbService(
        rcbServiceId: Int,
        numberOfRefills: Int,
        refillSize: Int,
        windowSizeMs: Int,
        maxUnderflows: Int
    ) = rcbServiceOrchestrator.configureRcbService(
        rcbServiceId,
        numberOfRefills,
        refillSize,
        windowSizeMs,
        maxUnderflows
    )

    fun setVibrateValue(rcbServiceId: Int, vibrateValue: Int) =
        rcbServiceOrchestrator.hackBufferValue(rcbServiceId, vibrateValue)

    fun toggleRcb(rcbServiceId: Int, isResumed: Boolean) =
        if (isResumed) {
            rcbServiceOrchestrator.pauseRcbService(rcbServiceId)
        } else {
            rcbServiceOrchestrator.startRcbService(rcbServiceId)
        }

    fun resumeRcbService(rcbServiceId: Int) =
        rcbServiceOrchestrator.resumeRcbService(rcbServiceId)

    fun pauseRcbService(rcbServiceId: Int) =
        rcbServiceOrchestrator.pauseRcbService(rcbServiceId)

    fun pauseAllRcbServices() = domainMap.keys.forEach {
        pauseRcbService(it)
    }

    fun stopAllRcbServices() = domainMap.keys.forEach {
        rcbServiceOrchestrator.stopRcbService(it)
    }

    fun deleteRcbService(rcbServiceId: Int) {
        rcbServiceOrchestrator.deleteRcbService(rcbServiceId)
        domainMap.remove(rcbServiceId)
    }

    fun deleteAllRcbServices() = domainMap.keys.forEach {
        rcbServiceOrchestrator.deleteRcbService(it)
    }.also {
        domainMap.clear()
    }

    private fun requireDeviceDomain(rcbServiceId: Int) =
        domainMap[rcbServiceId]
            ?: throw IllegalArgumentException("Invalid rcbServiceId")
}
