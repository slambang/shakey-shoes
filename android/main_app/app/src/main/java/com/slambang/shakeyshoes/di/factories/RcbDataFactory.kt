package com.slambang.shakeyshoes.di.factories

import com.slambang.shakeyshoes.audio.SettableDataSource
import com.slambang.shakeyshoes.view.rcb.INITIAL_VIBRATE_VALUE
import javax.inject.Inject

class RcbDataFactory @Inject constructor() {

    fun newRcbDataSource() =
        SettableDataSource(
            INITIAL_VIBRATE_VALUE
        )
}
