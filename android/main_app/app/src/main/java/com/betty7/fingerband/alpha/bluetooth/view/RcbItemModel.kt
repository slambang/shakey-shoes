package com.betty7.fingerband.alpha.bluetooth.view

const val INITIAL_VIBRATE_VALUE = 0
const val DELETE_ALL_BUFFERS_ID = -1

data class RcbItemModel(
    val id: Int,
    var selectedDeviceId: Int = 0,
    val page1: Page1Model = Page1Model(),
    val page2: Page2Model = Page2Model(),
    val page3: Page3Model = Page3Model(),
    val header: ItemHeaderModel = ItemHeaderModel()
)

data class ItemHeaderModel(
    var deviceName: String = "",
    var isConnected: Boolean = false,
    var isConnecting: Boolean = false
)

sealed class PageModel

data class RcbConfigModel(
    var refillSize: Int = 4,
    var refillCount: Int = 3,
    var windowSize: Int = 10,
    var maxUnderflows: Int = 50,
    var maxSize: String = "",
    var actualSize: String ="0",
    var latency: String ="0",
    var maxUnderflowTime: String ="0"
)

data class Page1Model(
    var status: String = "",
    var macAddress: String = "",
    var pairingPin: String = "",
    var baudRateBytes: String = "",
    var connectButtonEnabled: Boolean = true,
    var connectButtonText: String = ""
) : PageModel()

data class Page2Model(
    var config: RcbConfigModel = RcbConfigModel(),
    var applyButtonEnabled: Boolean = false,
    var progressVisible: Boolean = false
) : PageModel()

data class Page3Model(
    var maxVibrateValue: Int = 254,
    var currentVibrateValue: Int = INITIAL_VIBRATE_VALUE,
    var resumeButtonEnabled: Boolean = false,
    var isResumed: Boolean = false,
    var resumeButtonText: String = "",
    var successRate: String = "",
    var errorRate: String = ""
) : PageModel()
