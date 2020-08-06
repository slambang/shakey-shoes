package com.betty7.fingerband.alpha.bluetooth.view

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.betty7.fingerband.alpha.bluetooth.domain.RcbServiceOrchestrator
import com.betty7.fingerband.alpha.bluetooth.domain.CircularBufferStatus
import com.betty7.fingerband.alpha.bluetooth.domain.DeviceAccuracyDomain
import com.betty7.fingerband.alpha.bluetooth.domain.DeviceDomain

abstract class RcbDemoActivityViewModel : ViewModel() {

    abstract fun subscribe(
        owner: LifecycleOwner,
        showDeviceListObserver: (List<String>) -> Unit,
        bufferItemObserver: (BufferItemViewModel) -> Unit,
        launchUrlObserver: (String) -> Unit,
        bufferItemPageObserver: (Int, Int) -> Unit
    )

    abstract fun pause()
    abstract fun resume()
    abstract fun stop()

    abstract fun onDeviceSelected(owner: LifecycleOwner, deviceId: Int)

    abstract fun createBuffer()
    abstract fun connectBuffer(bufferId: Int)
    abstract fun configureBuffer(bufferId: Int)
    abstract fun toggleBuffer(bufferId: Int)
    abstract fun resumeBuffer(bufferId: Int)
    abstract fun pauseBuffer(bufferId: Int)
    abstract fun stopBuffer(bufferId: Int)
    abstract fun checkConfig(
        bufferId: Int,
        numberOfRefills: String,
        refillSize: String,
        windowSizeMs: String,
        maxUnderflows: String
    )

    abstract fun onProjectUrlClicked()
    abstract fun onProductUrlClicked(deviceId: Int)

    abstract fun deleteAllBuffers()
    abstract fun deleteBufferItem(bufferId: Int)

    abstract fun bluetoothDenied()
    abstract fun setVibrateValue(bufferId: Int, vibrateValue: Int)
}

