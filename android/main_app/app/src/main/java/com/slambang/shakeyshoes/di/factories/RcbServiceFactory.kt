package com.slambang.shakeyshoes.di.factories

import android.content.Context
import android.os.Build
import com.slambang.rcb.bluetooth.BluetoothConnectionImpl
import com.slambang.rcb.service.RcbServiceErrorMapper
import com.slambang.rcb.service.RcbService
import com.slambang.rcb.service.RcbServiceImpl
import com.slambang.rcb.service.RcbStateMapper
import com.slambang.shakeyshoes.di.scope.ApplicationContext
import com.slambang.shakeyshoes.domain.MockBluetoothConnection
import com.slambang.shakeyshoes.util.SchedulerProvider
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class RcbServiceFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val schedulerProvider: SchedulerProvider
) {
    private val scheduler: Scheduler
        get() = schedulerProvider.newThread

    private val isEmulator: Boolean
        get() = KNOWN_EMULATOR_HARDWARE.contains(Build.HARDWARE)

    fun newRcbService(): RcbService {

        val bluetoothConnection = if (isEmulator) {
            MockBluetoothConnection(scheduler, CompositeDisposable())
        } else {
            BluetoothConnectionImpl.newInstance(context, scheduler)
        }

        val stateMapper = RcbStateMapper()
        val errorMapper = RcbServiceErrorMapper()
        return RcbServiceImpl(bluetoothConnection, errorMapper, stateMapper)
    }

    companion object {
        private val KNOWN_EMULATOR_HARDWARE = listOf<String>(
//            "goldfish",
//            "ranchu"
        )
    }
}
