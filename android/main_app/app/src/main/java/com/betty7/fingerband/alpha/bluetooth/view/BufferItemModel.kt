package com.betty7.fingerband.alpha.bluetooth.view

const val INITIAL_VIBRATE_VALUE = 0
const val DELETE_ALL_BUFFERS_ID = -1

data class BufferItemViewModel(
    val id: Int,
    var selectedDeviceId: Int = 0,
    val page0: Page0ViewModel = Page0ViewModel(),
    val page1: Page1ViewModel = Page1ViewModel(),
    val page2: Page2ViewModel = Page2ViewModel(),
    val header: BufferItemHeaderViewModel = BufferItemHeaderViewModel()
)

data class BufferItemHeaderViewModel(
    var deviceName: String = "",
    var isConnected: Boolean = false,
    var isConnecting: Boolean = false
)

sealed class PageModel

data class BufferConfigViewModel(
    var refillSize: Int = 4,
    var refillCount: Int = 3,
    var windowSize: Int = 10,
    var maxUnderflows: Int = 50,
    var maxSize: String = "",
    var actualSize: String ="0",
    var latency: String ="0",
    var maxUnderflowTime: String ="0"
)

data class Page0ViewModel(
    var status: String = "",
    var macAddress: String = "",
    var pairingPin: String = "",
    var baudRateBytes: String = "",
    var connectButtonEnabled: Boolean = true,
    var connectButtonText: String = ""
) : PageModel()

data class Page1ViewModel(
    var config: BufferConfigViewModel = BufferConfigViewModel(),
    var applyButtonEnabled: Boolean = false,
    var progressVisible: Boolean = false
) : PageModel()

data class Page2ViewModel(
    var maxVibrateValue: Int = 254,
    var currentVibrateValue: Int = INITIAL_VIBRATE_VALUE,
    var resumeButtonEnabled: Boolean = false,
    var isResumed: Boolean = false,
    var resumeButtonText: String = "",
    var successRate: String = "",
    var errorRate: String = ""
) : PageModel()
