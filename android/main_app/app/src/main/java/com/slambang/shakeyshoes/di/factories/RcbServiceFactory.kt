package com.slambang.shakeyshoes.di.factories

import android.os.Build
import com.slambang.bluetooth_connection.BluetoothConnectionImpl
import com.slambang.bluetooth_connection.BluetoothProvider
import com.slambang.rcb_service.RcbService
import com.slambang.rcb_service.RcbServiceErrorMapper
import com.slambang.rcb_service.RcbServiceImpl
import com.slambang.rcb_service.RcbStateMapper
import com.slambang.shakeyshoes.domain.MockBluetoothConnection
import com.slambang.shakeyshoes.util.SchedulerProvider
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class RcbServiceFactory @Inject constructor(
    private val bluetoothProvider: BluetoothProvider,
    private val schedulerProvider: SchedulerProvider,
    private val rcbStateMapper: RcbStateMapper,
    private val rcbServiceErrorMapper: RcbServiceErrorMapper,
    private val subscriptions: CompositeDisposable
) {
    private val scheduler: Scheduler
        get() = schedulerProvider.newThread

    private val isEmulator: Boolean
        get() = KNOWN_EMULATOR_HARDWARE.contains(Build.HARDWARE)

    fun newRcbService(): RcbService =
        RcbServiceImpl(
            getBluetoothConnection(),
            rcbServiceErrorMapper,
            rcbStateMapper
        )

    private fun getBluetoothConnection() =
        if (isEmulator) {
            MockBluetoothConnection(scheduler, subscriptions)
        } else {
            // Would be nice to to have `Impl` in the name here
            BluetoothConnectionImpl.newInstance(scheduler, bluetoothProvider, subscriptions)
        }

    companion object {
        private val KNOWN_EMULATOR_HARDWARE = listOf<String>(
//            "goldfish",
//            "ranchu"
        )
    }
}
