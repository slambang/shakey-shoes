package com.slambang.shakeyshoes.di.factories

import com.slambang.shakeyshoes.data.audio.SettableRcbDataSource
import com.slambang.shakeyshoes.view.rcb.INITIAL_VIBRATE_VALUE
import javax.inject.Inject

class RcbDataFactory @Inject constructor() {

    fun newRcbDataSource() =
        SettableRcbDataSource(
            INITIAL_VIBRATE_VALUE
        )
}
