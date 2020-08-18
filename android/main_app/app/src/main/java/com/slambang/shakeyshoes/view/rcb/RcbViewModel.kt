package com.slambang.shakeyshoes.view.rcb

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

// Firebase
// --------
// slambang.dev@gmail.com
// Slambang<usual>!

// Optimisation: Can this be delegated instead of inherited?
abstract class RcbViewModel : ViewModel() {

    abstract val itemModelsLiveData: LiveData<List<RcbItemModel>>
    abstract val removeAllBuffersLiveData: LiveData<Unit>
    abstract val removeAllMenuOptionEnabledLiveData: LiveData<Boolean>
    abstract val showDeviceListLiveData: LiveData<List<Pair<Int, String>>>
    abstract val bufferItemPageLiveData: LiveData<Pair<Int, Int>>
    abstract val itemDeletedLiveData: LiveData<Int>
    abstract val errorLiveData: LiveData<String>

    // Lifecycle events
    abstract fun onStart()
    abstract fun onPause()
    abstract fun onStop()

    // Rcb events
    abstract fun onRcbDeviceSelected(deviceDomainId: Int)
    abstract fun onProductUrlClicked(modelId: Int)
    abstract fun onAddRcbClicked()
    abstract fun onConnectRcbClicked(modelId: Int)
    abstract fun onConfigureRbClicked(modelId: Int)
    abstract fun toggleRcb(modelId: Int)
    abstract fun onResumeRcbClicked(modelId: Int)
    abstract fun onPauseRcbClicked(modelId: Int)
    abstract fun onRemoveRcbItemClicked(modelId: Int)
    abstract fun setVibrateValue(modelId: Int, vibrateValue: Int)
    abstract fun checkRcbConfig(
        modelId: Int,
        numberOfRefills: String,
        refillSize: String,
        windowSizeMs: String,
        maxUnderflows: String
    )

    // Menu events
    abstract fun onVisitRepoClicked()
    abstract fun onRemoveAllRcbsClicked()
}
