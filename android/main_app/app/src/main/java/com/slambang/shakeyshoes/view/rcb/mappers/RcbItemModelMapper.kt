package com.slambang.shakeyshoes.view.rcb.mappers

import com.slambang.shakeyshoes.R
import com.slambang.shakeyshoes.domain.BluetoothDeviceAccuracyDomain
import com.slambang.shakeyshoes.domain.BluetoothDeviceDomain
import com.slambang.shakeyshoes.domain.RcbServiceStatus
import com.slambang.shakeyshoes.util.StringProvider
import com.slambang.shakeyshoes.util.TimeProvider
import com.slambang.shakeyshoes.view.rcb.ActivePageModel
import com.slambang.shakeyshoes.view.rcb.RcbItemModel
import javax.inject.Inject

class RcbItemModelMapper @Inject constructor(
    private val strings: StringProvider,
    private val timeProvider: TimeProvider
) {

    fun mapAccuracies(domain: BluetoothDeviceAccuracyDomain, model: RcbItemModel) {

        val totalFrames = (domain.refillCount * model.page2.config.refillSize)
        val successFrames = if (totalFrames == 0) 0 else totalFrames - domain.underflowCount

        val successPercent = mapPercentage(totalFrames, successFrames)
        val errorPercent = mapPercentage(totalFrames, domain.underflowCount)

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

    private fun mapPercentage(value: Int, maxValue: Int): Float =
        if (value == 0) {
            0f
        } else {
            ((100f / value) * maxValue)
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
        model: RcbItemModel,
        numberOfRefills: Int,
        refillSize: Int,
        windowSizeMs: Int,
        maxUnderflows: Int
    ) {
        model.page2.config.refillCount = numberOfRefills
        model.page2.config.refillSize = refillSize
        model.page2.config.windowSize = windowSizeMs
        model.page2.config.maxUnderflows = maxUnderflows

        val freeHeapBytes = refillSize * numberOfRefills
        model.page2.config.actualSize = strings.getString(
            R.string.buffer_item_page_1_free_heap_size,
            freeHeapBytes
        )

        val latency = (refillSize * numberOfRefills) * windowSizeMs
        model.page2.config.latency = strings.getString(
            R.string.buffer_item_page_1_latency,
            latency
        )

        val underflowTime = windowSizeMs * maxUnderflows
        model.page2.config.maxUnderflowTime = strings.getString(
            R.string.buffer_item_page_1_underflow_time,
            underflowTime
        )
    }

    fun mapState(domainModel: BluetoothDeviceDomain, itemModel: RcbItemModel) {

        mapPage(itemModel, domainModel)
        mapConnectionState(domainModel, itemModel)

        when (domainModel.status) {
            is RcbServiceStatus.Disconnected -> mapDisconnectedState(itemModel)
            is RcbServiceStatus.Connecting -> mapConnectingState(itemModel)
            is RcbServiceStatus.Setup -> mapSetupState(itemModel, domainModel)
            is RcbServiceStatus.Ready -> mapReadyState(itemModel)
            is RcbServiceStatus.Paused -> mapPausedState(itemModel)
            is RcbServiceStatus.Resumed -> mapResumedState(itemModel)
            is RcbServiceStatus.Error,
            is RcbServiceStatus.NotFound,
            is RcbServiceStatus.Disabled,
            is RcbServiceStatus.Unavailable -> mapUnavailableState(itemModel)
        }
    }

    private fun mapConnectionState(domainModel: BluetoothDeviceDomain, itemModel: RcbItemModel) {
        mapStatus(itemModel, domainModel.status)

        val isConnected = isConnected(domainModel.status)
        mapConnectButtonText(isConnected, itemModel)
        itemModel.header.isConnected = isConnected
        itemModel.page1.connectButtonEnabled = !isConnected
    }

    private fun mapConnectButtonText(isConnected: Boolean, itemModel: RcbItemModel) {
        when (isConnected) {
            true -> R.string.disconnect
            false -> R.string.connect
        }.let {
            itemModel.page1.connectButtonText = strings.getString(it)
        }
    }

    private fun mapConnectingState(itemModel: RcbItemModel) {
        itemModel.header.isConnecting = true
        itemModel.page1.connectButtonEnabled = false
    }

    private fun mapDisconnectedState(itemModel: RcbItemModel) {

        itemModel.page3.resumeButtonText = strings.getString(R.string.resume)

        mapConfig(
            itemModel,
            itemModel.page2.config.refillCount,
            itemModel.page2.config.refillSize,
            itemModel.page2.config.windowSize,
            itemModel.page2.config.maxUnderflows,
        )
    }

    private fun mapSetupState(itemModel: RcbItemModel, domainModel: BluetoothDeviceDomain) =
        with(itemModel) {
            header.isConnected = true
            header.isConnecting = false
            page2.applyButtonEnabled = true
            page2.config.maxSize = strings.getString(
                R.string.buffer_item_page_1_max_size,
                (domainModel.status as RcbServiceStatus.Setup).freeHeapBytes
            )
        }

    private fun mapReadyState(itemModel: RcbItemModel) {
        itemModel.page1.connectButtonEnabled = false
        itemModel.page2.applyButtonEnabled = false
        itemModel.page3.resumeButtonEnabled = true
    }

    private fun mapUnavailableState(itemModel: RcbItemModel) {
        itemModel.header.isConnecting = false
        itemModel.page1.connectButtonEnabled = true
        itemModel.page3.isResumed = false
    }

    private fun mapPausedState(itemModel: RcbItemModel) {
        itemModel.page3.isResumed = false
        itemModel.page3.resumeButtonText = strings.getString(R.string.resume)
    }

    private fun mapResumedState(itemModel: RcbItemModel) {
        itemModel.page3.isResumed = true
        itemModel.page3.resumeButtonText = strings.getString(R.string.pause)
    }

    private fun mapPage(itemModel: RcbItemModel, domainModel: BluetoothDeviceDomain) =
        when (domainModel.status) {
            is RcbServiceStatus.Error -> 0
            is RcbServiceStatus.Setup -> 1
            is RcbServiceStatus.Ready -> 2
            else -> null
        }?.let { activePage ->
            itemModel.activePage = ActivePageModel(
                pageIndex = activePage,
                since = timeProvider.now()
            )
        }

    private fun mapStatus(itemModel: RcbItemModel, status: RcbServiceStatus) =
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
        }?.let {
            itemModel.page1.status = it
        }

    private fun isConnected(status: RcbServiceStatus) =
        CONNECTED_STATES.contains(status::class)

    companion object {
        private val CONNECTED_STATES = listOf(
            RcbServiceStatus.Connecting::class,
            RcbServiceStatus.Setup::class,
            RcbServiceStatus.Ready::class,
            RcbServiceStatus.Refill::class,
            RcbServiceStatus.Underflow::class,
            RcbServiceStatus.Paused::class,
            RcbServiceStatus.Resumed::class
        )
    }
}
