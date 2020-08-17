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
    abstract fun onConnectRcbClicked(rcbServiceId: Int)
    abstract fun onConfigureRbClicked(rcbServiceId: Int)
    abstract fun toggleRcb(rcbServiceId: Int)
    abstract fun onResumeRcbClicked(rcbServiceId: Int)
    abstract fun onPauseRcbClicked(rcbServiceId: Int)
    abstract fun onDeleteRcbItemClicked(rcbServiceId: Int)
    abstract fun checkRcbConfig(
        rcbServiceId: Int,
        numberOfRefills: String,
        refillSize: String,
        windowSizeMs: String,
        maxUnderflows: String
    )

    // Menu events
    abstract fun onProjectUrlClicked()
    abstract fun onDeleteAllBuffersClicked()

    abstract fun setVibrateValue(rcbServiceId: Int, vibrateValue: Int)
}
