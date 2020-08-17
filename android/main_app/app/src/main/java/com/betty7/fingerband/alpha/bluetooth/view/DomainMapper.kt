package com.betty7.fingerband.alpha.bluetooth.view

import com.betty7.fingerband.alpha.R
import com.betty7.fingerband.alpha.bluetooth.domain.DeviceAccuracyDomain
import com.betty7.fingerband.alpha.bluetooth.domain.DeviceDomain
import com.betty7.fingerband.alpha.bluetooth.domain.RcbServiceStatus

class DomainMapper(
    private val resources: ViewResources
) {

    fun mapAccuracies(domain: DeviceAccuracyDomain, model: RcbItemModel) {

        val totalFrames = (domain.refillCount * model.page2.config.refillSize)
        val successFrames = if (totalFrames == 0) 0 else totalFrames - domain.underflowCount

        val successPercent = if (totalFrames == 0) 0f else ((100f / totalFrames) * successFrames)
        val errorPercent =
            if (totalFrames == 0) 0f else ((100f / totalFrames) * domain.underflowCount)

        model.page3
        model.page3.successRate =
            resources.getString(R.string.buffer_item_accuracy_format, totalFrames, successPercent)

        model.page3.errorRate = resources.getString(
            R.string.buffer_item_accuracy_format,
            domain.underflowCount,
            errorPercent
        )
    }

    fun mapSelectedDevice(domainModel: DeviceDomain): RcbItemModel {

        val rcbItemModel = RcbItemModel(domainModel.id)

        rcbItemModel.selectedDeviceId = domainModel.id
        rcbItemModel.header.deviceName = domainModel.name
        rcbItemModel.page1.baudRateBytes =
            resources.getString(R.string.buffer_item_page_0_baud, domainModel.baudRateBytes)
        rcbItemModel.page1.macAddress = domainModel.macAddress
        rcbItemModel.page1.pairingPin = domainModel.pairingPin

        return rcbItemModel
    }

    fun mapConfig(
        numberOfRefills: Int,
        refillSize: Int,
        windowSizeMs: Int,
        maxUnderflows: Int,
        model: RcbItemModel
    ) {
        model.page2.config.refillCount = numberOfRefills
        model.page2.config.refillSize = refillSize
        model.page2.config.windowSize = windowSizeMs
        model.page2.config.maxUnderflows = maxUnderflows

        model.page2.config.actualSize = resources.getString(
            R.string.buffer_item_page_1_actual_size,
            refillSize * numberOfRefills
        )

        model.page2.config.latency = resources.getString(
            R.string.buffer_item_page_1_latency,
            (refillSize * numberOfRefills) * windowSizeMs
        )

        model.page2.config.maxUnderflowTime = resources.getString(
            R.string.buffer_item_page_1_underflow_time,
            windowSizeMs * maxUnderflows
        )
    }

    fun mapPage(deviceDomain: DeviceDomain) =
        when (deviceDomain.status) {
            is RcbServiceStatus.Error -> 0
            is RcbServiceStatus.Setup -> 1
            is RcbServiceStatus.Ready -> 2
            else -> null
        }

    fun mapState(domainModel: DeviceDomain, model: RcbItemModel) {
        when (domainModel.status) {
            is RcbServiceStatus.Disconnected -> {

                model.page3.resumeButtonText = resources.getString(R.string.resume)
                setMaxSize(model, domainModel.freeHeapBytes)

                mapConfig(
                    model.page2.config.refillCount,
                    model.page2.config.refillSize,
                    model.page2.config.windowSize,
                    model.page2.config.maxUnderflows,
                    model
                )
            }
            is RcbServiceStatus.Connecting -> {
                model.header.isConnecting = true
                model.page1.connectButtonEnabled = false
            }
            is RcbServiceStatus.Setup -> {
                setMaxSize(model, domainModel.freeHeapBytes)
                model.header.isConnected = true
                model.header.isConnecting = false
                model.page2.applyButtonEnabled = true
            }
            is RcbServiceStatus.Ready -> {
                model.page1.connectButtonEnabled = false
                model.page2.applyButtonEnabled = false
                model.page3.resumeButtonEnabled = true
            }
            is RcbServiceStatus.Error -> {
                model.header.isConnecting = false
                model.page1.connectButtonEnabled = true
                model.page3.isResumed = false
            }
            is RcbServiceStatus.Paused -> {
                model.page3.isResumed = false
                model.page3.resumeButtonText = resources.getString(R.string.resume)
            }
            is RcbServiceStatus.Resumed -> {
                model.page3.isResumed = true
                model.page3.resumeButtonText = resources.getString(R.string.pause)
            }
        }

        val isConnected = isConnected(domainModel.status)
        model.header.isConnected = isConnected

        model.page1.status = mapStatus(domainModel.status)
        model.page1.connectButtonEnabled = !isConnected
        when (isConnected) {
            true -> R.string.disconnect
            false -> R.string.connect
        }.let {
            model.page1.connectButtonText = resources.getString(it)
        }
    }

    private fun mapStatus(status: RcbServiceStatus) =
        when (status) {
            is RcbServiceStatus.Disconnected -> resources.getString(R.string.disconnected)
            is RcbServiceStatus.Connecting -> resources.getString(R.string.connecting)
            is RcbServiceStatus.Setup -> resources.getString(R.string.setup)
            is RcbServiceStatus.Ready -> resources.getString(R.string.ready)
            is RcbServiceStatus.Paused -> resources.getString(R.string.paused)
            is RcbServiceStatus.Resumed -> resources.getString(R.string.resumed)
            is RcbServiceStatus.Error -> resources.getString(R.string.error, status.message)
        }

    private fun setMaxSize(model: RcbItemModel, maxSize: Int) {
        model.page2.config.maxSize = resources.getString(
            R.string.buffer_item_page_1_max_size,
            maxSize
        )
    }

    private fun isConnected(status: RcbServiceStatus) =
        (status !is RcbServiceStatus.Connecting &&
                status !is RcbServiceStatus.Disconnected &&
                status !is RcbServiceStatus.Error)
}
