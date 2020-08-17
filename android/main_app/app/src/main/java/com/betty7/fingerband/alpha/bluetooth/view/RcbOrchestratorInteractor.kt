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
        rcbServiceOrchestrator.subscribe({ domainId, status ->
            requireDeviceDomain(domainId).let {
                it.status = status
                rcbServiceStatusObserver(it)
            }
        }, { domainId ->
            requireDeviceDomain(domainId).let { // TODO needs to be rcb service id!
                rcbServiceAccuracyObserver(it.accuracies)
            }
        })
    }

    fun getAvailableDeviceNames() = deviceRepoInteractor.getAvailableDeviceNames()

    fun createRcbService(deviceDomainId: Int): DeviceDomain {
        val rcbServiceId = rcbServiceOrchestrator.createRcbService()
        val deviceDomain = deviceRepoInteractor.takeDevice(deviceDomainId)
        domainMap[rcbServiceId] = deviceDomain
        return deviceDomain
    }

    fun connectBufferService(domainId: Int) {
        val rcbServiceId = requireRcbServiceId(domainId)
        val domain = requireDeviceDomain(rcbServiceId)
        rcbServiceOrchestrator.connectRcbService(
            rcbServiceId,
            domain.macAddress,
            domain.serviceUuid
        )
    }

    fun configureRcbService(
        domainId: Int,
        numberOfRefills: Int,
        refillSize: Int,
        windowSizeMs: Int,
        maxUnderflows: Int
    ) = rcbServiceOrchestrator.configureRcbService(
        domainId,
        numberOfRefills,
        refillSize,
        windowSizeMs,
        maxUnderflows
    )

    fun setVibrateValue(domainId: Int, vibrateValue: Int) =
        rcbServiceOrchestrator.hackBufferValue(domainId, vibrateValue)

    fun toggleRcb(domainId: Int, isResumed: Boolean) =
        if (isResumed) {
            rcbServiceOrchestrator.pauseRcbService(domainId)
        } else {
            rcbServiceOrchestrator.startRcbService(domainId)
        }

    fun resumeRcbService(domainId: Int) =
        rcbServiceOrchestrator.resumeRcbService(domainId)

    fun pauseRcbService(domainId: Int) =
        rcbServiceOrchestrator.pauseRcbService(domainId)

    fun pauseAllRcbServices() = domainMap.keys.forEach {
        pauseRcbService(it)
    }

    fun stopAllRcbServices() = domainMap.keys.forEach {
        rcbServiceOrchestrator.stopRcbService(it)
    }

    fun deleteRcbService(deviceDomainId: Int) {

        val rcbServiceId = requireRcbServiceId(deviceDomainId)
        rcbServiceOrchestrator.deleteRcbService(rcbServiceId)

        domainMap.remove(rcbServiceId)?.let {
            deviceRepoInteractor.returnDevice(it.id)
        } ?: throw IllegalArgumentException("No device for service id $rcbServiceId")
    }

    fun deleteAllRcbServices() = domainMap.forEach {
        rcbServiceOrchestrator.deleteRcbService(it.key)
        deviceRepoInteractor.returnDevice(it.value.id)
    }.also {
        domainMap.clear()
    }

    private fun requireDeviceDomain(rcbServiceId: Int) =
        domainMap[rcbServiceId]
            ?: throw IllegalArgumentException("Invalid rcbServiceId")

    private fun requireRcbServiceId(deviceDomainId: Int): Int {
        for (entry in domainMap) {
            if (entry.value.id == deviceDomainId) {
                return entry.key
            }
        }

        throw IllegalArgumentException("Invalid rcbServiceId")
    }
}
