package com.betty7.fingerband.alpha.bluetooth.view

import androidx.lifecycle.MutableLiveData
import com.betty7.fingerband.alpha.bluetooth.domain.*
import java.lang.IllegalStateException

typealias A = List<Pair<Int, Pair<RcbItemModel, Int>>>

// Responsible for mapping device-domains to item-models
class RcbDemoActivityViewModelImpl(
    private val domainMapper: DomainMapper,
    private val productUrlMapper: ProductUrlMapper,
    private val rcbOrchestratorInteractor: RcbOrchestratorInteractor
) : RcbDemoActivityViewModel() {

    // LiveData
    override val showDeviceListLiveData: SingleLiveEvent<List<Pair<Int, String>>> =
        SingleLiveEvent()
    override val launchUrlLiveData: SingleLiveEvent<String> = SingleLiveEvent()
    override val bufferItemPageLiveData: SingleLiveEvent<Pair<Int, Int>> = SingleLiveEvent()
    override val itemModelsLiveData = MutableLiveData<List<RcbItemModel>>()

    // For maintaining the order of items
    private val itemOrderedList = mutableListOf<RcbItemModel>()

    override fun onStart() =
        rcbOrchestratorInteractor.subscribe(::onDomainUpdated, ::onRcbServiceAccuracy)

    override fun checkRcbConfig(
        modelId: Int,
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

        requireBufferItemModel(modelId).let {
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

    override fun onCreateRcbServiceClicked() {
        val availableDeviceNames = rcbOrchestratorInteractor.getAvailableDeviceNames()
        showDeviceListLiveData.postValue(availableDeviceNames)
    }

    override fun onDeviceSelected(deviceDomainId: Int) {

        val deviceDomain = rcbOrchestratorInteractor.createRcbService(deviceDomainId)
        val rcbItemModel = domainMapper.mapSelectedDevice(deviceDomain)

        itemOrderedList.add(rcbItemModel)
        setBufferItemPage(deviceDomain, rcbItemModel)
        domainMapper.mapState(deviceDomain, rcbItemModel)
        domainMapper.mapAccuracies(deviceDomain.accuracies, rcbItemModel)

        emitModels()
    }

    override fun onConnectRcbClicked(modelId: Int) =
        rcbOrchestratorInteractor.connectBufferService(modelId)

    override fun onConfigureRbClicked(modelId: Int) =
        requireBufferItemModel(modelId).let {
            rcbOrchestratorInteractor.configureRcbService(
                modelId,
                it.page2.config.refillCount,
                it.page2.config.refillSize,
                it.page2.config.windowSize,
                it.page2.config.maxUnderflows
            )
        }

    private fun onRcbServiceAccuracy(domain: DeviceAccuracyDomain) =
        requireBufferItemModel(domain.id).let {
            domainMapper.mapAccuracies(domain, it)
        }.also {
            emitModels()
        }

    private fun onDomainUpdated(domain: DeviceDomain) =
        requireBufferItemModel(domain.id).let {
            setBufferItemPage(domain, it)
            domainMapper.mapState(domain, it)
        }.also {
            emitModels()
        }

    private fun setBufferItemPage(deviceDomain: DeviceDomain, model: RcbItemModel) =
        domainMapper.mapPage(deviceDomain)?.let { page ->
            itemOrderedList.indexOfFirst {
                it.id == model.id
            }.let { index ->
                bufferItemPageLiveData.postValue(Pair(index, page))
            }
        }

    override fun setVibrateValue(modelId: Int, vibrateValue: Int) =
        rcbOrchestratorInteractor.setVibrateValue(modelId, vibrateValue)

    override fun toggleRcb(modelId: Int) =
        requireBufferItemModel(modelId).let {
            rcbOrchestratorInteractor.toggleRcb(
                modelId,
                it.page3.isResumed
            )
                .also { emitModels() }
        }

    override fun onResumeRcbClicked(modelId: Int) =
        rcbOrchestratorInteractor.resumeRcbService(modelId)

    override fun onPauseRcbClicked(modelId: Int) =
        rcbOrchestratorInteractor.pauseRcbService(modelId)

    override fun onPause() =
        rcbOrchestratorInteractor.pauseAllRcbServices()

    override fun onStop() =
        rcbOrchestratorInteractor.stopAllRcbServices()

    override fun onProjectUrlClicked() =
        launchUrlLiveData.postValue(PROJECT_REPO_URL)

    override fun onProductUrlClicked(modelId: Int) =
        launchUrlLiveData.postValue(productUrlMapper.map(modelId))

    override fun onDeleteAllBuffersClicked() {
        rcbOrchestratorInteractor.deleteAllRcbServices()
        itemOrderedList.clear()
        emitModels()
    }

    override fun onDeleteRcbItemClicked(modelId: Int) {

        rcbOrchestratorInteractor.deleteRcbService(modelId)

        if (!itemOrderedList.removeAll { it.id == modelId }) {
            throw IllegalStateException("Model with id $modelId is not in the ordered list")
        }

        emitModels()
    }

    // Split into 2 model streams: Add & Update
    private fun emitModels() = itemModelsLiveData.postValue(itemOrderedList)

    private fun requireBufferItemModel(modelId: Int): RcbItemModel =
        itemOrderedList.find { it.id == modelId }
            ?: throw IllegalArgumentException("Invalid modelId: $modelId")

    companion object {
        private const val PROJECT_REPO_URL = "https://bitbucket.org/slambang2/fingerband"
    }
}
