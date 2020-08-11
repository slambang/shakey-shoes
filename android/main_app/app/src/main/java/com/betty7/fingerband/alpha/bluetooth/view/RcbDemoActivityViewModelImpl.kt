package com.betty7.fingerband.alpha.bluetooth.view

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.betty7.fingerband.alpha.bluetooth.domain.*

class RcbDemoActivityViewModelImpl(
    private val domainMapper: DomainMapper,
    private val rcbServiceOrchestrator: RcbServiceOrchestrator,
    private val deviceRepoInteractor: DeviceRepositoryInteractor,
    private val showDeviceListLiveData:SingleLiveEvent<List<String>> = SingleLiveEvent(),
    private val launchUrlLiveData: SingleLiveEvent<String> = SingleLiveEvent(),
    private val rcbItemLiveDataMap: MutableMap<Int, DefaultLiveData<RcbItemModel>> = mutableMapOf(),
    private val bufferItemPageLiveData: SingleLiveEvent<Pair<Int, Int>> = SingleLiveEvent()
) : RcbDemoActivityViewModel() {

    private lateinit var bufferItemObserver: (RcbItemModel) -> Unit

    // TODO: This should be handled by the Activity & its VM!
    override fun onBluetoothDenied() {}

    override fun subscribe(
        owner: LifecycleOwner,
        showDeviceListObserver: (List<String>) -> Unit,
        bufferItemObserver: (RcbItemModel) -> Unit,
        launchUrlObserver: (String) -> Unit,
        bufferItemPageObserver: (Int, Int) -> Unit
    ) {
        rcbServiceOrchestrator.subscribe(::onBufferServiceStatus, ::onBufferAccuracy)
        this.bufferItemObserver = bufferItemObserver
        showDeviceListLiveData.observe(owner, Observer { showDeviceListObserver(it) })
        launchUrlLiveData.observe(owner, Observer { launchUrlObserver(it) })
        bufferItemPageLiveData.observe(
            owner,
            Observer { bufferItemPageObserver(it.first, it.second) })
    }

    override fun checkConfig(
        rcbServiceId: Int,
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

        requireBufferItem(rcbServiceId).apply {
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

        val rcbServiceId = rcbServiceOrchestrator.createBufferService()

        rcbItemLiveDataMap[rcbServiceId] = DefaultLiveData(RcbItemModel(rcbServiceId)).apply {
            observe(owner, Observer { bufferItemObserver(it) })
        }

        rcbServiceOrchestrator.beginBufferService(rcbServiceId)

        requireBufferItem(rcbServiceId).apply {
            val deviceDomain = getDeviceDomain(deviceId)
            domainMapper.mapSelectedDevice(deviceDomain, value)
        }.repostValue()
    }

    override fun onConnectBufferClicked(rcbServiceId: Int) =
        requireBufferItem(rcbServiceId).value.let {
            rcbServiceOrchestrator.connectBufferService(rcbServiceId, getDeviceDomain(it.selectedDeviceId))
        }

    override fun onConfigureBufferClicked(rcbServiceId: Int) {
        requireBufferItem(rcbServiceId).apply {
            rcbServiceOrchestrator.configureBufferService(
                rcbServiceId,
                value.page2.config.refillCount,
                value.page2.config.refillSize,
                value.page2.config.windowSize,
                value.page2.config.maxUnderflows
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

    private fun setBufferItemPage(deviceDomain: DeviceDomain, model: RcbItemModel) =
        when (deviceDomain.status) {
            RcbServiceState.ERROR -> 0
            RcbServiceState.SETUP -> 1
            RcbServiceState.READY -> 2
            else -> null
        }?.let {
            bufferItemPageLiveData.postValue(Pair(model.id, it))
        }

    override fun setVibrateValue(rcbServiceId: Int, vibrateValue: Int) =
        rcbServiceOrchestrator.hackBufferValue(rcbServiceId, vibrateValue)

    override fun toggleBufferService(rcbServiceId: Int) =
        requireBufferItem(rcbServiceId).apply {
            if (value.page3.isResumed) {
                rcbServiceOrchestrator.pauseBufferService(rcbServiceId)
            } else {
                rcbServiceOrchestrator.startBufferService(rcbServiceId)
            }
        }.repostValue()

    override fun onResumeBufferClicked(rcbServiceId: Int) =
        rcbServiceOrchestrator.resumeBufferService(rcbServiceId)

    override fun onPauseBufferClicked(rcbServiceId: Int) =
        rcbServiceOrchestrator.pauseBufferService(rcbServiceId)

    override fun onPause() =
        rcbItemLiveDataMap.keys.forEach { onPauseBufferClicked(it) }

    override fun onResume() =
        rcbItemLiveDataMap.keys.forEach { onResumeBufferClicked(it) }

    override fun onStop() =
        rcbItemLiveDataMap.keys.forEach { rcbServiceOrchestrator.stopBufferService(it) }

    override fun onProjectUrlClicked() =
        launchUrlLiveData.postValue(PROJECT_REPO_URL)

    override fun onProductUrlClicked(deviceId: Int) =
        launchUrlLiveData.postValue(getDeviceDomain(deviceId).productUrl)

    override fun onDeleteAllBuffersClicked() =
        with(rcbItemLiveDataMap.iterator()) {
            forEach {
                rcbServiceOrchestrator.deleteBufferService(it.key)
                remove()
            }
        }

    override fun onDeleteBufferItemClicked(rcbServiceId: Int) {
        rcbItemLiveDataMap.remove(rcbServiceId)?.let {
            rcbServiceOrchestrator.deleteBufferService(rcbServiceId)
        }
    }

    private fun requireBufferItem(rcbServiceId: Int) =
        rcbItemLiveDataMap[rcbServiceId]
            ?: throw IllegalArgumentException("Invalid rcbServiceId: $rcbServiceId")

    companion object {
        private const val PROJECT_REPO_URL = "https://bitbucket.org/slambang2/fingerband"
    }
}
