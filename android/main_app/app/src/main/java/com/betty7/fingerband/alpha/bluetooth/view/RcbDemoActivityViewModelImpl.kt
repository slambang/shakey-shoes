package com.betty7.fingerband.alpha.bluetooth.view

import androidx.lifecycle.MutableLiveData
import com.betty7.fingerband.alpha.bluetooth.domain.*
import java.lang.IllegalStateException

typealias A = List<Pair<Int, Pair<RcbItemModel, Int>>>

// Responsible for mapping device domains to item models
class RcbDemoActivityViewModelImpl(
    private val domainMapper: DomainMapper,
    private val repoInteractor: DeviceRepositoryInteractor,
    private val rcbOrchestratorInteractor: RcbOrchestratorInteractor
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

    override fun onStart() =
        rcbOrchestratorInteractor.subscribe(::onBufferServiceStatus, ::onBufferAccuracy)

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
        val deviceNames = rcbOrchestratorInteractor.getAvailableDeviceNames()
        showDeviceListLiveData.postValue(deviceNames)
    }

    override fun onDeviceSelected(deviceDomainId: Int) {

        val (rcbServiceId, deviceDomain) = rcbOrchestratorInteractor.createRcbService(deviceDomainId)

        val model = RcbItemModel(rcbServiceId)
        domainMapper.mapSelectedDevice(deviceDomain, model)

        serviceToModelMap[rcbServiceId] = model
        itemOrderedList.add(model)

        setBufferItemPage(deviceDomain, model)
        domainMapper.mapState(deviceDomain, model)
        domainMapper.mapAccuracies(deviceDomain.accuracies, model)

        emitModels()
    }

    override fun onConnectRcbClicked(rcbServiceId: Int) =
        rcbOrchestratorInteractor.connectBufferService(rcbServiceId)

    override fun onConfigureRbClicked(rcbServiceId: Int) =
        requireBufferItem(rcbServiceId).let {
            rcbOrchestratorInteractor.configureRcbService(
                rcbServiceId,
                it.page2.config.refillCount,
                it.page2.config.refillSize,
                it.page2.config.windowSize,
                it.page2.config.maxUnderflows
            )
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
        rcbOrchestratorInteractor.setVibrateValue(rcbServiceId, vibrateValue)

    override fun toggleRcb(rcbServiceId: Int) =
        requireBufferItem(rcbServiceId).let {
            rcbOrchestratorInteractor.toggleRcb(
                rcbServiceId,
                it.page3.isResumed
            )
                .also { emitModels() }
        }

    override fun onResumeRcbClicked(rcbServiceId: Int) =
        rcbOrchestratorInteractor.resumeRcbService(rcbServiceId)

    override fun onPauseRcbClicked(rcbServiceId: Int) =
        rcbOrchestratorInteractor.pauseRcbService(rcbServiceId)

    override fun onPause() =
        rcbOrchestratorInteractor.pauseAllRcbServices()

    override fun onStop() =
        rcbOrchestratorInteractor.stopAllRcbServices()

    override fun onProjectUrlClicked() =
        launchUrlLiveData.postValue(PROJECT_REPO_URL)

    override fun onProductUrlClicked(deviceDomainId: Int) =
        launchUrlLiveData.postValue(repoInteractor.getProductUrl(deviceDomainId))

    override fun onDeleteAllBuffersClicked() {
        rcbOrchestratorInteractor.deleteAllRcbServices()
        serviceToModelMap.clear()
        itemOrderedList.clear()
        emitModels()
    }

    override fun onDeleteRcbItemClicked(rcbServiceId: Int) {

        rcbOrchestratorInteractor.deleteRcbService(rcbServiceId)

        serviceToModelMap.remove(rcbServiceId)
            ?: throw IllegalStateException("Service with id $rcbServiceId is not in the service map")

        if (!itemOrderedList.removeAll { it.id == rcbServiceId }) {
            throw IllegalStateException("Model with id $rcbServiceId is not in the ordered list")
        }

        emitModels()
    }

    // Split into 2 model streams: Add & Update
    private fun emitModels() = itemModelsLiveData.postValue(itemOrderedList)

    private fun requireBufferItem(rcbServiceId: Int): RcbItemModel =
        serviceToModelMap[rcbServiceId]
            ?: throw IllegalArgumentException("Invalid rcbServiceId: $rcbServiceId")

    companion object {
        private const val PROJECT_REPO_URL = "https://bitbucket.org/slambang2/fingerband"
    }
}
