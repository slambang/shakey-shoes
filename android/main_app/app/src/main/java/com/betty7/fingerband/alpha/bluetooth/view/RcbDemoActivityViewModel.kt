package com.betty7.fingerband.alpha.bluetooth.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class RcbDemoActivityViewModel : ViewModel() {

    abstract val itemModelsLiveData: MutableLiveData<List<RcbItemModel>>
    abstract val showDeviceListLiveData: SingleLiveEvent<List<String>>
    abstract val launchUrlLiveData: SingleLiveEvent<String>
    abstract val bufferItemPageLiveData: SingleLiveEvent<Pair<Int, Int>>

    // Lifecycle events
    abstract fun onStart()
    abstract fun onPause()
    abstract fun onStop()

    // Device events
    abstract fun onDeviceSelected(deviceDomainId: Int)
    abstract fun onProductUrlClicked(deviceDomainId: Int)

    // Rcb events
    abstract fun onCreateRcbClicked()
    abstract fun onConnectRcbClicked(modelId: Int)
    abstract fun onConfigureRbClicked(modelId: Int)
    abstract fun toggleRcb(modelId: Int)
    abstract fun onResumeRcbClicked(modelId: Int)
    abstract fun onPauseRcbClicked(modelId: Int)
    abstract fun onDeleteRcbItemClicked(modelId: Int)
    abstract fun checkRcbConfig(
        modelId: Int,
        numberOfRefills: String,
        refillSize: String,
        windowSizeMs: String,
        maxUnderflows: String
    )

    // Menu events
    abstract fun onProjectUrlClicked()
    abstract fun onDeleteAllBuffersClicked()

    abstract fun setVibrateValue(modelId: Int, vibrateValue: Int)
}
