package com.slambang.shakeyshoes.view.rcb

import androidx.lifecycle.LiveData

interface RcbViewModel {

    val removeAllMenuOptionEnabledLiveData: LiveData<Boolean>
    val removeAllBuffersLiveData: LiveData<Unit>
    val showDeviceListLiveData: LiveData<List<Pair<Int, String>>>
    val bufferItemPageLiveData: LiveData<Pair<Int, Int>>
    val itemModelsLiveData: LiveData<List<RcbItemModel>>
    val itemDeletedLiveData: LiveData<Int>
    val bluetoothStatusLiveData: LiveData<String>
    val errorLiveData: LiveData<String>

    fun onResume()

    fun onPause()

    fun onStop()

    fun onAddRcbClicked()

    fun onConnectRcbClicked(modelId: Int)

    fun onConfigureRbClicked(modelId: Int)

    fun onToggleRcb(modelId: Int)

    fun onPauseRcbClicked(modelId: Int)

    fun onResumeRcbClicked(modelId: Int)

    fun onRemoveRcbItemClicked(modelId: Int)

    fun onCheckRcbConfig(
        modelId: Int,
        numberOfRefills: String,
        refillSize: String,
        windowSizeMs: String,
        maxUnderflows: String
    )

    fun onRcbDeviceSelected(deviceDomainId: Int)

    fun onProductUrlClicked(modelId: Int)

    fun onVisitRepoClicked()

    fun onRemoveAllRcbsClicked()

    fun onSetVibrateValue(modelId: Int, vibrateValue: Int)
}
