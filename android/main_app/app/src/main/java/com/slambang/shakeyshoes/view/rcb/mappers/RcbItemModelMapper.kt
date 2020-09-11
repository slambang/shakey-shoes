package com.slambang.shakeyshoes.view.rcb.mappers

import com.slambang.shakeyshoes.R
import com.slambang.shakeyshoes.domain.BluetoothDeviceAccuracyDomain
import com.slambang.shakeyshoes.domain.BluetoothDeviceDomain
import com.slambang.shakeyshoes.domain.RcbServiceStatus
import com.slambang.shakeyshoes.util.StringProvider
import com.slambang.shakeyshoes.view.rcb.ActivePageModel
import com.slambang.shakeyshoes.view.rcb.RcbItemModel
import javax.inject.Inject

class RcbItemModelMapper @Inject constructor(
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
            strings.getString(
                R.string.buffer_item_page_3_accuracy_template,
                totalFrames,
                successPercent
            )

        model.page3.errorRate = strings.getString(
            R.string.buffer_item_page_3_accuracy_template,
            domain.underflowCount,
            errorPercent
        )
    }

    fun mapSelectedDevice(domainModel: BluetoothDeviceDomain): RcbItemModel {

        val rcbItemModel = RcbItemModel(domainModel.id)

        rcbItemModel.selectedDeviceId = domainModel.id
        rcbItemModel.header.deviceName = domainModel.name
        rcbItemModel.page1.baudRateBytes =
            strings.getString(R.string.buffer_item_page_1_baud_template, domainModel.baudRateBytes)
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

    fun mapState(domainModel: BluetoothDeviceDomain, itemModel: RcbItemModel) {
        when (domainModel.status) {
            is RcbServiceStatus.Disconnected -> {

                itemModel.page3.resumeButtonText = strings.getString(R.string.resume)

                mapConfig(
                    itemModel.page2.config.refillCount,
                    itemModel.page2.config.refillSize,
                    itemModel.page2.config.windowSize,
                    itemModel.page2.config.maxUnderflows,
                    itemModel
                )
            }
            is RcbServiceStatus.Connecting -> {
                itemModel.header.isConnecting = true
                itemModel.page1.connectButtonEnabled = false
            }
            is RcbServiceStatus.Setup -> {
                setMaxSize(itemModel, (domainModel.status as RcbServiceStatus.Setup).freeHeapBytes)
                itemModel.header.isConnected = true
                itemModel.header.isConnecting = false
                itemModel.page2.applyButtonEnabled = true
            }
            is RcbServiceStatus.Ready -> {
                itemModel.page1.connectButtonEnabled = false
                itemModel.page2.applyButtonEnabled = false
                itemModel.page3.resumeButtonEnabled = true
            }
            is RcbServiceStatus.Error,
            is RcbServiceStatus.NotFound,
            is RcbServiceStatus.Disabled,
            is RcbServiceStatus.Unavailable -> {
                itemModel.header.isConnecting = false
                itemModel.page1.connectButtonEnabled = true
                itemModel.page3.isResumed = false
            }
            is RcbServiceStatus.Paused -> {
                itemModel.page3.isResumed = false
                itemModel.page3.resumeButtonText = strings.getString(R.string.resume)
            }
            is RcbServiceStatus.Resumed -> {
                itemModel.page3.isResumed = true
                itemModel.page3.resumeButtonText = strings.getString(R.string.pause)
            }
        }

        mapPage(domainModel)?.let {
            itemModel.activePage = it
        }

        val isConnected = isConnected(domainModel.status)
        itemModel.header.isConnected = isConnected

        mapStatus(domainModel.status)?.let {
            itemModel.page1.status = it
        }

        itemModel.page1.connectButtonEnabled = !isConnected
        when (isConnected) {
            true -> R.string.disconnect
            false -> R.string.connect
        }.let {
            itemModel.page1.connectButtonText = strings.getString(it)
        }
    }

    private fun mapPage(deviceDomain: BluetoothDeviceDomain): ActivePageModel? {
        val activePage = when (deviceDomain.status) {
            is RcbServiceStatus.Error -> 0
            is RcbServiceStatus.Setup -> 1
            is RcbServiceStatus.Ready -> 2
            else -> null
        }

        return if (activePage != null) {
            ActivePageModel(
                pageIndex = activePage,
                since = System.currentTimeMillis() // TODO Move to provider for easier testing!
            )
        } else {
            null
        }
    }

    private fun mapStatus(status: RcbServiceStatus): String? =
        when (status) {
            is RcbServiceStatus.Disconnected -> strings.getString(R.string.disconnected)
            is RcbServiceStatus.Connecting -> strings.getString(R.string.connecting)
            is RcbServiceStatus.Setup -> strings.getString(R.string.setup)
            is RcbServiceStatus.Ready -> strings.getString(R.string.ready)
            is RcbServiceStatus.Paused -> strings.getString(R.string.paused)
            is RcbServiceStatus.Unavailable -> strings.getString(R.string.unavailable)
            is RcbServiceStatus.Disabled -> strings.getString(R.string.disabled)
            is RcbServiceStatus.NotFound -> strings.getString(R.string.not_found)
            is RcbServiceStatus.Resumed -> strings.getString(R.string.resumed)
            is RcbServiceStatus.Error -> strings.getString(
                R.string.error_template,
                status.cause?.message ?: strings.getString(R.string.unknown_error)
            )
            is RcbServiceStatus.Unknown -> strings.getString(R.string.unknown_error)
            else -> null
        }

    private fun setMaxSize(model: RcbItemModel, maxSize: Int) {
        model.page2.config.maxSize = strings.getString(
            R.string.buffer_item_page_1_max_size,
            maxSize
        )
    }

    private fun isConnected(status: RcbServiceStatus) =
        (status is RcbServiceStatus.Setup ||
                status is RcbServiceStatus.Ready ||
                status is RcbServiceStatus.Refill ||
                status is RcbServiceStatus.Underflow ||
                status is RcbServiceStatus.Paused ||
                status is RcbServiceStatus.Resumed)
}
