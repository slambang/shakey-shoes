package com.betty7.fingerband.alpha.bluetooth.view

import android.annotation.SuppressLint
import com.betty7.fingerband.alpha.R
import com.betty7.fingerband.alpha.bluetooth.domain.DeviceAccuracyDomain
import com.betty7.fingerband.alpha.bluetooth.domain.DeviceDomain
import com.betty7.fingerband.alpha.bluetooth.domain.RcbServiceState
import java.util.*

class DomainMapper(private val resources: ViewResources) {

    fun mapAccuracies(domain: DeviceAccuracyDomain, model: RcbItemModel) {

        val totalFrames = (domain.refillCount * model.page2.config.refillSize)
        val successFrames = if (totalFrames == 0) 0 else totalFrames - domain.underflowCount

        val successPercent = if (totalFrames == 0) 0f else ((100f / totalFrames) * successFrames)
        val errorPercent =
            if (totalFrames == 0) 0f else ((100f / totalFrames) * domain.underflowCount)

        model.page3.successRate =
            resources.getString(R.string.buffer_item_accuracy_format, totalFrames, successPercent)

        model.page3.errorRate = resources.getString(
            R.string.buffer_item_accuracy_format,
            domain.underflowCount,
            errorPercent
        )
    }

    fun mapSelectedDevice(domainModel: DeviceDomain, model: RcbItemModel) {
        model.selectedDeviceId = domainModel.id
        model.header.deviceName = domainModel.name
        model.page1.baudRateBytes =
            resources.getString(R.string.buffer_item_page_0_baud, domainModel.baudRateBytes)
        model.page1.macAddress = domainModel.macAddress
        model.page1.pairingPin = domainModel.pairingPin
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

    fun mapState(domainModel: DeviceDomain, model: RcbItemModel) {
        when (domainModel.status) {
            RcbServiceState.DISCONNECTED -> {

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
            RcbServiceState.CONNECTING -> {
                model.header.isConnecting = true
                model.page1.connectButtonEnabled = false
            }
            RcbServiceState.SETUP -> {
                setMaxSize(model, domainModel.freeHeapBytes)
                model.header.isConnected = true
                model.header.isConnecting = false
                model.page2.applyButtonEnabled = true
            }
            RcbServiceState.READY -> {
                model.page1.connectButtonEnabled = false
                model.page2.applyButtonEnabled = false
                model.page3.resumeButtonEnabled = true
            }
            RcbServiceState.ERROR -> {
                model.header.isConnecting = false
                model.page1.connectButtonEnabled = true
                model.page3.isResumed = false
            }
            RcbServiceState.PAUSED -> {
                model.page3.isResumed = false
                model.page3.resumeButtonText = resources.getString(R.string.resume)
            }
            RcbServiceState.RESUMED -> {
                model.page3.isResumed = true
                model.page3.resumeButtonText = resources.getString(R.string.pause)
            }
        }

        val isConnected = isConnected(domainModel.status)
        model.header.isConnected = isConnected

        model.page1.status = getStatus(domainModel.status)
        model.page1.connectButtonEnabled = !isConnected
        when (isConnected) {
            true -> R.string.disconnect
            false -> R.string.connect
        }.let {
            model.page1.connectButtonText = resources.getString(it)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun getStatus(status: RcbServiceState) =
        status.name.toLowerCase(Locale.getDefault()).capitalize().also {
            return if (status == RcbServiceState.ERROR) {
                "$it (${status.message})"
            } else {
                it
            }
        }

    private fun setMaxSize(model: RcbItemModel, maxSize: Int) {
        model.page2.config.maxSize = resources.getString(
            R.string.buffer_item_page_1_max_size,
            maxSize
        )
    }

    private fun isConnected(status: RcbServiceState) =
        (status != RcbServiceState.CONNECTING &&
                status != RcbServiceState.DISCONNECTED &&
                status != RcbServiceState.ERROR)
}
