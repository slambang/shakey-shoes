package com.slambang.shakeyshoes.di.factories

import android.content.Context
import com.slambang.rcb.bluetooth.BluetoothDevice
import com.slambang.rcb.service.RcbServiceErrorMapper
import com.slambang.rcb.service.RcbService
import com.slambang.rcb.service.RcbServiceImpl
import com.slambang.rcb.service.RcbStateMapper
import com.slambang.shakeyshoes.di.scope.ApplicationContext
import com.slambang.shakeyshoes.domain.MockBluetoothDevice
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
        get() = false;//KNOWN_EMULATOR_HARDWARE.contains(Build.HARDWARE)

    fun newRcbService(): RcbService {

        val bluetoothDevice = if (isEmulator) {
            MockBluetoothDevice(scheduler, CompositeDisposable())
        } else {
            BluetoothDevice.newInstance(context, scheduler)
        }

        val stateMapper = RcbStateMapper()
        val errorMapper = RcbServiceErrorMapper()
        return RcbServiceImpl(bluetoothDevice, errorMapper, stateMapper)
    }

    companion object {
        private val KNOWN_EMULATOR_HARDWARE = listOf(
            "goldfish",
            "ranchu"
        )
    }
}
