package com.slambang.shakeyshoes.util

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.slambang.rcb.bluetooth.BluetoothProvider
import com.slambang.shakeyshoes.di.scope.ApplicationContext
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import javax.inject.Inject

enum class BluetoothStatus {
    ON,
    OFF,
    UNAVAILABLE
}

class ObservableBluetoothStatus @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bluetoothProvider: BluetoothProvider
) {

    private lateinit var emitter: ObservableEmitter<BluetoothStatus>

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            if (intent.action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_ON -> emitter.onNext(BluetoothStatus.ON)
                    BluetoothAdapter.STATE_OFF -> emitter.onNext(BluetoothStatus.OFF)
                }
            }
        }
    }

    fun subscribe(): Observable<BluetoothStatus> =

        Observable.create { emitter ->

            this.emitter = emitter

            val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            context.registerReceiver(receiver, filter)
            emitter.onNext(getCurrentBluetoothState())

            emitter.setCancellable {
                context.unregisterReceiver(receiver)
            }
        }

    private fun getCurrentBluetoothState() =
        when {
            !bluetoothProvider.isBluetoothAvailable -> BluetoothStatus.UNAVAILABLE
            bluetoothProvider.isBluetoothEnabled -> BluetoothStatus.ON
            else -> BluetoothStatus.OFF
        }
}
