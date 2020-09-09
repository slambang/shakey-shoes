package com.slambang.shakeyshoes.view.rcb

const val MAX_VIBRATE_VALUE = 254
const val INITIAL_VIBRATE_VALUE = 0

data class RcbItemModel(
    val id: Int,
    var selectedDeviceId: Int = 0,
    val page1: Page1Model = Page1Model(id),
    val page2: Page2Model = Page2Model(id),
    val page3: Page3Model = Page3Model(id),
    var activePage: ActivePageModel = ActivePageModel(),
    val header: ItemHeaderModel = ItemHeaderModel()
)

data class ActivePageModel(
    val pageIndex: Int = 0,
    val since: Long = 0
)

data class ItemHeaderModel(
    var deviceName: String = "",
    var isConnected: Boolean = false,
    var isConnecting: Boolean = false
)

data class RcbConfigModel(
    var refillSize: Int = 4,
    var refillCount: Int = 3,
    var windowSize: Int = 10,
    var maxUnderflows: Int = 50,
    var maxSize: String = "",
    var actualSize: String = "0",
    var latency: String = "0",
    var maxUnderflowTime: String = "0"
)

sealed class PageModel

data class Page1Model(
    val id: Int,
    var status: String = "",
    var macAddress: String = "",
    var pairingPin: String = "",
    var baudRateBytes: String = "",
    var connectButtonEnabled: Boolean = true,
    var connectButtonText: String = ""
) : PageModel()

data class Page2Model(
    val id: Int,
    var config: RcbConfigModel = RcbConfigModel(),
    var applyButtonEnabled: Boolean = false,
    var progressVisible: Boolean = false
) : PageModel()

data class Page3Model(
    val id: Int,
    var maxVibrateValue: Int = MAX_VIBRATE_VALUE,
    var currentVibrateValue: Int = INITIAL_VIBRATE_VALUE,
    var resumeButtonEnabled: Boolean = false,
    var isResumed: Boolean = false,
    var resumeButtonText: String = "",
    var successRate: String = "",
    var errorRate: String = ""
) : PageModel()
