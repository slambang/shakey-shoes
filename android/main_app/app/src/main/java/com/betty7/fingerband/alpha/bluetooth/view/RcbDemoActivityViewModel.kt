package com.betty7.fingerband.alpha.bluetooth.view

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.betty7.fingerband.alpha.bluetooth.domain.*

abstract class RcbDemoActivityViewModel : ViewModel() {

    abstract fun subscribe(
        owner: LifecycleOwner,
        showDeviceListObserver: (List<String>) -> Unit,
        bufferItemObserver: (BufferItemViewModel) -> Unit,
        launchUrlObserver: (String) -> Unit,
        bufferItemPageObserver: (Int, Int) -> Unit
    )

    // Lifecycle events
    abstract fun onPause()
    abstract fun onResume()
    abstract fun onStop()

    abstract fun onDeviceSelected(owner: LifecycleOwner, deviceId: Int)
    abstract fun onCreateBufferClicked()
    abstract fun onConnectBufferClicked(bufferServiceId: Int)
    abstract fun onConfigureBufferClicked(bufferServiceId: Int)
    abstract fun toggleBufferService(bufferServiceId: Int)
    abstract fun onResumeBufferClicked(bufferServiceId: Int)
    abstract fun onPauseBufferClicked(bufferServiceId: Int)
    abstract fun onProjectUrlClicked()
    abstract fun onProductUrlClicked(deviceId: Int)
    abstract fun onDeleteAllBuffersClicked()
    abstract fun onDeleteBufferItemClicked(bufferServiceId: Int)

    abstract fun onBluetoothDenied()
    abstract fun setVibrateValue(bufferServiceId: Int, vibrateValue: Int)
    abstract fun checkConfig(
        bufferServiceId: Int,
        numberOfRefills: String,
        refillSize: String,
        windowSizeMs: String,
        maxUnderflows: String
    )
}

class RcbDemoActivityViewModelImpl(
    private val domainMapper: DomainMapper,
    private val bufferOrchestrator: RcbServiceOrchestrator,
    private val deviceRepoInteractor: DeviceRepositoryInteractor,
    private val showDeviceListLiveData:SingleLiveEvent<List<String>> = SingleLiveEvent(),
    private val launchUrlLiveData: SingleLiveEvent<String> = SingleLiveEvent(),
    private val bufferItemLiveDataMap: MutableMap<Int, DefaultLiveData<BufferItemViewModel>> = mutableMapOf(),
    private val bufferItemPageLiveData: SingleLiveEvent<Pair<Int, Int>> = SingleLiveEvent()
) : RcbDemoActivityViewModel() {

    private lateinit var bufferItemObserver: (BufferItemViewModel) -> Unit

    init {
        bufferOrchestrator.subscribe(::onBufferServiceStatus, ::onBufferAccuracy)
    }

    // TODO: This should be handled by the Activity & its VM!
    override fun onBluetoothDenied() {}

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
        bufferServiceId: Int,
        numberOfRefills: String,
        refillSize: String,
        windowSizeMs: String,
        maxUnderflows: String
    ) {
        fun String.toSafeInt() =
            try {
                toInt()
            } catch (_: NumberFormatException) {
                0
            }

        requireBufferItem(bufferServiceId).apply {
            domainMapper.mapConfig(
                numberOfRefills.toSafeInt(),
                refillSize.toSafeInt(),
                windowSizeMs.toSafeInt(),
                maxUnderflows.toSafeInt(),
                value
            )
        }.repostValue()
    }

    override fun onCreateBufferClicked() {
        val deviceNames = deviceRepoInteractor.getDeviceNames()
        showDeviceListLiveData.postValue(deviceNames)
    }

    private fun getDeviceDomain(deviceId: Int): DeviceDomain {
        return deviceRepoInteractor.getDeviceDomain(deviceId)
    }

    override fun onDeviceSelected(owner: LifecycleOwner, deviceId: Int) {

        val bufferServiceId = bufferOrchestrator.createBufferService()

        bufferItemLiveDataMap[bufferServiceId] = DefaultLiveData(BufferItemViewModel(bufferServiceId)).apply {
            observe(owner, Observer { bufferItemObserver(it) })
        }

        bufferOrchestrator.beginBufferService(bufferServiceId)

        requireBufferItem(bufferServiceId).apply {
            val deviceDomain = getDeviceDomain(deviceId)
            domainMapper.mapSelectedDevice(deviceDomain, value)
        }.repostValue()
    }

    override fun onConnectBufferClicked(bufferServiceId: Int) =
        requireBufferItem(bufferServiceId).value.let {
            bufferOrchestrator.connectBufferService(bufferServiceId, getDeviceDomain(it.selectedDeviceId))
        }

    override fun onConfigureBufferClicked(bufferServiceId: Int) {
        requireBufferItem(bufferServiceId).apply {
            bufferOrchestrator.configureBufferService(
                bufferServiceId,
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

    private fun onBufferServiceStatus(domain: DeviceDomain) =
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

    override fun setVibrateValue(bufferServiceId: Int, vibrateValue: Int) =
        bufferOrchestrator.hackBufferValue(bufferServiceId, vibrateValue)

    override fun toggleBufferService(bufferServiceId: Int) =
        requireBufferItem(bufferServiceId).apply {
            if (value.page2.isResumed) {
                bufferOrchestrator.pauseBufferService(bufferServiceId)
            } else {
                bufferOrchestrator.startBufferService(bufferServiceId)
            }
        }.repostValue()

    override fun onResumeBufferClicked(bufferServiceId: Int) =
        bufferOrchestrator.resumeBufferService(bufferServiceId)

    override fun onPauseBufferClicked(bufferServiceId: Int) =
        bufferOrchestrator.pauseBufferService(bufferServiceId)

    override fun onPause() =
        bufferItemLiveDataMap.keys.forEach { onPauseBufferClicked(it) }

    override fun onResume() =
        bufferItemLiveDataMap.keys.forEach { onResumeBufferClicked(it) }

    override fun onStop() =
        bufferItemLiveDataMap.keys.forEach { bufferOrchestrator.stopBufferService(it) }

    override fun onProjectUrlClicked() =
        launchUrlLiveData.postValue(PROJECT_REPO_URL)

    override fun onProductUrlClicked(deviceId: Int) =
        launchUrlLiveData.postValue(getDeviceDomain(deviceId).productUrl)

    override fun onDeleteAllBuffersClicked() =
        with(bufferItemLiveDataMap.iterator()) {
            forEach {
                bufferOrchestrator.deleteBufferService(it.key)
                remove()
            }
        }

    override fun onDeleteBufferItemClicked(bufferServiceId: Int) {
        bufferItemLiveDataMap.remove(bufferServiceId)?.let {
            bufferOrchestrator.deleteBufferService(bufferServiceId)
        }
    }

    private fun requireBufferItem(bufferServiceId: Int) =
        bufferItemLiveDataMap[bufferServiceId]
            ?: throw IllegalArgumentException("Invalid bufferServiceId: $bufferServiceId")

    companion object {
        private const val PROJECT_REPO_URL = "https://bitbucket.org/slambang2/fingerband"
    }
}
