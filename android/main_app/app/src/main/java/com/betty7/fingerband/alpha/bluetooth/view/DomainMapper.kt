package com.betty7.fingerband.alpha.bluetooth.view

import android.annotation.SuppressLint
import com.betty7.fingerband.alpha.R
import com.betty7.fingerband.alpha.bluetooth.domain.CircularBufferStatus
import com.betty7.fingerband.alpha.bluetooth.domain.DeviceAccuracyDomain
import com.betty7.fingerband.alpha.bluetooth.domain.DeviceDomain
import java.util.*

class DomainMapper(private val resources: ViewResources) {

    fun mapDeviceNames(domains: List<DeviceDomain>) = domains.map { it.name }

    fun mapAccuracies(domain: DeviceAccuracyDomain, viewModel: BufferItemViewModel) {

        val totalFrames = (domain.refillCount * viewModel.page1.config.refillSize)
        val successFrames = if (totalFrames == 0) 0 else totalFrames - domain.underflowCount

        val successPercent = if (totalFrames == 0) 0f else ((100f / totalFrames) * successFrames)
        val errorPercent =
            if (totalFrames == 0) 0f else ((100f / totalFrames) * domain.underflowCount)

        viewModel.page2.successRate =
            resources.getString(R.string.buffer_item_accuracy_format, totalFrames, successPercent)

        viewModel.page2.errorRate = resources.getString(
            R.string.buffer_item_accuracy_format,
            domain.underflowCount,
            errorPercent
        )
    }

    fun mapSelectedDevice(domainModel: DeviceDomain, viewModel: BufferItemViewModel) {
        viewModel.selectedDeviceId = domainModel.id
        viewModel.header.deviceName = domainModel.name
        viewModel.page0.baudRateBytes =
            resources.getString(R.string.buffer_item_page_0_baud, domainModel.baudRateBytes)
        viewModel.page0.macAddress = domainModel.macAddress
        viewModel.page0.pairingPin = domainModel.pairingPin
    }

    fun mapConfig(
        numberOfRefills: Int,
        refillSize: Int,
        windowSizeMs: Int,
        maxUnderflows: Int,
        viewModel: BufferItemViewModel
    ) {
        viewModel.page1.config.refillCount = numberOfRefills
        viewModel.page1.config.refillSize = refillSize
        viewModel.page1.config.windowSize = windowSizeMs
        viewModel.page1.config.maxUnderflows = maxUnderflows

        viewModel.page1.config.actualSize = resources.getString(
            R.string.buffer_item_page_1_actual_size,
            refillSize * numberOfRefills
        )

        viewModel.page1.config.latency = resources.getString(
            R.string.buffer_item_page_1_latency,
            (refillSize * numberOfRefills) * windowSizeMs
        )

        viewModel.page1.config.maxUnderflowTime = resources.getString(
            R.string.buffer_item_page_1_underflow_time,
            windowSizeMs * maxUnderflows
        )
    }

    fun mapState(domainModel: DeviceDomain, viewModel: BufferItemViewModel) {
        when (domainModel.status) {
            CircularBufferStatus.DISCONNECTED -> {

                viewModel.page2.resumeButtonText = resources.getString(R.string.resume)
                setMaxSize(viewModel, domainModel.freeHeapBytes)

                mapConfig(
                    viewModel.page1.config.refillCount,
                    viewModel.page1.config.refillSize,
                    viewModel.page1.config.windowSize,
                    viewModel.page1.config.maxUnderflows,
                    viewModel
                )
            }
            CircularBufferStatus.CONNECTING -> {
                viewModel.header.isConnecting = true
                viewModel.page0.connectButtonEnabled = false
            }
            CircularBufferStatus.SETUP -> {
                setMaxSize(viewModel, domainModel.freeHeapBytes)
                viewModel.header.isConnected = true
                viewModel.header.isConnecting = false
                viewModel.page1.applyButtonEnabled = true
            }
            CircularBufferStatus.READY -> {
                viewModel.page0.connectButtonEnabled = false
                viewModel.page1.applyButtonEnabled = false
                viewModel.page2.resumeButtonEnabled = true
            }
            CircularBufferStatus.ERROR -> {
                viewModel.header.isConnecting = false
                viewModel.page0.connectButtonEnabled = true
                viewModel.page2.isResumed = false
            }
            CircularBufferStatus.PAUSED -> {
                viewModel.page2.isResumed = false
                viewModel.page2.resumeButtonText = resources.getString(R.string.resume)
            }
            CircularBufferStatus.RESUMED -> {
                viewModel.page2.isResumed = true
                viewModel.page2.resumeButtonText = resources.getString(R.string.pause)
            }
        }

        val isConnected = isConnected(domainModel.status)
        viewModel.header.isConnected = isConnected

        viewModel.page0.status = getStatus(domainModel.status)
        viewModel.page0.connectButtonEnabled = !isConnected
        when (isConnected) {
            true -> R.string.disconnect
            false -> R.string.connect
        }.let {
            viewModel.page0.connectButtonText = resources.getString(it)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun getStatus(status: CircularBufferStatus) =
        status.name.toLowerCase(Locale.getDefault()).capitalize().also {
            return if (status == CircularBufferStatus.ERROR) {
                "$it (${status.message})"
            } else {
                it
            }
        }

    private fun setMaxSize(viewModel: BufferItemViewModel, maxSize: Int) {
        viewModel.page1.config.maxSize = resources.getString(
            R.string.buffer_item_page_1_max_size,
            maxSize
        )
    }

    private fun isConnected(status: CircularBufferStatus) =
        (status != CircularBufferStatus.CONNECTING &&
                status != CircularBufferStatus.DISCONNECTED &&
                status != CircularBufferStatus.ERROR)
}
