package com.slambang.shakeyshoes.view.rcb

import androidx.lifecycle.LiveData

data class DialogModel (
    val titleResId: Int,
    val messageResId: Int,
    val onConfirmedListener: () -> Unit
)

interface RcbViewModel {

    val confirmDialogEvent: LiveData<DialogModel>
    val removeAllMenuOptionEnabledEvent: LiveData<Boolean>
    val removeAllItemsEvent: LiveData<Unit>
    val showDeviceListEvent: LiveData<List<Pair<Int, String>>>
    val newItemEvent: LiveData<Pair<RcbItemModel, Int>>
    val removeItemEvent: LiveData<Int>
    val bluetoothStatusEvent: LiveData<String>
    val errorEvent: LiveData<String>

    fun onResumeView()

    fun onPauseView()

    fun onStopView()

    fun onAddRcbClicked()

    fun onDeviceSelected(deviceDomainId: Int)

    fun onCheckRcbConfig(
        modelId: Int,
        numberOfRefills: String,
        refillSize: String,
        windowSizeMs: String,
        maxUnderflows: String
    )

    fun onVisitRepoClicked()

    fun onDeleteAllClicked()
}