class RcbDemoActivityViewModelImpl(
    private val domainMapper: DomainMapper,
    private val bufferOrchestrator: RcbServiceOrchestrator
) : RcbDemoActivityViewModel() {

    private lateinit var bufferItemObserver: (BufferItemViewModel) -> Unit

    private val showDeviceListLiveData = SingleLiveEvent<List<String>>()
    private val launchUrlLiveData = SingleLiveEvent<String>()
    private val bufferItemLiveDataMap = mutableMapOf<Int, DefaultLiveData<BufferItemViewModel>>()
    private val bufferItemPageLiveData = SingleLiveEvent<Pair<Int, Int>>()

    init {
        bufferOrchestrator.bufferStateObserver = ::onBufferState
        bufferOrchestrator.bufferAccuracyObserver = ::onBufferAccuracy
    }

    override fun bluetoothDenied() { /* TODO */
    }

    override fun subscribe(
        owner: LifecycleOwner,
        showDeviceListObserver: (List<String>) -> Unit,
        bufferItemObserver: (BufferItemViewModel) -> Unit,
        launchUrlObserver: (String) -> Unit,
        bufferItemPageObserver: (Int, Int) -> Unit
    ) {
        this.bufferItemObserver = bufferItemObserver
        showDeviceListLiveData.observe(owner, Observer { showDeviceListObserver(it) })
        launchUrlLiveData.observe(owner, Observer { launchUrlObserver(it) })
        bufferItemPageLiveData.observe(
            owner,
            Observer { bufferItemPageObserver(it.first, it.second) })
    }

    override fun checkConfig(
        bufferId: Int,
        numberOfRefills: String,
        refillSize: String,
        windowSizeMs: String,
        maxUnderflows: String
    ) = requireBufferItem(bufferId).apply {
        domainMapper.mapConfig(
            numberOfRefills.toSafeInt(),
            refillSize.toSafeInt(),
            windowSizeMs.toSafeInt(),
            maxUnderflows.toSafeInt(),
            value
        )
    }.repostValue()

    override fun createBuffer() {
        val deviceDomains = bufferOrchestrator.getDeviceDomains()
        val deviceNames = domainMapper.mapDeviceNames(deviceDomains)
        showDeviceListLiveData.postValue(deviceNames)
    }

    override fun onDeviceSelected(owner: LifecycleOwner, deviceId: Int) {

        val bufferId = bufferOrchestrator.createBuffer()

        bufferItemLiveDataMap[bufferId] = DefaultLiveData(BufferItemViewModel(bufferId)).apply {
            observe(owner, Observer { bufferItemObserver(it) })
        }

        bufferOrchestrator.begin(bufferId)

        requireBufferItem(bufferId).apply {
            val deviceDomain = bufferOrchestrator.getDeviceDomain(deviceId)
            domainMapper.mapSelectedDevice(deviceDomain, value)
        }.repostValue()
    }

    private fun String.toSafeInt() =
        try {
            toInt()
        } catch (_: NumberFormatException) {
            0
        }

    override fun connectBuffer(bufferId: Int) =
        requireBufferItem(bufferId).value.let {
            bufferOrchestrator.connectBuffer(bufferId, it.selectedDeviceId)
        }

    override fun configureBuffer(bufferId: Int) {
        requireBufferItem(bufferId).apply {
            bufferOrchestrator.configureBuffer(
                bufferId,
                value.page1.config.refillCount,
                value.page1.config.refillSize,
                value.page1.config.windowSize,
                value.page1.config.maxUnderflows
            )
        }
    }

    private fun onBufferAccuracy(domain: DeviceAccuracyDomain) =
        requireBufferItem(domain.id).apply {
            domainMapper.mapAccuracies(domain, value)
        }.repostValue()

    private fun onBufferState(domain: DeviceDomain) =
        requireBufferItem(domain.id).apply {
            domainMapper.mapState(domain, value)
            setBufferItemPage(domain, value)
        }.repostValue()

    private fun setBufferItemPage(deviceDomain: DeviceDomain, model: BufferItemViewModel) =
        when (deviceDomain.status) {
            CircularBufferStatus.ERROR -> 0
            CircularBufferStatus.SETUP -> 1
            CircularBufferStatus.READY -> 2
            else -> null
        }?.let {
            bufferItemPageLiveData.postValue(Pair(model.id, it))
        }

    override fun setVibrateValue(bufferId: Int, vibrateValue: Int) =
        bufferOrchestrator.hackBufferValue(bufferId, vibrateValue)

    override fun toggleBuffer(bufferId: Int) =
        requireBufferItem(bufferId).apply {
            if (value.page2.isResumed) {
                bufferOrchestrator.pause(bufferId)
            } else {
                bufferOrchestrator.start(bufferId)
            }
        }.repostValue()

    override fun resumeBuffer(bufferId: Int) = bufferOrchestrator.resume(bufferId)

    override fun pauseBuffer(bufferId: Int) = bufferOrchestrator.pause(bufferId)

    override fun stopBuffer(bufferId: Int) = bufferOrchestrator.stop(bufferId)

    override fun pause() = bufferItemLiveDataMap.keys.forEach { pauseBuffer(it) }

    override fun resume() = bufferItemLiveDataMap.keys.forEach { resumeBuffer(it) }

    override fun stop() = bufferItemLiveDataMap.keys.forEach { stopBuffer(it) }

    override fun onProjectUrlClicked() = launchUrlLiveData.postValue(PROJECT_REPO_URL)

    override fun onProductUrlClicked(deviceId: Int) =
        launchUrlLiveData.postValue(bufferOrchestrator.getDeviceDomain(deviceId).productUrl)

    override fun deleteAllBuffers() =
        with(bufferItemLiveDataMap.iterator()) {
            forEach {
                bufferOrchestrator.deleteBuffer(it.key)
                remove()
            }
        }

    override fun deleteBufferItem(bufferId: Int) {
        bufferItemLiveDataMap.remove(bufferId)?.let {
            bufferOrchestrator.deleteBuffer(bufferId)
        }
    }

    private fun requireBufferItem(bufferId: Int) =
        bufferItemLiveDataMap[bufferId]
            ?: throw IllegalArgumentException("Invalid bufferId: $bufferId")

    companion object {
        private const val PROJECT_REPO_URL = "https://bitbucket.org/slambang2/fingerband"
    }
}
