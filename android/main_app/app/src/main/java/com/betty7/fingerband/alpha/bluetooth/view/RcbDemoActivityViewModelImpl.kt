package com.betty7.fingerband.alpha.bluetooth.view

import androidx.lifecycle.MutableLiveData
import com.betty7.fingerband.alpha.bluetooth.domain.*
import java.lang.IllegalStateException

typealias A = List<Pair<Int, Pair<RcbItemModel, Int>>>

class RcbDemoActivityViewModelImpl(
    private val domainMapper: DomainMapper,
    private val rcbServiceOrchestrator: RcbServiceOrchestrator, // TODO: Add interactor!
    private val deviceRepoInteractor: DeviceRepositoryInteractor
) : RcbDemoActivityViewModel() {

    // LiveData
    override val showDeviceListLiveData: SingleLiveEvent<List<String>> = SingleLiveEvent()
    override val launchUrlLiveData: SingleLiveEvent<String> = SingleLiveEvent()
    override val bufferItemPageLiveData: SingleLiveEvent<Pair<Int, Int>> = SingleLiveEvent()
    override val itemModelsLiveData = MutableLiveData<List<RcbItemModel>>()

    // For fast lookup when we only have the rcb service id
    private val serviceToModelMap = mutableMapOf<Int, RcbItemModel>()
    // For maintaining the order of items
    private val itemOrderedList = mutableListOf<RcbItemModel>()

    override fun onStart() {
        rcbServiceOrchestrator.subscribe(::onBufferServiceStatus, ::onBufferAccuracy)
    }

    override fun checkRcbConfig(
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

        requireBufferItem(rcbServiceId).let {
            domainMapper.mapConfig(
                numberOfRefills.toSafeInt(),
                refillSize.toSafeInt(),
                windowSizeMs.toSafeInt(),
                maxUnderflows.toSafeInt(),
                it
            )
        }.also {
            emitModels()
        }
    }

    override fun onCreateRcbClicked() {
        val deviceNames = deviceRepoInteractor.getAllDeviceNames()
        showDeviceListLiveData.postValue(deviceNames)
    }

    private fun getDeviceDomain(deviceId: Int): DeviceDomain {
        return deviceRepoInteractor.getDeviceDomain(deviceId)
    }

    override fun onDeviceSelected(deviceDomainId: Int) {

        // Interactor
        val rcbServiceId = rcbServiceOrchestrator.createBufferService()
        val deviceDomain = getDeviceDomain(deviceDomainId)
        val model = RcbItemModel(rcbServiceId)
        domainMapper.mapSelectedDevice(deviceDomain, model)

        serviceToModelMap[rcbServiceId] = model
        itemOrderedList.add(model)

        // Hmmm... Why can't we use the domain above?!
        // There seems to be a mismatch wth ID's!
        val emptyDomain = DeviceDomain(rcbServiceId)
        setBufferItemPage(emptyDomain, model)
        domainMapper.mapState(emptyDomain, model)
        domainMapper.mapAccuracies(emptyDomain.accuracies, model)

        emitModels()
    }

    override fun onConnectRcbClicked(rcbServiceId: Int) =
        requireBufferItem(rcbServiceId).let {
            // Interactor
            rcbServiceOrchestrator.connectBufferService(
                rcbServiceId,
                getDeviceDomain(it.selectedDeviceId) // Move repo to interactor
            )
        }

    override fun onConfigureRbClicked(rcbServiceId: Int) {
        requireBufferItem(rcbServiceId).let {
            rcbServiceOrchestrator.configureBufferService(
                rcbServiceId,
                it.page2.config.refillCount,
                it.page2.config.refillSize,
                it.page2.config.windowSize,
                it.page2.config.maxUnderflows
            )
        }
    }

    private fun onBufferAccuracy(domain: DeviceAccuracyDomain) {
        requireBufferItem(domain.id).let {
            domainMapper.mapAccuracies(domain, it)
        }.also {
            emitModels()
        }
    }

    private fun onBufferServiceStatus(domain: DeviceDomain) {
        requireBufferItem(domain.id).let {
            setBufferItemPage(domain, it)
            domainMapper.mapState(domain, it)
        }.also {
            emitModels()
        }
    }

    private fun setBufferItemPage(deviceDomain: DeviceDomain, model: RcbItemModel) =
        domainMapper.mapPage(deviceDomain)?.let { page ->
            itemOrderedList.indexOfFirst {
                it.id == model.id
            }.let { index ->
                bufferItemPageLiveData.postValue(Pair(index, page))
            }
        }

    override fun setVibrateValue(rcbServiceId: Int, vibrateValue: Int) =
        rcbServiceOrchestrator.hackBufferValue(rcbServiceId, vibrateValue)

    // Interactor contains if logic
    override fun toggleRcb(rcbServiceId: Int) {
        requireBufferItem(rcbServiceId).let {
            if (it.page3.isResumed) {
                rcbServiceOrchestrator.pauseBufferService(rcbServiceId)
            } else {
                rcbServiceOrchestrator.startBufferService(rcbServiceId)
            }
        }.also {
            emitModels()
        }
    }

    override fun onResumeRcbClicked(rcbServiceId: Int) =
        rcbServiceOrchestrator.resumeBufferService(rcbServiceId)

    override fun onPauseRcbClicked(rcbServiceId: Int) =
        rcbServiceOrchestrator.pauseBufferService(rcbServiceId)

    override fun onPause() = itemOrderedList.forEach {
        onPauseRcbClicked(it.id)
    }

    // Interactor.stopAll()
    override fun onStop() = itemOrderedList.forEach {
        rcbServiceOrchestrator.stopBufferService(it.id)
    }

    override fun onProjectUrlClicked() =
        launchUrlLiveData.postValue(PROJECT_REPO_URL)

    override fun onProductUrlClicked(deviceDomainId: Int) =
        launchUrlLiveData.postValue(getDeviceDomain(deviceDomainId).productUrl)

    override fun onDeleteAllBuffersClicked() =
        itemOrderedList.forEach {
            rcbServiceOrchestrator.deleteBufferService(it.id)
            serviceToModelMap.remove(it.id)
        }.also {
            itemOrderedList.clear()
        }

    override fun onDeleteRcbItemClicked(rcbServiceId: Int) {

        rcbServiceOrchestrator.deleteBufferService(rcbServiceId)

        serviceToModelMap.remove(rcbServiceId)
            ?: throw IllegalStateException("Service with id $rcbServiceId is not in the service map")

        if (!itemOrderedList.removeAll { it.id == rcbServiceId }) {
            throw IllegalStateException("Model with id $rcbServiceId is not in the ordered list")
        }
    }

    // TODO: Split into 2 model streams: Add & Update
    private fun emitModels() {
        itemModelsLiveData.postValue(itemOrderedList)
    }

    private fun requireBufferItem(rcbServiceId: Int) =
        serviceToModelMap[rcbServiceId]
            ?: throw IllegalArgumentException("Invalid rcbServiceId: $rcbServiceId")

    companion object {
        private const val PROJECT_REPO_URL = "https://bitbucket.org/slambang2/fingerband"
    }
}
