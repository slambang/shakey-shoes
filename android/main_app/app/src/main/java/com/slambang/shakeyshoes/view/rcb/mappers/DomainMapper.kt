package com.slambang.shakeyshoes.view.rcb.mappers

import com.slambang.shakeyshoes.R
import com.slambang.shakeyshoes.domain.BluetoothDeviceAccuracyDomain
import com.slambang.shakeyshoes.domain.BluetoothDeviceDomain
import com.slambang.shakeyshoes.domain.RcbServiceStatus
import com.slambang.shakeyshoes.util.StringProvider
import com.slambang.shakeyshoes.view.rcb.RcbItemModel
import javax.inject.Inject

class DomainMapper @Inject constructor(
    private val strings: StringProvider
) {

    fun mapAccuracies(domain: BluetoothDeviceAccuracyDomain, model: RcbItemModel) {

        val totalFrames = (domain.refillCount * model.page2.config.refillSize)
        val successFrames = if (totalFrames == 0) 0 else totalFrames - domain.underflowCount

        val successPercent = if (totalFrames == 0) 0f else ((100f / totalFrames) * successFrames)
        val errorPercent =
            if (totalFrames == 0) 0f else ((100f / totalFrames) * domain.underflowCount)

        model.page3
        model.page3.successRate =
            strings.getString(R.string.buffer_item_accuracy_format, totalFrames, successPercent)

        model.page3.errorRate = strings.getString(
            R.string.buffer_item_accuracy_format,
            domain.underflowCount,
            errorPercent
        )
    }

    fun mapSelectedDevice(domainModel: BluetoothDeviceDomain): RcbItemModel {

        val rcbItemModel =
            RcbItemModel(domainModel.id)

        rcbItemModel.selectedDeviceId = domainModel.id
        rcbItemModel.header.deviceName = domainModel.name
        rcbItemModel.page1.baudRateBytes =
            strings.getString(R.string.buffer_item_page_0_baud, domainModel.baudRateBytes)
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

        model.page2.config.actualSize = strings.getString(
            R.string.buffer_item_page_1_actual_size,
            refillSize * numberOfRefills
        )

        model.page2.config.latency = strings.getString(
            R.string.buffer_item_page_1_latency,
            (refillSize * numberOfRefills) * windowSizeMs
        )

        model.page2.config.maxUnderflowTime = strings.getString(
            R.string.buffer_item_page_1_underflow_time,
            windowSizeMs * maxUnderflows
        )
    }

    fun mapPage(deviceDomain: BluetoothDeviceDomain) =
        when (deviceDomain.status) {
            is RcbServiceStatus.Error -> 0
            is RcbServiceStatus.Setup -> 1
            is RcbServiceStatus.Ready -> 2
            else -> null
        }

    fun mapState(domainModel: BluetoothDeviceDomain, model: RcbItemModel) {
        when (domainModel.status) {
            is RcbServiceStatus.Disconnected -> {

                model.page3.resumeButtonText = strings.getString(R.string.resume)
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
                model.page3.resumeButtonText = strings.getString(R.string.resume)
            }
            is RcbServiceStatus.Resumed -> {
                model.page3.isResumed = true
                model.page3.resumeButtonText = strings.getString(R.string.pause)
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
            model.page1.connectButtonText = strings.getString(it)
        }
    }

    private fun mapStatus(status: RcbServiceStatus) =
        when (status) {
            is RcbServiceStatus.Disconnected -> strings.getString(R.string.disconnected)
            is RcbServiceStatus.Connecting -> strings.getString(R.string.connecting)
            is RcbServiceStatus.Setup -> strings.getString(R.string.setup)
            is RcbServiceStatus.Ready -> strings.getString(R.string.ready)
            is RcbServiceStatus.Paused -> strings.getString(R.string.paused)
            is RcbServiceStatus.Resumed -> strings.getString(R.string.resumed)
            is RcbServiceStatus.Error -> strings.getString(R.string.error, status.message)
        }

    private fun setMaxSize(model: RcbItemModel, maxSize: Int) {
        model.page2.config.maxSize = strings.getString(
            R.string.buffer_item_page_1_max_size,
            maxSize
        )
    }

    private fun isConnected(status: RcbServiceStatus) =
        (status !is RcbServiceStatus.Connecting &&
                status !is RcbServiceStatus.Disconnected &&
                status !is RcbServiceStatus.Error)
}
