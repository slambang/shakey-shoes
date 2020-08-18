package com.slambang.shakeyshoes.view.rcb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slambang.shakeyshoes.domain.BluetoothDeviceAccuracyDomain
import com.slambang.shakeyshoes.domain.BluetoothDeviceDomain
import com.slambang.shakeyshoes.util.SchedulerProvider
import com.slambang.shakeyshoes.view.base.SingleLiveEvent
import com.slambang.shakeyshoes.view.rcb.interactors.RcbOrchestratorInteractor
import com.slambang.shakeyshoes.view.rcb.mappers.*
import io.reactivex.disposables.CompositeDisposable
import java.lang.IllegalStateException
import javax.inject.Inject

class RcbViewModel @Inject constructor(
    private val navigator: RcbNavigator,
    private val schedulers: SchedulerProvider,
    private val disposables: CompositeDisposable,

    private val bluetoothInteractor: BluetoothInteractor,


    private val domainMapper: DomainMapper,
    private val productUrlMapper: ProductUrlMapper,
    private val rcbOrchestratorInteractor: RcbOrchestratorInteractor,
    private val orderedItemList: MutableList<RcbItemModel> = mutableListOf(),

    // LiveData
    private val _itemModelsLiveData: MutableLiveData<List<RcbItemModel>>,
    private val _bluetoothStatusLiveData: MutableLiveData<String>,
    private val _removeAllBuffersLiveData: SingleLiveEvent<Unit>,
    private val _removeAllMenuOptionEnabledLiveData: SingleLiveEvent<Boolean>,
    private val _showDeviceListLiveData: SingleLiveEvent<List<Pair<Int, String>>>,
    private val _bufferItemPageLiveData: SingleLiveEvent<Pair<Int, Int>>,
    private val _itemDeletedLiveData: SingleLiveEvent<Int>,
    private val _errorLiveData: SingleLiveEvent<String>
) : ViewModel() {

    val removeAllMenuOptionEnabledLiveData: LiveData<Boolean>
        get() = _removeAllMenuOptionEnabledLiveData

    val removeAllBuffersLiveData: LiveData<Unit>
        get() = _removeAllBuffersLiveData

    val showDeviceListLiveData: LiveData<List<Pair<Int, String>>>
        get() = _showDeviceListLiveData

    val bufferItemPageLiveData: LiveData<Pair<Int, Int>>
        get() = _bufferItemPageLiveData

    val itemModelsLiveData: LiveData<List<RcbItemModel>>
        get() = _itemModelsLiveData

    val itemDeletedLiveData: LiveData<Int>
        get() = _itemDeletedLiveData

    val bluetoothStatusLiveData: LiveData<String>
        get() = _bluetoothStatusLiveData

    val errorLiveData: LiveData<String>
        get() = _errorLiveData

    fun onResume() {
        observeBluetoothState()
        rcbOrchestratorInteractor.subscribe(::onDomainUpdated, ::onRcbServiceAccuracy)
    }

    private fun observeBluetoothState() {
        bluetoothInteractor.observeBluetoothStatus()
            .subscribeOn(schedulers.io)
            .subscribe({
                _bluetoothStatusLiveData.postValue(it)
            }, {
                _errorLiveData.postValue(it.message)
            })
            .also { disposables.add(it) }
    }

    // TODO Threading
    fun onAddRcbClicked() {
        val availableDeviceNames = rcbOrchestratorInteractor.getAvailableDeviceNames()
        _showDeviceListLiveData.postValue(availableDeviceNames)
    }

    // TODO Threading
    fun onConnectRcbClicked(modelId: Int) =
        rcbOrchestratorInteractor.connectBufferService(modelId)

    // TODO Threading
    fun onConfigureRbClicked(modelId: Int) =
        requireBufferItemModel(modelId).let {
            rcbOrchestratorInteractor.configureRcbService(
                modelId,
                it.page2.config.refillCount,
                it.page2.config.refillSize,
                it.page2.config.windowSize,
                it.page2.config.maxUnderflows
            )
        }

    // TODO Threading
    fun toggleRcb(modelId: Int) =
        requireBufferItemModel(modelId).let {
            rcbOrchestratorInteractor.toggleRcb(
                modelId,
                it.page3.isResumed
            )
                .also { emitModels() }
        }

    // TODO Threading
    fun onPauseRcbClicked(modelId: Int) =
        rcbOrchestratorInteractor.pauseRcbService(modelId)

    // TODO Threading
    fun onResumeRcbClicked(modelId: Int) =
        rcbOrchestratorInteractor.resumeRcbService(modelId)

    // TODO Threading
    fun onRemoveRcbItemClicked(modelId: Int) {

        rcbOrchestratorInteractor.deleteRcbService(modelId)

        val index = orderedItemList.indexOfFirst {
            it.id == modelId
        }

        if (index == -1) {
            throw IllegalStateException("Model with id $modelId is not in the ordered list")
        }

        orderedItemList.removeAt(index)
        _itemDeletedLiveData.postValue(index)
        _removeAllMenuOptionEnabledLiveData.postValue(orderedItemList.isNotEmpty())
    }

    // TODO Threading
    fun checkRcbConfig(
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

    // TODO Threading
    fun onPause() =
        rcbOrchestratorInteractor.pauseAllRcbServices()

    // TODO Threading
    fun onStop() =
        rcbOrchestratorInteractor.stopAllRcbServices()

    // TODO Threading
    fun onRcbDeviceSelected(deviceDomainId: Int) {

        val deviceDomain = rcbOrchestratorInteractor.createRcbService(deviceDomainId)
        val rcbItemModel = domainMapper.mapSelectedDevice(deviceDomain)

        orderedItemList.add(rcbItemModel)
        setBufferItemPage(deviceDomain, rcbItemModel)
        domainMapper.mapState(deviceDomain, rcbItemModel)
        domainMapper.mapAccuracies(deviceDomain.accuracies, rcbItemModel)

        emitModels()
        _removeAllMenuOptionEnabledLiveData.postValue(true)
    }

    // TODO Threading
    fun onProductUrlClicked(modelId: Int) =
        navigator.navigateToUrl(productUrlMapper.map(modelId))

    // TODO Threading
    fun onVisitRepoClicked() = navigator.navigateToUrl(PROJECT_REPO_URL)

    // TODO Threading
    fun onRemoveAllRcbsClicked() {
        rcbOrchestratorInteractor.deleteAllRcbServices()
        orderedItemList.clear()
        _removeAllBuffersLiveData.postValue(Unit)
        _removeAllMenuOptionEnabledLiveData.postValue(false)
    }

    // TODO Threading
    fun setVibrateValue(modelId: Int, vibrateValue: Int) =
        rcbOrchestratorInteractor.setVibrateValue(modelId, vibrateValue)

    override fun onCleared() {
        disposables.clear()
    }

    private fun displayErrorMessage(error: Throwable) {
        _errorLiveData.postValue("An error occurred: ${error.message}") // Add mapper
    }

    private fun onDomainUpdated(domain: BluetoothDeviceDomain) =
        requireBufferItemModel(domain.id).let {
            setBufferItemPage(domain, it)
            domainMapper.mapState(domain, it)
        }.also {
            emitModels()
        }

    private fun onRcbServiceAccuracy(domain: BluetoothDeviceAccuracyDomain) =
        requireBufferItemModel(domain.id).let {
            domainMapper.mapAccuracies(domain, it)
        }.also {
            emitModels()
        }

    private fun setBufferItemPage(deviceDomain: BluetoothDeviceDomain, model: RcbItemModel) =
        domainMapper.mapPage(deviceDomain)?.let { page ->
            orderedItemList.indexOfFirst {
                it.id == model.id
            }.let { index ->
                _bufferItemPageLiveData.postValue(Pair(index, page))
            }
        }

    // Split into 2 model streams: Add & Update
    private fun emitModels() = _itemModelsLiveData.postValue(orderedItemList)

    private fun requireBufferItemModel(modelId: Int): RcbItemModel =
        orderedItemList.find { it.id == modelId }
            ?: throw IllegalArgumentException("Invalid modelId: $modelId")

    companion object {
        private const val PROJECT_REPO_URL = "https://github.com/slambang/shakey_shoes"
    }
}
