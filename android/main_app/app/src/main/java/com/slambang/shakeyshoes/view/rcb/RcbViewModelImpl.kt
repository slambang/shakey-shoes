package com.slambang.shakeyshoes.view.rcb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slambang.shakeyshoes.domain.BluetoothDeviceAccuracyDomain
import com.slambang.shakeyshoes.domain.BluetoothDeviceDomain
import com.slambang.shakeyshoes.domain.use_cases.RcbOrchestratorUseCase
import com.slambang.shakeyshoes.util.SchedulerProvider
import com.slambang.shakeyshoes.view.base.SingleLiveEvent
import com.slambang.shakeyshoes.view.rcb.mappers.ErrorMapper
import com.slambang.shakeyshoes.view.rcb.mappers.RcmItemModelMapper
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * TODO
 *  - Map/visualise models: Entity -> Domain -> View (how does it look?)
 *  - Do we need separate RcbOrchestratorUseCase & RcbServiceOrchestrator?
 *  - Improve ViewModel -> View communication.
 *      - Improve RecyclerViews (use payloads!)
 */
class RcbViewModelImpl @Inject constructor(
    private val errorMapper: ErrorMapper,
    private val navigator: RcbViewNavigator,
    private val schedulers: SchedulerProvider,
    private val disposables: CompositeDisposable,
    private val itemModelMapper: RcmItemModelMapper,
    private val orderedItemList: MutableList<RcbItemModel>,
    private val bluetoothStateObserver: BluetoothStateObserver,
    private val rcbOrchestratorUseCase: RcbOrchestratorUseCase,
    // LiveData
    private val _errorLiveData: SingleLiveEvent<String>,
    private val _itemDeletedLiveData: SingleLiveEvent<Int>,
    private val _removeAllBuffersLiveData: SingleLiveEvent<Unit>,
    private val _bluetoothStatusLiveData: MutableLiveData<String>,
    private val _bufferItemPageLiveData: SingleLiveEvent<Pair<Int, Int>>,
    private val _itemModelsLiveData: MutableLiveData<List<RcbItemModel>>,
    private val _removeAllMenuOptionEnabledLiveData: SingleLiveEvent<Boolean>,
    private val _showDeviceListLiveData: SingleLiveEvent<List<Pair<Int, String>>>,
) : RcbViewModel, ViewModel() {

    // Is there a cleaner way to do this?
    override val removeAllMenuOptionEnabledLiveData: LiveData<Boolean>
        get() = _removeAllMenuOptionEnabledLiveData

    override val removeAllBuffersLiveData: LiveData<Unit>
        get() = _removeAllBuffersLiveData

    override val showDeviceListLiveData: LiveData<List<Pair<Int, String>>>
        get() = _showDeviceListLiveData

    override val bufferItemPageLiveData: LiveData<Pair<Int, Int>>
        get() = _bufferItemPageLiveData

    override val itemModelsLiveData: LiveData<List<RcbItemModel>>
        get() = _itemModelsLiveData

    override val itemDeletedLiveData: LiveData<Int>
        get() = _itemDeletedLiveData

    override val bluetoothStatusLiveData: LiveData<String>
        get() = _bluetoothStatusLiveData

    override val errorLiveData: LiveData<String>
        get() = _errorLiveData

    override fun onResume() = async {
        observeBluetoothState()
        rcbOrchestratorUseCase.subscribe(::onDomainUpdated, ::onRcbServiceAccuracy)
    }

    private fun observeBluetoothState() {
        bluetoothStateObserver.observeBluetoothStatus()
            .subscribeOn(schedulers.io)
            .subscribe({
                _bluetoothStatusLiveData.postValue(it)
            }, {
                _errorLiveData.postValue(it.message)
            })
            .also { disposables.add(it) }
    }

    override fun onAddRcbClicked() = async {
        val availableDeviceNames = rcbOrchestratorUseCase.getAvailableDeviceNames()
        _showDeviceListLiveData.postValue(availableDeviceNames)
    }

    override fun onConnectRcbClicked(modelId: Int) = async {
        rcbOrchestratorUseCase.connectBufferService(modelId)
    }

    override fun onConfigureRbClicked(modelId: Int) = async {
        requireBufferItemModel(modelId).let {
            rcbOrchestratorUseCase.configureRcbService(
                modelId,
                it.page2.config.refillCount,
                it.page2.config.refillSize,
                it.page2.config.windowSize,
                it.page2.config.maxUnderflows
            )
        }
    }

    override fun onToggleRcb(modelId: Int) = async {
        requireBufferItemModel(modelId).let {
            rcbOrchestratorUseCase.toggleRcb(
                modelId,
                it.page3.isResumed
            )
                .also { emitModels() }
        }
    }

    override fun onPauseRcbClicked(modelId: Int) = async {
        rcbOrchestratorUseCase.pauseRcbService(modelId)
    }

    override fun onResumeRcbClicked(modelId: Int) = async {
        rcbOrchestratorUseCase.resumeRcbService(modelId)
    }

    override fun onRemoveRcbItemClicked(modelId: Int) = async {

        rcbOrchestratorUseCase.deleteRcbService(modelId)

        val index = orderedItemList.indexOfFirst {
            it.id == modelId
        }.also {
            if (it == -1) {
                throw IllegalStateException("Model with id $modelId is not in the ordered list")
            }
        }

        orderedItemList.removeAt(index)
        _itemDeletedLiveData.postValue(index)
        _removeAllMenuOptionEnabledLiveData.postValue(orderedItemList.isNotEmpty())
    }

    override fun onCheckRcbConfig(
        modelId: Int,
        numberOfRefills: String,
        refillSize: String,
        windowSizeMs: String,
        maxUnderflows: String
    ) = async {
        fun String.toSafeInt() =
            try {
                toInt()
            } catch (_: NumberFormatException) {
                0
            }

        requireBufferItemModel(modelId).let {
            itemModelMapper.mapConfig(
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

    override fun onPause() = async {
        rcbOrchestratorUseCase.pauseAllRcbServices()
    }

    override fun onStop() = async {
        rcbOrchestratorUseCase.stopAllRcbServices()
    }

    override fun onRcbDeviceSelected(deviceDomainId: Int) = async {

        val deviceDomain = rcbOrchestratorUseCase.createRcbService(deviceDomainId)
        val rcbItemModel = itemModelMapper.mapSelectedDevice(deviceDomain)

        setBufferItemPage(deviceDomain, rcbItemModel)
        itemModelMapper.mapState(deviceDomain, rcbItemModel)
        itemModelMapper.mapAccuracies(deviceDomain.accuracies, rcbItemModel)

        _removeAllMenuOptionEnabledLiveData.postValue(true)
        orderedItemList.add(rcbItemModel)
        emitModels()
    }

    override fun onProductUrlClicked(modelId: Int) = async {
        navigator.navigateToProduct(modelId)
    }

    override fun onVisitRepoClicked() = async {
        navigator.navigateToRepo()
    }

    override fun onRemoveAllRcbsClicked() = async {
        rcbOrchestratorUseCase.deleteAllRcbServices()
        orderedItemList.clear()
        _removeAllBuffersLiveData.postValue(Unit)
        _removeAllMenuOptionEnabledLiveData.postValue(false)
    }

    override fun onSetVibrateValue(modelId: Int, vibrateValue: Int) = async {
        rcbOrchestratorUseCase.setVibrateValue(modelId, vibrateValue)
    }

    override fun onCleared() = disposables.clear()

    private fun onDomainUpdated(domain: BluetoothDeviceDomain) =
        requireBufferItemModel(domain.id).let {
            setBufferItemPage(domain, it)
            itemModelMapper.mapState(domain, it)
        }.also {
            emitModels()
        }

    private fun onRcbServiceAccuracy(domain: BluetoothDeviceAccuracyDomain) =
        requireBufferItemModel(domain.id).let {
            itemModelMapper.mapAccuracies(domain, it)
        }.also {
            emitModels()
        }

    private fun setBufferItemPage(deviceDomain: BluetoothDeviceDomain, model: RcbItemModel) =
        itemModelMapper.mapPage(deviceDomain)?.let { page ->
            orderedItemList.indexOfFirst {
                it.id == model.id
            }.let { index ->
                _bufferItemPageLiveData.postValue(Pair(index, page))
            }
        }

    // Split into 2 model streams: Add & Update
    private fun emitModels() = _itemModelsLiveData.postValue(orderedItemList)

    private fun async(task: () -> Unit) {
        Single.fromCallable(task)
            .subscribeOn(schedulers.io)
            .doOnError { onError(it) }
            .subscribe()
            .also { disposables.add(it) }
    }

    private fun onError(error: Throwable) =
        _errorLiveData.postValue(
            errorMapper.map(error)
        ).also {
            error.printStackTrace()
        }

    private fun requireBufferItemModel(modelId: Int): RcbItemModel =
        orderedItemList.find { it.id == modelId }
            ?: throw IllegalArgumentException("Invalid modelId: $modelId")
}
