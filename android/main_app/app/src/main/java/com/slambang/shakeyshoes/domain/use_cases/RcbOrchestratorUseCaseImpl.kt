package com.slambang.shakeyshoes.domain.use_cases

import com.slambang.shakeyshoes.domain.BluetoothDeviceAccuracyDomain
import com.slambang.shakeyshoes.domain.BluetoothDeviceDomain
import com.slambang.shakeyshoes.domain.RcbServiceOrchestrator
import com.slambang.shakeyshoes.domain.RcbServiceStatus
import javax.inject.Inject

// Responsible for mapping rcb service id's to device domains
class RcbOrchestratorUseCaseImpl @Inject constructor(
    private val rcbServiceOrchestrator: RcbServiceOrchestrator,
    private val deviceRepoUseCase: DeviceRepositoryUseCaseImpl,
    private val domainMap: MutableMap<Int, BluetoothDeviceDomain>
) : RcbOrchestratorUseCase {

    override lateinit var rcbServiceStatusObserver: (BluetoothDeviceDomain) -> Unit

    override lateinit var rcbServiceAccuracyObserver: (BluetoothDeviceAccuracyDomain) -> Unit

    override fun subscribe(
        rcbServiceStatusObserver: (BluetoothDeviceDomain) -> Unit,
        rcbServiceAccuracyObserver: (BluetoothDeviceAccuracyDomain) -> Unit
    ) {
        this.rcbServiceStatusObserver = rcbServiceStatusObserver
        this.rcbServiceAccuracyObserver = rcbServiceAccuracyObserver
        rcbServiceOrchestrator.subscribe(::onRcbStatusUpdate)
    }

    override fun onRcbStatusUpdate(rcbServiceId: Int, status: RcbServiceStatus) {

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

    override fun getAvailableDeviceNames(): List<Pair<Int, String>> =
        deviceRepoUseCase.getAvailableDeviceNames()
            .map { Pair(it.id, it.name) }

    override fun createRcbService(deviceDomainId: Int): BluetoothDeviceDomain {
        val rcbServiceId = rcbServiceOrchestrator.createRcbService()
        val deviceDomain = deviceRepoUseCase.popDevice(deviceDomainId)
        domainMap[rcbServiceId] = deviceDomain
        return deviceDomain
    }

    override fun connectBufferService(domainId: Int) {
        val rcbServiceId = requireRcbServiceId(domainId)
        val domain = requireDeviceDomain(rcbServiceId)
        rcbServiceOrchestrator.connectRcbService(
            rcbServiceId,
            domain.macAddress,
            domain.serviceUuid
        )
    }

    override fun configureRcbService(
        domainId: Int,
        numberOfRefills: Int,
        refillSize: Int,
        windowSizeMs: Int,
        maxUnderflows: Int
    ) = rcbServiceOrchestrator.configureRcbService(
        requireRcbServiceId(domainId),
        numberOfRefills,
        refillSize,
        windowSizeMs,
        maxUnderflows
    )

    override fun setVibrateValue(domainId: Int, vibrateValue: Int) =
        requireRcbServiceId(domainId).let {
            rcbServiceOrchestrator.hackBufferValue(it, vibrateValue)
        }

    override fun toggleRcb(domainId: Int, isResumed: Boolean) =
        requireRcbServiceId(domainId).let {
            if (isResumed) {
                rcbServiceOrchestrator.pauseRcbService(it)
            } else {
                rcbServiceOrchestrator.resumeRcbService(it)
            }
        }

    override fun pauseRcbService(domainId: Int) =
        rcbServiceOrchestrator.pauseRcbService(domainId)

    override fun pauseAllRcbServices() =
        domainMap.keys.forEach {
            pauseRcbService(it)
        }

    override fun stopAllRcbServices() =
        domainMap.keys.forEach {
            rcbServiceOrchestrator.stopRcbService(it)
        }

    override fun removeItem(deviceDomainId: Int) {

        val rcbServiceId = requireRcbServiceId(deviceDomainId)
        rcbServiceOrchestrator.removeRcbService(rcbServiceId)

        domainMap.remove(rcbServiceId)?.let {
            deviceRepoUseCase.pushDevice(it.id)
        } ?: throw IllegalArgumentException("No device for service id $rcbServiceId")
    }

    override fun removeAllItems() = domainMap.forEach {
        rcbServiceOrchestrator.removeRcbService(it.key)
        deviceRepoUseCase.pushDevice(it.value.id)
    }.also {
        domainMap.clear()
    }

    private fun emitAccuracyUpdate(
        rcbServiceId: Int,
        transformer: (BluetoothDeviceAccuracyDomain) -> Unit
    ) {
        requireDeviceDomain(rcbServiceId).accuracies.apply {
            transformer(this)
        }.also {
            rcbServiceAccuracyObserver(it)
        }
    }

    private fun requireDeviceDomain(rcbServiceId: Int) =
        domainMap[rcbServiceId]
            ?: throw IllegalArgumentException("Required device domain $rcbServiceId")

    private fun requireRcbServiceId(deviceDomainId: Int): Int {
        for (entry in domainMap) {
            if (entry.value.id == deviceDomainId) {
                return entry.key
            }
        }

        throw IllegalArgumentException("Invalid rcbServiceId")
    }
}
