package com.slambang.shakeyshoes.view.rcb

import androidx.lifecycle.LiveData

data class DialogModel (
    val titleResId: Int,
    val messageResId: Int,
    val onSuccessListener: () -> Unit
)

interface RcbViewModel {

    val confirmDialogLiveData: LiveData<DialogModel>
    val removeAllMenuOptionEnabledLiveData: LiveData<Boolean>
    val removeAllBuffersLiveData: LiveData<Unit>
    val showDeviceListLiveData: LiveData<List<Pair<Int, String>>>
    val bufferItemPageLiveData: LiveData<Pair<Int, Int>>
    val itemModelsLiveData: LiveData<Pair<RcbItemModel, Int>>
    val itemDeletedLiveData: LiveData<Int>
    val bluetoothStatusLiveData: LiveData<String>
    val errorLiveData: LiveData<String>

    fun onResume()

    fun onPause()

    fun onStop()

    fun onAddRcbClicked()

    fun onRcbDeviceSelected(deviceDomainId: Int)

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
