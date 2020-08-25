package com.slambang.shakeyshoes.view.rcb.interactors

import com.slambang.shakeyshoes.domain.BluetoothDeviceAccuracyDomain
import com.slambang.shakeyshoes.domain.BluetoothDeviceDomain
import com.slambang.shakeyshoes.domain.RcbServiceOrchestrator
import com.slambang.shakeyshoes.domain.RcbServiceStatus
import java.lang.IllegalArgumentException
import javax.inject.Inject

// Responsible for mapping rcb service id's to device domains
class RcbOrchestratorInteractor @Inject constructor(
    private val rcbServiceOrchestrator: RcbServiceOrchestrator,
    private val deviceRepoInteractor: DeviceRepositoryInteractor, // Hmm... interactor used here?
    private val domainMap: MutableMap<Int, BluetoothDeviceDomain>
) { // add interface here

    private lateinit var rcbServiceStatusObserver: (BluetoothDeviceDomain) -> Unit
    private lateinit var rcbServiceAccuracyObserver: (BluetoothDeviceAccuracyDomain) -> Unit

    fun subscribe(
        rcbServiceStatusObserver: (BluetoothDeviceDomain) -> Unit,
        rcbServiceAccuracyObserver: (BluetoothDeviceAccuracyDomain) -> Unit
    ) {
        this.rcbServiceStatusObserver = rcbServiceStatusObserver
        this.rcbServiceAccuracyObserver = rcbServiceAccuracyObserver
        rcbServiceOrchestrator.subscribe(::onRcbStatusUpdate)
    }

    private fun onRcbStatusUpdate(rcbServiceId: Int, status: RcbServiceStatus) {

        when (status) {
            RcbServiceStatus.Refill -> {
                emitAccuracyUpdate(rcbServiceId) {
                    it.refillCount++
                }
            }
            RcbServiceStatus.Underflow -> {
                emitAccuracyUpdate(rcbServiceId) {
                    it.underflowCount++
                }
            }
            else -> {
                requireDeviceDomain(rcbServiceId).let {
                    it.status = status
                    rcbServiceStatusObserver(it)
                }
            }
        }
    }

    private fun emitAccuracyUpdate(rcbServiceId: Int, transformer: (BluetoothDeviceAccuracyDomain) -> Unit) {
        requireDeviceDomain(rcbServiceId).accuracies.apply {
            transformer(this)
        }.also {
            rcbServiceAccuracyObserver(it)
        }
    }

    fun getAvailableDeviceNames() = deviceRepoInteractor.getAvailableDeviceNames()

    fun createRcbService(deviceDomainId: Int): BluetoothDeviceDomain {
        val rcbServiceId = rcbServiceOrchestrator.createRcbService()
        val deviceDomain = deviceRepoInteractor.reserveDevice(deviceDomainId)
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
    ) {
        val rcbServiceId = requireRcbServiceId(domainId)
        rcbServiceOrchestrator.configureRcbService(
            rcbServiceId,
            numberOfRefills,
            refillSize,
            windowSizeMs,
            maxUnderflows
        )
    }

    fun setVibrateValue(domainId: Int, vibrateValue: Int) =
        requireRcbServiceId(domainId).let {
            rcbServiceOrchestrator.hackBufferValue(it, vibrateValue)
        }

    fun toggleRcb(domainId: Int, isResumed: Boolean) =
        requireRcbServiceId(domainId).let {
            if (isResumed) {
                rcbServiceOrchestrator.pauseRcbService(it)
            } else {
                rcbServiceOrchestrator.startRcbService(it)
            }
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
