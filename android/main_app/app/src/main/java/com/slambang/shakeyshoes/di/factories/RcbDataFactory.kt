package com.slambang.shakeyshoes.di.factories

import com.slambang.shakeyshoes.audio.SettableDataSource
import javax.inject.Inject

const val INITIAL_VIBRATE_VALUE = 0

class RcbDataFactory @Inject constructor() {

    fun newRcbDataSource() =
        SettableDataSource(
            INITIAL_VIBRATE_VALUE
        )
}
