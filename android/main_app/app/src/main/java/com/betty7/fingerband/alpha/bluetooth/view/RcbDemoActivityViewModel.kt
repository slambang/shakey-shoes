package com.betty7.fingerband.alpha.bluetooth.view

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel

abstract class RcbDemoActivityViewModel : ViewModel() {

    abstract fun subscribe(
        owner: LifecycleOwner,
        showDeviceListObserver: (List<String>) -> Unit,
        bufferItemObserver: (RcbItemModel) -> Unit,
        launchUrlObserver: (String) -> Unit,
        bufferItemPageObserver: (Int, Int) -> Unit
    )

    // Lifecycle events
    abstract fun onPause()
    abstract fun onResume()
    abstract fun onStop()

    abstract fun onDeviceSelected(owner: LifecycleOwner, deviceId: Int)
    abstract fun onCreateBufferClicked()

    abstract fun onConnectBufferClicked(rcbServiceId: Int)
    abstract fun onConfigureBufferClicked(rcbServiceId: Int)
    abstract fun toggleBufferService(rcbServiceId: Int)
    abstract fun onResumeBufferClicked(rcbServiceId: Int)
    abstract fun onPauseBufferClicked(rcbServiceId: Int)
    abstract fun onProductUrlClicked(deviceId: Int)
    abstract fun onDeleteBufferItemClicked(rcbServiceId: Int)

    // Menu events
    abstract fun onProjectUrlClicked()
    abstract fun onDeleteAllBuffersClicked()

    abstract fun setVibrateValue(rcbServiceId: Int, vibrateValue: Int)
    abstract fun checkConfig(
        rcbServiceId: Int,
        numberOfRefills: String,
        refillSize: String,
        windowSizeMs: String,
        maxUnderflows: String
    )
}
