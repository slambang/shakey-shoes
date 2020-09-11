package com.slambang.shakeyshoes.view.rcb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.slambang.shakeyshoes.R
import com.slambang.shakeyshoes.domain.BluetoothDeviceAccuracyDomain
import com.slambang.shakeyshoes.domain.BluetoothDeviceDomain
import com.slambang.shakeyshoes.domain.use_cases.RcbOrchestratorUseCase
import com.slambang.shakeyshoes.util.SchedulerProvider
import com.slambang.shakeyshoes.view.base.SingleLiveEvent
import com.slambang.shakeyshoes.view.rcb.mappers.ErrorMapper
import com.slambang.shakeyshoes.view.rcb.mappers.RcbItemModelMapper
import com.slambang.shakeyshoes.view.rcb.rcb_item_view.BufferItemViewListener
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * TODO
 *  - Map/visualise models: Entity -> Domain -> View (how does it look?)
 *  - Do we need separate RcbOrchestratorUseCase & RcbServiceOrchestrator?
 *  - Refactor editConfig.
 */
class RcbViewModelImpl @Inject constructor(
    private val errorMapper: ErrorMapper,
    private val navigator: RcbViewNavigator,
    private val schedulers: SchedulerProvider,
    private val disposables: CompositeDisposable,
    private val itemModelMapper: RcbItemModelMapper,
    private val orderedItemList: MutableList<RcbItemModel>,
    private val bluetoothStateObserver: BluetoothStateObserver,
    private val rcbOrchestratorUseCase: RcbOrchestratorUseCase,
    // LiveData
    private val _errorEvent: SingleLiveEvent<String>,
    private val _removeItemEvent: SingleLiveEvent<Int>,
    private val _removeAllItemsEvent: SingleLiveEvent<Unit>,
    private val _bluetoothStatusEvent: MutableLiveData<String>,
    private val _confirmDialogEvent: SingleLiveEvent<DialogModel>,
    private val _itemModelsEvent: MutableLiveData<Pair<RcbItemModel, Int>>,
    private val _removeAllMenuOptionEnabledEvent: SingleLiveEvent<Boolean>,
    private val _showDeviceListEvent: SingleLiveEvent<List<Pair<Int, String>>>,
) : RcbViewModel, BufferItemViewListener, ViewModel() {

    // Is there a cleaner way to do this?
    override val confirmDialogEvent: LiveData<DialogModel>
        get() = _confirmDialogEvent

    override val removeAllMenuOptionEnabledEvent: LiveData<Boolean>
        get() = _removeAllMenuOptionEnabledEvent

    override val removeAllItemsEvent: LiveData<Unit>
        get() = _removeAllItemsEvent

    override val showDeviceListEvent: LiveData<List<Pair<Int, String>>>
        get() = _showDeviceListEvent

    override val newItemEvent: LiveData<Pair<RcbItemModel, Int>>
        get() = _itemModelsEvent

    override val removeItemEvent: LiveData<Int>
        get() = _removeItemEvent

    override val bluetoothStatusEvent: LiveData<String>
        get() = _bluetoothStatusEvent

    override val errorEvent: LiveData<String>
        get() = _errorEvent

    override fun onResumeView() = async {
        observeBluetoothState()
        rcbOrchestratorUseCase.subscribe(::onDomainUpdated, ::onRcbServiceAccuracy)
    }

    private fun observeBluetoothState() {
        bluetoothStateObserver.observeBluetoothStatus()
            .subscribeOn(schedulers.io)
            .subscribe({
                _bluetoothStatusEvent.postValue(it)
            }, {
                _errorEvent.postValue(it.message)
            })
            .also { disposables.add(it) }
    }

    override fun onAddRcbClicked() = async {
        val availableDeviceNames = rcbOrchestratorUseCase.getAvailableDeviceNames()
        _showDeviceListEvent.postValue(availableDeviceNames)
    }

    override fun onConnectClicked(modelId: Int) = async {
        rcbOrchestratorUseCase.connectBufferService(modelId)
    }

    override fun onApplyClicked(modelId: Int) = async {
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

    override fun onPauseRcbClicked(modelId: Int) = async {
        rcbOrchestratorUseCase.pauseRcbService(modelId)
    }

    override fun onDeleteClicked(modelId: Int) = async {
        DialogModel(
            titleResId = R.string.delete_buffer_dialog_title,
            messageResId = R.string.delete_buffer_dialog_message,
            onConfirmedListener = { onDeleteConfirmed(modelId) }
        ).also { _confirmDialogEvent.postValue(it) }
    }

    private fun onDeleteConfirmed(modelId: Int) {
        rcbOrchestratorUseCase.removeItem(modelId)

        val index = orderedItemList.indexOfFirst {
            it.id == modelId
        }.also {
            if (it == -1) {
                throw IllegalStateException("Model with id $modelId is not in the ordered list")
            }
        }

        orderedItemList.removeAt(index)
        _removeItemEvent.postValue(index)
        _removeAllMenuOptionEnabledEvent.postValue(orderedItemList.isNotEmpty())
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

            emitModel(it)
        }
    }

    override fun onPauseView() = async {
        rcbOrchestratorUseCase.pauseAllRcbServices()
    }

    override fun onStopView() = async {
        rcbOrchestratorUseCase.stopAllRcbServices()
    }

    override fun onDeviceSelected(deviceDomainId: Int) = async {

        val deviceDomain = rcbOrchestratorUseCase.createRcbService(deviceDomainId)
        val rcbItemModel = itemModelMapper.mapSelectedDevice(deviceDomain)

        itemModelMapper.mapState(deviceDomain, rcbItemModel)
        itemModelMapper.mapAccuracies(deviceDomain.accuracies, rcbItemModel)

        _removeAllMenuOptionEnabledEvent.postValue(true)
        orderedItemList.add(rcbItemModel)
        emitModel(rcbItemModel)
    }

    override fun onProductUrlClicked(modelId: Int) = async {
        navigator.navigateToProduct(modelId)
    }

    override fun onVisitRepoClicked() = async {
        navigator.navigateToRepo()
    }

    override fun onDeleteAllClicked() = async {
        DialogModel(
            titleResId = R.string.delete_all_buffers_dialog_title,
            messageResId = R.string.delete_all_buffers_dialog_message,
            onConfirmedListener = ::onDeleteAllConfirmed
        ).also { _confirmDialogEvent.postValue(it) }
    }

    private fun onDeleteAllConfirmed() = async {
        rcbOrchestratorUseCase.removeAllItems()
        orderedItemList.clear()
        _removeAllItemsEvent.postValue(Unit)
        _removeAllMenuOptionEnabledEvent.postValue(false)
    }

    override fun onSetVibrateValue(modelId: Int, value: Int) = async {
        rcbOrchestratorUseCase.setVibrateValue(modelId, value)
    }

    override fun onResumeClicked(modelId: Int) = async {
        requireBufferItemModel(modelId).let {
            rcbOrchestratorUseCase.toggleRcb(
                modelId,
                it.page3.isResumed
            )
            emitModel(it)
        }
    }

    override fun onEditConfigClicked(modelId: Int) = async {
//        ::displayConfig
    }

    override fun onCleared() = disposables.clear()

    private fun onDomainUpdated(domain: BluetoothDeviceDomain) =
        requireBufferItemModel(domain.id).let {
            itemModelMapper.mapState(domain, it)
            emitModel(it)
        }

    private fun onRcbServiceAccuracy(domain: BluetoothDeviceAccuracyDomain) =
        requireBufferItemModel(domain.id).let {
            itemModelMapper.mapAccuracies(domain, it)
            emitModel(it)
        }

    private fun emitModel(model: RcbItemModel) =
        _itemModelsEvent.postValue(model to orderedItemList.indexOf(model))

    private fun async(task: () -> Unit) {
        Single.fromCallable(task)
            .subscribeOn(schedulers.io)
            .doOnError { onError(it) }
            .subscribe()
            .also { disposables.add(it) }
    }

    private fun onError(error: Throwable) =
        _errorEvent.postValue(
            errorMapper.map(error)
        ).also {
            error.printStackTrace()
        }

    private fun requireBufferItemModel(modelId: Int): RcbItemModel =
        orderedItemList.find { it.id == modelId }
            ?: throw IllegalArgumentException("Invalid modelId: $modelId")
}
